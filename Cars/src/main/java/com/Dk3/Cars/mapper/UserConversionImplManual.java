package com.Dk3.Cars.mapper;

import com.Dk3.Cars.dto.UserDto;
import com.Dk3.Cars.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;

@Component
@Primary
public class UserConversionImplManual implements UserConversion {

    @Override
    public User toEntity(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserid(dto.getUserid());
        user.setFirst(dto.getFirst());
        user.setLast(dto.getLast());
        user.setEmail(dto.getEmail());
        user.setContact(dto.getContact());
        user.setPassword(dto.getPassword());
        return user;
    }

    @Override
    public UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setUserid(user.getUserid());
        dto.setFirst(user.getFirst());
        dto.setLast(user.getLast());
        dto.setEmail(user.getEmail());
        dto.setContact(user.getContact());
        dto.setPassword(user.getPassword());
        return dto;
    }
}
