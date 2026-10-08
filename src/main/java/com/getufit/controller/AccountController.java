package com.getufit.controller;

import com.getufit.model.GymClass;
import com.getufit.model.Member;
import com.getufit.model.Trainer;
import com.getufit.repository.ClassRegistrationRepository;
import com.getufit.repository.GymClassRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final RegistrationService service;
    private final GymClassRepository classes;
    private final ClassRegistrationRepository registrations;
    private final TrainerRepository trainers;

    @GetMapping
    public String dashboard(@RequestAttribute("currentMember") Member member, Model model) {
        model.addAttribute("member", member);
        model.addAttribute("registrations", service.forMember(member.getMemberID()));
        return "account";
    }

    @GetMapping("/classes")
    public String schedule(@RequestAttribute("currentMember") Member member, Model model) {
        List<GymClass> list = classes.findByClassDateGreaterThanEqualOrderByClassDateAscStartTimeAsc(LocalDate.now());
        Map<Long, Long> seats = new HashMap<>();
        for (GymClass c : list) {
            seats.put(c.getClassID(),
                    c.getCapacity() - registrations.countByGymClass_ClassIDAndStatus(c.getClassID(), "Registered"));
        }
        Map<Long, String> trainerNames = new HashMap<>();
        for (Trainer t : trainers.findAll()) {
            trainerNames.put(t.getTrainerID(), t.getTrainerName());
        }
        Set<Long> booked = service.forMember(member.getMemberID()).stream()
                .filter(r -> "Registered".equals(r.getStatus()))
                .map(r -> r.getGymClass().getClassID())
                .collect(Collectors.toSet());
        model.addAttribute("member", member);
        model.addAttribute("classes", list);
        model.addAttribute("seats", seats);
        model.addAttribute("trainerNames", trainerNames);
        model.addAttribute("booked", booked);
        return "account-classes";
    }

    @PostMapping("/classes/register")
    public String register(@RequestAttribute("currentMember") Member member,
                           @RequestParam Long classId, RedirectAttributes ra) {
        try {
            service.register(member.getMemberID(), classId);
            ra.addFlashAttribute("message", "Registration successful");
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/account/classes";
    }

    @PostMapping("/registrations/cancel")
    public String cancel(@RequestAttribute("currentMember") Member member,
                         @RequestParam Long registrationId, RedirectAttributes ra) {
        boolean owned = service.forMember(member.getMemberID()).stream()
                .anyMatch(r -> r.getRegistrationID().equals(registrationId));
        if (!owned) {
            ra.addFlashAttribute("error", "Registration not found");
            return "redirect:/account";
        }
        service.cancel(registrationId);
        ra.addFlashAttribute("message", "Registration cancelled");
        return "redirect:/account";
    }
}