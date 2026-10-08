package com.getufit.controller;

import com.getufit.model.ClassRegistration;
import com.getufit.model.GymClass;
import com.getufit.model.TrainerAccount;
import com.getufit.repository.ClassRegistrationRepository;
import com.getufit.repository.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trainer")
@RequiredArgsConstructor
public class TrainerPortalController {
    private final GymClassRepository classes;
    private final ClassRegistrationRepository registrations;

    @GetMapping
    public String dashboard(@RequestAttribute("currentTrainer") TrainerAccount trainer, Model model) {
        List<GymClass> list = classes.findByTrainerIDOrderByClassDateAscStartTimeAsc(trainer.getTrainerID());
        Map<Long, Long> booked = new HashMap<>();
        for (GymClass c : list) {
            booked.put(c.getClassID(), registrations.countByGymClass_ClassIDAndStatus(c.getClassID(), "Registered"));
        }
        model.addAttribute("trainer", trainer);
        model.addAttribute("classes", list);
        model.addAttribute("booked", booked);
        model.addAttribute("today", LocalDate.now());
        return "trainer-dashboard";
    }

    @GetMapping("/classes/{classId}")
    public String classMembers(@RequestAttribute("currentTrainer") TrainerAccount trainer,
                               @PathVariable Long classId, Model model) {
        GymClass gymClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!trainer.getTrainerID().equals(gymClass.getTrainerID())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<ClassRegistration> list = registrations.findByGymClass_ClassIDAndStatus(classId, "Registered");
        model.addAttribute("gymClass", gymClass);
        model.addAttribute("registrations", list);
        return "trainer-class-members";
    }
}