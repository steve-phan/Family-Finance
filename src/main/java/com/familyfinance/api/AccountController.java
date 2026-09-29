package com.familyfinance.api;

import com.familyfinance.api.dto.AccountResponse;
import com.familyfinance.api.dto.DepositRequest;
import com.familyfinance.security.CurrentUser;
import com.familyfinance.service.AccountService;
import com.familyfinance.service.DepositService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final DepositService depositService;

    public AccountController(AccountService accountService, DepositService depositService) {
        this.accountService = accountService;
        this.depositService = depositService;
    }

    @GetMapping("/me")
    public AccountResponse myAccount() {
        return accountService.myAccount(CurrentUser.require());
    }

    @GetMapping("/members/{memberId}")
    public AccountResponse memberAccount(@PathVariable Long memberId) {
        return accountService.accountOf(CurrentUser.require(), memberId);
    }

    @PostMapping("/me/deposits")
    public AccountResponse deposit(@Valid @RequestBody DepositRequest request) {
        return depositService.deposit(CurrentUser.require(), request.amount());
    }
}
