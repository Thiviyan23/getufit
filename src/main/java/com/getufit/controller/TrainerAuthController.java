package com.getufit.controller;

import com.getufit.model.TrainerAccount;
import com.getufit.repository.TrainerAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/trainer")
@RequiredArgsConstructor
public class TrainerAuthController {
    private final TrainerAccountRepository trainers;
    private final PasswordEncoder encoder;

    @GetMapping("/login")
    public String form(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("trainerId") != null) {
            return "redirect:/trainer";
        }
        return "trainer-login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletRequest request, Model model) {
        TrainerAccount trainer = trainers.findByEmail(email.trim()).orElse(null);
        boolean valid = trainer != null && trainer.getPasswordHash() != null
                && encoder.matches(password, trainer.getPasswordHash());
        if (!valid) {
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("email", email);
            return "trainer-login";
        }
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute("trainerId", trainer.getTrainerID());
        return "redirect:/trainer";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/trainer/login?logout";
    }
}