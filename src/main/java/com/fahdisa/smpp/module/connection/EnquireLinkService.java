/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.connection;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prodigy4440
 */
public class EnquireLinkService implements Runnable {

    private final Logger logger;

    private final BindService bindService;

    public EnquireLinkService(BindService bindService) {
        this.bindService = bindService;
        this.logger = LoggerFactory.getLogger(EnquireLinkService.class);
    }

    public EnquireLinkService(BindService bindService, Logger logger) {
        this.bindService = bindService;
        this.logger = logger;
    }

    @Override
    public void run() {
        if ((this.bindService.getSmppSession() != null) && this.bindService.getSmppSession().isBound()) {
            try {
                this.bindService.getSmppSession().enquireLink(new EnquireLink(), 10000L);
            } catch (RecoverablePduException | UnrecoverablePduException
                    | SmppTimeoutException | SmppChannelException
                    | InterruptedException ex) {
                if(ex.getCause() instanceof SmppChannelException){
                    this.bindService.bind();
                }
                this.logger.error("Send Enquire Link Error {}", ex);
            }
        }
    }

}
