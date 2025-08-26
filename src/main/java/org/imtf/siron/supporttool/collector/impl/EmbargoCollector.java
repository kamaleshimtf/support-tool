package org.imtf.siron.supporttool.collector.impl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.filter.acceptfilter.AcceptExtension;
import org.imtf.siron.supporttool.filter.acceptfilter.FilterChainAnd;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectFileName;
import org.imtf.siron.supporttool.helper.EnvironmentManager;
import org.imtf.siron.supporttool.helper.FileManager;
import org.imtf.siron.supporttool.model.FilterPaths;
import org.imtf.siron.supporttool.model.ProductClientInfo;
import org.imtf.siron.supporttool.model.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ApplicationScoped
public class EmbargoCollector implements ProductCollector {

    private static final Logger logger = LoggerFactory.getLogger(EmbargoCollector.class);
    private final List<Pattern> fileFilters = new ArrayList<>();
    private final List<Pattern> folderFilters = new ArrayList<>();

    private final FileManager fileManager;
    private final EnvironmentManager environmentManager;

    @Inject
    public EmbargoCollector(FileManager fileManager, EnvironmentManager environmentManager) {
        this.fileManager = fileManager;
        this.environmentManager = environmentManager;
    }

    @Override
    public void collect(String destinationPath, ProductType productType, ProductClientInfo productClientInfo) {
        logger.info("Collecting Embargo product data for type: {}", productType.name());

        String destinationFolder = fileManager.createSubFolder(destinationPath, productType.name());

        if (!productClientInfo.getClientIds().isEmpty()) {
            collectClientPaths(productType, destinationFolder, productClientInfo);
        } else {
            logger.info("No client IDs provided for {}", productType.name());
        }
        collectSystemPaths(destinationFolder, productClientInfo);

    }
    private void collectSystemPaths(String destinationPath, ProductClientInfo info) {
        logger.info("Collecting Embargo system/custom folders");

        Map<String, String> systemPaths = getSystemPaths(info.getRootPath(), destinationPath);
        Map<String, FilterPaths> filteredPaths = new HashMap<>();

        for (Map.Entry<String, String> entry : systemPaths.entrySet()) {
            Path source = Paths.get(entry.getKey());
            Path dest = Paths.get(entry.getValue());

            if (Files.exists(source)) {
                logger.info("Adding system path: {}", source);
                FilterChainAnd filterChainAnd = new FilterChainAnd(fileFilters, folderFilters);
                filterChainAnd.addFilter(new RejectFileName(Pattern.compile(".*\\QSO\\E.*")));
                filterChainAnd.addFilter(new RejectFileName(Pattern.compile(".*\\QSWIFT\\E.*")));
                filteredPaths.put(source.toString(),new FilterPaths(dest.toString(), filterChainAnd));
            } else {
                logger.warn("Path does not exist: {}", source);
            }
        }

        fileManager.copySourceToDestinationPath(filteredPaths);
    }

