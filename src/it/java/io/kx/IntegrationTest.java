package io.kx;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.domain.LoanAppDomainStatus;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.domain.LoanProcDomainStatus;
import io.kx.loanproc.view.LoanProcViewModel;
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
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {
    private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);
    @Autowired
    private WebClient webClient;

    private Duration timeout = Duration.of(5, ChronoUnit.SECONDS);

    @Test
    public void loanAppHappyPath() throws Exception {
        var loanAppId = UUID.randomUUID().toString();
        var submitRequest = new LoanAppApi.SubmitRequest(
                "clientId",
                5000,
                2000,
                36);

        logger.info("Sending submit...");
        ResponseEntity<LoanAppApi.EmptyResponse> emptyRes =
                webClient.post()
                        .uri("/loanapp/"+loanAppId+"/submit")
                        .bodyValue(submitRequest)
                        .retrieve()
                        .toEntity(LoanAppApi.EmptyResponse.class)
                        .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        logger.info("Sending get...");
        LoanAppApi.GetResponse getRes =
                webClient.get()
                        .uri("/loanapp/"+loanAppId)
                        .retrieve()
                        .bodyToMono(LoanAppApi.GetResponse.class)
                        .block(timeout);

        assertEquals(LoanAppDomainStatus.STATUS_IN_REVIEW,getRes.state().status());

        logger.info("Sending approve...");
        emptyRes =
                webClient.post()
                        .uri("/loanapp/"+loanAppId+"/approve")
                        .retrieve()
                        .toEntity(LoanAppApi.EmptyResponse.class)
                        .block(timeout);

        logger.info("Sending get...");
        getRes =
                webClient.get()
                        .uri("/loanapp/"+loanAppId)
                        .retrieve()
                        .bodyToMono(LoanAppApi.GetResponse.class)
                        .block(timeout);

        assertEquals(LoanAppDomainStatus.STATUS_APPROVED,getRes.state().status());


    }

    @Test
    public void loanProcHappyPath() throws Exception {
        var loanAppId = UUID.randomUUID().toString();
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

        //views are eventually consistent
        Thread.sleep(2000);
        logger.info("Checking view...");
        LoanProcViewModel.ViewRecord viewRes =
                webClient.post()
                        .uri("/loanproc/views/by-status")
                        .bodyValue(new LoanProcViewModel.ViewRequest(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name()))
                        .retrieve()
                        .bodyToMono(LoanProcViewModel.ViewRecord.class)
                        .block(timeout);

        assertEquals(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name(),viewRes.statusId());

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


        ClientResponse emptyViewRes =
                webClient.post()
                        .uri("/loanproc/views/by-status")
                        .bodyValue(new LoanProcViewModel.ViewRequest(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name()))
                        .exchangeToMono(Mono::just)
                        .block(timeout);

        assertEquals(HttpStatus.NOT_FOUND,emptyViewRes.statusCode());
    }


    @Test
    public void endToEndHappyPath() throws Exception {
        var loanAppId = UUID.randomUUID().toString();
        var reviewerId = "99999";
        var submitRequest = new LoanAppApi.SubmitRequest(
                "clientId",
                5000,
                2000,
                36);

        logger.info("Sending loan app submit...");
        ResponseEntity<LoanAppApi.EmptyResponse> emptyLaRes =
                webClient.post()
                        .uri("/loanapp/"+loanAppId+"/submit")
                        .bodyValue(submitRequest)
                        .retrieve()
                        .toEntity(LoanAppApi.EmptyResponse.class)
                        .block(timeout);

        assertEquals(HttpStatus.OK,emptyLaRes.getStatusCode());


        //views are eventually consistent
        Thread.sleep(2000);
        logger.info("Checking loan proc view for STATUS_READY_FOR_REVIEW...");
        LoanProcViewModel.ViewRecord viewLpRes =
                webClient.post()
                        .uri("/loanproc/views/by-status")
                        .bodyValue(new LoanProcViewModel.ViewRequest(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name()))
                        .retrieve()
                        .bodyToMono(LoanProcViewModel.ViewRecord.class)
                        .block(timeout);

        assertEquals(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name(),viewLpRes.statusId());


        logger.info("Sending loan proc approve...");
        ResponseEntity<LoanProcApi.EmptyResponse> emptyLpRes =
                webClient.post()
                        .uri("/loanproc/"+loanAppId+"/approve")
                        .bodyValue(new LoanProcApi.ApproveRequest(reviewerId))
                        .retrieve()
                        .toEntity(LoanProcApi.EmptyResponse.class)
                        .block(timeout);

        Thread.sleep(2000);

        logger.info("Sending get on loan app...");
        LoanAppApi.GetResponse getRes =
                webClient.get()
                        .uri("/loanapp/"+loanAppId)
                        .retrieve()
                        .bodyToMono(LoanAppApi.GetResponse.class)
                        .block(timeout);

        assertEquals(LoanAppDomainStatus.STATUS_APPROVED,getRes.state().status());


    }

}
