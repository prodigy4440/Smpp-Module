/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.fahdisa.smpp.module.connection.BindService;
import com.fahdisa.smpp.module.domain.SmsStatus;
import com.fahdisa.smpp.module.config.SmppConfig;
import com.fahdisa.smpp.module.handler.SmsListener;
import com.fahdisa.smpp.module.ussd.UssdServiceOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prodigy4440
 */
public class Session {

    private final Logger logger = LoggerFactory.getLogger(Session.class);

    private final BindService bindService;

    private final List<ScheduledFuture<?>> WORKER = new LinkedList<>();

    private final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private Boolean rebind = false;

    private final Integer nextRebind;

    private final Map<String, Object> metaData = new HashMap<>();

    public Session(SmppConfig config) {
        this.rebind = config.getAutoRebind();
        this.nextRebind = config.getRebindTime();
        this.bindService = new BindService(config.getTag(), config.getSystemId(),
                config.getPassword(), config.getSystemType(), config.getSmppBindType(),
                config.getHost(), config.getPort(), config.getConnectionTimeout());
    }

    public void setSmsReceiver(SmsListener smsListener) {
        if (Objects.nonNull(bindService)) {
            bindService.setSmsListener(smsListener);
        }
    }

    public void bindSession() {

        if (Objects.nonNull(bindService)) {
            if (rebind) {
                ScheduledFuture<?> scheduleFuture = SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
                    bindService.bind();
                }, 2, nextRebind, TimeUnit.MILLISECONDS);
                WORKER.add(scheduleFuture);
            } else {
                bindService.bind();
            }
        }

    }

    public synchronized SmsStatus sendSms(String sender, String message, String receiver) {
        if (Objects.nonNull(bindService.getSmppSession())) {
            try {
                byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM);
                SubmitSm submit = new SubmitSm();
                submit.setSourceAddress(new Address((byte) 5, (byte) 9, sender));
                submit.setDestAddress(new Address((byte) 1, (byte) 1, receiver));
                submit.setShortMessage(textBytes);
                SubmitSmResp submitSmResp = bindService.getSmppSession().submit(submit, 10000);
                String messageId = submitSmResp.getMessageId();
                SmsStatus smsStatus = new SmsStatus(true, messageId, "Success");
                return smsStatus;
            } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                    | SmppTimeoutException | SmppChannelException ie) {
                logger.error("Bind Error {}", ie);
                return new SmsStatus(false, null, ie.getMessage());
            }
        } else {
            return new SmsStatus(false, null, "Session not bound");
        }

    }

    public SmsStatus sendLongSms(String sender, String receiver, String message,
            boolean requestDeliveryReceipt) {

        if (Objects.nonNull(bindService.getSmppSession())) {
            byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_ISO_8859_1);
            try {
                SubmitSm submitMsg = new SubmitSm();
                // add delivery receipt if enabled.
                if (requestDeliveryReceipt) {
                    submitMsg.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
                }
                submitMsg.setSourceAddress(new Address((byte) 0x03, (byte) 0x00, sender));
                submitMsg.setDestAddress(new Address((byte) 0x01, (byte) 0x01, receiver));
                if (textBytes != null && textBytes.length > 255) {
                    submitMsg.addOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes, "message_payload"));
                } else {
                    submitMsg.setShortMessage(textBytes);
                }

                SubmitSmResp submitResp = bindService.getSmppSession().submit(submitMsg, 15000);
                SmsStatus status = new SmsStatus(true, submitResp.getMessageId(), "Success");
                return status;
            } catch (RecoverablePduException | SmppChannelException | SmppTimeoutException
                    | UnrecoverablePduException | InterruptedException ex) {
                logger.error("Submit_Sm Error {}", ex);
                return new SmsStatus(false, null, ex.getMessage());
            }
        } else {
            return new SmsStatus(false, null, "Session not bound");
        }
    }

    //    msgType continue is 1 and end is 2
    public void sendUssd(String source, String destination, String message, int messageType, String meta) {
        try {
            byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM);
            SubmitSm submit = new SubmitSm();
            submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);

            submit.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, source));
            submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, destination));
            submit.setShortMessage(textBytes);
            Tlv tlv;
            if (messageType == 1) {
                tlv = new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, new byte[]{0x02}, "ussd_service_op");
            } else {
                tlv = new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, new byte[]{0x11}, "ussd_service_op");
            }
            submit.setOptionalParameter(tlv);
            submit.setServiceType("USSD");
            bindService.getSmppSession().sendRequestPdu(submit, 10000, false);
        } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                | SmppTimeoutException | SmppChannelException ie) {
            logger.error("Submit_Sm Error {}", ie);
        }
    }

    public void sendAsyncUssd(String source, String destination, String message, UssdServiceOp ussdServiceOp, String meta) {
       sendAsyncUssd(source, destination, message, ussdServiceOp, meta, CharsetUtil.CHARSET_GSM);
    }

    public void sendAsyncUssd(String source, String destination, String message, UssdServiceOp ussdServiceOp, String meta, Charset charset) {
        try {

            byte[] textBytes = CharsetUtil.encode(message, charset);
            SubmitSm submit = new SubmitSm();
            submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);

            submit.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, source));
            submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, destination));

            submit.setDataCoding((byte) 0x0F);
            submit.setEsmClass((byte) 0x00);
            submit.setShortMessage(textBytes);
            switch (ussdServiceOp) {
                case PSSD_IND:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSD_IND.getValue()));
                    break;
                case PSSR_IND:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSR_IND.getValue()));
                    break;
                case USSR_REQ:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSR_REQ.getValue()));
                    break;
                case USSN_REQ:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSN_REQ.getValue()));
                    break;
                case PSSD_RES:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSD_RES.getValue()));
                    break;
                case PSSR_RES:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSR_RES.getValue()));
                    break;
                case USSR_CNF:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSR_CNF.getValue()));
                    break;
                case USSN_CNF:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSN_CNF.getValue()));
                    break;
                default:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSD_IND.getValue()));

            }

            submit.setServiceType(null);
            bindService.getSmppSession().sendRequestPdu(submit, 10000, false);
        } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                | SmppTimeoutException | SmppChannelException ie) {
            logger.error("Submit_Sm Error {}", ie);
        }
    }


    public void sendAsyncLongUssd(String source, String destination, String message, UssdServiceOp ussdServiceOp, String meta, Charset charset) {
        try {

            byte[] textBytes = CharsetUtil.encode(message, charset);
            SubmitSm submit = new SubmitSm();
            submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_NOT_REQUESTED);

            submit.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, source));
            submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, destination));

            submit.setDataCoding((byte) 0x0F);
            submit.setEsmClass((byte) 0x00);
            if (textBytes != null && textBytes.length > 255) {
                submit.addOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes, "message_payload"));
            } else {
                submit.setShortMessage(textBytes);
            }
            switch (ussdServiceOp) {
                case PSSD_IND:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSD_IND.getValue()));
                    break;
                case PSSR_IND:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSR_IND.getValue()));
                    break;
                case USSR_REQ:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSR_REQ.getValue()));
                    break;
                case USSN_REQ:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSN_REQ.getValue()));
                    break;
                case PSSD_RES:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSD_RES.getValue()));
                    break;
                case PSSR_RES:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSR_RES.getValue()));
                    break;
                case USSR_CNF:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSR_CNF.getValue()));
                    break;
                case USSN_CNF:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.USSN_CNF.getValue()));
                    break;
                default:
                    submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, UssdServiceOp.PSSD_IND.getValue()));

            }

            submit.setServiceType(null);
            bindService.getSmppSession().sendRequestPdu(submit, 10000, false);
        } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                | SmppTimeoutException | SmppChannelException ie) {
            logger.error("Submit_Sm Error {}", ie);
        }
    }


    public boolean querySms() {
        return false;
    }

    public boolean getBindStatus() {
        if (Objects.isNull(bindService.getSmppSession())) {
            return false;
        } else {
            return this.bindService.getSmppSession().isBound();
        }
    }

    public void unBindSession() {
        WORKER.forEach((scheduledFuture) -> {
            scheduledFuture.cancel(true);
        });
        WORKER.clear();
        if (Objects.nonNull(bindService)) {
            bindService.unbind();
        }
    }

    public void setMetaData(Map<String, Object> meta){
        if(Objects.nonNull(meta)){
            meta.forEach(new BiConsumer<String, Object>() {
                @Override
                public void accept(String s, Object o) {
                    metaData.put(s, o);
                }
            });
        }
    }

    public void addMetaData(String key, Object value){
        this.metaData.put(key, value);
    }

    public Map<String, Object> getMetaData(){
        return this.metaData;
    }

    public Object getMetaData(String key){
        return this.metaData.get(key);
    }

}
