package com.Dk3.Cars.mapper;

import com.Dk3.Cars.dto.UserDto;
import com.Dk3.Cars.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConversion {
    User toEntity(UserDto dto);
    UserDto toDto(User user);
}
