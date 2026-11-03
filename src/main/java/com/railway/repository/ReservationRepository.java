package com.railway.repository;

import com.railway.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByPnr(String pnr);

    @Query("SELECT r FROM Reservation r WHERE " +
           "LOWER(r.pnr) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.passenger) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.source) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.destination) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.trainNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY r.id DESC")
    List<Reservation> search(@Param("keyword") String keyword);
}
