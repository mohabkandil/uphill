package com.uphill.infrastructure.service;

import com.uphill.core.application.service.appointment.DoctorCalendarService;
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
public class DoctorCalendarServiceImpl implements DoctorCalendarService {

    private static final Logger log = LoggerFactory.getLogger(DoctorCalendarServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${external.mock.base-url:http://localhost:3001}")
    private String baseUrl;

    @Override
    public boolean updateDoctorCalendar(AppointmentEventPayload event) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/doctor-calendar", event, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Doctor calendar call failed: {}", ex.getMessage());
            return false;
        }
    }
}


