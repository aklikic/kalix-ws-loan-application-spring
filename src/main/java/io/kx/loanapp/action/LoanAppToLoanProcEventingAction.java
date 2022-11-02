package io.kx.loanapp.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanapp.domain.LoanAppDomainEvent;
import io.kx.loanproc.api.LoanProcApi;
import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class LoanAppToLoanProcEventingAction extends Action {

    private final ActionCreationContext ctx;
    private final WebClient webClient;

    @Autowired
    public LoanAppToLoanProcEventingAction(ActionCreationContext ctx, WebClient webClient) {
        this.ctx = ctx;
        this.webClient = webClient;
    }

    @Subscribe.EventSourcedEntity(LoanAppService.class)
    public Action.Effect<LoanAppApi.EmptyResponse> onSubmitted(LoanAppDomainEvent.Submitted event){
        CompletableFuture<LoanAppApi.EmptyResponse> processRes =
        webClient.post().uri("/loanproc/"+event.loanAppId()+"/process")
                .retrieve()
                .bodyToMono(LoanProcApi.EmptyResponse.class)
                .map(res -> LoanAppApi.EmptyResponse.of())
                .toFuture();

        return effects().asyncReply(processRes);
    }

    @Subscribe.EventSourcedEntity(LoanAppService.class)
    public Action.Effect<LoanAppApi.EmptyResponse> onApproved(LoanAppDomainEvent.Approved event){
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }

    @Subscribe.EventSourcedEntity(LoanAppService.class)
    public Action.Effect<LoanAppApi.EmptyResponse> onDeclined(LoanAppDomainEvent.Declined event){
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }
}
