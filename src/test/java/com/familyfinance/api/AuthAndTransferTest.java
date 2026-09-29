package com.familyfinance.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthAndTransferTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("family_finance")
            .withUsername("family")
            .withPassword("family");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void parentDepositsAndSendsToChildWhoPaysSibling() throws Exception {
        String parentToken = registerFamily();
        long childA = addMember(parentToken, "leo@example.com", "Leo", "CHILD");
        long childB = addMember(parentToken, "mia@example.com", "Mia", "CHILD");

        mockMvc.perform(post("/api/accounts/me/deposits")
                        .header("Authorization", bearer(parentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.00}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", bearer(parentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toMemberId\": %d, \"amount\": 30.00, \"description\": \"pocket money\"}".formatted(childA)))
                .andExpect(status().isCreated());

        String childToken = login("leo@example.com");
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", bearer(childToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toMemberId\": %d, \"amount\": 5.00, \"description\": \"snack\"}".formatted(childB)))
                .andExpect(status().isCreated());

        MvcResult childBalance = mockMvc.perform(get("/api/accounts/me")
                        .header("Authorization", bearer(childToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(childBalance.getResponse().getContentAsString());
        assertThat(body.get("balance").decimalValue()).isEqualByComparingTo("25.00");
    }

    @Test
    void familiesCannotSeeOrPayEachOther() throws Exception {
        String familyA = registerFamily("anna-a@example.com", "Family A");
        long childA = addMember(familyA, "child-a@example.com", "ChildA", "CHILD");

        mockMvc.perform(post("/api/accounts/me/deposits")
                        .header("Authorization", bearer(familyA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50.00}"))
                .andExpect(status().isOk());

        String familyB = registerFamily("anna-b@example.com", "Family B");
        addMember(familyB, "child-b@example.com", "ChildB", "CHILD");

        mockMvc.perform(get("/api/family/members")
                        .header("Authorization", bearer(familyB)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode members = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(members).hasSize(2);
                    members.forEach(member -> assertThat(member.get("email").asText()).doesNotContain("child-a"));
                });

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", bearer(familyB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toMemberId\": %d, \"amount\": 1.00}".formatted(childA)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accounts/members/%d".formatted(childA))
                        .header("Authorization", bearer(familyB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void childCannotDeposit() throws Exception {
        String parentToken = registerFamily("second@example.com", "Second Family");
        addMember(parentToken, "kid@example.com", "Kid", "CHILD");
        String childToken = login("kid@example.com");
        mockMvc.perform(post("/api/accounts/me/deposits")
                        .header("Authorization", bearer(childToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 10.00}"))
                .andExpect(status().isForbidden());
    }

    private String registerFamily() throws Exception {
        return registerFamily("anna@example.com", "Muller");
    }

    private String registerFamily(String email, String familyName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "familyName": "%s",
                                  "currency": "EUR",
                                  "email": "%s",
                                  "password": "secret123",
                                  "firstName": "Anna",
                                  "lastName": "Muller"
                                }
                                """.formatted(familyName, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long addMember(String parentToken, String email, String firstName, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/family/members")
                        .header("Authorization", bearer(parentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123",
                                  "firstName": "%s",
                                  "lastName": "Muller",
                                  "role": "%s"
                                }
                                """.formatted(email, firstName, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "secret123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
