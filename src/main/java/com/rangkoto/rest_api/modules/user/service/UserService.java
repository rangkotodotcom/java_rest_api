package com.rangkoto.rest_api.modules.user.service;

import com.rangkoto.rest_api.modules.user.model.User;
import com.rangkoto.rest_api.modules.user.repository.UserRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    private final AuditorAware<String> auditorAware;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, AuditorAware<String> auditorAware, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.auditorAware = auditorAware;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getById(Long id) {
        return repo.findById(id);
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return repo.save(user);
    }

    public void softDelete(User user) {
        String currentUser = auditorAware.getCurrentAuditor().orElse("system");
        user.softDelete(currentUser);
        repo.save(user);
    }


    public Optional<User> authenticate(String usernameOrEmail, String rawPassword) {
        Optional<User> userOpt = repo.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = repo.findByEmail(usernameOrEmail);
        }

        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

}
