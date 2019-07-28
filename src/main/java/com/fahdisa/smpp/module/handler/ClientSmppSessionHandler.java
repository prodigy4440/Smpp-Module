/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.handler;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DataSm;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.cloudhopper.smpp.type.SmppChannelException;
import com.fahdisa.smpp.module.connection.BindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prodigy4440
 */
public class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

    private Logger logger = LoggerFactory.getLogger(ClientSmppSessionHandler.class);

    private SmsListener smsListener;
    private BindService bindService;

    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public ClientSmppSessionHandler(BindService bindService) {
        super();
        this.bindService = bindService;
    }

    public ClientSmppSessionHandler(BindService bindService, SmsListener smsListener) {
        super();
        this.smsListener = smsListener;
        this.bindService = bindService;
    }

    public void setSmsListener(SmsListener smsListener) {
        this.smsListener = smsListener;
    }

    @Override
    public void firePduRequestExpired(PduRequest pduRequest) {
        super.firePduRequestExpired(pduRequest);
        if (bindService.getSmppSession().isClosed()) {
            bindService.bind();
        }
    }

    @Override
    public void fireRecoverablePduException(RecoverablePduException e) {
        super.fireRecoverablePduException(e);
        if (bindService.getSmppSession().isClosed()) {
            bindService.bind();
        }
    }

    @Override
    public PduResponse firePduRequestReceived(final PduRequest pduRequest) {
        PduResponse pduResponse = pduRequest.createResponse();
        logger.info("New PDU: {}", pduRequest);
        if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DATA_SM) {
            THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    DataSm dataSm = (DataSm) pduRequest;
                    String sender = dataSm.getSourceAddress().getAddress();
                    String receiver = dataSm.getDestAddress().getAddress();
                    String message = new String(dataSm.getShortMessage());
                    String itsSessionInfo = "";

                    if (dataSm.hasOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD)) {
                        Tlv tlv = dataSm.getOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD);
                        if (Objects.isNull(message) || message.isEmpty()) {
                            message = new String(tlv.getValue());
                        }
                    }

                    if (dataSm.hasOptionalParameter(SmppConstants.TAG_ITS_SESSION_INFO)) {
                        Tlv tlv = dataSm.getOptionalParameter(SmppConstants.TAG_ITS_SESSION_INFO);
                        try {
                            itsSessionInfo = tlv.getValueAsString();
                        } catch (TlvConvertException tce) {

                        }
                    }

                    if (Objects.nonNull(smsListener)) {
                        smsListener.onSms(sender, receiver, message);
                        smsListener.onUssd(sender, receiver, message, itsSessionInfo);
                    }
                }
            });

        } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
            THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    DeliverSm deliverSm = (DeliverSm) pduRequest;
                    String sender = deliverSm.getSourceAddress().getAddress();
                    String receiver = deliverSm.getDestAddress().getAddress();
                    String message = new String(deliverSm.getShortMessage());
                    String itsSessionInfo = "";

                    if (deliverSm.hasOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD)) {
                        Tlv tlv = deliverSm.getOptionalParameter(SmppConstants.TAG_MESSAGE_PAYLOAD);
                        if (Objects.isNull(message) || message.isEmpty()) {
                            message = new String(tlv.getValue());
                        }
                    }

                    if (deliverSm.hasOptionalParameter(SmppConstants.TAG_ITS_SESSION_INFO)) {
                        Tlv tlv = deliverSm.getOptionalParameter(SmppConstants.TAG_ITS_SESSION_INFO);
                        try {
                            itsSessionInfo = tlv.getValueAsString();
                        } catch (TlvConvertException tce) {

                        }
                    }

                    if (Objects.nonNull(smsListener)) {
                        smsListener.onSms(sender, receiver, message);
                        smsListener.onUssd(sender, receiver, message, itsSessionInfo);
                    }
                }
            });
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
            if (Objects.nonNull(bindService)) {
                bindService.bind();
            } else {
                logger.error("Bind Service is NULL");
            }
        } else if (t instanceof IOException) {
            logger.error("fireUnknownThrowable {}", t);
            if (bindService.getSmppSession().isClosed()) {
                if (Objects.nonNull(bindService)) {
                    bindService.bind();
                } else {
                    logger.error("Bind Service is NULL");
                }
            }
        } else if (t instanceof SmppChannelException) {
            logger.error("fireUnknownThrowable {}", t);
            if (bindService.getSmppSession().isClosed()) {
                if (Objects.nonNull(bindService)) {
                    bindService.bind();
                } else {
                    logger.error("Bind Service is NULL");
                }
            }
        } else {
            logger.error("fireUnknownThrowable {}", t);
            if (bindService.getSmppSession().isClosed()) {
                if (Objects.nonNull(bindService)) {
                    bindService.bind();
                } else {
                    logger.error("Bind Service is NULL");
                }
            }
        }

    }

    @Override
    public void fireChannelUnexpectedlyClosed() {
        if (Objects.nonNull(bindService)) {
            bindService.bind();
        } else {
            logger.error("Bind Service is NULL");
        }
    }

}
