/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import io.github.kasperdeng.frauddetection.model.Transaction;
import io.github.kasperdeng.frauddetection.service.FraudDetectionService;
import io.github.kasperdeng.frauddetection.service.FraudDetectionServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FraudDetectionControllerTest {

  @Mock
  private FraudDetectionService fraudDetectionService;

  private FraudDetectionServiceImpl fraudDetectionServiceImpl;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    fraudDetectionServiceImpl = new FraudDetectionServiceImpl(new BigDecimal("5000"),
        List.of("ACCT123456", "ACCT789012", "ACCT345678"),
        List.of("Suspicious Merchant LLC", "Questionable Goods Inc"));
  }

  @Test
  void testAnalyzeTransaction() {
    Transaction transaction = new Transaction(null, "ACCT-001", new BigDecimal("100.00"),
        "Safe Merchant", "New York", LocalDateTime.now());

    FraudDetectionResult expectedResult = new FraudDetectionResult(null, false,
        "Transaction appears legitimate");

    when(fraudDetectionService.analyzeTransaction(transaction)).thenReturn(expectedResult);

    FraudDetectionResult result = new FraudDetectionController(
        fraudDetectionServiceImpl).analyzeTransaction(transaction);

    assertThat(result).isEqualTo(expectedResult);
  }
}
