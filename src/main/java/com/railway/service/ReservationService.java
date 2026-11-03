package com.railway.service;

import com.railway.model.Reservation;
import com.railway.model.Train;
import com.railway.model.User;
import com.railway.repository.ReservationRepository;
import com.railway.repository.TrainRepository;
import com.railway.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
public class ReservationService {
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final ReservationRepository reservationRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    public ReservationService(UserRepository userRepository, TrainRepository trainRepository,
                             ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.reservationRepository = reservationRepository;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return userRepository.findByUsername(username.trim())
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    public List<Train> findAllTrains() {
        return trainRepository.findAll();
    }

    public String getTrainName(String trainNumber) {
        if (trainNumber == null || trainNumber.isBlank()) {
            return "";
        }
        return trainRepository.findByNumber(trainNumber.trim())
                .map(Train::getName)
                .orElse("");
    }

    public List<Reservation> listReservations(String keyword) {
        String cleaned = keyword == null ? "" : keyword.trim();
        if (cleaned.isBlank()) {
            return reservationRepository.findAll().stream()
                    .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                    .toList();
        }
        return reservationRepository.search(cleaned);
    }

    public Reservation createReservation(String passenger, String trainNo, String trainName,
                                        String travelClass, String dateText, String source, String destination) {
        String cleanPassenger = safeTrim(passenger);
        String cleanNo = safeTrim(trainNo);
        String cleanName = safeTrim(trainName);
        String cleanSource = safeTrim(source);
        String cleanDestination = safeTrim(destination);
        String cleanClass = safeTrim(travelClass);
        String cleanDateText = safeTrim(dateText);

        if (cleanPassenger.isEmpty() || cleanNo.isEmpty() || cleanName.isEmpty() || cleanSource.isEmpty() || cleanDestination.isEmpty() || cleanClass.isEmpty() || cleanDateText.isEmpty()) {
            throw new IllegalArgumentException("Please complete all booking fields before saving.");
        }
        if (!cleanNo.matches("\\d+")) {
            throw new IllegalArgumentException("Train number must contain digits only.");
        }
        LocalDate journeyDate;
        try {
            journeyDate = LocalDate.parse(cleanDateText, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Use a valid date in DD-MM-YYYY format.");
        }
        if (journeyDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Journey date cannot be in the past.");
        }

        String resolvedName = cleanName;
        Optional<Train> savedTrain = trainRepository.findByNumber(cleanNo);
        if (savedTrain.isPresent()) {
            resolvedName = savedTrain.get().getName();
        }

        String pnr = "RE" + System.currentTimeMillis() + String.format(Locale.US, "%03d", new Random().nextInt(1000));
        Reservation reservation = new Reservation(pnr, cleanPassenger, cleanNo, resolvedName, cleanClass, journeyDate, cleanSource, cleanDestination);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(String pnr) {
        Reservation reservation = reservationRepository.findByPnr(pnr).orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        reservationRepository.delete(reservation);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
