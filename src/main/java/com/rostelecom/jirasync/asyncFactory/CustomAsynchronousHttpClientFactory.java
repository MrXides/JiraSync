package com.rostelecom.jirasync.asyncFactory;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Jake Morgan {@literal <aleksey.tarasenkov@rt.ru>}
 */
public class CustomAsynchronousHttpClientFactory extends AsynchronousHttpClientFactory {
    @SuppressWarnings("unchecked")
    public DisposableHttpClient createClientCustom(final URI serverUri, final AuthenticationHandler authenticationHandler,
                                                   int socketTimeoutInSec, int requestTimeoutInSec) {
        final HttpClientOptions options = new HttpClientOptions();
        options.setSocketTimeout(socketTimeoutInSec, TimeUnit.SECONDS);
        options.setRequestTimeout(requestTimeoutInSec, TimeUnit.SECONDS);

        final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new NoOpEventPublisher(),
                new RestClientApplicationProperties(serverUri),
                new ThreadLocalContextManager() {
                    @Override
                    public Object getThreadLocalContext() {
                        return null;
                    }

                    @Override
                    public void setThreadLocalContext(Object context) {
                    }

                    @Override
                    public void clearThreadLocalContext() {
                    }
                });

        final HttpClient httpClient = defaultHttpClientFactory.create(options);

        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient);
            }
        };
    }


    private static class NoOpEventPublisher implements EventPublisher {
        @Override
        public void publish(Object o) {
        }

        @Override
        public void register(Object o) {
        }

        @Override
        public void unregister(Object o) {
        }

        @Override
        public void unregisterAll() {
        }
    }

    /**
     * Эти свойства используются для представления JRJC в качестве агента пользователя во время HTTP-запросов.
     */
    @SuppressWarnings("deprecation")
    private static class RestClientApplicationProperties implements ApplicationProperties {

        private final String baseUrl;

        private RestClientApplicationProperties(URI jiraURI) {
            this.baseUrl = jiraURI.getPath();
        }

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * Всегда будет абсолютный URL-адрес как у клиента.
         */
        @Nonnull
        @Override
        public String getBaseUrl(UrlMode urlMode) {
            return baseUrl;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Atlassian JIRA Rest Java Client";
        }

        @Nonnull
        @Override
        public String getPlatformId() {
            return ApplicationProperties.PLATFORM_JIRA;
        }

        @Nonnull
        @Override
        public String getVersion() {
            return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-com.atlassian.jira.rest.client");
        }

        @Nonnull
        @Override
        public Date getBuildDate() {
            // TODO реализовать с помощью MavenUtils, JRJC-123
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public String getBuildNumber() {
            // TODO реализовать с помощью MavenUtils, JRJC-123
            return String.valueOf(0);
        }

        @Override
        public File getHomeDirectory() {
            return new File(".");
        }

        @Override
        public String getPropertyValue(final String s) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    private static final class MavenUtils {
        private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);

        private static final String UNKNOWN_VERSION = "unknown";

        static String getVersion(String groupId, String artifactId) {
            final Properties props = new Properties();
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = MavenUtils.class.getResourceAsStream(String
                        .format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));
                props.load(resourceAsStream);
                return props.getProperty("version", UNKNOWN_VERSION);
            } catch (Exception e) {
                logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId);
                logger.debug("Got the following exception", e);
                return UNKNOWN_VERSION;
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
    }

}

