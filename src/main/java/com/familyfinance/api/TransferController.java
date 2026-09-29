package com.familyfinance.api;

import com.familyfinance.api.dto.SendMoneyRequest;
import com.familyfinance.api.dto.TransferResponse;
import com.familyfinance.security.CurrentUser;
import com.familyfinance.security.MemberPrincipal;
import com.familyfinance.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse send(@Valid @RequestBody SendMoneyRequest request) {
        MemberPrincipal principal = CurrentUser.require();
        return transferService.send(
                principal.getMemberId(),
                request.toMemberId(),
                request.amount(),
                request.description(),
                request.idempotencyKey()
        );
    }

    @GetMapping
    public Page<TransferResponse> history(@PageableDefault(size = 20) Pageable pageable) {
        MemberPrincipal principal = CurrentUser.require();
        if (principal.isParent()) {
            return transferService.historyForFamily(principal.getFamilyId(), pageable);
        }
        return transferService.historyForMember(principal.getMemberId(), pageable);
    }
}
