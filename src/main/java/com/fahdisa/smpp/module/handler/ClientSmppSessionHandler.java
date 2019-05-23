/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.handler;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.tlv.TlvConvertException;
import com.cloudhopper.smpp.type.RecoverablePduException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Objects;

import com.fahdisa.smpp.module.connection.BindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prodigy4440
 */
public class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

    private Logger logger = LoggerFactory.getLogger(ClientSmppSessionHandler.class);
    
    private SmsListener smsListener;
    private BindService bindService;
    
    public ClientSmppSessionHandler(BindService bindService){
        super();
    }
    
    public ClientSmppSessionHandler(BindService bindService, SmsListener smsListener){
        super();
        this.smsListener = smsListener;
    }
    
    public void setSmsListener(SmsListener smsListener){
        this.smsListener = smsListener;
    }
    
    @Override
    public void firePduRequestExpired(PduRequest pduRequest) {
        super.firePduRequestExpired(pduRequest);
    }

    @Override
    public void fireRecoverablePduException(RecoverablePduException e) {
        super.fireRecoverablePduException(e);
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        PduResponse pduResponse = pduRequest.createResponse();
        logger.info("New PDU: {}", pduRequest);
        if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DATA_SM) {
        } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
            DeliverSm deliverSm = (DeliverSm) pduRequest;
            String sender = deliverSm.getSourceAddress().getAddress();
            String receiver = deliverSm.getDestAddress().getAddress();
            String message = new String(deliverSm.getShortMessage());
            if (Objects.isNull(message) || message.isEmpty()) {
                ArrayList<Tlv> tlvs = deliverSm.getOptionalParameters();
                if (Objects.nonNull(tlvs) && (!tlvs.isEmpty())) {
                    for (Tlv tlv : tlvs) {
                        if (tlv.getTag() == SmppConstants.TAG_MESSAGE_PAYLOAD) {
                            message = new String(tlv.getValue());
                        }
                    }
                }
            }

            Tlv itsTlv = null;
            ArrayList<Tlv> tlvs = deliverSm.getOptionalParameters();
            if (Objects.nonNull(tlvs) && (!tlvs.isEmpty())) {
                for (Tlv tlv : tlvs) {
                    if (tlv.getTag() == SmppConstants.TAG_ITS_SESSION_INFO) {
                        itsTlv = tlv;
                    }
                }
            }

            if (Objects.nonNull(smsListener)) {
                smsListener.onSms(sender, receiver, message);
                if (Objects.nonNull(itsTlv)) {
                    try {
                        smsListener.onUssd(sender, receiver, message, itsTlv.getValueAsString());
                    } catch (TlvConvertException ex) {
                        logger.error("Error fetching tlvParameter info", ex);
                    }
                }

            }
        }
        return pduResponse;
    }

    @Override
    public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
        super.fireExpectedPduResponseReceived(pduAsyncResponse);
    }

    @Override
    public void fireUnknownThrowable(Throwable t) {
        if (t instanceof ClosedChannelException) {
            if(Objects.nonNull(bindService)){
                bindService.bind();
            }
            bindService.bind();
        } else if (t instanceof IOException) {
            logger.error("fireUnknownThrowable {}", t);
        } else {
            logger.error("fireUnknownThrowable {}", t);
        }
    }

    @Override
    public void fireChannelUnexpectedlyClosed() {
        if(Objects.nonNull(bindService)){
                bindService.bind();
        }
        bindService.bind();
    }

}
