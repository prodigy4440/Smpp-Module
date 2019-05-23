/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module.util;

import org.slf4j.LoggerFactory;

/**
 *
 * @author prodigy4440
 */
public class MessageLogger {

    public static void info( Class clazz, String message) {
        LoggerFactory.getLogger(clazz).info(message);
    }

    public static void info(Class clazz, String message, Throwable throwable) {
        LoggerFactory.getLogger(clazz).info(message, throwable);
    }

    public static void warn(Class clazz, String message ) {
        LoggerFactory.getLogger(clazz).warn(message);
    }

    public static void warn(Class clazz, String message, Throwable throwable) {
        LoggerFactory.getLogger(clazz).warn(message, throwable);
    }

    public static void error(Class clazz, String message) {
        LoggerFactory.getLogger(clazz).error(message);
    }
    
    public static void error(Class clazz, String message,Throwable throwable) {
        LoggerFactory.getLogger(clazz).error(message, throwable);
    }

    public static void debug(Class clazz, String message) {
        LoggerFactory.getLogger(clazz).debug(message);
    }
    
    public static void debug(Class clazz, String message,Throwable throwable) {
        LoggerFactory.getLogger(clazz).debug(message, throwable);
    }
}
