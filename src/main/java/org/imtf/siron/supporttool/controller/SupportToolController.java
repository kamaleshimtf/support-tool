package org.imtf.siron.supporttool.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.imtf.siron.supporttool.constant.SupportToolConstant;
import org.imtf.siron.supporttool.model.ProductRequest;
import org.imtf.siron.supporttool.service.ProductInfoService;
import org.imtf.siron.supporttool.service.SystemInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

@Path("/api/support")
public class SupportToolController {

    private static final Logger logger = LoggerFactory.getLogger(SupportToolController.class);

    @Inject
    ProductInfoService productInfoService;

    @Inject
    SystemInfoService systemInfoService;

    @POST
    @Path("/product-info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed("admin")
    public Response fetchProductInfo(ProductRequest productRequest) throws IOException {
        java.nio.file.Path zipFilePath = productInfoService.getProductInfo(productRequest);

        File file = zipFilePath.toFile();
        long fileSize = Files.size(zipFilePath);

        StreamingOutput stream = output -> {
            try {
                Files.copy(zipFilePath, output);
                output.flush();
            } catch (IOException ioEx) {
                logger.error("Error while streaming ZIP file: {}", zipFilePath, ioEx);
                throw new WebApplicationException("File streaming failed", ioEx);
            }
        };

        logger.info("Returning ZIP file '{}' ({} bytes)", file.getName(), fileSize);

        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, fileSize)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }

    @GET
    @Path("/system-info")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed("admin")
    public Response systemInfo() {
        try {

            String zipFileName = systemInfoService.generateZipName(SupportToolConstant.SYSTEM_INFO_FOLDER_NAME);
            java.nio.file.Path zipStream = systemInfoService.collectAndZip(SupportToolConstant.SYSTEM_INFO_FOLDER_NAME, zipFileName);

            File file = zipStream.toFile();
            FileInputStream fileInputStream = new FileInputStream(file);

            return Response.ok(fileInputStream)
                    .header("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"")
                    .header("Content-Length", Files.size(zipStream))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while generating ZIP: " + e.getMessage())
                    .build();
        }
    }
}
