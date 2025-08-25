package org.imtf.siron.supporttool.helper.systeminfo;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkTools {

    public String getIpAddress() {
        String ipAddress = "";
        try {
            byte[] rawBytes = InetAddress.getLocalHost().getAddress();
            int i = 4;
            ipAddress = "";
            for (byte raw : rawBytes) {
                ipAddress += (raw & 0xFF);
                if (--i > 0) {
                    ipAddress += ".";
                }
            }
            return ipAddress;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

}
