package com.medbid.auth.mapper;

import com.medbid.auth.dto.UserDto;
import com.medbid.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "role.id", target = "roleId")
    UserDto toDto(User user);
}
