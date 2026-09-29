package com.getufit.controller;

import com.getufit.model.ClassRegistration;
import com.getufit.model.GymClass;
import com.getufit.model.Trainer;
import com.getufit.repository.ClassRegistrationRepository;
import com.getufit.repository.GymClassRepository;
import com.getufit.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trainers/{trainerId}/classes")
@RequiredArgsConstructor
public class TrainerClassController {
    private final TrainerRepository trainers;
    private final GymClassRepository classes;
    private final ClassRegistrationRepository registrations;

    @GetMapping
    public String myClasses(@PathVariable Long trainerId, Model model) {
        Trainer trainer = trainers.findById(trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<GymClass> list = classes.findByTrainerIDOrderByClassDateAscStartTimeAsc(trainerId);
        Map<Long, Long> booked = new HashMap<>();
        for (GymClass c : list) {
            booked.put(c.getClassID(), registrations.countByGymClass_ClassIDAndStatus(c.getClassID(), "Registered"));
        }
        model.addAttribute("trainer", trainer);
        model.addAttribute("classes", list);
        model.addAttribute("booked", booked);
        return "trainer-classes";
    }

    @GetMapping("/{classId}")
    public String classMembers(@PathVariable Long trainerId, @PathVariable Long classId, Model model) {
        Trainer trainer = trainers.findById(trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        GymClass gymClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!trainerId.equals(gymClass.getTrainerID())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<ClassRegistration> list = registrations.findByGymClass_ClassIDAndStatus(classId, "Registered");
        model.addAttribute("trainer", trainer);
        model.addAttribute("gymClass", gymClass);
        model.addAttribute("registrations", list);
        return "class-members";
    }
}