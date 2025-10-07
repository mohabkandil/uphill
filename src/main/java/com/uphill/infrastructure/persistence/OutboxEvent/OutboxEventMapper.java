package com.uphill.infrastructure.persistence.OutboxEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uphill.core.domain.OutboxEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OutboxEventMapper {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Mapping(target = "payload", expression = "java(entity.getPayload().toString())")
    public abstract OutboxEvent toDomain(OutboxEventEntity entity);
    
    public OutboxEventEntity toEntity(OutboxEvent domain) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(domain.getId());
        entity.setAggregateId(domain.getAggregateId());
        entity.setAggregateType(domain.getAggregateType());
        entity.setEventType(domain.getEventType());
        try {
            entity.setPayload(objectMapper.readTree(domain.getPayload()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON payload", ex);
        }
        entity.setStatus(domain.getStatus());
        entity.setRetryCount(domain.getRetryCount());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setNextRetryAt(domain.getNextRetryAt());
        return entity;
    }
}
