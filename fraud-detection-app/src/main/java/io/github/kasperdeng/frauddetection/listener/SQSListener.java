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

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import io.github.kasperdeng.frauddetection.model.Transaction;
import io.github.kasperdeng.frauddetection.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unused")
public class SQSListener {
  private final FraudDetectionService fraudDetectionService;

  public SQSListener(FraudDetectionService fraudDetectionService) {
    this.fraudDetectionService = fraudDetectionService;
  }

  @SqsListener(value = "${cloud.aws.queue.transactions}", acknowledgementMode = "ON_SUCCESS")
  public void processTransaction(Transaction transaction) {
    log.info("Received transaction: {}", transaction.getTransactionId());

    FraudDetectionResult result = fraudDetectionService.analyzeTransaction(transaction);

    if (result.isFraudulent()) {
      log.warn("FRAUD DETECTED - Transaction ID: {}, Reason: {}",
          transaction.getTransactionId(), result.getReason());
    } else {
      log.info("Transaction {} appears legitimate", transaction.getTransactionId());
    }
  }
}
