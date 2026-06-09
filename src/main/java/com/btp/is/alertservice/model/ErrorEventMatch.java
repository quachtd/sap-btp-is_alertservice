package com.btp.is.alertservice.model;

public class ErrorEventMatch {

    private final MessageProcessingLog message;
    private final String pid;
    private final String errorEvent;

    public ErrorEventMatch(MessageProcessingLog message, String pid, String errorEvent) {
        this.message = message;
        this.pid = pid;
        this.errorEvent = errorEvent;
    }

    public MessageProcessingLog getMessage() {
        return message;
    }

    public String getPid() {
        return pid;
    }

    public String getErrorEvent() {
        return errorEvent;
    }
}
