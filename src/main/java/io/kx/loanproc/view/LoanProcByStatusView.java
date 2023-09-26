//package io.kx.loanproc.view;
//
//import io.kx.loanproc.api.LoanProcService;
//import io.kx.loanproc.domain.LoanProcDomainEvent;
//import io.kx.loanproc.domain.LoanProcDomainStatus;
//import kalix.javasdk.view.View;
//import kalix.springsdk.annotations.Query;
//import kalix.springsdk.annotations.Subscribe;
//import kalix.springsdk.annotations.Table;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import reactor.core.publisher.Flux;
//
//@Table("loanproc_by_status")
//public class LoanProcByStatusView extends View<LoanProcViewModel.ViewRecord>
//{
//
//
//    @PostMapping("/loanproc/views/by-status")
//    @Query("SELECT * FROM loanproc_by_status WHERE statusId = :statusId")
//    public Flux<LoanProcViewModel.ViewRecord> getLoanProcByStatus(@RequestBody LoanProcViewModel.ViewRequest request){
//        return null;
//    }
//
//    @Subscribe.EventSourcedEntity(LoanProcService.class)
//    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomainEvent.ReadyForReview event){
//        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name(),event.loanAppId(), event.timestamp().toEpochMilli()));
//    }
//    @Subscribe.EventSourcedEntity(LoanProcService.class)
//    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomainEvent.Approved event){
//        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomainStatus.STATUS_APPROVED.name(),event.loanAppId(), event.timestamp().toEpochMilli()));
//    }
//    @Subscribe.EventSourcedEntity(LoanProcService.class)
//    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomainEvent.Declined event){
//        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomainStatus.STATUS_DECLINED.name(), event.loanAppId(), event.timestamp().toEpochMilli()));
//    }
//
//}
