package com.imtf.cstool.supporttool.service;

import com.imtf.cstool.supporttool.collector.SystemInfoCollector;
import com.imtf.cstool.supporttool.helper.systeminfo.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SystemInfoService {

    private static final Logger logger = LoggerFactory.getLogger(SystemInfoService.class);

    @Autowired
    SystemInfoCollector systemInfoWriterService;

    @Autowired
    OperatingSystem osService;


    public Path collectAndZip(String folderName, String zipFileName) throws IOException {
        String basePath = osService.getApplicationPath();
        String fullOutputPath = basePath + File.separator + folderName;

        logger.info("System info Destination Path :{}", fullOutputPath);
        // Step 1: collect all info into .txt files
        String finalSystemFolder = systemInfoWriterService.exportSystemInfo(fullOutputPath);

        logger.info("System info Destination Final Path :{}", finalSystemFolder);

        // Step 2: zip those files
        File zipFile = zipFolder(fullOutputPath, zipFileName);


        // Step 3: return zip file as StreamingOutput
       return zipFile.toPath();//output -> Files.copy(zipFile.toPath(), output);
    }

//      Compresses the folder into a ZIP file

    private File zipFolder(String folderPath, String zipFileName) throws IOException {
        File srcDir = new File(folderPath);
        File zipFile = new File(srcDir.getParent(), zipFileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            zipDirectory(srcDir, srcDir.getName(), zipOutputStream);
        }

        return zipFile;
    }


    //     Adds all files and subfolders to the ZIP output
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
