package com.eidiko.user_service.controller;

import com.eidiko.user_service.dto.UserRequest;
import com.eidiko.user_service.entity.User;
import com.eidiko.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@PathVariable String username, @RequestBody UserRequest request) {
        return userService.findByUsername(username)
                .map(existingUser -> {
                    existingUser.setEmail(request.getEmail());
                    existingUser.setFullName(request.getFullName());
                    existingUser.setPhoneNumber(request.getPhoneNumber());
                    User updatedUser = userService.update(existingUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    userService.delete(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
