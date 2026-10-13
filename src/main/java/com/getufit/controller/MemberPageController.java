package com.getufit.controller;

import com.getufit.model.Membership;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.getufit.model.Member;
import com.getufit.model.Membership;
import com.getufit.model.MembershipPackage;
import com.getufit.model.Payment;
import com.getufit.repository.MemberRepository;
import com.getufit.repository.MembershipPackageRepository;
import com.getufit.repository.MembershipRepository;
import com.getufit.repository.PaymentRepository;
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
    private final MembershipPackageRepository packages;
    private final MembershipRepository memberships;
    private final PaymentRepository payments;
    private final PasswordEncoder encoder;

    @GetMapping
    public String list(Model model) {
        List<Member> memberList = members.findAll();
        Map<Long, Membership> latestMembership = new HashMap<>();
        for (Member m : memberList) {
            List<Membership> list = memberships.findByMember_MemberIDOrderByStartDateDesc(m.getMemberID());
            if (!list.isEmpty()) {
                latestMembership.put(m.getMemberID(), list.get(0));
            }
        }
        model.addAttribute("members", memberList);
        model.addAttribute("membershipMap", latestMembership);
        return "members";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("packages", packages.findAll());
        model.addAttribute("isNew", true);
        return "member-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("member") Member member, BindingResult result,
                         @RequestParam String password,
                         @RequestParam(required = false) Long packageId,
                         @RequestParam(required = false) String paymentType,
                         Model model, RedirectAttributes ra) {
        if (member.getEmail() != null && members.existsByEmail(member.getEmail())) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }
        boolean weak = password.length() < 6;
        if (weak) {
            model.addAttribute("passwordError", "Password must be at least 6 characters");
        }
        if (packageId == null) {
            model.addAttribute("packageError", "Please select a membership package");
        }
        if (result.hasErrors() || weak || packageId == null) {
            model.addAttribute("isNew", true);
            model.addAttribute("packages", packages.findAll());
            return "member-form";
        }
        MembershipPackage plan = packages.findById(packageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found"));

        member.setPasswordHash(encoder.encode(password));
        member.setJoinDate(LocalDate.now());
        member.setStatus("Active");
        members.save(member);

        Membership membership = new Membership();
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStartDate(LocalDate.now());
        membership.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
        membership.setStatus("Active");
        memberships.save(membership);

        Payment payment = new Payment();
        payment.setMembership(membership);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(plan.getPrice());
        payment.setPaymentStatus("Completed");
        payment.setPaymentType((paymentType == null || paymentType.isBlank()) ? "Cash" : paymentType);
        payments.save(payment);

        ra.addFlashAttribute("message", "Member added, membership created and payment recorded");
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