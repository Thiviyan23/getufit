package com.getufit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private String className;
    private Long trainerID;
    private LocalDate classDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classType;
    private Integer capacity;
}