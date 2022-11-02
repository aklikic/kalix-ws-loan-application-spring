package io.kx.loanproc.view;

import java.util.List;

public sealed interface LoanProcViewModel {
    //record ViewRecord(String statusId, LoanProcDomainStatus status, String loanAppId, Instant lastUpdated) implements LoanProcViewModel{}
    record ViewRecord(String statusId, String loanAppId, long lastUpdated) implements LoanProcViewModel{}
    record ViewRequest(String statusId) implements LoanProcViewModel{}
    record ViewResponse(List<ViewRecord> records) implements LoanProcViewModel{}
}
