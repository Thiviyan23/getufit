package com.getufit.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class JoinForm {
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name is too long")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;

    @NotBlank(message = "Select a gender")
    @Pattern(regexp = "^(Male|Female|Other)?$", message = "Select a gender from the list")
    private String gender;

    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address is too long")
    private String address;

    @Pattern(regexp = "^0\\d{9}$", message = "Enter a valid 10-digit phone number")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Size(max = 100, message = "Email is too long")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
    private String password;

    @NotBlank(message = "Confirm your password")
    private String confirmPassword;
}