    private Map<String, String> getSystemPaths(String root, String dest) {
        Map<String, String> paths = new HashMap<>();

        String sysSource = Paths.get(root, ProductConstant.PRODUCT_SYSTEM_FOLDER).toString();
        String sysDest = fileManager.createSubFolder(dest, ProductConstant.PRODUCT_SYSTEM_FOLDER);

        String customSource = Paths.get(root, ProductConstant.PRODUCT_CUSTOM_FOLDER).toString();
        String customDest = fileManager.createSubFolder(dest, ProductConstant.PRODUCT_CUSTOM_FOLDER);

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_WEB_CLIENT, ProductConstant.EMBARGO_SYSTEM_WEB_CLIENT_PROPERTIES).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_WEB_CLIENT, ProductConstant.EMBARGO_SYSTEM_WEB_CLIENT_PROPERTIES).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_PARAMETRIZATION,
                        ProductConstant.EMBARGO_SYSTEM_PARAMETRIZATION_LPLR, ProductConstant.EMBARGO_SYSTEM_PARAMETRIZATION_SIRON_EMBARGO,
                        ProductConstant.EMBARGO_SYSTEM_PARAMETRIZATION_RELEASE).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_WEB_CLIENT).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_SCORING, ProductConstant.EMBARGO_SYSTEM_SCORING_DEFAULT).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_SCORING, ProductConstant.EMBARGO_SYSTEM_SCORING_DEFAULT).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_SCORING, ProductConstant.EMBARGO_SYSTEM_SCORING_ENV).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_SCORING, ProductConstant.EMBARGO_SYSTEM_SCORING_ENV).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_SCORING, ProductConstant.EMBARGO_SYSTEM_SCORING_WORK).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_SCORING, ProductConstant.EMBARGO_SYSTEM_SCORING_WORK).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_INSTALL, ProductConstant.EMBARGO_SYSTEM_INSTALL_LOG).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_INSTALL, ProductConstant.EMBARGO_SYSTEM_INSTALL_LOG).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME_SIRONEAI).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME_SIRONEAI).toString());

        paths.put(Paths.get(sysSource, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME_EMBARGO).toString(),
                Paths.get(sysDest, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME, ProductConstant.EMBARGO_SYSTEM_TBELLERHOME_EMBARGO).toString());

        paths.put(Paths.get(customSource, ProductConstant.EMBARGO_CUSTOM_SCORING).toString(), Paths.get(customDest, ProductConstant.EMBARGO_CUSTOM_SCORING).toString());

        paths.put(Paths.get(customSource, ProductConstant.EMBARGO_CUSTOM_TOOL).toString(), Paths.get(customDest, ProductConstant.EMBARGO_CUSTOM_TOOL).toString());

        paths.put(Paths.get(customSource, ProductConstant.EMBARGO_CUSTOM_WEB_CLIENT).toString(), Paths.get(customDest, ProductConstant.EMBARGO_CUSTOM_WEB_CLIENT).toString());
        return  paths;
    }

    private void collectClientPaths(ProductType productType, String destinationPath, ProductClientInfo info) {
        logger.info("Collecting AML client-specific data");

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
    private Map<String, String> createClientPathMap(String root, String dest) {
        Map<String, String> map = new HashMap<>();
        map.put(Paths.get(root, ProductConstant.EMBARGO_CLIENT_WORKSWIFT).toString(), Paths.get(dest, ProductConstant.EMBARGO_CLIENT_WORKSWIFT).toString());
        map.put(Paths.get(root, ProductConstant.EMBARGO_CLIENT_DATA, ProductConstant.EMBARGO_CLIENT_DATA_INDICES).toString(),
                Paths.get(dest, ProductConstant.EMBARGO_CLIENT_DATA, ProductConstant.EMBARGO_CLIENT_DATA_INDICES).toString());
        map.put(Paths.get(root, ProductConstant.EMBARGO_CLIENT_LOG).toString(), Paths.get(dest, ProductConstant.EMBARGO_CLIENT_LOG).toString());
        map.put(Paths.get(root, ProductConstant.EMBARGO_CLIENT_CUSTOM).toString(), Paths.get(dest, ProductConstant.EMBARGO_CLIENT_CUSTOM).toString());

        for (int i=1; i<99; i++) {
            String sourcePath = Paths.get(root, ProductConstant.EMBARGO_CLIENT_WORKSWIFT + i).toString();
            String destPath = Paths.get(dest, ProductConstant.EMBARGO_CLIENT_WORKSWIFT + i).toString();
            File sourceDir = new File(sourcePath);

            if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                logger.info("No more directories found at index {}. Breaking loop.", i);
                break;
            }
            map.put(sourcePath, destPath);
        }

        return map;
    }
    public Map<String, FilterPaths> prepareFilteredPaths(Map<String, String> pathMap) {
        Map<String, FilterPaths> clientKycResult = new HashMap<>();

        for (Map.Entry<String, String> entry : pathMap.entrySet()) {

            Path source = Paths.get(entry.getKey());
            if (Files.exists(source)) {
                if (source.endsWith(ProductConstant.EMBARGO_CLIENT_DATA_INDICES)) {
                    FilterChainAnd filterChainAnd;
                    filterChainAnd = new FilterChainAnd(fileFilters, folderFilters);
                    filterChainAnd.addFilter(new AcceptExtension(".csv")).addFilter(new AcceptExtension(".txt"));
                    filterChainAnd.addFilter(new RejectFileName(Pattern.compile(".*\\QSO\\E.*")));
                    filterChainAnd.addFilter(new RejectFileName(Pattern.compile(".*\\QSO\\E.*")));

                    clientKycResult.put(source.toString(), new FilterPaths(entry.getValue(), filterChainAnd));
                }
                else {
                    FilterChainAnd filterChainAnd = new FilterChainAnd(fileFilters, folderFilters);
                    filterChainAnd.addFilter(new RejectFileName(Pattern.compile(".*\\QSO\\E.*")));
                    filterChainAnd.addFilter(new RejectFileName(Pattern.compile(".*\\QSWIFT\\E.*")));
                    clientKycResult.put(source.toString(), new FilterPaths(entry.getValue(), filterChainAnd));
                }
            }
            else {
                logger.warn("Path not found: {}", source);
            }
        }
        return clientKycResult;
    }
}
