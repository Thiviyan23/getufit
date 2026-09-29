package com.getufit.controller;

import com.getufit.model.ClassRegistration;
import com.getufit.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService service;

    public record RegisterRequest(Long memberId, Long classId) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassRegistration register(@RequestBody RegisterRequest request) {
        return service.register(request.memberId(), request.classId());
    }

    @PutMapping("/{id}/cancel")
    public ClassRegistration cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/member/{memberId}")
    public List<ClassRegistration> forMember(@PathVariable Long memberId) {
        return service.forMember(memberId);
    }
}