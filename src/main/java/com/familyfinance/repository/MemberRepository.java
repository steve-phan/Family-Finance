package com.familyfinance.repository;

import com.familyfinance.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Member> findByFamily_IdOrderByLastNameAscFirstNameAsc(Long familyId);
}
