package org.imtf.siron.supporttool.helper;

import jakarta.enterprise.context.ApplicationScoped;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.constant.SupportToolConstant;
import org.imtf.siron.supporttool.exception.InvalidPathException;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.model.FilterPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lingala.zip4j.ZipFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ApplicationScoped
public class FileManager {

    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

    public String createTempDirectory() {
        Path applicationPath = getApplicationPath();

        if (!permissionCheck(applicationPath)) {
            logger.error("Insufficient permissions to create directory in application path: {}", applicationPath);
            throw new java.lang.SecurityException("Insufficient file system permissions");
        }

        Path supportToolDir = applicationPath.resolve(SupportToolConstant.SUPPORT_FOLDER_NAME);
        try {
            if (Files.exists(supportToolDir)) {
                logger.info("Support folder already exists. Deleting: {}", supportToolDir);
                deleteTempDirectory(supportToolDir);
            }

            Files.createDirectories(supportToolDir);
            logger.info("Support folder created at: {}", supportToolDir);

            return supportToolDir.toAbsolutePath().toString();

        } catch (IOException exception) {
            logger.error("Failed to create or delete support folder '{}': {}", supportToolDir, exception.getMessage());
            throw new RuntimeException("Failed to manage support folder: " + supportToolDir, exception);
        }
    }

