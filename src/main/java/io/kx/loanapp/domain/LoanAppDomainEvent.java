package io.kx.loanapp.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = LoanAppDomainEvent.Submitted.class, name = "submitted"),
                @JsonSubTypes.Type(value = LoanAppDomainEvent.Approved.class, name = "approved"),
                @JsonSubTypes.Type(value = LoanAppDomainEvent.Declined.class, name = "declined")
        })
public sealed interface LoanAppDomainEvent {

    @TypeName("loan-submitted")
    record Submitted(String loanAppId,
                     String clientId,
                     Integer clientMonthlyIncomeCents,
                     Integer loanAmountCents,
                     Integer loanDurationMonths,
                     Instant timestamp) implements LoanAppDomainEvent{}


    @TypeName("loan-approved")
    record Approved(String loanAppId, Instant timestamp) implements LoanAppDomainEvent{}

    @TypeName("loan-declined")
    record Declined(String loanAppId, String reason, Instant timestamp) implements LoanAppDomainEvent{}
}
