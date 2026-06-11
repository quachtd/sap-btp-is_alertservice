package com.quachtd.btp.is.alertservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PidInterfaceEntry {

    private final String pid;
    private final String interfaceKey;

    public PidInterfaceEntry(String pid, String interfaceKey) {
        this.pid = pid;
        this.interfaceKey = interfaceKey;
    }

    public String getPid() {
        return pid;
    }

    @JsonProperty("interface")
    public String getInterfaceKey() {
        return interfaceKey;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PidInterfaceEntry that = (PidInterfaceEntry) other;
        return Objects.equals(pid, that.pid) && Objects.equals(interfaceKey, that.interfaceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, interfaceKey);
    }

    @Override
    public String toString() {
        return "{pid=" + pid + ", interface=" + interfaceKey + "}";
    }
}
