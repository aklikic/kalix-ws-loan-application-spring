package io.kx.loanproc.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = LoanProcDomainEvent.ReadyForReview.class, name = "ready-for-review"),
                @JsonSubTypes.Type(value = LoanProcDomainEvent.Approved.class, name = "approved"),
                @JsonSubTypes.Type(value = LoanProcDomainEvent.Declined.class, name = "declined")
        })
public sealed interface LoanProcDomainEvent {
    record ReadyForReview(String loanAppId,
                          Instant timestamp) implements LoanProcDomainEvent {}

    record Approved(String loanAppId, String reviewerId, Instant timestamp) implements LoanProcDomainEvent {}
    record Declined(String loanAppId, String reviewerId, String reason, Instant timestamp) implements LoanProcDomainEvent {}
}
