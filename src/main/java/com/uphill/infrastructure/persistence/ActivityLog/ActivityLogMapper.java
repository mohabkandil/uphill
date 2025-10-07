package com.uphill.infrastructure.persistence.ActivityLog;

import com.uphill.core.domain.ActivityLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {
    
    ActivityLog toDomain(ActivityLogEntity entity);
    
    ActivityLogEntity toEntity(ActivityLog domain);
}
