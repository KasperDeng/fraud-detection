/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.testcase;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {
        "pretty",
        "html:target/cucumber-html-report/fraud-detection-report.html",
        "json:target/cucumber-json-report/fraud-detection-report-ct-report.json"
    },
    features = {"src/test/resources/io/github/kasperdeng/frauddetection/testcase"},
    glue = {"io.github.kasperdeng.frauddetection.steps"},
    tags = "@fraud-detection")
public class FraudDetectionTest {

  @BeforeClass
  public static void setUp() {
  }
}
