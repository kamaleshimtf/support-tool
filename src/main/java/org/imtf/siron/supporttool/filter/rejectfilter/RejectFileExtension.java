package org.imtf.siron.supporttool.filter.rejectfilter;

import java.io.File;
import java.io.FileFilter;

public class RejectFileExtension implements FileFilter {

    private final String rejectExtension;

    public RejectFileExtension(String rejectExtension){
        this.rejectExtension = rejectExtension;
    }

    @Override
    public boolean accept(File pathname) {

        if (pathname.isDirectory()){
            return true;
        }
        return !pathname.getAbsolutePath().toLowerCase().endsWith(this.rejectExtension);
    }
}
