package com.getufit.repository;

import com.getufit.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndMemberIDNot(String email, Long memberID);
}