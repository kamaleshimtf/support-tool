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

        if (zipFilePath == null) {
            logger.error("Received null zipFilePath from productInfoService");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to generate product info ZIP file.")
                    .build();
        }

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
    public Response systemInfo() throws IOException{

            String zipFileName = systemInfoService.generateZipName(SupportToolConstant.SYSTEM_INFO_FOLDER_NAME);
            java.nio.file.Path zipStream = systemInfoService.collectAndZip(SupportToolConstant.SYSTEM_INFO_FOLDER_NAME, zipFileName);

            File file = zipStream.toFile();
            FileInputStream fileInputStream = new FileInputStream(file);

            return Response.ok(fileInputStream)
                    .header("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"")
                    .header("Content-Length", Files.size(zipStream))
                    .build();
    }

    @POST
    @Path("/collect-info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed("admin")
    public Response collectInfo(ProductRequest productRequest) throws IOException {
        java.nio.file.Path productZip = null;
        java.nio.file.Path systemZip = null;

        try {
            productZip = productInfoService.getProductInfo(productRequest);
            logger.info("Product info ZIP generated: {}",
                    productZip != null ? productZip.toString() : "NULL");
        } catch (Exception e) {
            logger.error("Failed to generate product info ZIP", e);
        }

        try {
            String sysZipName = systemInfoService.generateZipName(SupportToolConstant.SYSTEM_INFO_FOLDER_NAME);
            systemZip = systemInfoService.collectAndZip(SupportToolConstant.SYSTEM_INFO_FOLDER_NAME, sysZipName);
            logger.info("System info ZIP generated: {}",
                    systemZip != null ? systemZip.toString() : "NULL");
        } catch (Exception e) {
            logger.error("Failed to generate system info ZIP", e);
        }

        if (productZip == null && systemZip == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to generate both product and system info.")
                    .build();
        }

        // Create a final ZIP containing whichever files exist
        java.nio.file.Path finalZip = Files.createTempFile("support-collect-", ".zip");
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(finalZip))) {
            if (productZip != null) {
                addToZip(zos, productZip.toFile(), "product-info.zip");
            }
            if (systemZip != null) {
                addToZip(zos, systemZip.toFile(), "system-info.zip");
            }
        }

        long fileSize = Files.size(finalZip);
        StreamingOutput stream = output -> {
            try {
                Files.copy(finalZip, output);
                output.flush();
            } catch (IOException ioEx) {
                logger.error("Error while streaming final ZIP: {}", finalZip, ioEx);
                throw new WebApplicationException("File streaming failed", ioEx);
            }
        };

        logger.info("Returning merged ZIP '{}' ({} bytes)", finalZip.getFileName(), fileSize);

        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + finalZip.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, fileSize)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }

    private void addToZip(java.util.zip.ZipOutputStream zos, File file, String entryName) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            zos.putNextEntry(new java.util.zip.ZipEntry(entryName));
            fis.transferTo(zos);
            zos.closeEntry();
        }
    }
}
