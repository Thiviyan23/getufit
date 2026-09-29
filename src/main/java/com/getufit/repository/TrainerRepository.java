package com.getufit.repository;

import com.getufit.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndTrainerIDNot(String email, Long trainerID);
}