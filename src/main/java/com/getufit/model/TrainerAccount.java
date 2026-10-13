package com.getufit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "Trainer")
@Getter
@Setter
public class TrainerAccount {
    @Id
    @Column(name = "TrainerID")
    private Long trainerID;
    @Column(name = "TrainerName")
    private String fullName;
    @Column(name = "Specialization")
    private String specialization;
    @Column(name = "Phone")
    private String phone;
    @Column(name = "Email")
    private String email;
    @Column(name = "PasswordHash")
    private String passwordHash;
}