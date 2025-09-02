package org.imtf.siron.supporttool.model;


import org.imtf.siron.supporttool.filter.acceptfilter.AcceptAllFilter;

import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

public class FilterPaths {

    public FileFilter getFilter() {
        return filter;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    private final FileFilter filter;
    private final String destinationPath;

    public FilterPaths(String destinationPath, FileFilter filter) {
        this.filter = filter;
        this.destinationPath = destinationPath;
    }

    public FilterPaths(String destinationPath, List<Pattern> fileFilters, List<Pattern> folderFilters) {
        this.destinationPath = destinationPath;
        this.filter = new AcceptAllFilter(folderFilters,fileFilters);
    }


}
