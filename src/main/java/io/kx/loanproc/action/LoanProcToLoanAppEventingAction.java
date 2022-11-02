package io.kx.loanproc.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanapp.domain.LoanAppDomainEvent;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.springsdk.annotations.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

public class LoanProcToLoanAppEventingAction extends Action {

    private final ActionCreationContext ctx;
    private final WebClient webClient;

    @Autowired
    public LoanProcToLoanAppEventingAction(ActionCreationContext ctx, WebClient webClient) {
        this.ctx = ctx;
        this.webClient = webClient;
    }

    @Subscribe.EventSourcedEntity(LoanProcService.class)
    public Effect<LoanProcApi.EmptyResponse> onApproved(LoanProcDomainEvent.ReadyForReview event){
        return effects().reply(LoanProcApi.EmptyResponse.of());
    }

    @Subscribe.EventSourcedEntity(LoanProcService.class)
    public Effect<LoanProcApi.EmptyResponse> onApproved(LoanProcDomainEvent.Approved event){
        CompletableFuture<LoanProcApi.EmptyResponse> processRes =
        webClient.post().uri("/loanapp/"+event.loanAppId()+"/approve")
                .retrieve()
                .bodyToMono(LoanAppApi.EmptyResponse.class)
                .map(res -> LoanProcApi.EmptyResponse.of())
                .toFuture();

        return effects().asyncReply(processRes);
    }

    @Subscribe.EventSourcedEntity(LoanProcService.class)
    public Effect<LoanProcApi.EmptyResponse> onDeclined(LoanProcDomainEvent.Declined event){
        CompletableFuture<LoanProcApi.EmptyResponse> processRes =
                webClient.post().uri("/loanapp/"+event.loanAppId()+"/decline")
                        .retrieve()
                        .bodyToMono(LoanAppApi.EmptyResponse.class)
                        .map(res -> LoanProcApi.EmptyResponse.of())
                        .toFuture();

        return effects().asyncReply(processRes);
    }
}
