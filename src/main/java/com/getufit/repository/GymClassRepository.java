package com.getufit.repository;

import com.getufit.model.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {
    List<GymClass> findByClassDateGreaterThanEqualOrderByClassDateAscStartTimeAsc(LocalDate date);
}