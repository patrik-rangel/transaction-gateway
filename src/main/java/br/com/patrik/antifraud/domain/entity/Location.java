package br.com.patrik.antifraud.domain.entity;

public record Location (
        Double latitude,
        Double longitude
){
    public static Location create(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return new Location(latitude, longitude);
        } else {
            return null;
        }
    }
}
