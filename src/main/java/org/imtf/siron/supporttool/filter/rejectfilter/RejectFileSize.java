package com.imtf.cstool.supporttool.filter.rejectfilter;

import java.io.File;
import java.io.FileFilter;

public class RejectFileSize implements FileFilter {
    private long fileSize;

    public RejectFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean accept(File pathname) {
        return !(pathname.isFile() && pathname.length() >= fileSize);
    }
}
