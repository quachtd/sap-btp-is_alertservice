package com.btp.is.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-mesh")
public class EventMeshProperties {

    private String baseUrl;
    private String topic;
    private String publishPath = "/messagingrest/v1/topics/{topic}/messages";
    private boolean failOnError = true;
    private OAuth oauth = new OAuth();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPublishPath() {
        return publishPath;
    }

    public void setPublishPath(String publishPath) {
        this.publishPath = publishPath;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public void setOauth(OAuth oauth) {
        this.oauth = oauth;
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank()
                && topic != null && !topic.isBlank()
                && oauth.isConfigured();
    }

    public static class OAuth {
        private String tokenUrl;
        private String clientId;
        private String clientSecret;

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public boolean isConfigured() {
            return tokenUrl != null && !tokenUrl.isBlank()
                    && clientId != null && !clientId.isBlank()
                    && clientSecret != null && !clientSecret.isBlank();
        }
    }
}
