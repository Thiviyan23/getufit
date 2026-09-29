package com.getufit.controller;

import com.getufit.model.Member;
import com.getufit.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberPageController {
    private final MemberRepository members;
    private final PasswordEncoder encoder;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", members.findAll());
        return "members";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("isNew", true);
        return "member-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("member") Member member, BindingResult result,
                         @RequestParam String password, Model model, RedirectAttributes ra) {
        if (member.getEmail() != null && members.existsByEmail(member.getEmail())) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }
        boolean weak = password.length() < 6;
        if (weak) {
            model.addAttribute("passwordError", "Password must be at least 6 characters");
        }
        if (result.hasErrors() || weak) {
            model.addAttribute("isNew", true);
            return "member-form";
        }
        member.setPasswordHash(encoder.encode(password));
        member.setJoinDate(LocalDate.now());
        member.setStatus("Active");
        members.save(member);
        ra.addFlashAttribute("message", "Member added");
        return "redirect:/members";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Member member = members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("member", member);
        model.addAttribute("memberId", id);
        model.addAttribute("isNew", false);
        return "member-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("member") Member form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (form.getEmail() != null && members.existsByEmailAndMemberIDNot(form.getEmail(), id)) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }
        if (result.hasErrors()) {
            model.addAttribute("memberId", id);
            model.addAttribute("isNew", false);
            return "member-form";
        }
        Member member = members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        member.setFullName(form.getFullName());
        member.setDob(form.getDob());
        member.setGender(form.getGender());
        member.setAddress(form.getAddress());
        member.setPhone(form.getPhone());
        member.setEmail(form.getEmail());
        members.save(member);
        ra.addFlashAttribute("message", "Member updated");
        return "redirect:/members";
    }

    @PostMapping("/{id}/status")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Member member = members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        member.setStatus("Active".equals(member.getStatus()) ? "Inactive" : "Active");
        members.save(member);
        ra.addFlashAttribute("message", "Status changed to " + member.getStatus());
        return "redirect:/members";
    }
}