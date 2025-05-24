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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kasperdeng.frauddetection.model.Transaction;
import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceImplTest {

  private FraudDetectionServiceImpl fraudDetectionService;

  private Transaction normalTransaction;
  private Transaction highAmountTransaction;
  private Transaction suspiciousAccountTransaction;

  @BeforeEach
  void setUp() {
    fraudDetectionService = new FraudDetectionServiceImpl(new BigDecimal("5000"),
        List.of("ACCT123456,ACCT789012,ACCT345678"),
        List.of("Suspicious Merchant LLC,Questionable Goods Inc"));

    normalTransaction = new Transaction(
        "TXN123", "ACCT987654", new BigDecimal("100.00"),
        "Normal Merchant", "New York", LocalDateTime.now()
    );

    highAmountTransaction = new Transaction(
        "TXN456", "ACCT987654", new BigDecimal("15000.00"),
        "Normal Merchant", "New York", LocalDateTime.now()
    );

    suspiciousAccountTransaction = new Transaction(
        "TXN789", "ACCT123456", new BigDecimal("50.00"),
        "Normal Merchant", "New York", LocalDateTime.now()
    );
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testNormalTransaction() {
    FraudDetectionResult result = fraudDetectionService.analyzeTransaction(normalTransaction);
    assertFalse(result.isFraudulent());
    assertEquals("Transaction appears legitimate", result.getReason());
  }

  @Test
  void testHighAmountTransaction() {
    FraudDetectionResult result = fraudDetectionService.analyzeTransaction(highAmountTransaction);
    assertTrue(result.isFraudulent());
    assertTrue(result.getReason().contains("exceeds threshold"));
  }

  @Test
  void testSuspiciousAccountTransaction() {
    FraudDetectionResult result = fraudDetectionService.analyzeTransaction(
        suspiciousAccountTransaction);
    assertTrue(result.isFraudulent());
    assertTrue(result.getReason().contains("suspicious accounts list"));
  }
}
