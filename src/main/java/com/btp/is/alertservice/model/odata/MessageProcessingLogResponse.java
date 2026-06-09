package com.btp.is.alertservice.model.odata;

import com.btp.is.alertservice.model.MessageProcessingLog;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageProcessingLogResponse {

    @JsonProperty("d")
    private Data d;

    public List<MessageProcessingLog> getResults() {
        if (d == null || d.results == null) {
            return Collections.emptyList();
        }
        return d.results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Data {
        @JsonProperty("results")
        private List<MessageProcessingLog> results;
    }
}
