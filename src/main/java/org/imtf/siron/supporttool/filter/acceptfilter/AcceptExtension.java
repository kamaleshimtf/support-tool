package org.imtf.siron.supporttool.filter.acceptfilter;

import java.io.File;
import java.io.FileFilter;

public class AcceptExtension implements FileFilter {

    private final String extension;

    public AcceptExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            throw new IllegalArgumentException("Extension cannot be null or empty");
        }
        this.extension = extension.toLowerCase().startsWith(".")
                ? extension.toLowerCase()
                : "." + extension.toLowerCase();
    }

    @Override
    public boolean accept(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(extension);
    }
}
