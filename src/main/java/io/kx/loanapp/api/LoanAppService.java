package io.kx.loanapp.api;

import io.grpc.Status;
import io.kx.loanapp.domain.LoanAppDomainEvent;
import io.kx.loanapp.domain.LoanAppDomainState;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@Id("loanAppId")
@TypeId("loanapp")
@RequestMapping("/loanapp/{loanAppId}")
public class LoanAppService extends EventSourcedEntity<LoanAppDomainState, LoanAppDomainEvent> {

    private final String loanAppId;
    public LoanAppService(EventSourcedEntityContext context) {
        this.loanAppId = context.entityId();
    }

    @Override
    public LoanAppDomainState emptyState() {
        return LoanAppDomainState.empty(loanAppId);
    }


    @PostMapping("/submit")
    public Effect<LoanAppApi.EmptyResponse> submit(@RequestBody LoanAppApi.SubmitRequest request){
        switch (currentState().status()) {
            case STATUS_UNKNOWN -> {
                LoanAppDomainEvent.Submitted event =
                        new LoanAppDomainEvent.Submitted(
                                loanAppId,
                                request.clientId(),
                                request.clientMonthlyIncomeCents(),
                                request.loanAmountCents(),
                                request.loanDurationMonths(),
                                Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanAppApi.EmptyResponse.of());
            }
            case STATUS_IN_REVIEW -> {
                return effects().reply(LoanAppApi.EmptyResponse.of());
            }
            default -> {
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
            }
        }
    }

    @PostMapping("/approve")
    public Effect<LoanAppApi.EmptyResponse> approve(){
        switch (currentState().status()) {
            case STATUS_UNKNOWN -> {
                return effects().error("Not found", Status.Code.NOT_FOUND);
            }
            case STATUS_IN_REVIEW -> {
                LoanAppDomainEvent.Approved event = new LoanAppDomainEvent.Approved(loanAppId, Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanAppApi.EmptyResponse.of());
            }
            case STATUS_APPROVED -> {
                return effects().reply(LoanAppApi.EmptyResponse.of());
            }
            default -> {
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
            }
        }


    }

    @PostMapping("/decline")
    public Effect<LoanAppApi.EmptyResponse> decline(@RequestBody LoanAppApi.DeclineRequest request){
        switch (currentState().status()) {
            case STATUS_UNKNOWN -> {
                return effects().error("Not found", Status.Code.NOT_FOUND);
            }
            case STATUS_IN_REVIEW -> {
                LoanAppDomainEvent.Declined event = new LoanAppDomainEvent.Declined(loanAppId, request.reason(), Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanAppApi.EmptyResponse.of());
            }
            case STATUS_DECLINED -> {
                return effects().reply(LoanAppApi.EmptyResponse.of());
            }
            default -> {
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
            }
        }
    }

    @GetMapping
    public Effect<LoanAppApi.GetResponse> get(){
        return effects().reply(new LoanAppApi.GetResponse(currentState()));
    }

    @EventHandler
    public LoanAppDomainState onSubmitted(LoanAppDomainEvent.Submitted event){
        return currentState().onSubmitted(event);
    }
    @EventHandler
    public LoanAppDomainState onApproved(LoanAppDomainEvent.Approved event){
        return currentState().onApproved(event);
    }
    @EventHandler
    public LoanAppDomainState onDeclined(LoanAppDomainEvent.Declined event){
        return currentState().onDeclined(event);
    }
}
