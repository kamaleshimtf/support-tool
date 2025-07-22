package org.imtf.siron.supporttool.constant;

import jakarta.enterprise.context.ApplicationScoped;
import org.imtf.siron.supporttool.model.SironProductType;
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
    private static final Map<SironProductType, String> PRODUCT_ROOTMAP = new HashMap<>();


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
}
