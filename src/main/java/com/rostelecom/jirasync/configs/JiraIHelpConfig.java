package com.rostelecom.jirasync.configs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ihelp")
@Getter
@Setter
@RequiredArgsConstructor
public class JiraIHelpConfig {
    private String jiraUri;
    private String userName;
    private String password;
    private Integer socketTimeout;
    private Integer requestTimeout;
}
