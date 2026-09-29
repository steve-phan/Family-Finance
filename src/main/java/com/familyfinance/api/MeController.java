package com.familyfinance.api;

import com.familyfinance.api.dto.MeResponse;
import com.familyfinance.security.CurrentUser;
import com.familyfinance.service.MeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping
    public MeResponse me() {
        return meService.me(CurrentUser.require());
    }
}
