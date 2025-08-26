package org.imtf.siron.supporttool.filter.acceptfilter;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

public class AcceptAllFilter implements FileFilter {

    private List<Pattern> folderFilter;
    private List<Pattern> fileFilter;

    public AcceptAllFilter(List<Pattern> folderFilter, List<Pattern> fileFilter) {
        this.folderFilter = folderFilter;
        this.fileFilter = fileFilter;
    }

    @Override
    public boolean accept(File filePath){
        return true;
    }
}
