package com.rostelecom.jirasync.asyncFactory;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

import java.net.URI;

/**
 * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
 */
public class CustomAsynchronousJiraRestClientFactory extends AsynchronousJiraRestClientFactory {

    public JiraRestClient createCustom(final URI serverUri, final AuthenticationHandler authenticationHandler, int socketTimeoutInSec, int requestTimeoutInSec) {
        final DisposableHttpClient httpClient = new CustomAsynchronousHttpClientFactory()
                .createClientCustom(serverUri, authenticationHandler, socketTimeoutInSec, requestTimeoutInSec);
        return new AsynchronousJiraRestClient(serverUri, httpClient);
    }

    public JiraRestClient createWithBasicHttpAuthenticationCustom(final URI serverUri, final String username, final String password, final int socketTimeoutInSec, final int requestTimeoutInSec) {
        return createCustom(serverUri, new BasicHttpAuthenticationHandler(username, password), socketTimeoutInSec, requestTimeoutInSec);
    }

}
