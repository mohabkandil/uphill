package com.uphill.infrastructure.service;

import com.uphill.core.application.service.appointment.RoomService;
import com.uphill.core.exception.AppointmentSlotUnavailableException;
import com.uphill.core.domain.Room;
import com.uphill.infrastructure.persistence.EntityMapper;
import com.uphill.infrastructure.persistence.Room.RoomEntity;
import com.uphill.infrastructure.persistence.Room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final EntityMapper entityMapper;

    @Override
    public Room findAvailableRoom(final LocalDate date, final Long timeSlotId) {
        final List<RoomEntity> availableRooms = roomRepository.findAvailableByDateAndTimeSlot(date, timeSlotId);
        if (availableRooms.isEmpty()) {
            throw new AppointmentSlotUnavailableException("No available room for the requested date and time slot");
        }
        return entityMapper.toDomain(availableRooms.get(0));
    }
}


