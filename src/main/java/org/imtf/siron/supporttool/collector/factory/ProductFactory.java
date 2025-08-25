package org.imtf.siron.supporttool.service.collector;

import org.imtf.siron.supporttool.model.SironProductType;
import org.imtf.siron.supporttool.service.collector.impl.KycCollector;

public class ProductFactory {

    public ProductCollector getProductCollector(SironProductType sironProductType) {
        switch (sironProductType) {
            case KYC -> new KycCollector();
        }
    }
}
