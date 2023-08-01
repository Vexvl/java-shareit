package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {
    private static final String USER_API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("http://localhost:9090") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + USER_API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("", null, null);
    }

    public ResponseEntity<Object> getById(Long ownerId) {
        return get("/" + ownerId, ownerId, null);
    }

    public ResponseEntity<Object> addUser(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> editUser(Long ownerId, UserDto userDto) {
        return patch("/" + ownerId, ownerId, userDto);
    }

    public void deleteUser(Long ownerId) {
        delete("/" + ownerId);
    }
}