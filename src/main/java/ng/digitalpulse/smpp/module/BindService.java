/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module;

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
            SmppBindType smppBindType, String host, Integer port) {
        
        TAG = name;
        
        config = new SmppSessionConfiguration(smppBindType, systemId, password);
        config.setWindowSize(20);
        config.setName(name);
        config.setHost(host);
        config.setPort(port);
        config.setType(smppBindType);
        config.setSystemType(systemType);
        config.setConnectTimeout(45000);
        config.setRequestExpiryTimeout(45000);
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
            
            System.out.println("Bind Done For " + TAG + " ");
            System.out.println("Session Info, Name: "
                    + TAG + ", Bind Type: " + smppSession.getBindType()
                    + ", IsOpen: " + smppSession.isOpen() + ", IsBound: " + smppSession.isBound());
            
            scheduledFuture = SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                    new EnquireLinkService(smppSession), 0, 1, TimeUnit.SECONDS);
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
