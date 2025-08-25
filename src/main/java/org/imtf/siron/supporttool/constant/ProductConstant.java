package org.imtf.siron.supporttool.constant;

import jakarta.enterprise.context.ApplicationScoped;
import org.imtf.siron.supporttool.model.SironProductType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ProductConfig {

    private static final String AML_ROOT = "AML_ROOT";
    private static final String KYC_ROOT = "KYC_ROOT";
    private static final String ZEIDON_ROOT = "FS_ZEIDON_ROOT";
    private static final String EMBARGO_ROOT = "EMB_ROOT";
    private static final String RAS_ROOT = "RAS_ROOT";
    private static final String RCC_ROOT = "RCC_ROOT";

    public static final String PRODUCT_CLIENT_FOLDER_NAME = "client";
    private static final Map<SironProductType, String> PRODUCT_ROOTMAP = new HashMap<>();

    public static final String PRODUCT_ALL_CLIENTS = "ALL";

    public static final String SOURCE_ENVIRONMENT = "/system/set_env";
    public static final String DESTINATION_ENVIRONMENT_FILENAME = "set_env";

    public ProductConfig() {
        initializeSironProductRoots();
    }

    public void initializeSironProductRoots(){
        PRODUCT_ROOTMAP.put(SironProductType.AML, AML_ROOT);
        PRODUCT_ROOTMAP.put(SironProductType.KYC, KYC_ROOT);
        PRODUCT_ROOTMAP.put(SironProductType.ZEIDON, ZEIDON_ROOT);
        PRODUCT_ROOTMAP.put(SironProductType.EMBARGO, EMBARGO_ROOT);
        PRODUCT_ROOTMAP.put(SironProductType.RAS, RAS_ROOT);
        PRODUCT_ROOTMAP.put(SironProductType.RCC, RCC_ROOT);
    }

    public String getProductTypeByRoot(SironProductType rootVar) {
        return PRODUCT_ROOTMAP.get(rootVar);
    }

    public SironProductType getProductTypeByRootValue(String productTypeName) {
        for (Map.Entry<SironProductType, String> entry : PRODUCT_ROOTMAP.entrySet()) {
            if (entry.getValue().equals(productTypeName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public SironProductType getEnumFromString(String productTypeName) {
        return Arrays.stream(SironProductType.values())
                .filter(type -> type.name().equalsIgnoreCase(productTypeName))
                .findFirst()
                .orElse(null);
    }
}
