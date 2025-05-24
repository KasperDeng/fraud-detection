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

public interface FraudDetectionService {
  FraudDetectionResult analyzeTransaction(Transaction transaction);
}
