package com.imtf.cstool.supporttool.filter.rejectfilter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class RejectFolderName implements FileFilter {

    private Pattern folderNamePattern;

    public RejectFolderName(Pattern folderNamePattern){
        this.folderNamePattern = folderNamePattern;
    }

    @Override
    public boolean accept(File pathname) {
       return !folderNamePattern.matcher(pathname.getPath().toUpperCase()).find();
    }
}
