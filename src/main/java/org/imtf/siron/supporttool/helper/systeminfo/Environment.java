package com.imtf.cstool.supporttool.helper.systeminfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.StringTokenizer;

public class Environment {

    private int foundCount;
    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    public String foundCommandStr(String mySearchPath, String myCommand) {
        String result = "";
        boolean found = false;
        int res = 0;

        String osname = System.getProperty("os.name").toLowerCase();
        if (osname.contains("windows")) {
            // check on windows - for all extensions
            String[] extensions = {"", ".com", ".exe", ".bat", ".cmd"};
            for (int i = 0; i < extensions.length; i++) {
                String search = mySearchPath + "\\" + myCommand + extensions[i];
                found = new File(search).exists();
                if (found) {
                    res++;
                    result = result + "  found in " + search + "\n";
                }
            }
        } else {
            // check on unix
            String search = mySearchPath + "/" + myCommand;
            found = new File(search).exists();
            if (found) {

                res++;
                result = result + "  found in " + search + "\n";
            }
        }

        this.foundCount = res;
        return result;
    }

    public String whichPath(String command) {
        String result = "";
        try {
            boolean isWindows = false;
            String mypath = System.getenv("PATH");
            String osname = System.getProperty("os.name").toLowerCase();
            if (mypath == null) {
                result += "Environment variable PATH seems to be empty!\n";
                return result;
            }


            if (osname.contains("windows")) {
                isWindows = true;
            }

            String deli = "";
            if (isWindows) {
                deli = ";"; // path separator for %PATH% on windows
            } else {
                deli = ":"; // path separator for $PATH on UNIX
            }

            result += "which for command '" + command + "'\n";

            int found = 0;
            StringTokenizer st = new StringTokenizer(mypath, deli);
            while (st.hasMoreTokens()) {
                String val = st.nextToken();
                //found += foundCommand(val, command, false);
                result += foundCommandStr(val, command);
                //found++;
                found += this.foundCount;
            }

            if (found > 0) {
                result += command + " was found " + found + " times.\n";
            } else {
                result += command + " was not found on this system\n";
            }
            /**/
        }
        catch (Exception e) {
            logger.warn("Error While executing command '" + command + "'!", e);
        }

        return result;
    }
}
