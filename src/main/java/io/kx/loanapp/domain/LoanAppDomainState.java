package io.kx.loanapp.domain;

import java.time.Instant;

public record LoanAppDomainState(String loanAppId,
                                 String clientId,
                                 Integer clientMonthlyIncomeCents,
                                 Integer loanAmountCents,
                                 Integer loanDurationMonths,
                                 LoanAppDomainStatus status,
                                 String declineReason,
                                 Instant lastUpdatedTimestamp) {

    public static LoanAppDomainState empty(String loanAppId){
        return new LoanAppDomainState(loanAppId,null,null,null, null,LoanAppDomainStatus.STATUS_UNKNOWN,null,null);
    }
    public LoanAppDomainState onSubmitted(LoanAppDomainEvent.Submitted event){
        return new LoanAppDomainState(this.loanAppId, event.clientId(), event.clientMonthlyIncomeCents(), event.loanAmountCents(), event.loanDurationMonths(), LoanAppDomainStatus.STATUS_IN_REVIEW,null,event.timestamp());
    }
    public LoanAppDomainState onApproved(LoanAppDomainEvent.Approved event){
        return new LoanAppDomainState(this.loanAppId, this.clientId, this.clientMonthlyIncomeCents, this.loanAmountCents, this.loanDurationMonths, LoanAppDomainStatus.STATUS_APPROVED,null,event.timestamp());
    }
    public LoanAppDomainState onDeclined(LoanAppDomainEvent.Declined event){
        return new LoanAppDomainState(this.loanAppId, this.clientId, this.clientMonthlyIncomeCents, this.loanAmountCents, this.loanDurationMonths, LoanAppDomainStatus.STATUS_DECLINED,event.reason(),event.timestamp());
    }
}
