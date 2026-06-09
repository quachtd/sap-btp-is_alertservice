package com.btp.is.alertservice.model.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlternativePartnerResponse {

    @JsonProperty("d")
    private Data d;

    public String getPid() {
        return d != null ? d.pid : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Data {
        @JsonProperty("Pid")
        private String pid;
    }
}
