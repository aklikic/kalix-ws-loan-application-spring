package io.kx.loanapp.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = LoanAppDomainEvent.Submitted.class, name = "submitted"),
                @JsonSubTypes.Type(value = LoanAppDomainEvent.Approved.class, name = "approved"),
                @JsonSubTypes.Type(value = LoanAppDomainEvent.Declined.class, name = "declined")
        })
public sealed interface LoanAppDomainEvent {
    record Submitted(String loanAppId,
                     String clientId,
                     Integer clientMonthlyIncomeCents,
                     Integer loanAmountCents,
                     Integer loanDurationMonths,
                     Instant timestamp) implements LoanAppDomainEvent{}

    record Approved(String loanAppId, Instant timestamp) implements LoanAppDomainEvent{}
    record Declined(String loanAppId, String reason, Instant timestamp) implements LoanAppDomainEvent{}
}
