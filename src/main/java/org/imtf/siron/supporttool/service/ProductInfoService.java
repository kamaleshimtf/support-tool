package org.imtf.siron.supporttool.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.constant.ProductConfig;
import org.imtf.siron.supporttool.helper.Environment;
import org.imtf.siron.supporttool.helper.FileManager;
import org.imtf.siron.supporttool.model.SironProductInfo;
import org.imtf.siron.supporttool.model.SironProductRequest;
import org.imtf.siron.supporttool.model.SironProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProductInfoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductInfoService.class);

    @Inject
    FileManager fileManager;

    @Inject
    Environment environment;

    @Inject
    ProductConfig productConfig;

    public String getProductInfo(SironProductRequest sironProductRequests) {
        String tempDirectory = fileManager.createTempDirectory();
        searchProductInfo(sironProductRequests);
        return "Zip folder";
    }

    public void searchProductInfo(SironProductRequest sironProductRequests) {

        for (SironProductInfo sironProductInfo : sironProductRequests.getProducts()) {

            SironProductType sironProductType = SironProductType.valueOf(sironProductInfo.getProductName().toUpperCase());
            switch (sironProductType){
                case AML:
                    System.out.println(productConfig.getProductTypeByRoot(sironProductType));
                    break;
                case KYC:
                    System.out.println(environment.getProperty(productConfig.getProductTypeByRoot(sironProductType)));
                    break;
                case EMBARGO:
                    System.out.println("EMBARGO: " + sironProductInfo.getProductName());
                    break;
                case RCC:
                    System.out.println("RCC: " + sironProductInfo.getProductName());
                    break;
                case RAS:
                    System.out.println("RAS: " + sironProductInfo.getProductName());
                    break;
                case ZEIDON:
                    System.out.println("ZEIDON: " + sironProductInfo.getProductName());
                    break;
                case ALL:
                    System.out.println("ALL: " + sironProductInfo.getProductName());
                    break;
            }
        }
    }
}
