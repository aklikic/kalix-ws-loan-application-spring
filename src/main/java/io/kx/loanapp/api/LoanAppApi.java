package io.kx.loanapp.api;

import io.kx.loanapp.domain.LoanAppDomainState;

public sealed interface LoanAppApi {
    record SubmitRequest(String loanAppId,
                         String clientId,
                         Integer clientMonthlyIncomeCents,
                         Integer loanAmountCents,
                         Integer loanDurationMonths) implements LoanAppApi{}

    record ApproveRequest(String loanAppId) implements LoanAppApi{}
    record DeclineRequest(String loanAppId, String reason) implements LoanAppApi{}

    record EmptyResponse()implements LoanAppApi{
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    record GetResponse(LoanAppDomainState state) implements LoanAppApi{}
}
