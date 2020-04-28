package eu.sshopencloud.marketplace.mappers.auth;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.model.auth.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    List<UserDto> toDto(List<User> users);

}
