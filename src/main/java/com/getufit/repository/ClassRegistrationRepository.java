package com.getufit.repository;

import com.getufit.model.ClassRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRegistrationRepository extends JpaRepository<ClassRegistration, Long> {
    boolean existsByMember_MemberIDAndGymClass_ClassIDAndStatus(Long memberId, Long classId, String status);
    long countByGymClass_ClassIDAndStatus(Long classId, String status);
    List<ClassRegistration> findByMember_MemberID(Long memberId);
    List<ClassRegistration> findByGymClass_ClassIDAndStatus(Long classId, String status);
}