package com.getufit.controller;

import com.getufit.model.GymClass;
import com.getufit.model.Trainer;
import com.getufit.repository.ClassRegistrationRepository;
import com.getufit.repository.GymClassRepository;
import com.getufit.repository.MemberRepository;
import com.getufit.repository.TrainerRepository;
import com.getufit.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final RegistrationService service;
    private final MemberRepository members;
    private final GymClassRepository classes;
    private final ClassRegistrationRepository registrations;
    private final TrainerRepository trainers;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/classes")
    public String classes(@RequestParam(required = false) Long memberId, Model model) {
        List<GymClass> list = classes.findByClassDateGreaterThanEqualOrderByClassDateAscStartTimeAsc(LocalDate.now());
        Map<Long, Long> seats = new HashMap<>();
        for (GymClass c : list) {
            seats.put(c.getClassID(), c.getCapacity() - registrations.countByGymClass_ClassIDAndStatus(c.getClassID(), "Registered"));
        }
        Map<Long, String> trainerNames = new HashMap<>();
        for (Trainer t : trainers.findAll()) {
            trainerNames.put(t.getTrainerID(), t.getTrainerName());
        }
        model.addAttribute("classes", list);
        model.addAttribute("seats", seats);
        model.addAttribute("trainerNames", trainerNames);
        model.addAttribute("members", members.findAll());
        model.addAttribute("memberId", memberId);
        return "classes";
    }

    @PostMapping("/classes/register")
    public String register(@RequestParam Long memberId, @RequestParam Long classId, RedirectAttributes ra) {
        try {
            service.register(memberId, classId);
            ra.addFlashAttribute("message", "Registration successful");
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/classes?memberId=" + memberId;
    }

    @GetMapping("/registrations")
    public String mine(@RequestParam Long memberId, Model model) {
        model.addAttribute("registrations", service.forMember(memberId));
        model.addAttribute("memberId", memberId);
        return "registrations";
    }


    @PostMapping("/registrations/cancel")
    public String cancel(@RequestParam Long registrationId, @RequestParam Long memberId, RedirectAttributes ra) {
        try {
            service.cancel(registrationId);
            ra.addFlashAttribute("message", "Registration cancelled");
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/registrations?memberId=" + memberId;
    }
}