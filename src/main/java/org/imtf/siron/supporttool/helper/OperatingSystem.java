package org.imtf.siron.supporttool.helper;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OperatingSystem {

    public boolean isWindows(){
        String osName =  System.getProperty("os.name");
        return osName.contains("Windows");
    }


}
