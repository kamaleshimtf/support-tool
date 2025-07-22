package org.imtf.siron.supporttool.helper;

import jakarta.enterprise.context.ApplicationScoped;
import org.imtf.siron.supporttool.constant.SupportTool;
import org.imtf.siron.supporttool.exception.InvalidPathException;
import org.imtf.siron.supporttool.exception.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@ApplicationScoped
public class FileManager {

    private final Logger logger = LoggerFactory.getLogger(FileManager.class);

    public String createTempDirectory() {
        Path applicationPath = getApplicationPath();

        if (!permissionCheck(applicationPath)) {
            logger.error("Insufficient permissions to create directory in application path: {}", applicationPath);
            throw new SecurityException("Insufficient file system permissions");
        }

        Path supportToolDir = applicationPath.resolve(SupportTool.SUPPORT_FOLDER_NAME);
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

        } catch (SecurityException exception) {
            logger.error("SecurityException: Access denied while checking permissions for path '{}'. {}", applicationPath, exception.getMessage());
            throw new SecurityException(exception.getMessage());
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
        } catch (SecurityException securityException) {
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
}
