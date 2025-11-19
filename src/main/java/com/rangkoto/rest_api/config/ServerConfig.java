package com.rangkoto.rest_api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ServerConfig {

    @Value("${app.server-id}")
    private String serverId;

}
