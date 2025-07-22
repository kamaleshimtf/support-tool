package org.imtf.siron.supporttool.controller;


import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.imtf.siron.supporttool.model.SironProductRequest;
import org.imtf.siron.supporttool.service.ProductInfoService;

@Path("api/siron")
public class SupportToolController {

    @Inject
    ProductInfoService productInfoService;

    @POST
    @Path("/product-info")
    public Response fetchProductInfo(SironProductRequest sironProductRequest) {
        return Response.ok()
                .entity(productInfoService.getProductInfo(sironProductRequest))
                .build();
    }
}
