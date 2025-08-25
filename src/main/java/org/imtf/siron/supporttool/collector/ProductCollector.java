package org.imtf.siron.supporttool.service.collector;

import org.imtf.siron.supporttool.model.ProductClientInfo;
import org.imtf.siron.supporttool.model.SironProductType;

public interface ProductCollector {
    void collect(SironProductType productType, ProductClientInfo productClientInfo);
}
