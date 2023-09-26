package io.kx.loanapp.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanapp.domain.LoanAppDomainEvent;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Subscribe.EventSourcedEntity(value = LoanAppService.class, ignoreUnknown = true)
public class LoanAppToLoanProcEventingAction extends Action {

    private Logger logger = LoggerFactory.getLogger(LoanAppToLoanProcEventingAction.class);


    private final ActionCreationContext ctx;

    private final ComponentClient componentClient;

    public LoanAppToLoanProcEventingAction(ActionCreationContext ctx, ComponentClient componentClient) {
        this.componentClient = componentClient;
        this.ctx = ctx;
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onSubmitted(LoanAppDomainEvent.Submitted event) {
        logger.info("+++++++++++++++++++++++++");
        logger.info("eventId : " + event.loanAppId());
        logger.info("+++++++++++++++++++++++++");

        return effects().asyncReply(
                componentClient
                        .forEventSourcedEntity(event.loanAppId())
                        .call(LoanProcService::process)
                        .execute()
                        .thenApply(__ -> LoanAppApi.EmptyResponse.of())
        );
    }
}