package io.kx.loanproc.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanproc.LoanProcConfig;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Subscribe.EventSourcedEntity(value = LoanProcService.class, ignoreUnknown = true)
public class LoanProcTimeoutTriggerAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(LoanProcTimeoutTriggerAction.class);
    public static final int defaultTimeoutMillis = 2000;
    private final ComponentClient componentClient;
    private final LoanProcConfig config;

    public LoanProcTimeoutTriggerAction(ComponentClient componentClient, LoanProcConfig config) {
        this.componentClient = componentClient;
        this.config = config;
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onReadyForReview(LoanProcDomainEvent.ReadyForReview event){
        logger.info("onReadyForReview: {}",event.loanAppId());
        var deferredCall = componentClient
                .forEventSourcedEntity(event.loanAppId())
                .call(LoanProcService::decline).params(new LoanProcApi.DeclineRequest("SYSTEM", "timeout by timer"));
        timers().startSingleTimer(getTimerName(event.loanAppId()),
                Duration.ofMillis(config.getTimeoutMillis()),
                deferredCall);
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onApproved(LoanProcDomainEvent.Approved event){
        logger.info("onApproved: {}",event.loanAppId());
        timers().cancel(getTimerName(event.loanAppId()));
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }
    public Action.Effect<LoanAppApi.EmptyResponse> onDeclined(LoanProcDomainEvent.Declined event){
        logger.info("onDeclined: {}",event.loanAppId());
        timers().cancel(getTimerName(event.loanAppId()));
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }
//
//    private int getTimeoutMillis(){
//        String timeoutMillis = System.getenv("LOAN_PROC_TIMEOUT_MILLIS");
//        try {
//            return StringUtils.hasLength(timeoutMillis) ? Integer.parseInt(timeoutMillis) : defaultTimeoutMillis;
//        }catch(NumberFormatException e){
//            return defaultTimeoutMillis;
//        }
//    }

    private String getTimerName(String loanAppId){
        return "timeout-"+loanAppId;
    }
}
