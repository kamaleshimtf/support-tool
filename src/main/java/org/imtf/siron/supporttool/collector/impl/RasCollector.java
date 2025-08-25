package org.imtf.siron.supporttool.collector.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.filter.acceptfilter.AcceptExtension;
import org.imtf.siron.supporttool.filter.acceptfilter.FilterChainAnd;
import org.imtf.siron.supporttool.helper.EnvironmentManager;
import org.imtf.siron.supporttool.helper.FileManager;
import org.imtf.siron.supporttool.model.FilterPaths;
import org.imtf.siron.supporttool.model.ProductClientInfo;
import org.imtf.siron.supporttool.model.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ApplicationScoped
public class RasCollector implements ProductCollector {

    private static final Logger logger = LoggerFactory.getLogger(RasCollector.class);
    private final List<Pattern> fileFilters = new ArrayList<>();
    private final List<Pattern> folderFilters = new ArrayList<>();

    private final FileManager fileManager;
    private final EnvironmentManager environmentManager;

    @Inject
    public RasCollector(FileManager fileManager,  EnvironmentManager environmentManager) {
        this.fileManager = fileManager;
        this.environmentManager = environmentManager;
    }

    @Override
    public void collect(String destinationPath, ProductType productType, ProductClientInfo productClientInfo) {
        logger.info("Collecting RAS product data for type: {}", productType.name());

        String destinationFolder = fileManager.createSubFolder(destinationPath, productType.name());

        if (!productClientInfo.getClientIds().isEmpty()) {
            collectClientPaths(productType, destinationFolder, productClientInfo);
        } else {
            logger.info("No client IDs provided for {}", productType.name());
        }

        collectSystemPaths(destinationFolder, productClientInfo);
    }

    private void collectSystemPaths(String destinationPath, ProductClientInfo info) {
        logger.info("Collecting RAS system/custom folders");

        Map<String, String> systemPaths = getSystemPaths(info.getRootPath(), destinationPath);
        Map<String, FilterPaths> filteredPaths = new HashMap<>();

        for (Map.Entry<String, String> entry : systemPaths.entrySet()) {
            Path source = Paths.get(entry.getKey());
            Path dest = Paths.get(entry.getValue());

            if (Files.exists(source)) {
                logger.info("Adding system path: {}", source);

                if (source.endsWith(ProductConstant.RAS_CUSTOM_REPORT)){
                    FilterChainAnd filterChainAnd = new FilterChainAnd(fileFilters, folderFilters);
                    filterChainAnd.addFilter(new AcceptExtension(".dat"));
                    filteredPaths.put(source.toString(),new FilterPaths(dest.toString(), filterChainAnd));
                }
            } else {
                logger.warn("Path does not exist: {}", source);
            }
        }

        fileManager.copySourceToDestinationPath(filteredPaths);
    }
    private Map<String, String> getSystemPaths(String root, String dest) {
        Map<String, String> paths = new HashMap<>();

        String customSource = Paths.get(root, ProductConstant.PRODUCT_CUSTOM_FOLDER).toString();
        String customDest = fileManager.createSubFolder(dest, ProductConstant.PRODUCT_CUSTOM_FOLDER);

        String globalSource = Paths.get(root, ProductConstant.RAS_GLOBAL).toString();
        String globalDest = fileManager.createSubFolder(dest, ProductConstant.RAS_GLOBAL);

        paths.put(Paths.get(customSource, ProductConstant.RAS_CUSTOM_REPORT).toString(),
                Paths.get(customDest, ProductConstant.RAS_CUSTOM_REPORT).toString());
        paths.put(Paths.get(customSource, ProductConstant.RAS_CUSTOM_REPORT, ProductConstant.RAS_CUSTOM_REPORT_STYLE).toString(),
                Paths.get(customDest, ProductConstant.RAS_CUSTOM_REPORT, ProductConstant.RAS_CUSTOM_REPORT_STYLE).toString());
        paths.put(Paths.get(customSource, ProductConstant.RAS_CUSTOM_WEBCLIENT, ProductConstant.RAS_CUSTOM_WEBCLIENT_PROPERTIES).toString(),
                Paths.get(customDest, ProductConstant.RAS_CUSTOM_WEBCLIENT, ProductConstant.RAS_CUSTOM_WEBCLIENT_PROPERTIES).toString());

        paths.put(globalSource, globalDest);
        return paths;
    }
    private void collectClientPaths(ProductType productType, String destinationPath, ProductClientInfo info) {
        logger.info("Collecting RAS client-specific data");

        if (info.getClientIds().isEmpty()) {
            throw new NotFoundException("No client IDs found for product " + productType + " at " + destinationPath);
        }

        for (String clientId : info.getClientIds()) {
            String clientDestination = fileManager.createSubFolder(destinationPath, ProductConstant.PRODUCT_CLIENT_FOLDER, clientId);

            Map<String, String> env = environmentManager.getProductEnvironment(
                    productType, clientDestination, info.getRootPath(), clientId);

            if (env.isEmpty()) {
                logger.warn("Environment variables not found for client: {}", clientId);
                continue;
            }

            String clientRoot = Paths.get(info.getRootPath(), ProductConstant.PRODUCT_CLIENT_FOLDER, clientId).toString();
            Map<String, String> clientPaths = createClientPathMap(clientRoot, clientDestination);

            Map<String, FilterPaths> filteredPaths = prepareFilteredPaths(clientPaths);

            logger.info("Filtered Paths : {}", filteredPaths);
            fileManager.copySourceToDestinationPath(filteredPaths);
        }
    }
    public Map<String, FilterPaths> prepareFilteredPaths(Map<String, String> pathMap) {
        Map<String, FilterPaths> clientKycResult = new HashMap<>();

        for (Map.Entry<String, String> entry : pathMap.entrySet()) {

            Path source = Paths.get(entry.getKey());
            if (Files.exists(source)) {
                clientKycResult.put(source.toString(), new FilterPaths(entry.getValue(), fileFilters, folderFilters));
            }
            else {
                logger.warn("Path not found: {}", source);
            }
        }
        return clientKycResult;
    }

    private Map<String, String> createClientPathMap(String root, String dest) {
        Map<String, String> map = new HashMap<>();
        map.put(
                Paths.get(root, ProductConstant.RAS_CLIENT_CUSTOM, ProductConstant.RAS_CLIENT_CUSTOM_REPORT).toString(),
                Paths.get(dest, ProductConstant.RAS_CLIENT_CUSTOM, ProductConstant.RAS_CLIENT_CUSTOM_REPORT).toString()
        );
        map.put(
                Paths.get(root, ProductConstant.RAS_CLIENT_DATA, ProductConstant.RAS_CLIENT_DATA_CONTROL).toString(),
                Paths.get(dest, ProductConstant.RAS_CLIENT_DATA, ProductConstant.RAS_CLIENT_DATA_CONTROL).toString()
        );
        map.put(
                Paths.get(root, ProductConstant.RAS_CLIENT_DATA, ProductConstant.RAS_CLIENT_DATA_REPORT_DE, ProductConstant.RAS_CLIENT_DATA_REPORT_RAS_REPORT_CSV).toString(),
                Paths.get(dest, ProductConstant.RAS_CLIENT_DATA, ProductConstant.RAS_CLIENT_DATA_REPORT_DE, ProductConstant.RAS_CLIENT_DATA_REPORT_RAS_REPORT_CSV).toString()
        );

        return map;
    }
}
