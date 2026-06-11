package com.quachtd.btp.is.alertservice.model.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StringParameterResponse {

    @JsonProperty("d")
    private Data d;

    public String getValue() {
        return d != null ? d.value : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Data {
        @JsonProperty("Value")
        private String value;
    }
}
