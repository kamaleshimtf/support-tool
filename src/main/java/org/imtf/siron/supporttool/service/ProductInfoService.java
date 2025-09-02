package org.imtf.siron.supporttool.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.constant.ProductConstant;
import org.imtf.siron.supporttool.constant.SupportToolConstant;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.helper.EnvironmentManager;
import org.imtf.siron.supporttool.helper.FileManager;
import org.imtf.siron.supporttool.model.ProductClientInfo;
import org.imtf.siron.supporttool.model.ProductInfo;
import org.imtf.siron.supporttool.model.ProductRequest;
import org.imtf.siron.supporttool.model.ProductType;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.collector.factory.ProductFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProductInfoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductInfoService.class);

    public Map<ProductType, ProductClientInfo> productRootMap = new HashMap<>();
    public String destinationPath;

    @Inject
    FileManager fileManager;

    @Inject
    EnvironmentManager environmentManager;

    @Inject
    ProductConstant productConstant;

    @Inject
    ProductFactory productFactory;


    public Path getProductInfo(ProductRequest productRequests) throws IOException {
        this.destinationPath = fileManager.createTempDirectory();
        searchProductInfo(productRequests);
        validateClientFolders(productRootMap);
        collectAllProducts(productRootMap);
        String timestamp = new SimpleDateFormat(SupportToolConstant.ZIP_FOLDER_NAME_TIME_FORMAT).format(new Date());
        String zipFileName =  timestamp + "_" + SupportToolConstant.SUPPORT_FOLDER_NAME + ".zip";
        return fileManager.zipCreation(
                Paths.get(this.destinationPath), zipFileName);
    }

    public void searchProductInfo(ProductRequest productRequests) {

        if (productRequests == null || productRequests.getProducts().isEmpty()) {
            logger.info("No products found in the request");
            throw new NotFoundException("No products found in the request");
        }

        for (ProductInfo productInfo : productRequests.getProducts()) {

            String productName = productInfo.getProductName();
            List<String> clientIds = productInfo.getClientIds();

            if (productName == null || productName.isEmpty()) {
                logger.info("Product name not found in the request");
                throw new NotFoundException("Product name not found in the request");
            }

            if (clientIds == null || clientIds.isEmpty()) {
                logger.info("Client Ids not found in the request");
                throw new NotFoundException("Client Ids not found in the request");
            }

            logger.info("Searching product info for {}", productName);

            ProductType productType = productConstant.getEnumFromString(productName);
            if (productType == null) {
                logger.info("Invalid product name: {}", productName);
                throw new NotFoundException("Invalid product name: " + productName);
            }

            if (productType == ProductType.ALL) {
                logger.info("Searching ALL product info");
                productRootMap.putAll(getAllProductInfo(environmentManager.getAllEnvironment(), clientIds));
            } else {
                logger.info("Found {} product type", productType);
                String rootPath = productConstant.getProductTypeByRoot(productType);
                String environmentPath = environmentManager.getEnvironmentByKey(rootPath);
                productRootMap.put(productType, new ProductClientInfo(environmentPath, clientIds));
            }
        }
    }

    public void validateClientFolders(Map<ProductType, ProductClientInfo> productRootMap) {

        for (Map.Entry<ProductType, ProductClientInfo> productEntry : productRootMap.entrySet()) {
            ProductType productType = productEntry.getKey();
            ProductClientInfo productClientInfo = productEntry.getValue();
            String productRoot = productClientInfo.getRootPath();
            List<String> clientIds = productClientInfo.getClientIds();

            if (productType.equals(ProductType.ZEIDON)) {
                logger.info("No client folders expected for Zeidon product at path: {}", productRoot);
                continue;
            }

            if (productType.equals(ProductType.ALL)) {
                logger.info("Collect All client folders for product at path: {}", productRoot);
                continue;
            }

            if (productRoot == null) {
                logger.warn("Product [{}] is not installed.", productType);
                throw new NotFoundException("Product [" + productType + "] is not installed.");
            }

            if (!clientIds.contains(ProductConstant.PRODUCT_ALL_CLIENTS)) {
                for (String clientId : clientIds) {
                    if (fileManager.isClientFolderExists(productRoot, clientId)) {
                        logger.warn("Missing client folder for product [{}], clientId [{}], path: {}",
                                productType, clientId, productRoot);
                        throw new NotFoundException(String.format(
                                "Client folder not found for product [%s] at path [%s], clientId: [%s]",
                                productType, productRoot, clientId
                        ));
                    }
                }
            }
            else {
                if (fileManager.isClientFolderExists(productRoot, ProductConstant.PRODUCT_ALL_CLIENTS)) {

                    logger.info("Collect All client Ids for product at path: {}", productRoot);
                    productClientInfo.setClientIds(
                            fileManager.getClientIds(Paths.get(productRoot, ProductConstant.PRODUCT_CLIENT_FOLDER).toString())
                    );
                }
            }

            logger.info("All client folders exist for product: {}, path: {}", productType, productRoot);
        }
    }

    public Map<ProductType, ProductClientInfo> getAllProductInfo(Map<String, String> systemEnvironment, List<String> clientIds) {

        if (systemEnvironment == null || systemEnvironment.isEmpty()){
            logger.info("system environment is empty");
            return new HashMap<>();
        }

        Map<ProductType, ProductClientInfo> productTypeMap = new HashMap<ProductType, ProductClientInfo>();

        for (String key : systemEnvironment.keySet()){
            ProductType productType = productConstant.getProductTypeByRootValue(key);
            if (productType != null){
                logger.info("Found product type {}", productType);
                productTypeMap.put(productType,
                        new ProductClientInfo(systemEnvironment.get(key),clientIds)
                );
            }
        }
        return productTypeMap;
    }

    public void collectAllProducts(Map<ProductType, ProductClientInfo> productRootMap) {

        for (Map.Entry<ProductType, ProductClientInfo> productEntry : productRootMap.entrySet()) {
            ProductCollector collector = productFactory.getProductCollector(productEntry.getKey());
            if (collector != null) {
                collector.collect(this.destinationPath,productEntry.getKey(), productEntry.getValue());
            }
        }
    }
}
