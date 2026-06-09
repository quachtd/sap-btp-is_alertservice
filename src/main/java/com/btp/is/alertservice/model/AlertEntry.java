package com.btp.is.alertservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertEntry {

    private final String recipient;
    private final AlertEntryType type;
    private final String singleValue;
    private final List<PidInterfaceEntry> groupValue;
    private final int errorNumber;

    private AlertEntry(
            String recipient,
            AlertEntryType type,
            String singleValue,
            List<PidInterfaceEntry> groupValue,
            int errorNumber) {
        this.recipient = recipient;
        this.type = type;
        this.singleValue = singleValue;
        this.groupValue = groupValue;
        this.errorNumber = errorNumber;
    }

    public static AlertEntry single(String recipient, String errorEvent, int errorNumber) {
        return new AlertEntry(recipient, AlertEntryType.SINGLE, errorEvent, null, errorNumber);
    }

    public static AlertEntry group(String recipient, List<PidInterfaceEntry> pidEntries, int errorNumber) {
        return new AlertEntry(recipient, AlertEntryType.GROUP, null, List.copyOf(pidEntries), errorNumber);
    }

    public String getRecipient() {
        return recipient;
    }

    public AlertEntryType getType() {
        return type;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    @JsonProperty("value")
    public Object getValue() {
        return type == AlertEntryType.SINGLE ? singleValue : groupValue;
    }

    @JsonIgnore
    public String getSingleValue() {
        return singleValue;
    }

    @JsonIgnore
    public List<PidInterfaceEntry> getGroupValue() {
        return groupValue;
    }

    @Override
    public String toString() {
        if (type == AlertEntryType.SINGLE) {
            return recipient + " (SINGLE) -> " + singleValue + ", errorNumber=" + errorNumber;
        }
        return recipient + " (GROUP) -> " + groupValue + ", errorNumber=" + errorNumber;
    }
}
