package io.kx.loanproc.api;

import io.grpc.Status;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import io.kx.loanproc.domain.LoanProcDomainState;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.annotations.TypeName;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@Id("loanAppId")
@TypeId("loanAppId")
@RequestMapping("/loanproc/{loanAppId}")
public class LoanProcService extends EventSourcedEntity<LoanProcDomainState, LoanProcDomainEvent> {

    private final String loanAppId;

    public LoanProcService(EventSourcedEntityContext context) {
        this.loanAppId = context.entityId();
    }

    @Override
    public LoanProcDomainState emptyState() {
        return LoanProcDomainState.empty(loanAppId);
    }


    @PostMapping("/process")
    public Effect<LoanProcApi.EmptyResponse> process(){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                LoanProcDomainEvent.ReadyForReview event =
                        new LoanProcDomainEvent.ReadyForReview(
                                loanAppId,
                                Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanProcApi.EmptyResponse.of());

            case STATUS_READY_FOR_REVIEW:
                return effects().reply(LoanProcApi.EmptyResponse.of());
            case STATUS_APPROVED:
            case STATUS_DECLINED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }
    }

    @PostMapping("/approve")
    public Effect<LoanProcApi.EmptyResponse> approve(@RequestBody LoanProcApi.ApproveRequest request){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                return effects().error("Not found", Status.Code.NOT_FOUND);
            case STATUS_READY_FOR_REVIEW:
                LoanProcDomainEvent.Approved event = new LoanProcDomainEvent.Approved(loanAppId, request.reviewerId(), Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanProcApi.EmptyResponse.of());
            case STATUS_APPROVED:
                return effects().reply(LoanProcApi.EmptyResponse.of());
            case STATUS_DECLINED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }


    }

    @PostMapping("/decline")
    public Effect<LoanProcApi.EmptyResponse> decline(@RequestBody LoanProcApi.DeclineRequest request){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                return effects().error("Not found", Status.Code.NOT_FOUND);
            case STATUS_READY_FOR_REVIEW:
                LoanProcDomainEvent.Declined event = new LoanProcDomainEvent.Declined(loanAppId, request.reviewerId(), request.reason(),Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanProcApi.EmptyResponse.of());
            case STATUS_DECLINED:
                return effects().reply(LoanProcApi.EmptyResponse.of());
            case STATUS_APPROVED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }
    }

    @GetMapping
    public Effect<LoanProcApi.GetResponse> get(){
        return effects().reply(new LoanProcApi.GetResponse(currentState()));
    }

    @EventHandler
    public LoanProcDomainState onReadyForReview(LoanProcDomainEvent.ReadyForReview event){
        return currentState().onReadyForReview(event);
    }
    @EventHandler
    public LoanProcDomainState onApproved(LoanProcDomainEvent.Approved event){
        return currentState().onApproved(event);
    }
    @EventHandler
    public LoanProcDomainState onDeclined(LoanProcDomainEvent.Declined event){
        return currentState().onDeclined(event);
    }
}
