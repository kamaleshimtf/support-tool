package org.imtf.siron.supporttool.collector.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.exception.NotFoundException;
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
public class AmlCollector implements ProductCollector {

    private static final Logger logger = LoggerFactory.getLogger(AmlCollector.class);
    private final List<Pattern> fileFilters = new ArrayList<>();
    private final List<Pattern> folderFilters = new ArrayList<>();

    private final FileManager fileManager;
    private final EnvironmentManager environmentManager;

    @Inject
    public AmlCollector(FileManager fileManager, EnvironmentManager environmentManager) {
        this.fileManager = fileManager;
        this.environmentManager = environmentManager;
    }

    @Override
    public void collect(String destinationPath, ProductType productType, ProductClientInfo productClientInfo) {

        logger.info("Collecting AML product data for type: {}", productType.name());

        String destinationFolder = fileManager.createSubFolder(destinationPath, productType.name());

        if (!productClientInfo.getClientIds().isEmpty()) {
            collectClientPaths(productType, destinationFolder, productClientInfo);
        } else {
            logger.info("No client IDs provided for {}", productType.name());
        }

        collectSystemPaths(destinationFolder, productClientInfo);
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

            fileManager.copySourceToDestinationPath(filteredPaths);
        }
    }

    private Map<String, String> createClientPathMap(String root, String dest) {
        Map<String, String> map = new HashMap<>();
        map.put(Paths.get(root, ProductConstant.AML_CLIENT_LOG).toString(), Paths.get(dest, ProductConstant.AML_CLIENT_LOG).toString());
        map.put(Paths.get(root, ProductConstant.AML_CLIENT_LOG_ARCHIVE).toString(), Paths.get(dest, ProductConstant.AML_CLIENT_LOG_ARCHIVE).toString());
        map.put(Paths.get(root, ProductConstant.AML_CLIENT_TEMPORARY).toString(), Paths.get(dest, ProductConstant.AML_CLIENT_TEMPORARY).toString());
        map.put(Paths.get(root, ProductConstant.AML_CLIENT_CUSTOM).toString(), Paths.get(dest, ProductConstant.AML_CLIENT_CUSTOM).toString());
        map.put(Paths.get(root, ProductConstant.AML_CLIENT_LOG_DELETE).toString(), Paths.get(dest, ProductConstant.AML_CLIENT_LOG_DELETE).toString());
        return map;
    }

    private void collectSystemPaths(String destinationPath, ProductClientInfo info) {
        logger.info("Collecting AML system/global/custom folders");

        Map<String, String> systemPaths = getSystemPaths(info.getRootPath(), destinationPath);

        Map<String, FilterPaths> filteredPaths = new HashMap<>();

        for (Map.Entry<String, String> entry : systemPaths.entrySet()) {
            Path source = Paths.get(entry.getKey());
            Path dest = Paths.get(entry.getValue());

            if (Files.exists(source)) {
                logger.info("Adding system path: {}", source);
                if (ProductConstant.PRODUCT_CUSTOM_FOLDER.equals(source.toString())) {
                    filteredPaths.put(source.toString(), new FilterPaths(dest.toString(), new FilterChainAnd(fileFilters, folderFilters)));
                } else {
                    filteredPaths.put(source.toString(), new FilterPaths(dest.toString(), fileFilters, folderFilters));
                }
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

        String globalSource = Paths.get(root, ProductConstant.AML_GLOBAL).toString();
        String globalDest = fileManager.createSubFolder(dest, ProductConstant.AML_GLOBAL);

        String customSource = Paths.get(root, ProductConstant.PRODUCT_CUSTOM_FOLDER).toString();
        String customDest = fileManager.createSubFolder(dest, ProductConstant.PRODUCT_CUSTOM_FOLDER);

        // Scoring & Parametrization paths
        paths.put(Paths.get(sysSource, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_DEFAULT).toString(),
                Paths.get(sysDest, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_DEFAULT).toString());
        paths.put(Paths.get(sysSource, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_ENV).toString(),
                Paths.get(sysDest, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_ENV).toString());
        paths.put(Paths.get(sysSource, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_SKELETON).toString(),
                Paths.get(sysDest, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_SKELETON).toString());
        paths.put(Paths.get(sysSource, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_TEMPLATE).toString(),
                Paths.get(sysDest, ProductConstant.AML_SYSTEM_SCORING, ProductConstant.AML_SYSTEM_SCORING_TEMPLATE).toString());
        paths.put(Paths.get(sysSource, ProductConstant.AML_SYSTEM_SCORING,
                        ProductConstant.AML_SYSTEM_PARAMETRIZATION, ProductConstant.AML_SYSTEM_PARAMETRIZATION_BATCH).toString(),
                Paths.get(sysDest, ProductConstant.AML_SYSTEM_SCORING,
                        ProductConstant.AML_SYSTEM_PARAMETRIZATION, ProductConstant.AML_SYSTEM_PARAMETRIZATION_BATCH).toString());


        paths.put(Paths.get(sysSource, ProductConstant.AML_SYSTEM_SCORING,
                        ProductConstant.AML_SYSTEM_PARAMETRIZATION, ProductConstant.AML_SYSTEM_PARAMETRIZATION_IPLR,
                        ProductConstant.AML_SYSTEM_PARAMETRIZATION_BATCH_IPLR_SironAML, ProductConstant.AML_SYSTEM_PARAMETRIZATION_BATCH_IPLR_SironAML_REALSE).toString(),
                Paths.get(sysDest, ProductConstant.AML_SYSTEM_SCORING,
                        ProductConstant.AML_SYSTEM_PARAMETRIZATION, ProductConstant.AML_SYSTEM_PARAMETRIZATION_IPLR,
                        ProductConstant.AML_SYSTEM_PARAMETRIZATION_BATCH_IPLR_SironAML, ProductConstant.AML_SYSTEM_PARAMETRIZATION_BATCH_IPLR_SironAML_REALSE).toString());

        // Global
        paths.put(Paths.get(globalSource, ProductConstant.AML_GLOBAL_INIT, ProductConstant.AML_GLOBAL_INTT_LOG).toString(),
                Paths.get(globalDest, ProductConstant.AML_GLOBAL_INIT, ProductConstant.AML_GLOBAL_INTT_LOG).toString());
        paths.put(Paths.get(globalSource, ProductConstant.AML_GLOBAL_TEMPORARY).toString(),
                Paths.get(globalDest, ProductConstant.AML_GLOBAL_TEMPORARY).toString());

        // Custom
        paths.put(customSource, customDest);

        return paths;
    }

    public Map<String, FilterPaths> prepareFilteredPaths(Map<String, String> pathMap) {
        Map<String, FilterPaths> result = new HashMap<>();
        for (Map.Entry<String, String> entry : pathMap.entrySet()) {
            Path source = Paths.get(entry.getKey());
            if (Files.exists(source)) {
                logger.info("Adding path: {}", source);
                result.put(source.toString(), new FilterPaths(entry.getValue(), fileFilters, folderFilters));
            } else {
                logger.warn("Path not found: {}", source);
            }
        }
        return result;
    }
}
