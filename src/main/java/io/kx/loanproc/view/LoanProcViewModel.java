package io.kx.loanproc.view;

import io.kx.loanproc.domain.LoanProcDomainStatus;

import java.time.Instant;
import java.util.List;

public sealed interface LoanProcViewModel {
    record ViewRecord(String statusId, LoanProcDomainStatus status, String loanAppId, Instant lastUpdated) implements LoanProcViewModel{}
    record ViewRequest(String statusId) implements LoanProcViewModel{}
    record ViewResponse(List<ViewRecord> records) implements LoanProcViewModel{}
}
