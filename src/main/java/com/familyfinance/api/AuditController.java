package com.familyfinance.api;

import com.familyfinance.api.dto.AuditEventResponse;
import com.familyfinance.security.CurrentUser;
import com.familyfinance.security.MemberPrincipal;
import com.familyfinance.service.AuditService;
import com.familyfinance.service.FamilyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-events")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public Page<AuditEventResponse> list(@PageableDefault(size = 50) Pageable pageable) {
        MemberPrincipal principal = CurrentUser.require();
        FamilyService.requireParent(principal);
        return auditService.list(principal.getFamilyId(), pageable);
    }
}
