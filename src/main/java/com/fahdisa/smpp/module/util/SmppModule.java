/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.util;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppBindType;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author prodigy4440
 */
public class SmppModule {

    //Test
    public static void main(String[] args) throws InterruptedException {
//        Session session = new Session("Bind tag","SystemId", "System Password",
//                SmppBindType.TRANSMITTER, "ip", 8080);
//        
//        TimeUnit.SECONDS.sleep(10);
//        session.bindSession();
//        System.out.println("Session Status: " + session.getBindStatus());
//        TimeUnit.SECONDS.sleep(10);
//        session.unBindSession();
//        MobileNetwork.Network phone = Telcoms.getInstance().getNetwork("08131631151");
//        System.out.println(phone);
        byte[] message = CharsetUtil.encode("Sample | message", CharsetUtil.CHARSET_UTF_8);
        System.out.println(new String(message));
        System.out.println(CharsetUtil.decode(message, CharsetUtil.CHARSET_GSM));
        System.out.println(CharsetUtil.decode(message, CharsetUtil.CHARSET_GSM7));
        System.out.println(CharsetUtil.decode(message, CharsetUtil.CHARSET_UTF_8));
        System.out.println(new String(message));
    }

}
