package org.imtf.siron.supporttool.filter.acceptfilter;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.file.Files;
import java.nio.file.Path;

public class AcceptLatestLimit implements FileFilter {

    private final Instant limit;

    public AcceptLatestLimit(long days) {
        this.limit = Instant.now().minus(days, ChronoUnit.DAYS);
    }

    @Override
    public boolean accept(File file) {
        try {
            Path path = file.toPath();
            FileTime fileTime = Files.getLastModifiedTime(path);
            return fileTime.toInstant().isAfter(limit);
        } catch (Exception e) {
            return false;
        }
    }
}
