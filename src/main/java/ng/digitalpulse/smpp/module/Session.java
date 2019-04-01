/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DataSm;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.LoggingOptions;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import ng.digitalpulse.smpp.module.domain.SmsStatus;
import ng.digitalpulse.smpp.module.util.MessageLogger;

/**
 *
 * @author prodigy4440
 */
public class Session {

    private SmppSession smppSession;

    private BindService bindService;

    private SmsListener smsListener;
    private EnquireLinkService enquireLinkService;

    private final List<ScheduledFuture<?>> WORKER = new LinkedList<>();

    private ScheduledExecutorService SCHEDULEDEXECUTORSERVICE;

    private String TAG = "";

    private Integer reBindTimeInMinutes;

    public Session(String tag, String systemId, String password, SmppBindType smppBindType,
            String host, Integer port) {
        this(tag, systemId, systemId, password, systemId, smppBindType, host, port);
    }

    public Session(String tag, String name, String systemId, String password, String systemType,
            SmppBindType smppBindType, String host, Integer port) {
        this(tag, name, systemId, password, systemType, smppBindType, host, port, 5);
    }

    public Session(String tag, String name, String systemId, String password, String systemType,
            SmppBindType smppBindType, String host, Integer port, Integer reBindTimeInMinutes) {
        this.TAG = tag;
        this.reBindTimeInMinutes = reBindTimeInMinutes;
        bindService = new BindService(name, systemType, smppBindType, host, port, systemId, password);
        enquireLinkService = new EnquireLinkService();
        SCHEDULEDEXECUTORSERVICE = Executors.newSingleThreadScheduledExecutor();
    }

    public void setSmsReceiver(SmsListener smsListener) {
        this.smsListener = smsListener;
    }

    public void bindSession() {
        if (Objects.nonNull(bindService)) {
            SCHEDULEDEXECUTORSERVICE.execute(() -> {
                bindService.bind();
            });
            ScheduledFuture<?> enquireLinScheduledFuture = SCHEDULEDEXECUTORSERVICE
                    .scheduleAtFixedRate(enquireLinkService, 5, 5, TimeUnit.SECONDS);
            WORKER.add(enquireLinScheduledFuture);
            ScheduledFuture<?> reBindScheduledFuture = SCHEDULEDEXECUTORSERVICE.scheduleAtFixedRate(() -> {
                bindService.bind();
            }, 2, reBindTimeInMinutes, TimeUnit.MINUTES);
            WORKER.add(reBindScheduledFuture);
        }
    }

