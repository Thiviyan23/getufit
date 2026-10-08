package com.getufit.controller;

import com.getufit.model.Trainer;
import com.getufit.repository.GymClassRepository;
import com.getufit.repository.TrainerRepository;
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
import java.util.List;

@Controller
@RequestMapping("/trainers")
@RequiredArgsConstructor
public class TrainerPageController {
    private static final List<String> SPECIALIZATIONS = List.of(
            "Strength & Conditioning",
            "Weight Loss",
            "Cardio & HIIT",
            "Yoga",
            "Pilates",
            "Zumba & Dance Fitness",
            "Women's Fitness",
            "Bodybuilding",
            "CrossFit",
            "Personal Training",
            "Nutrition & Diet"
    );

    private final TrainerRepository trainers;
    private final GymClassRepository classes;
    private final PasswordEncoder encoder;

    @ModelAttribute("specializations")
    public List<String> specializations() {
        return SPECIALIZATIONS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("trainers", trainers.findAll());
        return "trainers";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("trainer", new Trainer());
        model.addAttribute("isNew", true);
        return "trainer-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("trainer") Trainer trainer, BindingResult result,
                         @RequestParam(required = false) String password,
                         Model model, RedirectAttributes ra) {
        trainer.setTrainerID(null);
        if (trainer.getEmail() != null && trainers.existsByEmail(trainer.getEmail())) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }
        if (trainer.getSpecialization() != null && !trainer.getSpecialization().isBlank()
                && !SPECIALIZATIONS.contains(trainer.getSpecialization())) {
            result.rejectValue("specialization", "invalid", "Choose a specialization from the list");
        }
        boolean weak = password == null || password.length() < 6 || password.length() > 72;
        if (weak) {
            model.addAttribute("passwordError", "Password must be 6 to 72 characters");
        }
        if (result.hasErrors() || weak) {
            model.addAttribute("isNew", true);
            return "trainer-form";
        }
        trainer.setPasswordHash(encoder.encode(password));
        trainer.setStatus("Active");
        trainers.save(trainer);
        ra.addFlashAttribute("message", "Trainer added");
        return "redirect:/trainers";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Trainer trainer = trainers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("trainer", trainer);
        model.addAttribute("trainerId", id);
        model.addAttribute("isNew", false);
        return "trainer-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("trainer") Trainer form,
                         BindingResult result, @RequestParam(required = false) String password,
                         Model model, RedirectAttributes ra) {
        Trainer trainer = trainers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (form.getEmail() != null && trainers.existsByEmailAndTrainerIDNot(form.getEmail(), id)) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }
        String spec = form.getSpecialization();
        boolean unchanged = spec != null && spec.equals(trainer.getSpecialization());
        if (spec != null && !spec.isBlank() && !unchanged && !SPECIALIZATIONS.contains(spec)) {
            result.rejectValue("specialization", "invalid", "Choose a specialization from the list");
        }
        boolean newPassword = password != null && !password.isBlank();
        boolean weak = newPassword && (password.length() < 6 || password.length() > 72);
        if (weak) {
            model.addAttribute("passwordError", "Password must be 6 to 72 characters");
        }
        if (result.hasErrors() || weak) {
            model.addAttribute("trainerId", id);
            model.addAttribute("isNew", false);
            return "trainer-form";
        }
        trainer.setTrainerName(form.getTrainerName());
        trainer.setPhone(form.getPhone());
        trainer.setEmail(form.getEmail());
        trainer.setSpecialization(form.getSpecialization());
        if (newPassword) {
            trainer.setPasswordHash(encoder.encode(password));
        }
        trainers.save(trainer);
        ra.addFlashAttribute("message", newPassword ? "Trainer updated and password reset" : "Trainer updated");
        return "redirect:/trainers";
    }

    @PostMapping("/{id}/status")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Trainer trainer = trainers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if ("Active".equals(trainer.getStatus())) {
            if (classes.existsByTrainerIDAndClassDateGreaterThanEqual(id, LocalDate.now())) {
                ra.addFlashAttribute("error", "Cannot deactivate: trainer has upcoming classes");
                return "redirect:/trainers";
            }
            trainer.setStatus("Inactive");
        } else {
            trainer.setStatus("Active");
        }
        trainers.save(trainer);
        ra.addFlashAttribute("message", "Status changed to " + trainer.getStatus());
        return "redirect:/trainers";
    }
}