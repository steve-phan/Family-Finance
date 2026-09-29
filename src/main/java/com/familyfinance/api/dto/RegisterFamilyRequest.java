package com.familyfinance.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterFamilyRequest(
        @NotBlank @Size(max = 120) String familyName,
        @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName
) {
    public String currencyOrDefault() {
        return currency == null || currency.isBlank() ? "EUR" : currency;
    }
}
