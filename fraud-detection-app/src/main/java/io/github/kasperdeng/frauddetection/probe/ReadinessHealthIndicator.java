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

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;

@Component
@Slf4j
public class ReadinessHealthIndicator extends AbstractHealthIndicator {

  private final SqsAsyncClient sqsAsyncClient;
  private final AtomicBoolean isHealthy = new AtomicBoolean(false);

  public ReadinessHealthIndicator(SqsAsyncClient sqsAsyncClient) {
    this.sqsAsyncClient = sqsAsyncClient;
  }

  @PostConstruct
  protected void initScheduledExecutor() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
        new CustomizableThreadFactory("readiness-async-check"));
    executor.scheduleWithFixedDelay(this::checkSqsConnectivity, 0, 10, TimeUnit.SECONDS);
    log.debug("Init readiness check schedule.");
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    log.debug("Fraud Detection health check.");
    if (isHealthy.get()) {
      builder.up();
    } else {
      builder.down().withDetail("WARNING", "AWS SQS is not connected.");
    }
  }

  protected void checkSqsConnectivity() {
    sqsAsyncClient.listQueues(ListQueuesRequest.builder().maxResults(1).build())
        .whenComplete((response, ex) -> {
          if (ex == null) {
            isHealthy.set(true);
          } else {
            isHealthy.set(false);
            log.warn("[Health Check] Connectivity issue of AWS SQS.");
            if (log.isTraceEnabled()) {
              log.trace("[Health Check] Connectivity issue of AWS SQS.", ex);
            }
          }
        });
  }
}
