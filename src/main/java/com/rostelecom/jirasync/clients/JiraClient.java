package com.rostelecom.jirasync.clients;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.rostelecom.jirasync.asyncFactory.CustomAsynchronousJiraRestClientFactory;
import com.rostelecom.jirasync.settings.ConnectionSettings;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;

@Service
@NoArgsConstructor
public class JiraClient {
    @Autowired
    private ConnectionSettings connectionSettings;

    private String userName;
    private String password;
    private String jiraUrl;

    private int socketTimeout;
    private int requestTimeout;

    /**
     * SocketTimeout - время на ожидание ответа от запросов в Jira (в секундах)
     * RequestTimeout - время на ожидание ответа request запроса в Jira (в секундах)
     */
    @PostConstruct
    public void initialize() {
        this.userName = connectionSettings.getParentUserName();
        this.password = connectionSettings.getParentPassword();
        this.jiraUrl = connectionSettings.getParentJiraUrl();
        this.socketTimeout = 300;
        this.requestTimeout = 300;
    }

    /**
     * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
     */
    public JiraRestClient getJiraRestClient() {
        return new CustomAsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthenticationCustom(getJiraUri(), this.userName, this.password, socketTimeout, requestTimeout);
    }

    private URI getJiraUri() {
        return URI.create(this.jiraUrl);
    }
}
