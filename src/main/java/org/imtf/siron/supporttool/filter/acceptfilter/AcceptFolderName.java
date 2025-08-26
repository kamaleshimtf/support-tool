package org.imtf.siron.supporttool.filter.acceptfilter;

import java.io.File;
import java.io.FileFilter;

public class AcceptFolderName implements FileFilter {

    private final String folderName;

    public AcceptFolderName(String folderName) {
        this.folderName = folderName;
    }


    @Override
    public boolean accept(File pathname) {

       return pathname.isDirectory() && pathname.getPath().contains(folderName);
    }
}
