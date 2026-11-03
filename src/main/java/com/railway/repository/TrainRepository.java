package com.railway.repository;

import com.railway.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, String> {
    Optional<Train> findByNumber(String number);
}
