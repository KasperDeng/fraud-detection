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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kasperdeng.frauddetection.model.Transaction;
import io.github.kasperdeng.frauddetection.service.FraudDetectionService;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@Slf4j
public class SqsListenerManager implements SmartLifecycle {

  private final long INIT_DELAY_SECS = 5;
  private volatile long delaySeconds = INIT_DELAY_SECS;
  @Value("${cloud.aws.queue.urlUpdatePeriodInSecs:30}")
  private long queueUrlUpdatePeriodInSec;
  @Value("${cloud.aws.queue.transactions}")
  private String transactionsQueue;
  private final SqsAsyncClient sqsAsyncClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private SQSListener sqsListener;
  private volatile boolean running = false;

  private volatile String queueUrl = "";
  private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final AtomicReference<ScheduledFuture<?>> scheduledTaskRef = new AtomicReference<>();
  private final AtomicReference<Future<?>> taskRef = new AtomicReference<>();

  public SqsListenerManager(SqsAsyncClient sqsAsyncClient, SQSListener sqsListener) {
    this.sqsAsyncClient = sqsAsyncClient;
    this.sqsListener = sqsListener;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void start() {
    if (!this.running) {
      scheduleNextTask();
      this.running = true;
    }
  }

  private void scheduleNextTask() {
    ScheduledFuture<?> future = retryExecutor.scheduleWithFixedDelay(this::getQueueUrlTask,
        0, delaySeconds, TimeUnit.SECONDS);
    scheduledTaskRef.set(future);
  }

  protected void getQueueUrlTask() {
    try {
      String result = sqsAsyncClient.getQueueUrl(GetQueueUrlRequest.builder()
          .queueName(transactionsQueue)
          .build()).get().queueUrl();
      if (!Objects.equals(result, queueUrl)) {
        queueUrl = result;
        log.info("SQS getQueueUrl successfully " + queueUrl);
        delaySeconds = queueUrlUpdatePeriodInSec;
        if (taskRef.get() != null) {
          taskRef.get().cancel(true);
        }
        Future<?> future = executor.submit(this::pollMessages);
        taskRef.set(future);
        ScheduledFuture<?> scheduledFuture = scheduledTaskRef.get();
        if (scheduledFuture != null) {
          scheduledFuture.cancel(true);
          scheduleNextTask();
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Failed to get queue URL from SQS: " + e.getMessage());
      if (log.isTraceEnabled()) {
        log.trace("Failed to get queue URL from SQS", e);
      }
    }
  }

  private void pollMessages() {
    while (isRunning()) {
      pollMessage0();
    }
  }

  protected void pollMessage0() {
    sqsAsyncClient.receiveMessage(createRequest())
        .thenApply(ReceiveMessageResponse::messages)
        .thenApply(collectionList -> (Collection<Message>) collectionList)
        .thenAccept(
            msgCollection -> {
              for (Message message : msgCollection) {
                try {
                  sqsListener.processTransaction(
                      objectMapper.readValue(message.body(), Transaction.class));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                sqsAsyncClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl).build());
              }
            }
        );
  }

  private ReceiveMessageRequest createRequest() {
    return ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .maxNumberOfMessages(5)
        .waitTimeSeconds(10)
        .build();
  }

  @Override
  public void stop() {
    if (sqsListener != null) {
      sqsListener = null;
    }
    if (scheduledTaskRef.get() != null) {
      scheduledTaskRef.get().cancel(true);
    }
    if (taskRef.get() != null) {
      taskRef.get().cancel(true);
    }
    retryExecutor.shutdown();
    executor.shutdown();
    this.running = false;
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }
}
