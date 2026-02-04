package com.example.lab5_starter;

import java.io.Serializable;

public class City implements Serializable {
    private String name;
    private String province;

    // Firestore document id (NOT stored as a field in Firestore unless you set it)
    private String id;

    public City() {
        // Required empty constructor for Firestore
    }

    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    // --- Getters/Setters ---
    public String getName() { return name; }
    public String getProvince() { return province; }
    public void setName(String name) { this.name = name; }
    public void setProvince(String province) { this.province = province; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return name + ", " + province;
    }
}
