/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kasperdeng.frauddetection.model.Transaction;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public class SqsListenerManagerTest {

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private SqsAsyncClient sqsAsyncClient;

  @Mock
  private SQSListener sqsListener;

  private SqsListenerManager sqsListenerManager;

  private static LogCaptor logCaptor;

  @BeforeAll
  public static void setupLogCaptor() {
    logCaptor = LogCaptor.forClass(SqsListenerManager.class);
  }

  @AfterEach
  public void clearLogs() {
    logCaptor.clearLogs();
  }

  @AfterAll
  public static void tearDown() {
    logCaptor.close();
  }

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    sqsListenerManager = new SqsListenerManager(sqsAsyncClient, sqsListener);
  }

  @Test
  public void testIsAutoStartupReturnsTrue() {
    assertThat(sqsListenerManager.isAutoStartup()).isTrue();
  }

  @Test
  public void testStartSchedulesTask() throws NoSuchFieldException, IllegalAccessException {
    ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
    setRetryExecutorForTest(scheduledExecutorService);
    sqsListenerManager.start();
    verify(scheduledExecutorService).scheduleWithFixedDelay(any(Runnable.class), anyLong(),
        anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testStopAndCancelsTask()
      throws NoSuchFieldException, IllegalAccessException {
    ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
    Future<?> future = mock(Future.class);
    setScheduledTaskRefForTest(scheduledFuture);
    setTaskRefForTest(future);
    sqsListenerManager.stop();
    verify(scheduledFuture).cancel(true);
  }

  @Test
  public void testIsRunningReturnsTrue() throws NoSuchFieldException, IllegalAccessException {
    setRunningStatusForTest(true);
    assertThat(sqsListenerManager.isRunning()).isTrue();
  }

  @Test
  public void testGetQueueUrlTask()
      throws NoSuchFieldException, IllegalAccessException {
    when(sqsAsyncClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(
        CompletableFuture.completedFuture(
            GetQueueUrlResponse.builder().queueUrl("http://myQueueurl/accountId/myQueue")
                .build()));
    ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
    setScheduledTaskRefForTest(scheduledFuture);
    Future<?> future = mock(Future.class);
    setTaskRefForTest(future);
    ScheduledExecutorService retryExeService = mock(ScheduledExecutorService.class);
    setRetryExecutorForTest(retryExeService);
    ExecutorService exeService = mock(ExecutorService.class);
    setExecutorForTest(exeService);

    sqsListenerManager.getQueueUrlTask();

    verify(future).cancel(true);
    verify(scheduledFuture).cancel(true);
    verify(retryExeService).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
        any(TimeUnit.class));
    verify(exeService).submit(any(Runnable.class));
  }

  @Test
  public void testGetQueueUrlTaskFailed() throws ExecutionException, InterruptedException {
    logCaptor.setLogLevelToTrace();
    CompletableFuture<GetQueueUrlResponse> future = mock(CompletableFuture.class);
    when(sqsAsyncClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(future);
    when(future.get()).thenThrow(new ExecutionException("test exception", new Exception()));

    sqsListenerManager.getQueueUrlTask();

    assertThat(logCaptor.getErrorLogs()).containsExactly(
        "Failed to get queue URL from SQS: test exception");
    assertThat(logCaptor.getTraceLogs()).contains("Failed to get queue URL from SQS");
  }

  @Test
  public void testPollMessages()
      throws NoSuchFieldException, IllegalAccessException {
    setRunningStatusForTest(true);
    when(sqsAsyncClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(
        CompletableFuture.completedFuture(ReceiveMessageResponse.builder().messages(
            Message.builder().body("""
                {"transactionId": "TXN-123","accountId": "ACCT-001", "amount": 100.00,
                "merchant": "Safe Merchant","location": "New York"}
                """).build()).build()));

    sqsListenerManager.pollMessage0();

    verify(sqsListener).processTransaction(any(Transaction.class));
    verify(sqsAsyncClient).deleteMessage(any(DeleteMessageRequest.class));
  }

  private void setRunningStatusForTest(boolean running)
      throws NoSuchFieldException, IllegalAccessException {
    Field runningField = SqsListenerManager.class.getDeclaredField("running");
    runningField.setAccessible(true);
    runningField.setBoolean(sqsListenerManager, running);
  }

  private void setScheduledTaskRefForTest(ScheduledFuture<?> scheduledFuture)
      throws NoSuchFieldException, IllegalAccessException {
    Field scheduledTaskRefField = SqsListenerManager.class.getDeclaredField("scheduledTaskRef");
    scheduledTaskRefField.setAccessible(true);
    scheduledTaskRefField.set(sqsListenerManager, new AtomicReference<>(scheduledFuture));
  }

  private void setTaskRefForTest(Future<?> future)
      throws NoSuchFieldException, IllegalAccessException {
    Field scheduledTaskRefField = SqsListenerManager.class.getDeclaredField("taskRef");
    scheduledTaskRefField.setAccessible(true);
    scheduledTaskRefField.set(sqsListenerManager, new AtomicReference<>(future));
  }

  private void setRetryExecutorForTest(ScheduledExecutorService scheduledExecutorService)
      throws NoSuchFieldException, IllegalAccessException {
    Field retryExecutorField = SqsListenerManager.class.getDeclaredField("retryExecutor");
    retryExecutorField.setAccessible(true);
    retryExecutorField.set(sqsListenerManager, scheduledExecutorService);
  }

  private void setExecutorForTest(ExecutorService executorService)
      throws NoSuchFieldException, IllegalAccessException {
    Field executorField = SqsListenerManager.class.getDeclaredField("executor");
    executorField.setAccessible(true);
    executorField.set(sqsListenerManager, executorService);
  }
}
