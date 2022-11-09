package io.kx.loanproc.trigger;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Subscribe.EventSourcedEntity(value = LoanProcService.class, ignoreUnknown = true)
public class LoanProcTimeoutTriggerAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(LoanProcTimeoutTriggerAction.class);
    public static final int defaultTimeoutMillis = 2000;
    private final KalixClient kalixClient;

    public LoanProcTimeoutTriggerAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onReadyForReview(LoanProcDomainEvent.ReadyForReview event){
        logger.info("onReadyForReview: {}",event.loanAppId());
        var deferredCall = kalixClient.post("/loanproc/"+event.loanAppId()+"/decline",new LoanProcApi.DeclineRequest("SYSTEM", "timeout by timer"),LoanProcApi.EmptyResponse.class);
        timers().startSingleTimer(getTimerName(event.loanAppId()), Duration.ofMillis(getTimeoutMillis()),deferredCall);
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

    private int getTimeoutMillis(){
        String timeoutMillis = System.getenv("LOAN_PROC_TIMEOUT_MILLIS");
        try {
            return StringUtils.hasLength(timeoutMillis) ? Integer.parseInt(timeoutMillis) : defaultTimeoutMillis;
        }catch(NumberFormatException e){
            return defaultTimeoutMillis;
        }
    }

    private String getTimerName(String loanAppId){
        return "timeout-"+loanAppId;
    }
}
