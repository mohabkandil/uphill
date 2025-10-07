package com.uphill.infrastructure.persistence.Admin;

import com.uphill.core.domain.Admin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    
    Admin toDomain(AdminEntity jpaAdmin);
}
