package io.kx.loanproc.domain;

import java.time.Instant;

public record LoanProcDomainState(String loanAppId,
                                  String reviewerId,
                                  LoanProcDomainStatus status,
                                  String declineReason,
                                  Instant lastUpdatedTimestamp) {

    public static LoanProcDomainState empty(String loanAppId){
        return new LoanProcDomainState(loanAppId,null,LoanProcDomainStatus.STATUS_UNKNOWN,null, null);
    }
    public LoanProcDomainState onReadyForReview(LoanProcDomainEvent.ReadyForReview event){
        return new LoanProcDomainState(this.loanAppId, null, LoanProcDomainStatus.STATUS_READY_FOR_REVIEW,null,event.timestamp());
    }
    public LoanProcDomainState onApproved(LoanProcDomainEvent.Approved event){
        return new LoanProcDomainState(this.loanAppId, event.reviewerId(), LoanProcDomainStatus.STATUS_APPROVED,null,event.timestamp());
    }
    public LoanProcDomainState onDeclined(LoanProcDomainEvent.Declined event){
        return new LoanProcDomainState(this.loanAppId, event.reviewerId(), LoanProcDomainStatus.STATUS_DECLINED,event.reason(),event.timestamp());
    }
}
