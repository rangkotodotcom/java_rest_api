package com.rangkoto.rest_api.modules.user.service;

import com.rangkoto.rest_api.modules.user.model.User;
import com.rangkoto.rest_api.modules.user.repository.UserRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    private final AuditorAware<String> auditorAware;

    public UserService(UserRepository repo, AuditorAware<String> auditorAware) {
        this.repo = repo;
        this.auditorAware = auditorAware;
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getById(Long id) {
        return repo.findById(id);
    }

    public User save(User user) {
        return repo.save(user);
    }

//    public void delete(Long id) {
//        repo.deleteById(id);
//    }

//    public void delete(Long id, String user) {
//        repo.findById(id).ifPresent(u -> {
//            u.softDelete(user);    // tandai deletedAt + deletedBy
//            repo.save(u);
//        });
//    }

    public void softDelete(User user) {
        String currentUser = auditorAware.getCurrentAuditor().orElse("system");
        user.softDelete(currentUser);
        repo.save(user);
    }

}
