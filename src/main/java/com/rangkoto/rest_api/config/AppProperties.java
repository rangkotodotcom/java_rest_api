package com.rangkoto.rest_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String webUsername;
    private String webPassword;

    private String mobileUsername;
    private String mobilePassword;

}
