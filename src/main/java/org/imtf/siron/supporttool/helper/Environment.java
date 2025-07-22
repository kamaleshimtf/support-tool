package org.imtf.siron.supporttool.helper;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class Environment {

    public Map<String, String> getEnvironment(){
        return System.getenv();
    }

    public String getProperty(String key){
        return System.getProperty("KYC_ROOT");
    }
}
