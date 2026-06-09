package com.btp.is.alertservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageProcessingLog {

    @JsonProperty("ApplicationMessageType")
    private String applicationMessageType;

    @JsonProperty("Sender")
    private String sender;

    @JsonProperty("Receiver")
    private String receiver;

    @JsonProperty("IntegrationFlowName")
    private String integrationFlowName;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("CustomStatus")
    private String customStatus;

    public String getApplicationMessageType() {
        return applicationMessageType;
    }

    public void setApplicationMessageType(String applicationMessageType) {
        this.applicationMessageType = applicationMessageType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getIntegrationFlowName() {
        return integrationFlowName;
    }

    public void setIntegrationFlowName(String integrationFlowName) {
        this.integrationFlowName = integrationFlowName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomStatus() {
        return customStatus;
    }

    public void setCustomStatus(String customStatus) {
        this.customStatus = customStatus;
    }

    public String getSenderInterface() {
        return applicationMessageType;
    }

    public String getFieldValue(String fieldName) {
        return switch (fieldName) {
            case "Sender" -> sender;
            case "Receiver" -> receiver;
            case "IntegrationFlowName" -> integrationFlowName;
            case "Status" -> status;
            case "CustomStatus" -> customStatus;
            case "ApplicationMessageType" -> applicationMessageType;
            default -> null;
        };
    }

    public String interfaceKey() {
        return sender + "+" + applicationMessageType;
    }
}
