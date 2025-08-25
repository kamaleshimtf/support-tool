package com.imtf.cstool.supporttool.helper.systeminfo;


import oshi.SystemInfo;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;
import oshi.software.os.OSProcess;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Component
public class OperatingSystem {

    private static final Logger log = LoggerFactory.getLogger(OperatingSystem.class);
    public String osTypeName;
    public String tr; // path splitter
    public String ts; // path separator
    public String pf; // script file
    public String rem;
    public String var;
    public String cl;
    public String osArchitecture;
    public String jvmArchitecture;
    private boolean is64bit;
    private boolean isWindows;
    @Setter
    private int execTimeout;

    public OperatingSystem() {
        this.osTypeName = "";
        // define os specific variables
        if (isWindows()) {
            this.tr = "\\";
            this.ts = ";";
            this.pf = ".bat";
            this.rem = "rem ";
            this.var = "set";
            this.cl = "call ";
        } else {
            this.tr = "/";
            this.ts = ":";
            this.pf = ".sh";
            this.rem = "# ";
            this.var = "export";
            this.cl = "ksh ";
        }
        this.is64bit = false;
        //this.isWindows = false;
        this.osArchitecture = "";
        this.jvmArchitecture = "";
        this.execTimeout = 1000; // wait 1,5 second
    }

    public boolean isWindows() {
        boolean isWindows = false;
        String osname = System.getProperty("os.name").toLowerCase();
        if (osname.contains("windows")) {
            this.osTypeName = "windows";
            isWindows = true;
        } else if (osname.equals("aix")) {
            this.osTypeName = "aix";
        } else if (osname.equals("linux")) {
            this.osTypeName = "linux";
        } else if (osname.equals("sunos")) {
            this.osTypeName = "solaris";
        } else {
            this.osTypeName = osname;
        }
        this.isWindows = isWindows;
        return isWindows;
    }

