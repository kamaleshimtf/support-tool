package org.imtf.siron.supporttool.helper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.constant.EnvironmentConstant;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.constant.UnwantedFileConstant;
import org.imtf.siron.supporttool.model.SironProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    @Inject
    ProductConstant productConstant;

    @Inject
    OperatingSystem operatingSystem;

    @Inject
    FileManager fileManager;

    @Inject
    UnwantedFileConstant unwantedFileConstant;

    private final Map<String, String> productEnvironment = new HashMap<>();

    public String getEnvironmentByKey(String rootKey){
        return System.getenv(rootKey);
    }

    public Map<String,String> getAllEnvironment(){
        return System.getenv();
    }

    public Map<String, String> getProductEnvironment(SironProductType sironProductType, String destinationPath, String productRootPath, String clientId){

        String sourceDirectory = Paths.get(productRootPath, ProductConstant.SOURCE_ENVIRONMENT).toString();
        String destinationDirectory = Paths.get(destinationPath).toString();

        logger.info("Starting to fetch environment variables for product '{}', client '{}'", sironProductType, clientId);
        logger.debug("Source directory: '{}'", sourceDirectory);
        logger.debug("Destination directory: '{}'", destinationDirectory);

        int returnCode = getProductEnvironmentFromScript(
                buildEnvironmentVariables(sironProductType, productRootPath,false),
                sourceDirectory,
                destinationDirectory,
                clientId
        );

        if(returnCode != 0) {
            logger.warn("Initial attempt to fetch environment variables failed for product '{}'. Trying with fallback shell trace ON.", sironProductType);

           returnCode = getProductEnvironmentFromScript(
                    buildEnvironmentVariables(sironProductType,productRootPath,true),
                    sourceDirectory,
                    destinationDirectory,
                    clientId
            );

            if (returnCode != 0) {
                logger.error("Failed to fetch environment variables for product '{}', client '{}', even after fallback attempt.", sironProductType, clientId);
            } else {
                logger.info("Successfully fetched environment variables using fallback for product '{}', client '{}'", sironProductType, clientId);
            }
        }
        else {
            logger.info("Successfully fetched environment variables for product '{}', client '{}'", sironProductType, clientId);
        }
        return productEnvironment;
    }

    public int getProductEnvironmentFromScript(String[] input, String sourceDirectory, String destinationDirectory, String clientId){

        List<String> commands = getCommands(sourceDirectory, clientId);
        logger.info("Executing environment script for client '{}', source='{}'", clientId, sourceDirectory);
        logger.debug("Command: {}", String.join(" ", commands));

        int returnCode = 1;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            String environmentFilePath = fileManager.createFile(destinationDirectory,ProductConstant.DESTINATION_ENVIRONMENT_FILENAME);
            if (environmentFilePath != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        logger.debug("Script Output Line: {}", line);

                        if (line.contains("=")) {
                            int index = line.indexOf('=');
                            String key = line.substring(0, index).trim();
                            String value = line.substring(index + 1).trim();

                            this.productEnvironment.put(key, value);

                            if (unwantedFileConstant.isSensitive(key)) {

                                logger.debug("Sensitive key '{}' detected. Masking value.", key);
                                value = ProductConstant.SENSITIVE_CONTENT;
                            }
                            fileManager.fileWriter(environmentFilePath, key, value);
                        }
                    }
                }
                returnCode = process.waitFor();
            }

        }catch (Exception exception){
            throw new RuntimeException("Failed to execute environment setup script for client " + clientId +
                    " in directory '" + sourceDirectory + "': " + exception.getMessage(), exception);
        }
        return returnCode;
    }
    public String[] buildEnvironmentVariables(SironProductType productType, String productRootPath, Boolean shellStatus) {
        return new String[] {
                productConstant.getProductTypeByRoot(productType) + "=" + productRootPath,
                shellStatus ? EnvironmentConstant.FS_SHELL_TRACE_YES : EnvironmentConstant.FS_SHELL_TRACE_NO,
                EnvironmentConstant.PATH + "=" + getEnvironmentByKey(EnvironmentConstant.PATH)
        };
    }

    private List<String> getCommands(String sourceDirectory, String clientId) {
        List<String> commands = new ArrayList<>();

        if (operatingSystem.isWindows()) {
            commands.add(EnvironmentConstant.WINDOWS_CMD);
            commands.add(EnvironmentConstant.WINDOWS_CMD_ARG);
            commands.add(EnvironmentConstant.WINDOWS_CMD_CALL + " " + sourceDirectory + " " + clientId +
                    EnvironmentConstant.WINDOWS_CMD_SET);
        } else {
            commands.add(EnvironmentConstant.UNIX_SHELL);
            commands.add(EnvironmentConstant.UNIX_SHELL_ARG);
            commands.add(EnvironmentConstant.UNIX_SHELL_SOURCE + " " + sourceDirectory + " " + clientId +
                    EnvironmentConstant.UNIX_SHELL_SET);
        }
        return commands;
    }
}
