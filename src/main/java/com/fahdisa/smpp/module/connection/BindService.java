/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.connection;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.type.LoggingOptions;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fahdisa.smpp.module.handler.ClientSmppSessionHandler;
import com.fahdisa.smpp.module.handler.SmsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prodigy4440
 */
public class BindService {
    
    private final Logger logger = LoggerFactory.getLogger(BindService.class);
    
    private final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    
    private final SmppSessionConfiguration config;
    private DefaultSmppClient smppClient;
    private SmppSession smppSession;
    private final ClientSmppSessionHandler clientSmppSessionHandler;
    
    private final String TAG;
    
    public BindService(String name, String systemId, String password, String systemType,
            SmppBindType smppBindType, String host, Integer port, Integer connectionTimeout) {
        
       this.TAG = name;
        
        config = new SmppSessionConfiguration(smppBindType, systemId, password);
        config.setWindowSize(100);
        config.setName(name);
        config.setHost(host);
        config.setPort(port);
        config.setType(smppBindType);
        config.setSystemType(systemType);
        config.setConnectTimeout(connectionTimeout);
        config.setBindTimeout(connectionTimeout);
        config.setRequestExpiryTimeout(connectionTimeout);
        config.setWindowMonitorInterval(15000);
        config.setCountersEnabled(true);

        LoggingOptions loggingOptions = new LoggingOptions();
        loggingOptions.setLogPdu(false);
        loggingOptions.setLogBytes(false);
        config.setLoggingOptions(loggingOptions);
        
        clientSmppSessionHandler = new ClientSmppSessionHandler(this);
    }
    
    public void setSmsListener(SmsListener smsListener){
        if(Objects.nonNull(clientSmppSessionHandler)){
            clientSmppSessionHandler.setSmsListener(smsListener);
        }
    }
    
    public SmppSession getSmppSession(){
        return this.smppSession;
    }

    public void bind() {
        logger.info("****** Entering Session Bind For {}  ******", TAG);
        if (Objects.nonNull(smppSession)) {
            unbind();
        }
        this.smppClient = new DefaultSmppClient();
        try {
            smppSession = smppClient.bind(config, clientSmppSessionHandler);

            logger.info("Bind Successful, TAG: {}, Bind Type: {}, IsOpen: {}, IsBound: {}",
                    TAG, smppSession.getBindType(), smppSession.isOpen(), smppSession.isBound());

            scheduledFuture = SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                    new EnquireLinkService(this), 0, 2, TimeUnit.SECONDS);
        } catch (SmppTimeoutException | SmppChannelException
                | UnrecoverablePduException
                | InterruptedException ex) {
            logger.error("Bind Error {}", ex);
        }
        logger.info("****** Exiting Session Bind For {} ******", TAG);
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
