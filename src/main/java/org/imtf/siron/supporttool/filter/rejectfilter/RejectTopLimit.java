package org.imtf.siron.supporttool.filter.rejectfilter;

import org.imtf.siron.supporttool.filter.acceptfilter.AcceptFolderName;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RejectTopLimit implements FileFilter {

    private final String basePath;
    private final String prefix;
    private final int limit;

    private List<String> allowed;

    public RejectTopLimit(String basePath, String prefix, int limit) {
        this.basePath = basePath;
        this.prefix = prefix;
        this.limit = limit;
        this.allowed = loadAllowed();
    }

    private List<String> loadAllowed() {
        try (Stream<Path> stream = Files.list(Paths.get(basePath))) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(prefix))
                    .sorted(Comparator.reverseOrder())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error filtering top folders in " + basePath, e);
        }
    }

    @Override
    public boolean accept(File pathname) {
        File parent = pathname.getParentFile();
        if (parent == null) {
            return false;
        }

        String parentName = parent.getName();

        return allowed.contains(parentName);
    }
}
