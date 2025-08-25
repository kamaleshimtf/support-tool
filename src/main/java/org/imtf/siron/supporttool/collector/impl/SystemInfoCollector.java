package com.imtf.cstool.supporttool.collector;

import com.imtf.cstool.supporttool.helper.systeminfo.CSVFileHandler;
import com.imtf.cstool.supporttool.helper.systeminfo.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class SystemInfoCollector {

    private static final Logger log = LoggerFactory.getLogger(SystemInfoCollector.class);

    @Autowired
    OperatingSystem operatingSystem;

    public String exportSystemInfo(String destination_folder_path) {

        log.trace("This is the destination_folder_path path:{}", destination_folder_path);

        String dirname = destination_folder_path;
        File dst_folder = new File(destination_folder_path);
        if (!dst_folder.exists()) {
            try {
                Files.createDirectories(Paths.get(dirname));
            } catch (IOException e) {
                log.warn("Error creating SystemInfo folder!", e);
            }
        }

        log.info("sysInfo -part I");
        CSVFileHandler csv1 = new CSVFileHandler();
        csv1.createFile(dirname + operatingSystem.tr + "sysinfo.txt");
        String myPath = operatingSystem.getApplicationPath();
        csv1.csvWriter("Current Path:", myPath);
        operatingSystem.isWindows();
        csv1.csvWriter("Operating System Type is:", operatingSystem.osTypeName);
        operatingSystem.isX64();
        csv1.csvWriter("OS Architecture:", operatingSystem.osArchitecture);
        csv1.csvWriter("JVM Architecture:", operatingSystem.jvmArchitecture);


        // -----------------------------//


        log.info("SysInfo - part II");
        CSVFileHandler csv2 = new CSVFileHandler();
        csv2.createFile(dirname + operatingSystem.tr + "commands_installed.txt");
        csv2.writeLine("show which possible needed command line utilities are existing");
        csv2.writeLine("---------------------------------------------------------------");

        operatingSystem.setExecTimeout(500);
        boolean debug = true;
        csv2.writeLine(operatingSystem.installedCommand("sqlplus -V", debug));
        csv2.writeLine(operatingSystem.installedCommand("sqlldr", debug));

        // ms_sql
        csv2.writeLine(operatingSystem.installedCommand("sqlcmd -?", debug));
        csv2.writeLine(operatingSystem.installedCommand("bcp -v", debug));

        // db2
        csv2.writeLine(operatingSystem.installedCommand("db2level", debug));

        // other
        csv2.writeLine(operatingSystem.installedCommand("find /?", debug));
        csv2.writeLine(operatingSystem.installedCommand("bash --version", debug));
        csv2.writeLine(operatingSystem.installedCommand("ksh --version", debug));
        csv2.writeLine(operatingSystem.installedCommand("cmd", debug));


        // -----------------------------//


        log.info("SysInfo - part III");
        CSVFileHandler csv3 = new CSVFileHandler();
        csv3.createFile(dirname + operatingSystem.tr + "commands_found.txt");
        csv2.writeLine("show which possible needed command line utilities are existing");
        csv3.writeLine("------------------");

        // java
        csv3.writeLine(operatingSystem.whichPath("java"));
        csv3.writeLine(operatingSystem.whichPath("javac"));

        // dbms:
        csv3.writeLine(operatingSystem.whichPath("sqlcmd"));
        csv3.writeLine(operatingSystem.whichPath("bcp"));

        // dbms:
        csv3.writeLine(operatingSystem.whichPath("sqlplus"));
        csv3.writeLine(operatingSystem.whichPath("tnsping"));
        csv3.writeLine(operatingSystem.whichPath("sqlldr"));

        // dbms:
        csv3.writeLine(operatingSystem.whichPath("db2"));
        csv3.writeLine(operatingSystem.whichPath("db2level"));

        // os: unix
        csv3.writeLine(operatingSystem.whichPath("ksh"));
        csv3.writeLine(operatingSystem.whichPath("bash"));
        csv3.writeLine(operatingSystem.whichPath("sh"));

        // os: windows
        csv3.writeLine(operatingSystem.whichPath("cmd"));
        csv3.writeLine(operatingSystem.whichPath("find"));


        // -----------------------------//


        log.info("SysInfo - part IV");
        String env = dirname + operatingSystem.tr + "environment.txt";
        operatingSystem.writeEnvironment2File(env);
        log.info("file [" + env + "] created.");


        // -----------------------------//


        log.info("SysInfo - part V");
        CSVFileHandler csv4 = new CSVFileHandler();
        csv4.createFile(dirname + operatingSystem.tr + "hardware.txt");
        csv4.writeLine("------------------");
        log.info("hardwareInfo");

        Map<String, String> map = operatingSystem.hardwareInfo();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            csv4.writeLine(entry.getKey() + " " + entry.getValue());
        }

        csv4.writeLine("------------------");


        // check environment variables
        csv4.writeLine(operatingSystem.checkVariable2String("ORACLE_HOME"));
        csv4.writeLine(operatingSystem.checkVariable2String("ANT_HOME"));
        csv4.writeLine(operatingSystem.checkVariable2String("JAVA_HOME"));
        csv4.writeLine(operatingSystem.checkVariable2String("JAVA_OPTS"));
        csv4.writeLine(operatingSystem.checkVariable2String("NLS_LANG"));
        csv4.writeLine(operatingSystem.checkVariable2String("NLS_SORT"));
        csv4.writeLine(operatingSystem.checkVariable2String("DB2CODEPAGE"));


        // -----------------------------//


        log.info("SysInfo - part VI");
        log.info("get process list");
        CSVFileHandler csv5 = new CSVFileHandler();
        csv5.createFile(dirname + operatingSystem.tr + "processes.txt");

        String line = "";
        BufferedReader procs = operatingSystem.showProcesses();

        try {
            while ((line = procs.readLine()) != null) {
                csv5.writeLine(line);
            }
        } catch (IOException e) {
            log.warn("IO Exception while reading Processes!", e);
        }


        // -----------------------------//


        log.info("SysInfo - part VII");
        log.info("get process memory");
        CSVFileHandler csv6 = new CSVFileHandler();
        csv6.createFile(dirname + operatingSystem.tr + "memory_used.txt");

        line = "";
        BufferedReader ram = operatingSystem.showMemoryUsed();
        try {
            while ((line = ram.readLine()) != null) {
                if (line.length() > 0) {
                    csv6.writeLine(line);
                }
            }
        } catch (IOException e) {
            log.warn("IO Exception while reading Memory!", e);
        }

        log.info("SystemInfo - end");
        log.trace("Exiting exportSystemInfo ");

        return destination_folder_path;
    }
}