    public boolean isX64() {
        boolean is64bit = false;
        String arch = System.getProperty("os.arch").toLowerCase(); //gives JVM architecture
        if (this.isWindows()) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null); // false
        } else if (this.osTypeName.equals("sunos")) {
            is64bit = arch.equals("sparc");
        } else {
            is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
        }
        this.is64bit = is64bit;
        if (is64bit) this.osArchitecture = "x64";
        else this.osArchitecture = "x86";

        if (arch.equals("x86")) {
            this.jvmArchitecture = "jvm is x86 - 32-bit"; // windows
        } else if (arch.equals("x64")) {
            this.jvmArchitecture = "jvm is x64 - 64-bit"; // windows
        } else if (arch.equals("amd64")) {
            this.jvmArchitecture = "jvm is x64 - 64-bit"; // linux
        } else if (arch.equals("i386")) {
            this.jvmArchitecture = "jvm is x86 - 32-bit"; // linux
        } else if (arch.equals("ppc64")) {
            this.jvmArchitecture = "jvm is ppc64 - 64-bit"; // aix
        } else {
            this.jvmArchitecture = "jvm is ? [" + arch + "]";
        }


        return is64bit;
    }

    public String getApplicationPath() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            log.error("Failed to get application path", e);
            return "ERROR:Unable to determine application path";
        }
    }

    private static class Worker extends Thread {
        private final Process process;

        private Worker(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try {
                process.waitFor();
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
    }


    public Process testCommand(String command) {

        log.trace("Entering testCommand");
        String[] cmds;

        if (this.isWindows) {
            cmds = new String[]{"cmd", "/c", command};
        } else {
            cmds = new String[]{"ksh", "-c", command};
        }

        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec(cmds);

            Worker worker = new Worker(proc);
            worker.start();

            try {
                worker.join(this.execTimeout);
            } catch (InterruptedException ex) {
                worker.interrupt();
                Thread.currentThread().interrupt();
            } finally {
                proc.destroy();
            }

            if (proc.isAlive()) {
                System.out.println("Process did not finish in time.");
            } else {
                int exitCode = proc.exitValue();
                System.out.println("Process exited with code: " + exitCode);
            }

        } catch (Exception e) {
            log.warn("Error while waiting for Thread", e);
        }

        log.trace("Exiting");
        return proc;
    }

    public boolean isInstalled(String command) {
        Process proc = testCommand(command);

        if (proc == null) {
            log.warn("Process creation failed for command: {}", command);
            return false;
        }

        try {
            proc.waitFor();
            return proc.exitValue() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted while waiting for process", e);
        } catch (Exception e) {
            log.warn("Error while checking if command is installed", e);
        }

        return false;
    }

    public String installedCommand(String command, boolean debug) {
        boolean found = isInstalled(command);
        String result = "";
        if (found) {
            result = "success: command [" + command + "] was found on this system.";
        } else {
            result = "warning: command [" + command + "] was not found on this system!";
        }

        return result;
    }

    public String whichPath(String command) {
        log.trace("Entering whichPath");
        Environment w = new Environment();

        log.trace("Exiting whichPath");
        return w.whichPath(command);
    }

    public void writeEnvironment2File(String filepath) {
        CSVFileHandler csv = new CSVFileHandler(filepath);
        csv.resetFile();

        Map<String, String> environment = System.getenv();
        SortedMap<String, String> environmentSorted = new TreeMap<>(environment);

        for (Map.Entry<String, String> entry : environmentSorted.entrySet()) {
            csv.csvWriter(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, String> hardwareInfo() {
        log.trace("Entering hardwareInfo");
        // -------------------------------------------------------------------------
        HashMap<String, String> hardwareDetails = new LinkedHashMap<String, String>();

        try {
            SystemInfo si = new SystemInfo();

            oshi.software.os.OperatingSystem os = si.getOperatingSystem();

            if (os == null) throw new Exception("oshi pointer is null");
            if (os.toString() == null) throw new Exception("oshi pointer is null");

            // os
            // -------------------------------------------------------------------------
            hardwareDetails.put("os name", os.toString());

            // cpu
            // -------------------------------------------------------------------------
            HardwareAbstractionLayer hal = si.getHardware();
            hardwareDetails.put("cpu cores", "" + hal.getProcessor().getLogicalProcessors().size() + " CPU(s)");

            int core = 0;
            for (CentralProcessor.LogicalProcessor cpu : hal.getProcessor().getLogicalProcessors()) {
                hardwareDetails.put("cpu core " + core + " type", cpu.toString());
                core++;
            }

            // memory
            // -------------------------------------------------------------------------
            hardwareDetails.put("ram total", FormatUtil.formatBytes(hal.getMemory().getTotal()));
            hardwareDetails.put("ram available", FormatUtil.formatBytes(hal.getMemory().getAvailable()));
        } catch (RuntimeException e) {
            log.warn("oshi function are not available on this system. OS not supported.");

        } catch (IOException e) {
            log.warn("cannot retrieve hardware information.");

        } catch (Exception e) {
            log.warn("cannot retrieve hardware information.");
        }


        // disk info
        // -------------------------------------------------------------------------

        hardwareDetails.put("File system roots returned byFileSystemView.getFileSystemView():", "");
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] roots = fsv.getRoots();
        for (int i = 0; i < roots.length; i++) {
            hardwareDetails.put("Root: ", "" + roots[i]);
        }

        hardwareDetails.put("Home directory: ", "" + fsv.getHomeDirectory());

        hardwareDetails.put("File system roots returned by File.listRoots():", "");
        File[] f = File.listRoots();
        for (int i = 0; i < f.length; i++) {
            hardwareDetails.put("Drive: ", "" + f[i]);
            hardwareDetails.put("Display name: ", "" + fsv.getSystemDisplayName(f[i]));
            hardwareDetails.put("Is drive: ", "" + fsv.isDrive(f[i]));
            hardwareDetails.put("Is floppy: ", "" + fsv.isFloppyDrive(f[i]));
            hardwareDetails.put("Readable: ", "" + f[i].canRead());
            hardwareDetails.put("Writable: ", "" + f[i].canWrite());
            hardwareDetails.put("Total space: ", "" + f[i].getTotalSpace());
            hardwareDetails.put("Usable space: ", "" + f[i].getUsableSpace());
        }


        // lan info
        // -------------------------------------------------------------------------
        try {
            String computername = InetAddress.getLocalHost().getHostName();
            NetworkTools net = new NetworkTools();
            hardwareDetails.put("computername", computername);
            hardwareDetails.put("getAddress", net.getIpAddress());
            hardwareDetails.put("getCanonicalHostName", "" + InetAddress.getLocalHost().getCanonicalHostName());
            hardwareDetails.put("getHostAddress", "" + InetAddress.getLocalHost().getHostAddress());
            hardwareDetails.put("isLoopbackAddress", "" + InetAddress.getLocalHost().isLoopbackAddress());
        } catch (UnknownHostException e) {
            log.warn("can't get hostname");
        }

        // jvm info
        // -------------------------------------------------------------------------
        hardwareDetails.put("jvm cores", "" + Runtime.getRuntime().availableProcessors());
        hardwareDetails.put("jvm free memory (bytes)", "" + Runtime.getRuntime().freeMemory());
        long maxMemory = Runtime.getRuntime().maxMemory();
        hardwareDetails.put("jvm maximum memory (bytes): ", "" + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
        hardwareDetails.put("jvm total memory (bytes): ", "" + Runtime.getRuntime().totalMemory());

        // system properties
        // -------------------------------------------------------------------------
        Properties pr = System.getProperties();
        for (Map.Entry<Object, Object> e : new TreeMap<Object, Object>(pr).entrySet()) {
            if (!"line.separator".equals(e.getKey()))
                hardwareDetails.put(e.getKey().toString(), ":" + e.getValue());
        }

        // os process infos
        // -------------------------------------------------------------------------

        log.trace("Exiting hardwareInfo");
        return hardwareDetails;
    }

    public String checkVariable2String(String variable) {
        String result = "";
        String val = System.getenv(variable);
        result = variable + "=" + val;
        return result;
    }

    private BufferedReader getWindowsProcesses() throws IOException {
        log.trace("Entering getWindowsProcesses");
        BufferedReader input = null;


        Process p = Runtime.getRuntime().exec
                (System.getenv("windir") + "\\system32\\" + "tasklist.exe");
        input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        log.trace("Exiting getWindowsProcesses");
        return input;
    }

    private BufferedReader getLinuxProcesses() throws IOException {
        log.trace("Entering getLinuxProcesses");
        BufferedReader input = null;

        Process p = Runtime.getRuntime().exec("ps -e");
        input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        log.trace("Exiting getLinuxProcesses");
        return input;
    }

    public BufferedReader showProcesses() {
        log.info("Collecting Tasklist ...");

        this.setExecTimeout(10000); // wait 2 seconds

        try {
            // choose os (windows/unix)
            if (isWindows()) {
                log.info("on Windows ... running tasklist.exe");
                return this.getWindowsProcesses();
            } else {
                log.info("on Linux ... running ps -e");
                return this.getLinuxProcesses();
            }

        } catch (IOException e) {
            log.warn("Error getting Processes!", e);
        }
        return null;
    }

    private BufferedReader getWindowsMemory() throws IOException {
        log.trace("Entering getWindowsMemory");

        SystemInfo systemInfo = new SystemInfo();
        oshi.software.os.OperatingSystem os = systemInfo.getOperatingSystem();

        // ✅ Proper method signature: getProcesses(Predicate, Comparator, limit)
        List<OSProcess> processes = os.getProcesses(
                p -> true, // no filtering
                Comparator.comparing(OSProcess::getName, Comparator.nullsLast(String::compareToIgnoreCase)),
                Integer.MAX_VALUE // no limit
        );

        String hostname = InetAddress.getLocalHost().getHostName();

        StringBuilder sb = new StringBuilder();
        sb.append("Node,Name,PeakWorkingSetSize").append(System.lineSeparator());

        for (OSProcess process : processes) {
            sb.append(hostname)
                    .append(',')
                    .append(process.getName())
                    .append(',')
                    .append(process.getResidentSetSize()) // in bytes
                    .append(System.lineSeparator());
        }

        log.trace("Exiting getWindowsMemory");
        return new BufferedReader(new StringReader(sb.toString()));
    }

    private BufferedReader getLinuxMemory() throws IOException {
        log.trace("Entering getLinuxMemory");

        SystemInfo systemInfo = new SystemInfo();
        oshi.software.os.OperatingSystem os = systemInfo.getOperatingSystem();

        List<OSProcess> processes = os.getProcesses(
                p -> true, // no filtering
                Comparator.comparing(OSProcess::getName, Comparator.nullsLast(String::compareToIgnoreCase)),
                Integer.MAX_VALUE // no limit
        );

        String hostname = InetAddress.getLocalHost().getHostName();

        StringBuilder sb = new StringBuilder();
        sb.append("Node,Name,PeakWorkingSetSize").append(System.lineSeparator());

        for (OSProcess process : processes) {
            sb.append(hostname)
                    .append(',')
                    .append(process.getName())
                    .append(',')
                    .append(process.getResidentSetSize()) // bytes
                    .append(System.lineSeparator());
        }

        log.trace("Exiting getLinuxMemory");
        return new BufferedReader(new StringReader(sb.toString()));
    }

    public BufferedReader showMemoryUsed() {
        log.trace("Entering showMemoryUsed()");

        BufferedReader result;

        if (isWindows()) {
            try {
                result = this.getWindowsMemory();
                log.trace("Exiting showMemoryUsed() successfully [Windows]");
                return result;
            } catch (Exception e) {
                log.warn("Error executing memory command on Windows", e);
            }
        } else {
            try {
                result = this.getLinuxMemory();
                log.trace("Exiting showMemoryUsed() successfully [Linux]");
                return result;
            } catch (Exception e) {
                log.warn("Error executing memory command on Linux", e);
            }
        }

        log.warn("showMemoryUsed() returning NULL due to failure");
        log.trace("Exiting showMemoryUsed() with null");
        return null;
    }

}
