/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module;

import com.cloudhopper.smpp.SmppBindType;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author prodigy4440
 */
public class SmppModule {

    //Test
    public static void main(String[] args) throws InterruptedException {
//        Session session = new Session("Session Name","systemid", "password",
//                SmppBindType.TRANSMITTER, "ip-address", 38880);
//        
//        TimeUnit.SECONDS.sleep(10);
//        session.bindSession();
//        System.out.println("Session Status: " + session.getBindStatus());
//        TimeUnit.SECONDS.sleep(10);
//        session.unBindSession();
        MobileNetwork.Network phone = Telcoms.getInstance().getNetwork("08131631151");
        System.out.println(phone);
    }

}
