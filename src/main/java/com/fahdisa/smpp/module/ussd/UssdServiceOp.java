/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.ussd;

/**
 *
 * @author prodigy4440
 */
public enum UssdServiceOp {

    //0 dec
    PSSD_IND(new byte[]{0x00}), 
    //1 dec
    PSSR_IND(new byte[]{0x01}), 
    //2 dec
    USSR_REQ(new byte[]{0x02}),
    //3 dec
    USSN_REQ(new byte[]{0x03}),
    
    //4-15 are reserved
    
    //16 dec
    PSSD_RES(new byte[]{0x10}), 
    //17 dec
    PSSR_RES(new byte[]{0x11}), 
    //18 dec
    USSR_CNF(new byte[]{0x12}),
    //19 dec
    USSN_CNF(new byte[]{0x13});
  
    //20-31 reserved
    //32- 255 reserved for vendor specific
    
    UssdServiceOp(byte[] value) {
        this.value = value;
    }

    private final byte [] value;

    public byte[] getValue() {
        return value;
    }
}
