package com.uphill.core.application.service.activity;

import com.uphill.core.domain.ActivityEventType;
import com.uphill.core.domain.Appointment;
import com.uphill.core.domain.Doctor;
import com.uphill.core.domain.Room;

import java.util.EnumMap;
import java.util.Map;

final class ActivityDescriptionRegistry {

    private interface Formatter { String format(Appointment a, String timeSlot); }

    private static final Map<ActivityEventType, Formatter> FORMATTERS = new EnumMap<>(ActivityEventType.class);

    static {
        FORMATTERS.put(ActivityEventType.DOCTOR_SELECTED, (a, ts) -> {
            Doctor d = a.getDoctor();
            return String.format("Doctor %d (%s) selected for appointment on %s at %s",
                    d.getId(), d.getName(), a.getDate(), ts);
        });
        FORMATTERS.put(ActivityEventType.ROOM_SELECTED, (a, ts) -> {
            Room r = a.getRoom();
            return String.format("Room %d (%s) selected for appointment on %s at %s",
                    r.getId(), r.getName(), a.getDate(), ts);
        });
        FORMATTERS.put(ActivityEventType.APPOINTMENT_CREATED, (a, ts) -> {
            return String.format("Appointment created for patient %d with doctor %d on %s",
                    a.getPatient().getId(), a.getDoctor().getId(), a.getDate());
        });
        FORMATTERS.put(ActivityEventType.APPOINTMENT_STATUS_UPDATED, (a, ts) -> {
            return String.format("Appointment %d status updated to %s",
                    a.getId(), a.getStatus());
        });
    }

    static String build(final ActivityEventType type, final Appointment a, final String ts) {
        final Formatter formatter = FORMATTERS.get(type);
        return formatter != null ? formatter.format(a, ts) : type.name();
    }
}


