package com.uphill.infrastructure.service;

import com.uphill.core.application.service.appointment.RoomReservationService;
import com.uphill.core.domain.AppointmentEventPayload;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class RoomReservationServiceImpl implements RoomReservationService {

    private static final Logger log = LoggerFactory.getLogger(RoomReservationServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${external.mock.base-url:http://localhost:3001}")
    private String baseUrl;

    @Override
    public boolean reserveRoom(final AppointmentEventPayload event) {
        try {
            final ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/room-reservation", event, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Room reservation call failed: {}", ex.getMessage());
            return false;
        }
    }
}


