package com.rostelecom.jirasync.clients;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
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

    @PostConstruct
    public void initialize(){
        this.userName = connectionSettings.getParentUserName();
        this.password = connectionSettings.getParentPassword();
        this.jiraUrl = connectionSettings.getParentJiraUrl();
    }

    public JiraRestClient getJiraRestClient() {
        return new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(getJiraUri(), this.userName, this.password);
    }

    private URI getJiraUri() {
        return URI.create(this.jiraUrl);
    }
}
