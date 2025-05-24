/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.service;

import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import io.github.kasperdeng.frauddetection.model.Transaction;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

  private final BigDecimal amountThreshold;
  private final List<String> suspiciousAccounts;
  private final List<String> highRiskMerchants;

  public FraudDetectionServiceImpl(
      @Value("${fraud.detection.amount.threshold:10000}") BigDecimal amountThreshold,
      @Value("#{'${fraud.detection.suspicious.accounts:}'.split(',')}") List<String> suspiciousAccounts,
      @Value("#{'${fraud.detection.highRiskMerchants:}'.split(',')}") List<String> highRiskMerchants) {
    this.amountThreshold = amountThreshold;
    this.suspiciousAccounts = suspiciousAccounts;
    this.highRiskMerchants = highRiskMerchants;
  }

  @Override
  public FraudDetectionResult analyzeTransaction(Transaction transaction) {
    // Rule 1: Check for high amount transactions
    if (transaction.getAmount().compareTo(amountThreshold) > 0) {
      return new FraudDetectionResult(
          transaction.getTransactionId(),
          true,
          String.format("Transaction amount %s exceeds threshold %s",
              transaction.getAmount(), amountThreshold)
      );
    }

    // Rule 2: Check for suspicious accounts
    if (suspiciousAccounts.contains(transaction.getAccountId())) {
      return new FraudDetectionResult(
          transaction.getTransactionId(),
          true,
          String.format("Account %s is in suspicious accounts list",
              transaction.getAccountId())
      );
    }

    // Rule 3: Check for high-risk merchants
    if (highRiskMerchants.contains(transaction.getMerchant())) {
      return new FraudDetectionResult(
          transaction.getTransactionId(),
          true,
          String.format("Merchant %s is in high-risk merchants list",
              transaction.getMerchant())
      );
    }

    // Rule 4: Unusual location patterns (simplified example)
    if (transaction.getLocation() != null &&
        transaction.getLocation().contains("High Risk Zone")) {
      return new FraudDetectionResult(
          transaction.getTransactionId(),
          true,
          String.format("Transaction from high risk location: %s",
              transaction.getLocation())
      );
    }

    return new FraudDetectionResult(
        transaction.getTransactionId(),
        false,
        "Transaction appears legitimate"
    );
  }
}
