package org.imtf.siron.supporttool.filter.acceptfilter;



import org.imtf.siron.supporttool.constant.UnwantedFileExtension;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFileName;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFolderName;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FilterChainAnd implements FileFilter {

    private List<FileFilter> filters = new ArrayList<>();
    private List<Pattern> folderFilter;
    private List<Pattern> fileFilter;
    private UnwantedFileExtension unwantedFileExtension = new UnwantedFileExtension();

    public FilterChainAnd(List<Pattern> folderFilter, List<Pattern> fileFilter) {
        this.folderFilter = folderFilter;
        this.addFolderFilter();
        this.fileFilter = fileFilter;
        this.addFileFilter();
        this.filters.addAll(unwantedFileExtension.getFilters());
    }

    public FilterChainAnd addFilter(FileFilter filter) {
        this.filters.add(filter);
        return this;
    }

    public void addFolderFilter(){
        for(Pattern pattern : this.folderFilter){
            this.addFilter(new RejectFolderName(pattern));
        }
    }

    public void addFileFilter(){
        for(Pattern pattern : this.fileFilter){
            this.addFilter(new RejectFileName(pattern));
        }
    }

    @Override
    public boolean accept(File filePath){

        for (FileFilter filter : this.filters) {
            if (!filter.accept(filePath)) {
                return false;
            }
        }
        return true;
    }


}
