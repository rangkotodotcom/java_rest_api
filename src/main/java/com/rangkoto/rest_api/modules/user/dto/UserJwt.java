package com.rangkoto.rest_api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJwt {
    private String id;
    private String name;
    private String email;
    private String username;
    private List<String> roles;
}
