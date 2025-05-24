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

import io.github.kasperdeng.frauddetection.service.FraudDetectionService;
import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import io.github.kasperdeng.frauddetection.model.Transaction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud-detection")
@SuppressWarnings("unused")
public class FraudDetectionController {

  private final FraudDetectionService fraudDetectionService;

  public FraudDetectionController(FraudDetectionService fraudDetectionService) {
    this.fraudDetectionService = fraudDetectionService;
  }

  @PostMapping("/analyze")
  public FraudDetectionResult analyzeTransaction(@RequestBody Transaction transaction) {
    return fraudDetectionService.analyzeTransaction(transaction);
  }
}
