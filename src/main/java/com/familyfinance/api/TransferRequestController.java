package com.familyfinance.api;

import com.familyfinance.api.dto.CreateTransferRequestDto;
import com.familyfinance.api.dto.TransferRequestResponse;
import com.familyfinance.api.dto.TransferResponse;
import com.familyfinance.security.CurrentUser;
import com.familyfinance.service.TransferRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-requests")
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    public TransferRequestController(TransferRequestService transferRequestService) {
        this.transferRequestService = transferRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferRequestResponse create(@Valid @RequestBody CreateTransferRequestDto request) {
        return transferRequestService.create(
                CurrentUser.require(),
                request.payerMemberId(),
                request.amount(),
                request.message()
        );
    }

    @GetMapping("/incoming")
    public List<TransferRequestResponse> incoming() {
        return transferRequestService.incomingPending(CurrentUser.require());
    }

    @GetMapping("/outgoing")
    public List<TransferRequestResponse> outgoing() {
        return transferRequestService.outgoing(CurrentUser.require());
    }

    @PostMapping("/{id}/approve")
    public TransferResponse approve(@PathVariable Long id) {
        return transferRequestService.approve(CurrentUser.require(), id);
    }

    @PostMapping("/{id}/reject")
    public TransferRequestResponse reject(@PathVariable Long id) {
        return transferRequestService.reject(CurrentUser.require(), id);
    }

    @PostMapping("/{id}/cancel")
    public TransferRequestResponse cancel(@PathVariable Long id) {
        return transferRequestService.cancel(CurrentUser.require(), id);
    }
}
