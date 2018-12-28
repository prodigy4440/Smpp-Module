/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author prodigy4440
 */
public class SmsStatus implements Serializable{
    private boolean status;
    private String messageId;
    private String description;

    public SmsStatus() {
    }

    public SmsStatus(boolean status, String messageId, String description) {
        this.status = status;
        this.messageId = messageId;
        this.description = description;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.status ? 1 : 0);
        hash = 19 * hash + Objects.hashCode(this.messageId);
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
        final SmsStatus other = (SmsStatus) obj;
        if (this.status != other.status) {
            return false;
        }
        return Objects.equals(this.messageId, other.messageId);
    }

    @Override
    public String toString() {
        return "SmsStatus{" + "status=" + status + ", messageId=" + messageId + '}';
    }
    
}
