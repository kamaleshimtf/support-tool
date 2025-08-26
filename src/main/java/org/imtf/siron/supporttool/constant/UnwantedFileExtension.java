package org.imtf.siron.supporttool.constant;


import lombok.Getter;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFileExtension;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFileName;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFileSize;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFolderName;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public class UnwantedFileExtension {

    private final List<FileFilter> filters = new ArrayList<>();

    private static final List<String> FILE_EXTENSIONS = List.of(
            ".jar", ".ear", ".example", ".template", ".pdf", ".exe", ".bmp", ".jpg", ".jpeg", ".jfr",
            ".part", ".htm", ".ttf", ".war", ".ddl", ".pse", ".pem", ".key", ".cer", ".crt", ".p12", ".pfx", ".der", ".dat"
    );

    private static final List<String> FOLDER_NAMES = List.of(
            "USER_EXIT", "DOCUMENTATION"
    );

    private static final List<String> REJECTED_FILE_NAME_PATTERNS = List.of(
            ".*\\QLOG3\\E.*",
            "GWNKUNDE",
            "EDAPEW",
            "TAXACT"
    );
    private static final long MEGABYTE = 1024 * 1024;

    public UnwantedFileExtension() {
        rejectFileExtensions();
        rejectFolderNames();
       // rejectFileNamePatterns();
        rejectFileSize();
    }

    private void rejectFileExtensions() {
        FILE_EXTENSIONS.forEach(ext -> filters.add(new RejectFileExtension(ext)));
    }

    private void rejectFolderNames() {
        FOLDER_NAMES.forEach(folderName ->
                filters.add(new RejectFolderName(Pattern.compile(Pattern.quote(folderName), Pattern.CASE_INSENSITIVE)))
        );
    }

    private void rejectFileNamePatterns() {
        REJECTED_FILE_NAME_PATTERNS.forEach(pattern ->
                filters.add(new RejectFileName(Pattern.compile(Pattern.quote(pattern), Pattern.CASE_INSENSITIVE)))
        );
    }

    private void rejectFileSize(){
        filters.add(new RejectFileSize(50 * MEGABYTE));
    }

}