    public synchronized SmsStatus sendSms(String sender, String message, String receiver) {
        if (Objects.nonNull(smppSession)) {
            try {
                byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM);
                SubmitSm submit = new SubmitSm();
                submit.setSourceAddress(new Address((byte) 5, (byte) 9, sender));
                submit.setDestAddress(new Address((byte) 1, (byte) 1, receiver));
                submit.setShortMessage(textBytes);
                SubmitSmResp submitSmResp = smppSession.submit(submit, 10000);
                String messageId = submitSmResp.getMessageId();
                SmsStatus smsStatus = new SmsStatus(true, messageId, "Success");
                return smsStatus;
            } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                    | SmppTimeoutException | SmppChannelException ie) {
                return new SmsStatus(false, null, ie.getMessage());
            }
        } else {
            return new SmsStatus(false, null, "Session not bound");
        }

    }

    public SmsStatus sendLongSms(String sender, String receiver, String message,
            boolean requestDeliveryReceipt) {

        if (Objects.nonNull(smppSession)) {

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

                SubmitSmResp submitResp = smppSession.submit(submitMsg, 15000);
                SmsStatus status = new SmsStatus(true, submitResp.getMessageId(), "Success");
                return status;
            } catch (RecoverablePduException | SmppChannelException | SmppTimeoutException | UnrecoverablePduException | InterruptedException ex) {
                return new SmsStatus(false, null, ex.getMessage());
            }
        } else {
            return new SmsStatus(false, null, "Session not bound");
        }
    }

    //    msgType continue is 1 and end is 2
    public void sendUssd(String source, String destination, String message, int messageType, String sessionInfo) {
        try {

            byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM);
            SubmitSm submit = new SubmitSm();
            submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_NOT_REQUESTED);

            submit.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, source));
            submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, destination));
            submit.setShortMessage(textBytes);
            if (messageType == 1) {
                submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, new byte[]{0x02}, SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_USSD_SERVICE_OP)));
            } else {
                submit.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, new byte[]{0x11}, SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_USSD_SERVICE_OP)));
            }
            submit.setOptionalParameter(new Tlv(SmppConstants.TAG_ITS_SESSION_INFO, HexUtil.toByteArray(sessionInfo), SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_ITS_SESSION_INFO)));
            submit.setServiceType("USSD");
            smppSession.sendRequestPdu(submit, 10000, false);
        } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                | SmppTimeoutException | SmppChannelException ie) {
            MessageLogger.error(Session.class, "Error in SubmitSm", ie);
        }
    }

        //    msgType continue is 1 and end is 2
    public void sendDataSmUssd(String source, String destination, String message, int messageType, String sessionInfo) {
        try {

            byte[] textBytes = CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM);
            DataSm dataSm = new DataSm();
            dataSm.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_NOT_REQUESTED);

            dataSm.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, source));
            dataSm.setDestAddress(new Address((byte) 0x01, (byte) 0x01, destination));
            if (messageType == 1) {
                dataSm.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, new byte[]{0x02}, SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_USSD_SERVICE_OP)));
            } else {
                dataSm.setOptionalParameter(new Tlv(SmppConstants.TAG_USSD_SERVICE_OP, new byte[]{0x11}, SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_USSD_SERVICE_OP)));
            }
            dataSm.setOptionalParameter(new Tlv(SmppConstants.TAG_ITS_SESSION_INFO, HexUtil.toByteArray(sessionInfo), SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_ITS_SESSION_INFO)));
            dataSm.setOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD,textBytes , SmppConstants.TAG_NAME_MAP.get(SmppConstants.TAG_MESSAGE_PAYLOAD)));
           
            dataSm.setServiceType("USSD");
            smppSession.sendRequestPdu(dataSm, 10000, false);
        } catch (InterruptedException | RecoverablePduException | UnrecoverablePduException
                | SmppTimeoutException | SmppChannelException ie) {
            MessageLogger.error(Session.class, "Error in SubmitSm", ie);
        }
    }

    
    public boolean querySms() {
        return false;
    }

    public boolean getBindStatus() {
        if (Objects.isNull(smppSession)) {
            return false;
        } else {
            return this.smppSession.isBound();
        }
    }

    public void unBindSession() {
        WORKER.forEach((scheduledFuture) -> {
            scheduledFuture.cancel(true);
        });
        if (Objects.nonNull(bindService)) {
            bindService.unbind();
            SCHEDULEDEXECUTORSERVICE.shutdown();
        }
    }

    private class BindService {

        private final SmppSessionConfiguration config;
        private DefaultSmppClient smppClient;
        private ScheduledFuture<?> scheduledFuture;

        public BindService(String name, String systemType, SmppBindType smppBindType,
                String host, Integer port, String systemId, String password) {
            config = new SmppSessionConfiguration(smppBindType, systemId, password);
            config.setWindowSize(20);
            config.setName(name);
            config.setHost(host);
            config.setPort(port);
            config.setType(smppBindType);
            config.setSystemType(systemType);
            config.setConnectTimeout(20000);
            config.setRequestExpiryTimeout(30000);
            config.setWindowMonitorInterval(15000);
            config.setCountersEnabled(true);

            LoggingOptions loggingOptions = new LoggingOptions();
            loggingOptions.setLogPdu(false);
            loggingOptions.setLogBytes(false);
            config.setLoggingOptions(loggingOptions);
        }

        public void bind() {
            System.out.println("****** Entering Session Bind For " + TAG + "  ******");
            if (Objects.nonNull(smppSession)) {
                unbind();
            }
            smppClient = new DefaultSmppClient();
            try {
                DefaultSmppSessionHandler handler = new ClientSmppSessionHandler();
                smppSession = smppClient.bind(config, handler);
                System.out.println("Bind Done For " + TAG + " ");
                System.out.println("Session Info, Name: "
                        + TAG + ", Bind Type: " + smppSession.getBindType()
                        + ", IsOpen: " + smppSession.isOpen() + ", IsBound: " + smppSession.isBound());
                scheduledFuture = SCHEDULEDEXECUTORSERVICE.scheduleAtFixedRate(new EnquireLinkService(), 0, 1, TimeUnit.SECONDS);
            } catch (SmppTimeoutException | SmppChannelException
                    | UnrecoverablePduException
                    | InterruptedException ex) {
                System.out.println("Bind Fail: " + ex.getMessage());
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("****** Exiting Session Bind For " + TAG + " ******");
        }

        public void unbind() {
            if (Objects.nonNull(smppSession) && smppSession.isBound()) {
                smppSession.unbind(5000);
            }
            if (Objects.nonNull(smppClient)) {
                smppClient.destroy();
            }
            if (Objects.nonNull(scheduledFuture)) {
                scheduledFuture.cancel(true);
            }
        }

    }

    private class EnquireLinkService implements Runnable {

        @Override
        public void run() {
            if ((smppSession != null) && smppSession.isBound()) {
                try {
                    smppSession.enquireLink(new EnquireLink(), 10000L);
                } catch (RecoverablePduException | UnrecoverablePduException
                        | SmppTimeoutException | SmppChannelException
                        | InterruptedException ex) {
                    Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    public static interface SmsListener {

        public void onSms(String sender, String receiver, String message);

        public void onUssd(String sender, String receiver, String message, String sessionInfo);
    }

    private class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

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
            if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DATA_SM) {
            } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
                DeliverSm deliverSm = (DeliverSm) pduRequest;
                System.out.println(deliverSm);
                String sender = deliverSm.getSourceAddress().getAddress();
                String receiver = deliverSm.getDestAddress().getAddress();
                String message = new String(deliverSm.getShortMessage());
                String sessionInfo = "";
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
               
                 ArrayList<Tlv> tlvs = deliverSm.getOptionalParameters();
                    if (Objects.nonNull(tlvs) && (!tlvs.isEmpty())) {
                        for (Tlv tlv : tlvs) {
                            if (tlv.getTag() == SmppConstants.TAG_ITS_SESSION_INFO) {
                                sessionInfo = HexUtil.toHexString(tlv.getValue());
                            }
                        }
                    }
                
                if (Objects.nonNull(smsListener)) {
                    smsListener.onSms(sender, receiver, message);
                    smsListener.onUssd(sender, receiver, message, sessionInfo);
                }
            }
            return pduResponse;
        }

        @Override
        public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
            super.fireExpectedPduResponseReceived(pduAsyncResponse);
        }
        

    }

        public static void main(String[] args) {
            System.out.println((byte)9);
        }
}
