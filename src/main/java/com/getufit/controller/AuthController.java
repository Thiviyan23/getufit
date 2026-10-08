package com.getufit.controller;

import com.getufit.model.Member;
import com.getufit.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final MemberRepository members;
    private final PasswordEncoder encoder;

    @GetMapping("/login")
    public String form(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("memberId") != null) {
            return "redirect:/account";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletRequest request, Model model) {
        Member member = members.findByEmail(email.trim()).orElse(null);
        boolean valid = member != null && member.getPasswordHash() != null
                && encoder.matches(password, member.getPasswordHash());
        if (!valid) {
            return fail(model, email, "Invalid email or password");
        }
        if ("Pending".equals(member.getStatus())) {
            return fail(model, email, "Your application is still pending approval. Staff will activate it soon.");
        }
        if (!"Active".equals(member.getStatus())) {
            return fail(model, email, "Your membership is not active. Please contact the front desk.");
        }

        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute("memberId", member.getMemberID());
        return "redirect:/account";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout";
    }

    private String fail(Model model, String email, String message) {
        model.addAttribute("error", message);
        model.addAttribute("email", email);
        return "login";
    }
}