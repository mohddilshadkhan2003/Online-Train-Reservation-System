package com.railway.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pnr;

    @Column(nullable = false)
    private String passenger;

    @Column(nullable = false)
    private String trainNo;

    @Column(nullable = false)
    private String trainName;

    @Column(nullable = false)
    private String travelClass;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    public Reservation() {
    }

    public Reservation(String pnr, String passenger, String trainNo, String trainName,
                      String travelClass, LocalDate journeyDate, String source, String destination) {
        this.pnr = pnr;
        this.passenger = passenger;
        this.trainNo = trainNo;
        this.trainName = trainName;
        this.travelClass = travelClass;
        this.journeyDate = journeyDate;
        this.source = source;
        this.destination = destination;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getPassenger() {
        return passenger;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(String travelClass) {
        this.travelClass = travelClass;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }
}
