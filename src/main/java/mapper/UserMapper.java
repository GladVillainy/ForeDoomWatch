package mapper;

import dto.AuthDTO;
import dto.UserDTO;
import entities.User;

import java.util.List;

public abstract class UserMapper implements IMapper<User, UserDTO, AuthDTO> {

    @Override
    public UserDTO toDTO(User entity) {
        return new UserDTO(
                entity.getEmail(),
                entity.getUsername(),
                entity.getUserId()
        );
    }

    public User toEntity(AuthDTO dto) {
        return new User(
                dto.username(),
                dto.email(),
                dto.password()
        );
    }

    @Override
    public List<UserDTO> toDTOList(List<User> entities) {
        return IMapper.super.toDTOList(entities);
    }
}