    public void deleteTempDirectory(Path applicationPath) {
        try (Stream<Path> walk = Files.walk(applicationPath)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            logger.warn("Failed to delete: {}", file.getAbsolutePath());
                        }
                    });
        } catch (IOException exception) {
            logger.error("Error while deleting directory {}: {}", applicationPath, exception.getMessage());
        }
    }

    public boolean permissionCheck(Path applicationPath) {
        try {
            boolean isReadable = Files.isReadable(applicationPath);
            boolean isWritable = Files.isWritable(applicationPath);
            boolean isExecutable = Files.isExecutable(applicationPath);

            if (!isReadable || !isWritable || !isExecutable) {
                logger.warn("Permission check failed for path '{}' [readable={}, writable={}, executable={}]",
                        applicationPath, isReadable, isWritable, isExecutable);
            }

            return isReadable && isWritable && isExecutable;

        } catch (java.lang.SecurityException exception) {
            logger.error("SecurityException: Access denied while checking permissions for path '{}'. {}", applicationPath, exception.getMessage());
            throw new java.lang.SecurityException(exception.getMessage());
        } catch (Exception exception) {
            logger.error("Unexpected exception occurred while checking file permissions for path '{}'. {}", applicationPath, exception.getMessage());
            throw exception;
        }
    }

    public Path getApplicationPath() {
        try {
            Path path = Paths.get("").toAbsolutePath();
            logger.info("Application running from path: {}", path);
            return path;
        } catch (java.lang.SecurityException securityException) {
            logger.error("SecurityException: Unable to access the current directory due to security restrictions.{}", securityException.getMessage());
            throw new SecurityException(securityException.getMessage());
        } catch (InvalidPathException invalidPathException) {
            logger.error("Invalid path while determining application path - {}", invalidPathException.getMessage());
            throw invalidPathException;
        } catch (Exception exception) {
            logger.error("Unexpected error while determining application path - {}", exception.getMessage());
            throw new RuntimeException("Unexpected error while determining application path", exception);
        }
    }

    public File getClientFolder(String productRoot, String clientId) {
        return Paths.get(productRoot, ProductConstant.PRODUCT_CLIENT_FOLDER,clientId).toFile();
    }

    public boolean isClientFolderExists(String productRoot, String clientId) {
        File clientFolder = getClientFolder(productRoot, clientId);
        return !clientFolder.exists();
    }

    public List<String> getClientIds(String clientFolderPath) {
        File clientFolder = new File(clientFolderPath);

        if (!clientFolder.exists() || !clientFolder.isDirectory()) {
            logger.error("Invalid client folder path: {}", clientFolderPath);
            throw new NotFoundException("Client folder path does not exist or is not a directory: " + clientFolderPath);
        }

        File[] subFolders = clientFolder.listFiles(File::isDirectory);

        if (subFolders == null || subFolders.length == 0) {
            logger.warn("No client folders found in path: {}", clientFolderPath);
            throw new NotFoundException("No client folders found at path: " + clientFolderPath);
        }

        Arrays.sort(subFolders, (firstFile, secondFile) -> firstFile.getName().compareToIgnoreCase(secondFile.getName()));

        List<String> clientIds = new ArrayList<String>();
        for (File folder : subFolders) {
            String name = folder.getName();
            clientIds.add(name);
            logger.debug("Found client folder: {}", name);
        }

        logger.info("Total {} client IDs found in {}", clientIds.size(), clientFolderPath);
        return clientIds;
    }


    public String createSubFolder(String basePath, String... subFolderName) {
        Path subFolder = Paths.get(basePath, subFolderName);

        try {
            Files.createDirectories(subFolder);
            logger.info("Sub-folder created: {}", subFolder);
            return subFolder.toString();
        }
        catch (IOException exception){
            logger.error("Failed to create sub folder '{}': {}", subFolder, exception.getMessage());
            throw new RuntimeException("Failed to create sub folder: " + subFolder, exception);
        }
    }

    public String createFile(String basePath, String fileName){
        File file = new File(basePath, fileName);
        if (!file.exists()) {
            try {
                File parentDirectory = file.getParentFile();
                if (parentDirectory != null && !parentDirectory.exists()) {
                    boolean parentDirectoryCreated = parentDirectory.mkdirs();
                    if (!parentDirectoryCreated) {
                        logger.error("Failed to create parent directory '{}': {}", parentDirectory, file.getAbsolutePath());
                    }
                }

                if (!file.exists()){
                    if (file.createNewFile()){
                        logger.info("Created file '{}': {}", file.getAbsolutePath(), fileName);
                    }
                    return file.getAbsoluteFile().toString();
                }

            }
            catch (IOException exception){
                logger.error("Failed to create file '{}': {}", file, exception.getMessage());
                throw new RuntimeException("Failed to create file: " + file, exception);
            }
        }
        return null;
    }

    public void fileWriter(String destinationPath, String key, String value){

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destinationPath, true));
            logger.trace("Writing file '{}': '{}': {}", destinationPath, key, value);
            bufferedWriter.write(key + " = " + value);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception exception){
            logger.error("Failed to write file '{}': {}", destinationPath, exception.getMessage());
            throw new RuntimeException("Failed to write file: " + destinationPath, exception);
        }

    }

    public void copySourceToDestinationPath(Map<String, FilterPaths> folderPaths){

        for (Map.Entry<String, FilterPaths> folderPath : folderPaths.entrySet()) {
            String sourcePath = folderPath.getKey();
            FilterPaths filterPaths = folderPath.getValue();

            File sourceDirectory = new File(sourcePath);
            File destinationDirectory = new File(filterPaths.getDestinationPath());

            if (!sourceDirectory.exists()) {
                logger.warn("Source directory '{}' does not exist or is not a directory.", sourceDirectory);
                continue;
            }

            try {
                logger.info("Copying source directory '{}' to '{}'", sourceDirectory, destinationDirectory);
                copyRecursive(sourceDirectory, destinationDirectory, filterPaths.getFilter());
            }
            catch (IOException exception){
                logger.error("Failed to copy source directory '{}': {}", sourceDirectory, exception.getMessage());
            }
        }
    }


    public void copyRecursive(File sourcePath, File destinationPath, FileFilter filter) throws IOException {
        if (sourcePath.isDirectory()) {
                if (!destinationPath.exists() && destinationPath.mkdirs()) {
                    logger.debug("Created directory '{}'", destinationPath);
                }

                File[] children = sourcePath.listFiles();
                if (children != null) {
                    for (File child : children) {
                        File destChild = new File(destinationPath, child.getName());
                        copyRecursive(child, destChild, filter);
                    }
                }

        } else {
            if (filter.accept(sourcePath)) {
                File parentDir = destinationPath.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        logger.debug("Created parent directory '{}'", parentDir);
                    }
                }
                Files.copy(sourcePath.toPath(), destinationPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied file '{}' to '{}'", sourcePath, destinationPath);
            } else {
                logger.debug("Skipping file '{}' due to filter rejection", sourcePath);
            }
        }
    }

    public Path zipCreation(Path sourceFolderPath, String zipFileName) throws IOException {
        if (!Files.exists(sourceFolderPath) || !Files.isDirectory(sourceFolderPath)) {
            throw new IllegalArgumentException("Source folder does not exist or is not a directory: " + sourceFolderPath);
        }

        if (!Files.exists(SupportToolConstant.ZIP_FOLDER)) {
            Files.createDirectories(SupportToolConstant.ZIP_FOLDER);
        }

        Path zipFilePath = SupportToolConstant.ZIP_FOLDER.resolve(zipFileName);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(SupportToolConstant.ZIP_FOLDER, "*.zip")) {
            for (Path file : stream) {
                try {
                    Files.deleteIfExists(file);
                    logger.info("Deleted existing zip file: {}", file);
                } catch (IOException ex) {
                    logger.warn("Failed to delete file '{}': {}", file, ex.getMessage());
                }
            }
        }

        try {
            ZipFile zipFile = new ZipFile(zipFilePath.toFile());
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);

            zipFile.addFolder(sourceFolderPath.toFile(), parameters);
            logger.info("Successfully created zipped folder '{}' to '{}'", sourceFolderPath, zipFilePath);

            return zipFile.getFile().toPath();
        } catch (Exception e) {
            logger.error("Failed to zip folder '{}': {}", sourceFolderPath, e.getMessage(), e);
            throw e;
        }
    }
}
