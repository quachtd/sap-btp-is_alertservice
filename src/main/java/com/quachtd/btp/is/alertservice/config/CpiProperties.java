package com.quachtd.btp.is.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cpi")
public class CpiProperties {

    private String baseUrl;
    private String globalPid = "Global_Alert";
    private String senderInterfaceSchemeHex = "53656e646572496e74657266616365";
    private OAuth oauth = new OAuth();
    private Mpl mpl = new Mpl();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getGlobalPid() {
        return globalPid;
    }

    public void setGlobalPid(String globalPid) {
        this.globalPid = globalPid;
    }

    public String getSenderInterfaceSchemeHex() {
        return senderInterfaceSchemeHex;
    }

    public void setSenderInterfaceSchemeHex(String senderInterfaceSchemeHex) {
        this.senderInterfaceSchemeHex = senderInterfaceSchemeHex;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public void setOauth(OAuth oauth) {
        this.oauth = oauth;
    }

    public Mpl getMpl() {
        return mpl;
    }

    public void setMpl(Mpl mpl) {
        this.mpl = mpl;
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

    public static class Mpl {
        private String filter = "Status eq 'ESCALATED'";
        private String select =
                "IntegrationArtifact,Status,ApplicationMessageType,Sender,Receiver,"
                        + "IntegrationFlowName,CustomStatus,LogStart,LogEnd";
        private String sampleFile;
        private int lookbackMinutes = 15;
        private String watermarkParameterId = "AlertWatermark";

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public String getSelect() {
            return select;
        }

        public void setSelect(String select) {
            this.select = select;
        }

        public String getSampleFile() {
            return sampleFile;
        }

        public void setSampleFile(String sampleFile) {
            this.sampleFile = sampleFile;
        }

        public int getLookbackMinutes() {
            return lookbackMinutes;
        }

        public void setLookbackMinutes(int lookbackMinutes) {
            this.lookbackMinutes = lookbackMinutes;
        }

        public String getWatermarkParameterId() {
            return watermarkParameterId;
        }

        public void setWatermarkParameterId(String watermarkParameterId) {
            this.watermarkParameterId = watermarkParameterId;
        }
    }
}
