package io.kx.loanproc.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;

@Subscribe.EventSourcedEntity(value = LoanProcService.class, ignoreUnknown = true)
public class LoanProcToLoanAppEventingAction extends Action {

    private final ActionCreationContext ctx;
    private final ComponentClient componentClient;

    public LoanProcToLoanAppEventingAction(ActionCreationContext ctx, ComponentClient componentClient) {
        this.ctx = ctx;
        this.componentClient = componentClient;
    }

    public Effect<LoanProcApi.EmptyResponse> onApproved(LoanProcDomainEvent.Approved event){
        return effects().asyncReply(
                componentClient
                        .forEventSourcedEntity(event.loanAppId())
                        .call(LoanAppService::approve)
                        .execute()
                        .thenApply(__ -> LoanProcApi.EmptyResponse.of())
        );
    }

    public Effect<LoanProcApi.EmptyResponse> onDeclined(LoanProcDomainEvent.Declined event){
        return effects().asyncReply(
                componentClient
                        .forEventSourcedEntity(event.loanAppId())
                        .call(LoanAppService::decline).params(new LoanAppApi.DeclineRequest(event.reason()))
                        .execute()
                        .thenApply(__ -> LoanProcApi.EmptyResponse.of())
        );
    }
}
