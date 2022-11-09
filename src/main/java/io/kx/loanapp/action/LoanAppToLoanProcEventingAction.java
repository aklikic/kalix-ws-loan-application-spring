package io.kx.loanapp.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanapp.domain.LoanAppDomainEvent;
import io.kx.loanproc.api.LoanProcApi;
import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = LoanAppService.class, ignoreUnknown = true)
public class LoanAppToLoanProcEventingAction extends Action {

    private final ActionCreationContext ctx;
    private final KalixClient kalixClient;

    public LoanAppToLoanProcEventingAction(ActionCreationContext ctx, KalixClient kalixClient) {
        this.ctx = ctx;
        this.kalixClient = kalixClient;
    }

//    @Subscribe.EventSourcedEntity(LoanAppService.class)
    public Action.Effect<LoanAppApi.EmptyResponse> onSubmitted(LoanAppDomainEvent.Submitted event){
        CompletionStage<LoanAppApi.EmptyResponse> processRes =
                kalixClient.post("/loanproc/"+event.loanAppId()+"/process",LoanProcApi.EmptyResponse.class).execute()
                        .thenApply(res -> LoanAppApi.EmptyResponse.of());

        return effects().asyncReply(processRes);
    }

//    @Subscribe.EventSourcedEntity(LoanAppService.class)
//    public Action.Effect<LoanAppApi.EmptyResponse> onApproved(LoanAppDomainEvent.Approved event){
//        return effects().reply(LoanAppApi.EmptyResponse.of());
//    }
//
//    @Subscribe.EventSourcedEntity(LoanAppService.class)
//    public Action.Effect<LoanAppApi.EmptyResponse> onDeclined(LoanAppDomainEvent.Declined event){
//        return effects().reply(LoanAppApi.EmptyResponse.of());
//    }
}
