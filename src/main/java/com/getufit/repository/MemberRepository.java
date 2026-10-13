package com.getufit.repository;

import com.getufit.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndMemberIDNot(String email, Long memberID);
    Optional<Member> findByEmail(String email);
}