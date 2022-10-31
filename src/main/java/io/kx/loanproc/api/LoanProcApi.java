package io.kx.loanproc.api;

import io.kx.loanproc.domain.LoanProcDomainState;

public sealed interface LoanProcApi {
    record ApproveRequest(String reviewerId) implements LoanProcApi {}
    record DeclineRequest(String reviewerId, String reason) implements LoanProcApi {}

    record EmptyResponse()implements LoanProcApi {
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    record GetResponse(LoanProcDomainState state) implements LoanProcApi {}
}
