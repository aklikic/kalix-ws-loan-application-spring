package io.kx.loanproc.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = LoanProcService.class, ignoreUnknown = true)
public class LoanProcToLoanAppEventingAction extends Action {

    private final ActionCreationContext ctx;
    private final KalixClient kalixClient;

    @Autowired
    public LoanProcToLoanAppEventingAction(ActionCreationContext ctx, KalixClient kalixClient) {
        this.ctx = ctx;
        this.kalixClient = kalixClient;
    }

    public Effect<LoanProcApi.EmptyResponse> onApproved(LoanProcDomainEvent.Approved event){
        CompletionStage<LoanProcApi.EmptyResponse> processRes =
                kalixClient.post("/loanapp/"+event.loanAppId()+"/approve",LoanAppApi.EmptyResponse.class).execute()
                        .thenApply(res -> LoanProcApi.EmptyResponse.of())
                        .exceptionally(e -> {
                            if(e.getCause() instanceof WebClientResponseException && ((WebClientResponseException)e.getCause()).getStatusCode() == HttpStatus.NOT_FOUND || ((WebClientResponseException)e.getCause()).getStatusCode() == HttpStatus.BAD_REQUEST){
                                //ignore if not found to not block
                                return LoanProcApi.EmptyResponse.of();
                            } else {
                                throw (RuntimeException)e;
                            }
                        });

        return effects().asyncReply(processRes);
    }

    public Effect<LoanProcApi.EmptyResponse> onDeclined(LoanProcDomainEvent.Declined event){
        CompletionStage<LoanProcApi.EmptyResponse> processRes =
                kalixClient.post("/loanapp/"+event.loanAppId()+"/decline",new LoanAppApi.DeclineRequest(event.reason()),LoanAppApi.EmptyResponse.class).execute()
                        .thenApply(res -> LoanProcApi.EmptyResponse.of())
                        .exceptionally(e -> {
                            if(e.getCause() instanceof WebClientResponseException && (((WebClientResponseException)e.getCause()).getStatusCode() == HttpStatus.NOT_FOUND || ((WebClientResponseException)e.getCause()).getStatusCode() == HttpStatus.BAD_REQUEST)){
                                //ignore if not found to not block
                                return LoanProcApi.EmptyResponse.of();
                            } else {
                                throw (RuntimeException)e;
                            }
                        });

        return effects().asyncReply(processRes);
    }
}
