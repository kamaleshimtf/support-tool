package org.imtf.siron.supporttool.constant;

import jakarta.enterprise.context.ApplicationScoped;
import org.imtf.siron.supporttool.model.ProductType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ProductConstant {

    private static final String AML_ROOT = "AML_ROOT";
    private static final String KYC_ROOT = "KYC_ROOT";
    private static final String ZEIDON_ROOT = "FS_ZEIDON_ROOT";
    private static final String EMBARGO_ROOT = "EMB_ROOT";
    private static final String RAS_ROOT = "RAS_ROOT";
    private static final String RCC_ROOT = "RCC_ROOT";

    private static final Map<ProductType, String> PRODUCT_ROOTMAP = new HashMap<>();

    public static final String PRODUCT_ALL_CLIENTS = "ALL";

    public static final String SOURCE_ENVIRONMENT = "/system/set_env";
    public static final String DESTINATION_ENVIRONMENT_FILENAME = "set_env.txt";

    public static final String SENSITIVE_CONTENT = "*****";
    public static final Set<String> SENSITIVE_CONTENT_EXTENSIONS = Set.of(".txt", ".log", ".conf", ".cfg", ".properties");


    public static final String PRODUCT_CLIENT_FOLDER = "client";
    public static final String PRODUCT_SYSTEM_FOLDER = "system";
    public static final String PRODUCT_CUSTOM_FOLDER = "custom";

    // AML product constants
    public static final String AML_CLIENT_LOG = "log";
    public static final String AML_CLIENT_LOG_ARCHIVE = "log_archive";
    public static final String AML_CLIENT_TEMPORARY = "temporary";
    public static final String AML_CLIENT_CUSTOM = "custom";
    public static final String AML_CLIENT_LOG_DELETE = "log_delete_archive";
    public static final String AML_SYSTEM_SCORING = "scoring";
    public static final String AML_SYSTEM_SCORING_DEFAULT = "default";
    public static final String AML_SYSTEM_SCORING_ENV = "env";
    public static final String AML_SYSTEM_SCORING_SKELETON = "skeleton";
    public static final String AML_SYSTEM_SCORING_TEMPLATE = "template";
    public static final String AML_SYSTEM_PARAMETRIZATION = "parameterization";
    public static final String AML_SYSTEM_PARAMETRIZATION_BATCH = "batch";
    public static final String AML_SYSTEM_PARAMETRIZATION_IPLR = "iplr";
    public static final String AML_SYSTEM_PARAMETRIZATION_BATCH_IPLR_SironAML = "SironAML";
    public static final String AML_SYSTEM_PARAMETRIZATION_BATCH_IPLR_SironAML_REALSE = "Release.txt";
    public static final String AML_GLOBAL = "global";
    public static final String AML_GLOBAL_INIT = "init";
    public static final String AML_GLOBAL_INTT_LOG = "log";
    public static final String AML_GLOBAL_TEMPORARY = "temporary";

    // KYC product constants
    public static final String KYC_CLIENT_LOG = "log";
    public static final String KYC_CLIENT_CUSTOM = "custom";
    public static final String KYC_CLIENT_DATA = "data";
    public static final String KYC_CLIENT_DATA_PRS = "prs";
    public static final String KYC_CLIENT_DATA_INDICES = "indices";
    public static final String KYC_CLIENT_ONLINE = "online";
    public static final String KYC_CLIENT_WORKCUST = "workcust";

    public static final String KYC_SYSTEM_WEB_CLIENT = "web_client";
    public static final String KYC_SYSTEM_WEB_CLIENT_PROPERTIES = "kyc_config.properties";
    public static final String KYC_SYSTEM_SCORING = "scoring";
    public static final String KYC_SYSTEM_SCORING_DEFAULT = "default";
    public static final String KYC_SYSTEM_SCORING_ENV = "env";
    public static final String KYC_SYSTEM_SCORING_WORK = "work";
    public static final String KYC_SYSTEM_INSTALL = "install";
    public static final String KYC_SYSTEM_INSTALL_LOG = "log";
    public static final String KYC_SYSTEM_TBELLERHOME = "tbellerhome";
    public static final String KYC_SYSTEM_TBELLERHOME_SIRONEAI = "sironeai";
    public static final String KYC_SYSTEM_TBELLERHOME_SIRONKYC = "sironkyc";
    public static final String KYC_CLIENT_ONLINE_PEPONL = "PEPONL";
    public static final String KYC_CLIENT_ONLINE_KYCAPI = "KYCAPI";

    // ZEIDON Product constants
    public static final String ZEIDON_SYSTEM_BIN = "bin";
    public static final String ZEIDON_SYSTEM_ZAPP = "zapp";
    public static final String ZEIDON_SYSTEM_BIN_RELEASEINFO = "releaseinfo.txt";
    public static final String ZEIDON_SYSTEM_BINX = "binx";

    public static final String ZEIDON_CUSTOM_LOG = "log";
    public static final String ZEIDON_CUSTOM_ZAPP = "zapp";

    // EMBARGO Product constants
    public static final String EMBARGO_CLIENT_WORKSWIFT = "workswift";
    public static final String EMBARGO_CLIENT_DATA = "data";
    public static final String EMBARGO_CLIENT_DATA_INDICES = "indices";
    public static final String EMBARGO_CLIENT_LOG = "log";
    public static final String EMBARGO_CLIENT_CUSTOM = "custom";
    public static final String EMBARGO_SYSTEM_WEB_CLIENT = "web_client";
    public static final String EMBARGO_SYSTEM_WEB_CLIENT_PROPERTIES = "embargo_config.properties";
    public static final String EMBARGO_SYSTEM_PARAMETRIZATION = "parameterization";
    public static final String EMBARGO_SYSTEM_PARAMETRIZATION_LPLR = "lplr";
    public static final String EMBARGO_SYSTEM_PARAMETRIZATION_SIRON_EMBARGO = "embargo_config";
    public static final String EMBARGO_SYSTEM_PARAMETRIZATION_RELEASE = "Release.txt";
    public static final String EMBARGO_SYSTEM_SCORING = "scoring";
    public static final String EMBARGO_SYSTEM_SCORING_DEFAULT = "default";
    public static final String EMBARGO_SYSTEM_SCORING_ENV = "env";
    public static final String EMBARGO_SYSTEM_SCORING_WORK = "work";
    public static final String EMBARGO_SYSTEM_INSTALL = "install";
    public static final String EMBARGO_SYSTEM_INSTALL_LOG = "log";
    public static final String EMBARGO_SYSTEM_TBELLERHOME = "tbellerhome";
    public static final String EMBARGO_SYSTEM_TBELLERHOME_SIRONEAI = "sironeai";
    public static final String EMBARGO_SYSTEM_TBELLERHOME_EMBARGO = "embargo";
    public static final String EMBARGO_CUSTOM_SCORING = "scoring";
    public static final String EMBARGO_CUSTOM_TOOL = "tool";
    public static final String EMBARGO_CUSTOM_WEB_CLIENT = "web_client";

    //RAS Product Constants
    public static final String RAS_CLIENT_CUSTOM = "custom";
    public static final String RAS_CLIENT_CUSTOM_REPORT = "report";
    public static final String RAS_CLIENT_DATA = "data";
    public static final String RAS_CLIENT_DATA_CONTROL = "control";
    public static final String RAS_CLIENT_DATA_REPORT_DE = "de";
    public static final String RAS_CLIENT_DATA_REPORT_RAS_REPORT_CSV = "ras_reports.csv";

    public static final String RAS_CUSTOM_REPORT = "report";
    public static final String RAS_CUSTOM_REPORT_STYLE = "tool";
    public static final String RAS_CUSTOM_WEBCLIENT = "web_client";
    public static final String RAS_CUSTOM_WEBCLIENT_PROPERTIES = "ras_config.properties";
    public static final String RAS_GLOBAL = "global";


    public ProductConstant() {
        initializeSironProductRoots();
    }

    public void initializeSironProductRoots() {
        PRODUCT_ROOTMAP.put(ProductType.AML, AML_ROOT);
        PRODUCT_ROOTMAP.put(ProductType.KYC, KYC_ROOT);
        PRODUCT_ROOTMAP.put(ProductType.ZEIDON, ZEIDON_ROOT);
        PRODUCT_ROOTMAP.put(ProductType.EMBARGO, EMBARGO_ROOT);
        PRODUCT_ROOTMAP.put(ProductType.RAS, RAS_ROOT);
        PRODUCT_ROOTMAP.put(ProductType.RCC, RCC_ROOT);
    }

    public String getProductTypeByRoot(ProductType rootVar) {
        return PRODUCT_ROOTMAP.get(rootVar);
    }

    public ProductType getProductTypeByRootValue(String productTypeName) {
        for (Map.Entry<ProductType, String> entry : PRODUCT_ROOTMAP.entrySet()) {
            if (entry.getValue().equals(productTypeName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ProductType getEnumFromString(String productTypeName) {
        return Arrays.stream(ProductType.values())
                .filter(type -> type.name().equalsIgnoreCase(productTypeName))
                .findFirst()
                .orElse(null);
    }
}
