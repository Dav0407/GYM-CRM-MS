package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapperImpl userMapper;

    @Test
    void toUserResponseDTO_ShouldMapCorrectly() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("johnDoe")
                .firstName("John")
                .lastName("Doe")
                .build();

        // When
        UserResponseDTO result = userMapper.toUserResponseDTO(user);

        // Then
        assertNotNull(result);
        assertEquals("johnDoe", result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }
}