/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.config;

import com.cloudhopper.smpp.SmppBindType;
import java.util.Objects;

/**
 *
 * @author prodigy4440
 */
public class ConnectionConfig {

    private String tag;
    private String systemId;
    private String password;
    private String systemType;
    private SmppBindType smppBindType = SmppBindType.TRANSCEIVER;
    private String host;
    private Integer port;
    private Boolean autoRebind = true;
    private Integer rebindTime = 5;

    public ConnectionConfig() {
    }

    public ConnectionConfig(String tag, String systemId,String systemType, String password, 
            SmppBindType smppBindType, String host, Integer port, Boolean autoRebind, 
            Integer rebindTime) {
        this.tag = tag;
        this.systemId = systemId;
        this.password = password;
        this.smppBindType = smppBindType;
        this.host = host;
        this.port = port;
        this.autoRebind = autoRebind;
        this.rebindTime = rebindTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SmppBindType getSmppBindType() {
        return smppBindType;
    }

    public void setSmppBindType(SmppBindType smppBindType) {
        this.smppBindType = smppBindType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getAutoRebind() {
        return autoRebind;
    }

    public void setAutoRebind(Boolean autoRebind) {
        this.autoRebind = autoRebind;
    }

    public Integer getRebindTime() {
        return rebindTime;
    }

    public void setRebindTime(Integer rebindTime) {
        this.rebindTime = rebindTime;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.tag);
        hash = 29 * hash + Objects.hashCode(this.systemId);
        hash = 29 * hash + Objects.hashCode(this.password);
        hash = 29 * hash + Objects.hashCode(this.smppBindType);
        hash = 29 * hash + Objects.hashCode(this.host);
        hash = 29 * hash + Objects.hashCode(this.port);
        hash = 29 * hash + Objects.hashCode(this.autoRebind);
        hash = 29 * hash + Objects.hashCode(this.rebindTime);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConnectionConfig other = (ConnectionConfig) obj;
        if (!Objects.equals(this.tag, other.tag)) {
            return false;
        }
        if (!Objects.equals(this.systemId, other.systemId)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.smppBindType != other.smppBindType) {
            return false;
        }
        if (!Objects.equals(this.port, other.port)) {
            return false;
        }
        if (!Objects.equals(this.autoRebind, other.autoRebind)) {
            return false;
        }
        if (!Objects.equals(this.rebindTime, other.rebindTime)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" + "tag=" + tag + ", systemId=" + systemId 
                + ", password=" + password + ", smppBindType=" + smppBindType 
                + ", host=" + host + ", port=" + port + ", autoRebind=" + autoRebind 
                + ", rebindTime=" + rebindTime + '}';
    }
    
}
