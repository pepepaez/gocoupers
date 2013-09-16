package com.coupers.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 7/24/13.
 */
public class CoupersObject {
    private String NAMESPACE = "http://tempuri.org/";
    private String SOAP_ACTION;
    private String URL;
    private String METHOD_NAME;
    private ArrayList<String> parameterKey = new ArrayList<String>();
    private ArrayList<String> parameterValue = new ArrayList<String>();
    private String tag[];

    public CoupersObject(String method_name){
        NAMESPACE = "http://tempuri.org/";
        SOAP_ACTION = NAMESPACE + method_name;
        URL = "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx";
        METHOD_NAME = method_name;
    }

    public void setTag(String tags[]){
        tag = tags;
    }

    public String[] getTag(){
        return tag;
    }

    public void addParameter(String key, String value){
        parameterKey.add(key);
        parameterValue.add(value);
    }

    public void removeAllParameters(){
        parameterKey.clear();
        parameterValue.clear();
    }

    public String getParameterKey(int index){
        return  parameterKey.get(index);
    }

    public String getParameterValue(int index){
        return parameterValue.get(index);
    }

    public int getParameterCount(){
        return parameterKey.size();
    }

    public String getNAMESPACE(){
        return  NAMESPACE;
    }

    public String getSOAP_ACTION(){
        return  SOAP_ACTION;
    }

    public  String getURL(){
        return URL;
    }

    public String getMETHOD_NAME(){
        return METHOD_NAME;
    }

}
