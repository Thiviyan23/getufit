package com.getufit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "GymClass")
@Getter
@Setter
public class GymClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classID;
    @NotBlank(message = "Class name is required")
    @Size(max = 100)
    private String className;
    @NotNull(message = "Select a trainer")
    private Long trainerID;
    @NotNull(message = "Class date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate classDate;
    @NotNull(message = "Start time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;
    @NotNull(message = "End time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;
    @NotBlank(message = "Select a class type")
    private String classType;
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 200, message = "Capacity cannot exceed 200")
    private Integer capacity;
}