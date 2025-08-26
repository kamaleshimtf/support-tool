package org.imtf.siron.supporttool.helper.systeminfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVFileHandler implements CSVFileOperations {

    protected String fileName;
    protected String separator = ";";

    public CSVFileHandler() {
        this.fileName = "";
        this.separator = ";";
    }

    public CSVFileHandler(String fileName) {
        this.fileName = fileName;
        this.separator = ";";
    }

    public void resetFile() {

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.fileName, false));
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void createFile(String fileName) {
        try {
            this.fileName = fileName;
            File file = new File(this.fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.out.print("I/O Exception: \n" + e.getMessage());
            throw new RuntimeException(e);
        }


    }

    public void csvWriter(String key, String value) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.fileName, true));
            writer.println(key + separator + value);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLine(String value) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.fileName, true));
            writer.println(value);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
