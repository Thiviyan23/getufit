package com.getufit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "ClassRegistration")
@Getter
@Setter
public class ClassRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationID;
    @ManyToOne
    @JoinColumn(name = "MemberID")
    private Member member;
    @ManyToOne
    @JoinColumn(name = "ClassID")
    private GymClass gymClass;
    private LocalDate registrationDate;
    private String status;
}