package io.kx.loanproc;

import io.kx.Main;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.domain.LoanProcDomainStatus;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.domain.LoanProcDomainStatus;
import kalix.springsdk.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This is a skeleton for implmenting integration tests for a Kalix application built with the Spring SDK.
 *
 * This test will initiate a Kalix Proxy using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 *
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {

  private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);
  @Autowired
  private WebClient webClient;

  private Duration timeout = Duration.of(5, ChronoUnit.SECONDS);

  @Test
  public void test() throws Exception {
    var loanAppId = "11";
    var reviewerId = "99999";

    logger.info("Sending process...");
    ResponseEntity<LoanProcApi.EmptyResponse> emptyRes =
    webClient.post()
            .uri("/loanproc/"+loanAppId+"/process")
            .retrieve()
            .toEntity(LoanProcApi.EmptyResponse.class)
            .block(timeout);

    assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

    logger.info("Sending get...");
    LoanProcApi.GetResponse getRes =
    webClient.get()
            .uri("/loanproc/"+loanAppId)
            .retrieve()
            .bodyToMono(LoanProcApi.GetResponse.class)
            .block(timeout);

    assertEquals(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW, getRes.state().status());

    logger.info("Sending approve...");
    emptyRes =
    webClient.post()
            .uri("/loanproc/"+loanAppId+"/approve")
            .bodyValue(new LoanProcApi.ApproveRequest(reviewerId))
            .retrieve()
            .toEntity(LoanProcApi.EmptyResponse.class)
            .block(timeout);

    logger.info("Sending get...");
    getRes =
    webClient.get()
            .uri("/loanproc/"+loanAppId)
            .retrieve()
            .bodyToMono(LoanProcApi.GetResponse.class)
            .block(timeout);

    assertEquals(LoanProcDomainStatus.STATUS_APPROVED,getRes.state().status());


  }
}