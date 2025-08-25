package org.imtf.siron.supporttool.helper.systeminfo;

/**
 * Methods for working with CSV files
 *
 * @author markus
 */
public interface CSVFileOperations {

    public void resetFile();

    public void createFile(String filename);

    public void csvWriter(String key,String value);

    public void writeLine(String value);

}