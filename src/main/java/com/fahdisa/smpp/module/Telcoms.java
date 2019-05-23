/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author prodigy4440
 */
public class Telcoms {

    private static final List<MobileNetwork> MOBILE_NETWORKS = new ArrayList<>();

    private static final Telcoms TELCOMS = new Telcoms();

    private Telcoms() {
        MOBILE_NETWORKS.add(new MobileNetwork.Builder()
                .setName("MTN Nigeria").setId("62102")
                .setNetwork(MobileNetwork.Network.MTN)
                .addPrefix("703").addPrefix("706").addPrefix("803")
                .addPrefix("806").addPrefix("810").addPrefix("813")
                .addPrefix("814").addPrefix("816").addPrefix("903")
                .addPrefix("906")
                .build());
        MOBILE_NETWORKS.add(new MobileNetwork.Builder()
                .setName("Globacom Nigeria").setId("62101")
                .setNetwork(MobileNetwork.Network.GLO)
                .addPrefix("705").addPrefix("801").addPrefix("805")
                .addPrefix("807").addPrefix("811").addPrefix("815")
                .addPrefix("905")
                .build());
        MOBILE_NETWORKS.add(new MobileNetwork.Builder()
                .setName("Airtel Nigeria").setId("62103")
                .setNetwork(MobileNetwork.Network.AIRTEL)
                .addPrefix("701").addPrefix("708").addPrefix("802")
                .addPrefix("808").addPrefix("812").addPrefix("902")
                .addPrefix("907")
                .build());
        MOBILE_NETWORKS.add(new MobileNetwork.Builder()
                .setName("Etisalat Nigeria").setId("62104")
                .setNetwork(MobileNetwork.Network.ETISALAT)
                .addPrefix("809").addPrefix("817").addPrefix("818")
                .addPrefix("908").addPrefix("909")
                .build());
    }

    public static Telcoms getInstance() {
        return TELCOMS;
    }

    public List<MobileNetwork> getMobileNetworks() {
        return Telcoms.MOBILE_NETWORKS;
    }

    public MobileNetwork.Network getNetwork(String phoneNumber) {

        if (Objects.isNull(phoneNumber) || phoneNumber.isEmpty()
                || (phoneNumber.length() < 10) || phoneNumber.length() > 15) {
            return MobileNetwork.Network.UNKNOWN;
        } else {
                String operator = phoneNumber.substring(phoneNumber.length() - 10, phoneNumber.length() - 7);
            for (MobileNetwork mobileNetwork : MOBILE_NETWORKS) {
                for (String prefix : mobileNetwork.getPrefixes()) {
                    if (Objects.equals(prefix, operator)) {
                        return mobileNetwork.getNetwork();
                    }
                }
            }
            return MobileNetwork.Network.UNKNOWN;
        }
    }

    public String formatPhoneNumber(String phoneNumber) {
        if (Objects.isNull(phoneNumber) || phoneNumber.isEmpty()
                || (phoneNumber.length() < 10) || phoneNumber.length() > 15) {
            return null;
        } else {
            if (phoneNumber.length() == 13) {
                return phoneNumber;
            } else {
                String identifier = phoneNumber.substring(phoneNumber.length() - 7);
                String operator = phoneNumber.substring(phoneNumber.length() - 10, phoneNumber.length() - 7);
                String country = "234";
                return country.concat(operator).concat(identifier);
            }

        }
    }

}
