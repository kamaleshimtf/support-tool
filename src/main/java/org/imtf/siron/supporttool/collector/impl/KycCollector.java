package org.imtf.siron.supporttool.collector.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.filter.acceptfilter.FilterChainAnd;
import org.imtf.siron.supporttool.filter.rejectfilter.RejectTopLimit;
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
public class KycCollector implements ProductCollector {

    private static final Logger logger = LoggerFactory.getLogger(KycCollector.class);
    private final List<Pattern> fileFilters = new ArrayList<>();
    private final List<Pattern> folderFilters = new ArrayList<>();

    @Inject
    EnvironmentManager environmentManager;

    @Inject
    FileManager fileManager;
    @Inject
    ProductConstant productConstant;

    @Override
    public void collect(String destinationPath, ProductType productType, ProductClientInfo productClientInfo) {

        logger.info("starting to collect KYC product information");
        String destinationFolder = fileManager.createSubFolder(destinationPath,productType.name());

        if (!productClientInfo.getClientIds().isEmpty()){
            getClientPaths(productType,destinationFolder,productClientInfo);
        }
        else {
            logger.info("No client IDs provided for {}", productType.name());
        }
        collectSystemPaths(destinationFolder, productClientInfo);
    }
    private void collectSystemPaths(String destinationPath, ProductClientInfo info) {
        logger.info("Collecting KYC system/custom folders");

        Map<String, String> systemPaths = getSystemPaths(info.getRootPath(), destinationPath);
        Map<String, FilterPaths> filteredPaths = new HashMap<>();

        for (Map.Entry<String, String> entry : systemPaths.entrySet()) {
            Path source = Paths.get(entry.getKey());
            Path dest = Paths.get(entry.getValue());

            if (Files.exists(source)) {
                logger.info("Adding system path: {}", source);
                filteredPaths.put(source.toString(), new FilterPaths(dest.toString(), new FilterChainAnd(fileFilters, folderFilters)));
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

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_WEB_CLIENT, ProductConstant.KYC_SYSTEM_WEB_CLIENT_PROPERTIES).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_WEB_CLIENT, ProductConstant.KYC_SYSTEM_WEB_CLIENT_PROPERTIES).toString());

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_SCORING, ProductConstant.KYC_SYSTEM_SCORING_DEFAULT).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_SCORING, ProductConstant.KYC_SYSTEM_SCORING_DEFAULT).toString());

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_SCORING, ProductConstant.KYC_SYSTEM_SCORING_ENV).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_SCORING, ProductConstant.KYC_SYSTEM_SCORING_ENV).toString());

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_SCORING, ProductConstant.KYC_SYSTEM_SCORING_WORK).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_SCORING, ProductConstant.KYC_SYSTEM_SCORING_WORK).toString());

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_INSTALL, ProductConstant.KYC_SYSTEM_INSTALL_LOG).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_INSTALL, ProductConstant.KYC_SYSTEM_INSTALL_LOG).toString());

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_TBELLERHOME, ProductConstant.KYC_SYSTEM_TBELLERHOME_SIRONEAI).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_TBELLERHOME, ProductConstant.KYC_SYSTEM_TBELLERHOME_SIRONEAI).toString());

        paths.put(Paths.get(sysSource, ProductConstant.KYC_SYSTEM_TBELLERHOME, ProductConstant.KYC_SYSTEM_TBELLERHOME_SIRONKYC).toString(),
                Paths.get(sysDest, ProductConstant.KYC_SYSTEM_TBELLERHOME, ProductConstant.KYC_SYSTEM_TBELLERHOME_SIRONKYC).toString());

        paths.put(customSource, customDest);

        return paths;
    }

    public void getClientPaths(ProductType productType, String destinationPath, ProductClientInfo productClientInfo) {

        logger.info("starting to collect KYC client information");

        if (productClientInfo.getClientIds().isEmpty()) {
            throw new NotFoundException("No client IDs found for product " + productType + " at " + destinationPath);
        }

        for(String clientId : productClientInfo.getClientIds()){

            String clientDestination = fileManager.createSubFolder(destinationPath, ProductConstant.PRODUCT_CLIENT_FOLDER, clientId);

            Map<String, String> env = environmentManager.getProductEnvironment(
                    productType, clientDestination, productClientInfo.getRootPath(), clientId);

            if (env.isEmpty()) {
                logger.warn("Environment variables not found for client: {}", clientId);
                continue;
            }
            String clientRoot = Paths.get(productClientInfo.getRootPath(), ProductConstant.PRODUCT_CLIENT_FOLDER, clientId).toString();
            Map<String, String> clientPaths = createClientPathMap(clientRoot, clientDestination);

            logger.info("Client Paths : {}", clientPaths);

            Map<String, FilterPaths> filteredPaths = prepareFilteredPaths(clientPaths);

            logger.info("Filtered Paths : {}", filteredPaths);
            fileManager.copySourceToDestinationPath(filteredPaths);
        }
    }
    private Map<String, String> createClientPathMap(String root, String dest) {
        Map<String, String> map = new HashMap<>();
        map.put(Paths.get(root, ProductConstant.KYC_CLIENT_LOG).toString(), Paths.get(dest, ProductConstant.KYC_CLIENT_LOG).toString());
        map.put(Paths.get(root, ProductConstant.KYC_CLIENT_CUSTOM).toString(), Paths.get(dest, ProductConstant.KYC_CLIENT_CUSTOM).toString());
        map.put(Paths.get(root, ProductConstant.KYC_CLIENT_DATA, ProductConstant.KYC_CLIENT_DATA_PRS).toString(),
                Paths.get(dest,  ProductConstant.KYC_CLIENT_DATA, ProductConstant.KYC_CLIENT_DATA_PRS).toString());
        map.put(Paths.get(root, ProductConstant.KYC_CLIENT_DATA, ProductConstant.KYC_CLIENT_DATA_INDICES).toString(),
                Paths.get(dest,  ProductConstant.KYC_CLIENT_DATA, ProductConstant.KYC_CLIENT_DATA_INDICES).toString());
        map.put(Paths.get(root, ProductConstant.KYC_CLIENT_ONLINE).toString(), Paths.get(dest, ProductConstant.KYC_CLIENT_ONLINE).toString());
        map.put(Paths.get(root, ProductConstant.KYC_CLIENT_WORKCUST).toString(), Paths.get(dest, ProductConstant.KYC_CLIENT_WORKCUST).toString());

        for (int i=1; i<99; i++){
            String sourcePath = Paths.get(root, ProductConstant.KYC_CLIENT_WORKCUST + i).toString();
            String destPath = Paths.get(dest, ProductConstant.KYC_CLIENT_WORKCUST + i).toString();

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

                logger.info("Adding path: {}", source);

                if (source.endsWith(ProductConstant.KYC_CLIENT_DATA_INDICES) ||
                        source.endsWith(ProductConstant.KYC_CLIENT_DATA_PRS) ) {
                    clientKycResult.put(source.toString(), new FilterPaths(entry.getValue(),
                            new FilterChainAnd(fileFilters, folderFilters)));
                }
                else if (source.endsWith(ProductConstant.KYC_CLIENT_ONLINE)) {
                    logger.info("online source: {}", source);
                    FilterChainAnd fileFilterChainAnd = new FilterChainAnd(fileFilters, folderFilters);
                    fileFilterChainAnd.addFilter(new RejectTopLimit(
                            source.toString(),
                            ProductConstant.KYC_CLIENT_ONLINE_PEPONL,
                            5
                    ));
                    clientKycResult.put(
                            source.toString(),
                            new FilterPaths(entry.getValue(),
                                    fileFilterChainAnd)
                    );
                }
                else {
                    clientKycResult.put(source.toString(), new FilterPaths(entry.getValue(), fileFilters, folderFilters));
                }
            } else {
                logger.warn("Path not found: {}", source);
            }
        }
        return clientKycResult;
    }

}
