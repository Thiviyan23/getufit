package com.getufit.repository;

import com.getufit.model.TrainerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerAccountRepository extends JpaRepository<TrainerAccount, Long> {
    Optional<TrainerAccount> findByEmail(String email);
}