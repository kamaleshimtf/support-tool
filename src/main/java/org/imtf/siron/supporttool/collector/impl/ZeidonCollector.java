package org.imtf.siron.supporttool.collector.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.filter.acceptfilter.AcceptExtension;
import org.imtf.siron.supporttool.filter.acceptfilter.AcceptLatestLimit;
import org.imtf.siron.supporttool.filter.acceptfilter.FilterChainAnd;
import org.imtf.siron.supporttool.helper.FileManager;
import org.imtf.siron.supporttool.model.FilterPaths;
import org.imtf.siron.supporttool.model.ProductClientInfo;
import org.imtf.siron.supporttool.model.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ApplicationScoped
public class ZeidonCollector implements ProductCollector {

    private static final Logger logger = LoggerFactory.getLogger(ZeidonCollector.class);
    private final List<Pattern> fileFilters = new ArrayList<>();
    private final List<Pattern> folderFilters = new ArrayList<>();

    private final FileManager fileManager;

    @Inject
    public ZeidonCollector(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void collect(String destinationPath, ProductType productType, ProductClientInfo productClientInfo) {

        logger.info("Collecting AML product data for type: {}", productType.name());

        String destinationFolder = fileManager.createSubFolder(destinationPath, productType.name());

        collectSystemPaths(destinationFolder, productClientInfo);
    }
    private void collectSystemPaths(String destinationPath, ProductClientInfo info) {
        logger.info("Collecting Zeidon folders");

        Map<String, String> systemPaths = getSystemPaths(info.getRootPath(), destinationPath);

        Map<String, FilterPaths> filteredPaths = new HashMap<>();

        for (Map.Entry<String, String> entry : systemPaths.entrySet()) {
            Path source = Paths.get(entry.getKey());
            Path dest = Paths.get(entry.getValue());

            if (Files.exists(source)) {
                logger.info("Adding system path: {}", source);
                if (source.endsWith(ProductConstant.ZEIDON_CUSTOM_LOG)) {
                    FileFilter fileFilter = new AcceptLatestLimit(7);
                    filteredPaths.put(source.toString(), new FilterPaths(dest.toString(), fileFilter));
                }
                else if(source.endsWith(ProductConstant.ZEIDON_CUSTOM_ZAPP)|| source.endsWith(ProductConstant.ZEIDON_SYSTEM_BINX)){
                    FileFilter fileFilter = new AcceptExtension(".ini");
                    filteredPaths.put(source.toString(), new FilterPaths(dest.toString(), fileFilter));
                }
                else {
                    filteredPaths.put(source.toString(), new FilterPaths(dest.toString(), new FilterChainAnd(fileFilters, folderFilters)));
                }
            } else {
                logger.warn("Path does not exist: {}", source);
            }
        }
        fileManager.copySourceToDestinationPath(filteredPaths);
    }
    private Map<String, String> getSystemPaths(String root, String dest) {
        Map<String, String> zeidonSystemPaths = new HashMap<>();

        String sysSource = Paths.get(root, ProductConstant.PRODUCT_SYSTEM_FOLDER).toString();
        String sysDest = fileManager.createSubFolder(dest, ProductConstant.PRODUCT_SYSTEM_FOLDER);

        String customSource = Paths.get(root, ProductConstant.PRODUCT_CUSTOM_FOLDER).toString();
        String customDest = fileManager.createSubFolder(dest, ProductConstant.PRODUCT_CUSTOM_FOLDER);
        File zeidonBin = new File(Paths.get(sysSource, ProductConstant.ZEIDON_SYSTEM_BIN).toString());

            zeidonSystemPaths.put(Paths.get(sysSource, ProductConstant.ZEIDON_SYSTEM_ZAPP).toString(),
                    Paths.get(sysDest, ProductConstant.ZEIDON_SYSTEM_ZAPP).toString());

            zeidonSystemPaths.put(Paths.get(sysSource, ProductConstant.ZEIDON_SYSTEM_BIN, ProductConstant.ZEIDON_SYSTEM_BIN_RELEASEINFO).toString(),
                    Paths.get(sysDest, ProductConstant.ZEIDON_SYSTEM_BIN, ProductConstant.ZEIDON_SYSTEM_BIN_RELEASEINFO).toString());

            zeidonSystemPaths.put(Paths.get(sysSource, ProductConstant.ZEIDON_SYSTEM_BINX).toString(),
                    Paths.get(sysDest, ProductConstant.ZEIDON_SYSTEM_BINX).toString());

            zeidonSystemPaths.put(Paths.get(customSource, ProductConstant.ZEIDON_CUSTOM_LOG).toString(),
                    Paths.get(customDest, ProductConstant.ZEIDON_CUSTOM_LOG).toString());

            zeidonSystemPaths.put(Paths.get(customSource, ProductConstant.ZEIDON_CUSTOM_ZAPP).toString(),
                    Paths.get(customDest, ProductConstant.ZEIDON_CUSTOM_ZAPP).toString());

        return zeidonSystemPaths;
    }
}
