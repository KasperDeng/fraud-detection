/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@ComponentScan(basePackages = {"io.github.kasperdeng.frauddetection"})
@Configuration
@Slf4j
public class FraudDetectionApplication {

  public static void main(String[] args) {
    try {
      SpringApplication.run(FraudDetectionApplication.class, args);
    } catch (Exception e) {
      log.error("Fail to start fraud detection service: {}", e.getMessage());
      log.error("Closing fraud detection due to error.");
      System.exit(-1);
    }
  }
}
