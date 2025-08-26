package org.imtf.siron.supporttool.filter.rejectfilter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class RejectFileName implements FileFilter {

    private final Pattern rejectFileNamePattern;

    public RejectFileName(Pattern rejectFileNamePattern){
        this.rejectFileNamePattern = rejectFileNamePattern;
    }

    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        String nameWithoutExtension = name.contains(".")
                ? name.substring(0, name.lastIndexOf('.'))
                : name;
        return rejectFileNamePattern.matcher(nameWithoutExtension).matches();
    }
}
