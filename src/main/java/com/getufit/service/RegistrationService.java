package com.getufit.service;

import com.getufit.model.ClassRegistration;
import com.getufit.model.GymClass;
import com.getufit.model.Member;
import com.getufit.repository.ClassRegistrationRepository;
import com.getufit.repository.GymClassRepository;
import com.getufit.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final ClassRegistrationRepository registrations;
    private final MemberRepository members;
    private final GymClassRepository classes;

    public ClassRegistration register(Long memberId, Long classId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        GymClass gymClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        if (!"Active".equals(member.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Member is not active");
        }
        if (registrations.existsByMember_MemberIDAndGymClass_ClassIDAndStatus(memberId, classId, "Registered")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered");
        }
        if (registrations.countByGymClass_ClassIDAndStatus(classId, "Registered") >= gymClass.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Class is full");
        }
        ClassRegistration registration = new ClassRegistration();
        registration.setMember(member);
        registration.setGymClass(gymClass);
        registration.setRegistrationDate(LocalDate.now());
        registration.setStatus("Registered");
        return registrations.save(registration);
    }

    public ClassRegistration cancel(Long registrationId) {
        ClassRegistration registration = registrations.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        registration.setStatus("Cancelled");
        return registrations.save(registration);
    }

    public List<ClassRegistration> forMember(Long memberId) {
        return registrations.findByMember_MemberID(memberId);
    }
}