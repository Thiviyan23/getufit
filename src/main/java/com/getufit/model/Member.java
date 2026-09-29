package com.getufit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "Member")
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberID;
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;
    @NotBlank(message = "Gender is required")
    private String gender;
    @NotBlank(message = "Address is required")
    private String address;
    @Pattern(regexp = "^0\\d{9}$", message = "Enter a valid 10-digit phone number")
    private String phone;
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
    @JsonIgnore
    private String passwordHash;
    private LocalDate joinDate;
    private String status;
}