package com.uphill.core.domain;

import com.uphill.core.exception.InvalidAppointmentStatusException;

public enum AppointmentStatus {
    BOOKED,
    CONFIRMED,
    CANCELLED,
    COMPLETED;

    public static AppointmentStatus fromString(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return AppointmentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidAppointmentStatusException("Invalid appointment status: " + value);
        }
    }
}
