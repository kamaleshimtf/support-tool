package org.imtf.siron.supporttool.collector.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.imtf.siron.supporttool.collector.ProductCollector;
import org.imtf.siron.supporttool.collector.impl.*;
import org.imtf.siron.supporttool.model.ProductType;

@ApplicationScoped
public class ProductFactory {

    @Inject
    KycCollector kycCollector;

    @Inject
    AmlCollector amlCollector;

    @Inject
    ZeidonCollector zeidonCollector;

    @Inject
    EmbargoCollector embargoCollector;

    @Inject
    RasCollector rasCollector;

    public ProductCollector getProductCollector(ProductType productType) {
        return switch (productType) {
            case AML -> amlCollector;
            case KYC -> kycCollector;
            case ZEIDON -> zeidonCollector;
            case EMBARGO -> embargoCollector;
            case RAS -> rasCollector;
            case RCC -> null;
            case ALL -> null;
        };
    }
}
