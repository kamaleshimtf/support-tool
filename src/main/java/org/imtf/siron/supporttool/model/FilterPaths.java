package com.imtf.cstool.supporttool.model;

import com.imtf.cstool.supporttool.filter.AcceptAllFilter;
import lombok.Getter;

import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public class FilterPaths {

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
