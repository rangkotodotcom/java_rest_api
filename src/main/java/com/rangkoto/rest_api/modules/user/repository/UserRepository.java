package com.rangkoto.rest_api.modules.user.repository;

import com.rangkoto.rest_api.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
