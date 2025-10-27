package com.rangkoto.rest_api.modules.user.controller;

import com.rangkoto.rest_api.modules.user.model.User;
import com.rangkoto.rest_api.modules.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return service.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User u) {
        return service.getById(id).map(user -> {
            user.setUsername(u.getUsername());
            user.setEmail(u.getEmail());
            return ResponseEntity.ok(service.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        if (service.getById(id).isPresent()) {
//            service.delete(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<User> optionalUser = service.getById(id);

        if (optionalUser.isPresent()) {
            service.softDelete(optionalUser.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

