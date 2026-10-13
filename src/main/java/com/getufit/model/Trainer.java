package com.getufit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Trainer")
@Getter
@Setter
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainerID;
    @NotBlank(message = "Trainer name is required")
    @Size(max = 100)
    private String trainerName;
    @Pattern(regexp = "^0\\d{9}$", message = "Enter a valid 10-digit phone number")
    private String phone;
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
    @NotBlank(message = "Specialization is required")
    @Size(max = 100)
    private String specialization;
    @JsonIgnore
    private String passwordHash;
    private String status;
}