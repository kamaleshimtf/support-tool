package org.imtf.siron.supporttool.service;



import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.impl.SystemInfoCollector;
import org.imtf.siron.supporttool.helper.FileManager;
import org.imtf.siron.supporttool.helper.systeminfo.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class SystemInfoService {

    private static final Logger logger = LoggerFactory.getLogger(SystemInfoService.class);

    @Inject
    SystemInfoCollector systemInfoWriterService;

    @Inject
    OperatingSystem osService;

    @Inject
    FileManager fileManager;


    public Path collectAndZip(String folderName, String zipFileName) throws IOException {
        String basePath = fileManager.getApplicationPath().toString();

        logger.info("Application Path :{}", basePath);
        logger.info("Application Exact Path:{}", Paths.get(basePath, folderName).toString());
        String fullOutputPath = Paths.get(basePath, folderName).toString();

        logger.info("System info Destination Path :{}", fullOutputPath);

        String finalSystemFolder = systemInfoWriterService.collectingSystemInformation(fullOutputPath);

        logger.info("System info Destination Final Path :{}", finalSystemFolder);


        File zipFile = zipFolder(fullOutputPath, zipFileName);

       return zipFile.toPath();
    }


    private File zipFolder(String folderPath, String zipFileName) throws IOException {
        File srcDir = new File(folderPath);

        File parentFolder = new File(srcDir.getParent(), "system-zip");
        if (!parentFolder.exists() && !parentFolder.mkdirs()) {
            throw new IOException("Failed to create folder: " + parentFolder.getAbsolutePath());
        }

        File[] existingZips = parentFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
        if (existingZips != null) {
            for (File oldZip : existingZips) {
                if (!oldZip.delete()) {
                    logger.warn("Failed to delete old zip file: {}", oldZip.getAbsolutePath());
                }
            }
        }

        File zipFile = new File(parentFolder, zipFileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            zipDirectory(srcDir, srcDir.getName(), zipOutputStream);
        }
        logger.info("Finished zipping System information : {}", zipFile.getAbsolutePath());
        return zipFile;
    }


    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    String entryName = parentFolder + "/" + file.getName();
                    zos.putNextEntry(new ZipEntry(entryName));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    public String generateZipName(String folderName) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        return "system-info-" + timestamp + ".zip";
    }
}
