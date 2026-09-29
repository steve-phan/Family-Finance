package com.familyfinance.api;

import com.familyfinance.api.dto.AllowanceResponse;
import com.familyfinance.api.dto.CreateAllowanceRequest;
import com.familyfinance.security.CurrentUser;
import com.familyfinance.service.AllowanceService;
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
@RequestMapping("/api/allowances")
public class AllowanceController {

    private final AllowanceService allowanceService;

    public AllowanceController(AllowanceService allowanceService) {
        this.allowanceService = allowanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AllowanceResponse create(@Valid @RequestBody CreateAllowanceRequest request) {
        return allowanceService.create(
                CurrentUser.require(),
                request.childMemberId(),
                request.amount(),
                request.period()
        );
    }

    @GetMapping
    public List<AllowanceResponse> list() {
        return allowanceService.listActive(CurrentUser.require());
    }

    @PostMapping("/{id}/disable")
    public AllowanceResponse disable(@PathVariable Long id) {
        return allowanceService.disable(CurrentUser.require(), id);
    }
}
