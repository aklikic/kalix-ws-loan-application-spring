package io.kx.loanproc;

import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import io.kx.loanproc.domain.LoanProcDomainState;
import io.kx.loanproc.domain.LoanProcDomainStatus;
import kalix.javasdk.testkit.EventSourcedResult;
import kalix.springsdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoanProcServiceTest {

    @Test
    public void happyPath(){
        var loanAppId = UUID.randomUUID().toString();
        EventSourcedTestKit<LoanProcDomainState, LoanProcService> testKit = EventSourcedTestKit.of(loanAppId,LoanProcService::new);

        var reviewerId = UUID.randomUUID().toString();
        EventSourcedResult<LoanProcApi.EmptyResponse> processResult = testKit.call(service -> service.process());
        LoanProcDomainEvent.ReadyForReview readyForReviewEvent = processResult.getNextEventOfType(LoanProcDomainEvent.ReadyForReview.class);
        assertEquals(loanAppId,readyForReviewEvent.loanAppId());
        LoanProcDomainState updatedStat = (LoanProcDomainState)processResult.getUpdatedState();
        assertEquals(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW,updatedStat.status());

        EventSourcedResult<LoanProcApi.EmptyResponse> approveResponse = testKit.call(service -> service.approve(new LoanProcApi.ApproveRequest(reviewerId)));
        LoanProcDomainEvent.Approved approvedEvent = approveResponse.getNextEventOfType(LoanProcDomainEvent.Approved.class);
        assertEquals(loanAppId,approvedEvent.loanAppId());

        updatedStat = (LoanProcDomainState)approveResponse.getUpdatedState();
        assertEquals(LoanProcDomainStatus.STATUS_APPROVED,updatedStat.status());
    }
}
