/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.digitalpulse.smpp.module.util;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 *
 * @author prodigy4440
 */
public class JsonUtil {

  public static <T> T fromJson(InputStream is, Class<T> aClass) {
    Gson gson = new Gson();
    return gson.fromJson(new BufferedReader(new InputStreamReader(is)), aClass);
  }

  public static <T> T fromJson(String json, Class<T> classOf) {
    Gson gson = new Gson();
    return gson.fromJson(json, classOf);
  }

  public static <T> T fromJson(String json, Type typeOf) {
    Gson gson = new Gson();
    return gson.fromJson(json, typeOf);
  }

  public static String toJson(Object src) {
    Gson gson = new Gson();
    return gson.toJson(src);
  }

  public static String toJson(Object src, Type type) {
    Gson gson = new Gson();
    return gson.toJson(src, type);
  }
  
}
