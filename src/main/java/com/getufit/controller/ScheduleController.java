package com.getufit.controller;

import com.getufit.model.GymClass;
import com.getufit.model.Trainer;
import com.getufit.repository.ClassRegistrationRepository;
import com.getufit.repository.GymClassRepository;
import com.getufit.repository.TrainerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private static final List<String> CLASS_TYPES = List.of(
            "Strength", "Cardio", "HIIT", "Yoga", "Pilates",
            "Zumba", "CrossFit", "Women's Special", "Personal Training"
    );

    private final GymClassRepository classes;
    private final TrainerRepository trainers;
    private final ClassRegistrationRepository registrations;

    @ModelAttribute("classTypes")
    public List<String> classTypes() {
        return CLASS_TYPES;
    }

    @ModelAttribute("trainerList")
    public List<Trainer> trainerList() {
        return trainers.findAll();
    }

    @GetMapping
    public String list(Model model) {
        List<GymClass> list = classes.findAllByOrderByClassDateAscStartTimeAsc();
        Map<Long, String> trainerNames = new HashMap<>();
        for (Trainer t : trainers.findAll()) {
            trainerNames.put(t.getTrainerID(), t.getTrainerName());
        }
        Map<Long, Long> booked = new HashMap<>();
        for (GymClass c : list) {
            booked.put(c.getClassID(), registrations.countByGymClass_ClassIDAndStatus(c.getClassID(), "Registered"));
        }
        model.addAttribute("classes", list);
        model.addAttribute("trainerNames", trainerNames);
        model.addAttribute("booked", booked);
        model.addAttribute("today", LocalDate.now());
        return "schedule";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        GymClass gymClass = new GymClass();
        gymClass.setCapacity(20);
        model.addAttribute("gymClass", gymClass);
        model.addAttribute("isNew", true);
        return "schedule-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("gymClass") GymClass form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        validate(form, result, null, null, true);
        if (result.hasErrors()) {
            model.addAttribute("isNew", true);
            return "schedule-form";
        }
        classes.save(form);
        ra.addFlashAttribute("message", "Class scheduled");
        return "redirect:/schedule";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        GymClass gymClass = classes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("gymClass", gymClass);
        model.addAttribute("classId", id);
        model.addAttribute("isNew", false);
        return "schedule-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("gymClass") GymClass form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        GymClass existing = classes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validate(form, result, id, existing, false);
        if (result.hasErrors()) {
            model.addAttribute("classId", id);
            model.addAttribute("isNew", false);
            return "schedule-form";
        }
        existing.setClassName(form.getClassName());
        existing.setTrainerID(form.getTrainerID());
        existing.setClassDate(form.getClassDate());
        existing.setStartTime(form.getStartTime());
        existing.setEndTime(form.getEndTime());
        existing.setClassType(form.getClassType());
        existing.setCapacity(form.getCapacity());
        classes.save(existing);
        ra.addFlashAttribute("message", "Class updated");
        return "redirect:/schedule";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (!classes.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (registrations.countByGymClass_ClassID(id) > 0) {
            ra.addFlashAttribute("error", "Cannot delete: this class has registrations");
            return "redirect:/schedule";
        }
        classes.deleteById(id);
        ra.addFlashAttribute("message", "Class deleted");
        return "redirect:/schedule";
    }

    private void validate(GymClass form, BindingResult result, Long excludeId, GymClass existing, boolean isNew) {
        if (form.getStartTime() != null && form.getEndTime() != null
                && !form.getEndTime().isAfter(form.getStartTime())) {
            result.rejectValue("endTime", "order", "End time must be after start time");
        }
        if (form.getClassDate() != null && form.getClassDate().isBefore(LocalDate.now())
                && (isNew || !form.getClassDate().equals(existing.getClassDate()))) {
            result.rejectValue("classDate", "past", "Class date cannot be in the past");
        }
        if (form.getClassType() != null && !form.getClassType().isBlank()
                && !CLASS_TYPES.contains(form.getClassType())
                && (isNew || !form.getClassType().equals(existing.getClassType()))) {
            result.rejectValue("classType", "invalid", "Choose a class type from the list");
        }
        if (form.getTrainerID() != null) {
            Trainer trainer = trainers.findById(form.getTrainerID()).orElse(null);
            boolean unchanged = existing != null && form.getTrainerID().equals(existing.getTrainerID());
            if (trainer == null) {
                result.rejectValue("trainerID", "missing", "Trainer not found");
            } else if (!"Active".equals(trainer.getStatus()) && !unchanged) {
                result.rejectValue("trainerID", "inactive", "Trainer is not active");
            }
        }
        if (existing != null && form.getCapacity() != null) {
            long booked = registrations.countByGymClass_ClassIDAndStatus(excludeId, "Registered");
            if (form.getCapacity() < booked) {
                result.rejectValue("capacity", "low", "Capacity is below the " + booked + " members already registered");
            }
        }
        if (!result.hasErrors()) {
            for (GymClass other : classes.findByTrainerIDAndClassDate(form.getTrainerID(), form.getClassDate())) {
                if (excludeId != null && excludeId.equals(other.getClassID())) {
                    continue;
                }
                if (form.getStartTime().isBefore(other.getEndTime()) && form.getEndTime().isAfter(other.getStartTime())) {
                    result.rejectValue("trainerID", "clash",
                            "Trainer already has " + other.getClassName() + " at " + other.getStartTime() + " - " + other.getEndTime());
                    break;
                }
            }
        }
    }
}
