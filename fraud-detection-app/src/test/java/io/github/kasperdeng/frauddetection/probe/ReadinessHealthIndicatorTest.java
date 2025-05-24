/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.probe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;

class ReadinessHealthIndicatorTest {

  @Mock
  private SqsAsyncClient sqsAsyncClient;

  private ReadinessHealthIndicator readinessHealthIndicator;

  private static LogCaptor logCaptor;

  @BeforeAll
  public static void setupLogCaptor() {
    logCaptor = LogCaptor.forClass(ReadinessHealthIndicator.class);
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
    readinessHealthIndicator = new ReadinessHealthIndicator(sqsAsyncClient);
  }

  @Test
  public void testHealthCheckWhenAwsSqsIsHealthy() {
    logCaptor.setLogLevelToDebug();
    Health.Builder builder = new Health.Builder();
    when(sqsAsyncClient.listQueues(Mockito.any(ListQueuesRequest.class))).thenReturn(
        CompletableFuture.completedFuture(ListQueuesResponse.builder().build()));

    readinessHealthIndicator.checkSqsConnectivity();

    readinessHealthIndicator.doHealthCheck(builder);

    assertThat(logCaptor.getDebugLogs()).containsExactly("Fraud Detection health check.");
    assertThat(Health.up().build()).isEqualTo(builder.build());
  }

  @Test
  public void testHealthCheckWhenAwsSqsIsNotHealthy() {
    logCaptor.setLogLevelToDebug();
    Health.Builder builder = new Health.Builder();
    doReturn(CompletableFuture.completedFuture(new RuntimeException())).when(sqsAsyncClient)
        .listQueues(Mockito.any(ListQueuesRequest.class));

    readinessHealthIndicator.checkSqsConnectivity();

    readinessHealthIndicator.doHealthCheck(builder);

    assertThat(logCaptor.getDebugLogs()).containsExactly("Fraud Detection health check.");
    assertThat(Health.down().withDetail("WARNING", "AWS SQS is not connected.").build()).isEqualTo(
        builder.build());
  }

  @Test
  public void testInitScheduledExecutor() {
    logCaptor.setLogLevelToDebug();
    assertDoesNotThrow(() -> readinessHealthIndicator.initScheduledExecutor());
    assertThat(logCaptor.getDebugLogs()).containsExactly("Init readiness check schedule.");
  }
}
