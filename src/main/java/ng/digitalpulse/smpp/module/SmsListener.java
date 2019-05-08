/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module;

/**
 *
 * @author prodigy4440
 */
public interface SmsListener {
    public void onSms(String sender, String receiver, String message);
    public void onUssd(String sender, String receiver, String message, String meta);
}
