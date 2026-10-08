package com.getufit.controller;

import com.getufit.dto.JoinForm;
import com.getufit.model.Member;
import com.getufit.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/join")
@RequiredArgsConstructor
public class JoinController {
    private final MemberRepository members;
    private final PasswordEncoder encoder;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("joinForm", new JoinForm());
        return "join";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("joinForm") JoinForm form, BindingResult result) {
        if (form.getEmail() != null && members.existsByEmail(form.getEmail())) {
            result.rejectValue("email", "duplicate", "This email is already registered");
        }
        if (form.getPassword() != null && form.getConfirmPassword() != null
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (result.hasErrors()) {
            return "join";
        }

        Member member = new Member();
        member.setFullName(form.getFullName());
        member.setDob(form.getDob());
        member.setGender(form.getGender());
        member.setAddress(form.getAddress());
        member.setPhone(form.getPhone());
        member.setEmail(form.getEmail());
        member.setPasswordHash(encoder.encode(form.getPassword()));
        member.setJoinDate(LocalDate.now());
        member.setStatus("Pending");

        try {
            members.save(member);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "duplicate", "This email is already registered");
            return "join";
        }
        return "redirect:/join/success";
    }

    @GetMapping("/success")
    public String success() {
        return "join-success";
    }
}