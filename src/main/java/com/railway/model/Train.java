package com.railway.model;

import jakarta.persistence.*;

@Entity
@Table(name = "trains")
public class Train {
    @Id
    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String name;

    public Train() {
    }

    public Train(String number, String name) {
        this.number = number;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
