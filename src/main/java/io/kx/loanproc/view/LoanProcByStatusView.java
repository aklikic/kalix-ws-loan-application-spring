package io.kx.loanproc.view;

import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import io.kx.loanproc.domain.LoanProcDomainStatus;
import kalix.javasdk.view.View;
import kalix.springsdk.annotations.Query;
import kalix.springsdk.annotations.Subscribe;
import kalix.springsdk.annotations.Table;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Table("loanproc_by_status")
public class LoanProcByStatusView extends View<LoanProcViewModel.ViewRecord>
{


    @Query("SELECT * FROM loanproc_by_status WHERE statusId = :statusId")
    @GetMapping("/loanproc/views/by-status/{statusId}")
    public LoanProcViewModel.ViewRecord getLoanProcByStatus(@PathVariable String statusId){
        return null;
    }

    @Subscribe.EventSourcedEntity(LoanProcService.class)
    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomainEvent.ReadyForReview event){
        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name(),LoanProcDomainStatus.STATUS_READY_FOR_REVIEW, event.loanAppId(), event.timestamp()));
    }
    @Subscribe.EventSourcedEntity(LoanProcService.class)
    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomainEvent.Approved event){
        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomainStatus.STATUS_APPROVED.name(),LoanProcDomainStatus.STATUS_APPROVED, event.loanAppId(), event.timestamp()));
    }
    @Subscribe.EventSourcedEntity(LoanProcService.class)
    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomainEvent.Declined event){
        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomainStatus.STATUS_DECLINED.name(),LoanProcDomainStatus.STATUS_DECLINED, event.loanAppId(), event.timestamp()));
    }

}
