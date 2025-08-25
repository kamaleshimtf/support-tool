package org.imtf.siron.supporttool.collector;

import org.imtf.siron.supporttool.model.ProductClientInfo;
import org.imtf.siron.supporttool.model.ProductType;

public interface ProductCollector {
    void collect(String destinationPath, ProductType productType, ProductClientInfo productClientInfo);
}
