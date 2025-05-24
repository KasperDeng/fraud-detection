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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import io.github.kasperdeng.frauddetection.model.Transaction;
import io.github.kasperdeng.frauddetection.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SQSListenerTest {

  @Mock
  private FraudDetectionService fraudDetectionService;

  private SQSListener sqsListener;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    sqsListener = new SQSListener(fraudDetectionService);
  }

  @Test
  public void testProcessTransactionByAnalyzeTransactionWithFraudulentTrue() {
    FraudDetectionResult result = mock(FraudDetectionResult.class);
    when(fraudDetectionService.analyzeTransaction(any(Transaction.class))).thenReturn(result);
    when(result.isFraudulent()).thenReturn(true);
    Transaction transaction = new Transaction();

    sqsListener.processTransaction(transaction);

    verify(fraudDetectionService).analyzeTransaction(transaction);
  }

  @Test
  public void testProcessTransactionByAnalyzeTransactionWithFraudulentFalse() {
    FraudDetectionResult result = mock(FraudDetectionResult.class);
    when(fraudDetectionService.analyzeTransaction(any(Transaction.class))).thenReturn(result);
    when(result.isFraudulent()).thenReturn(true);
    Transaction transaction = new Transaction();

    sqsListener.processTransaction(transaction);

    verify(fraudDetectionService).analyzeTransaction(transaction);
  }
}
