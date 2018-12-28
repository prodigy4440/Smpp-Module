/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author prodigy4440
 */
@Entity
@Table(name = "sms")
public class Sms implements Serializable{
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message_id")
    private String messageId;
    
    @Column(name = "sender")
    private String sender;
    
    @Column(name = "receiver")
    private String receiver;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "sent_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;
    
    @Column(name = "delivered_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredDate;

    public Sms() {
    }

    public Sms(String messageId, String sender, String receiver, String message, Date sentDate) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.sentDate = sentDate;
    }

    public Sms(String messageId, String sender, String receiver, String message, 
            Date sentDate, Date depiveredDate) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.sentDate = sentDate;
        this.deliveredDate = depiveredDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Date getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(Date deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        hash = 17 * hash + Objects.hashCode(this.messageId);
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
        final Sms other = (Sms) obj;
        if (!Objects.equals(this.messageId, other.messageId)) {
            return false;
        }
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Sms{" + "id=" + id + ", messageId=" + messageId + 
                ", sender=" + sender + ", receiver=" + receiver + 
                ", message=" + message + ", sentDate=" + sentDate + 
                ", depiveredDate=" + deliveredDate + '}';
    }
        
}
