package org.imtf.siron.supporttool.controller;

import io.quarkus.security.ForbiddenException;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.imtf.siron.supporttool.model.ProductInfo;
import org.imtf.siron.supporttool.model.ProductRequest;
import org.imtf.siron.supporttool.service.ProductInfoService;
import org.imtf.siron.supporttool.service.SystemInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
public class SupportToolControllerTest {
    ProductInfo productInfo = new ProductInfo();

    @Inject
    SupportToolController supportToolController;

    @InjectMock
    ProductInfoService productInfoService;

    @InjectMock
    SystemInfoService systemInfoService;

    @BeforeEach
    public void setup() {
        productInfo.setProductName("KYC");
        productInfo.setClientIds(List.of("ALL"));
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    public void testFetchAllProduct() throws IOException {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(List.of(productInfo));
        Path tempFile = Files.createTempFile("Test", ".zip");
        Mockito.when(productInfoService.getProductInfo(productRequest)).thenReturn(tempFile.toFile().toPath());

        Response response = supportToolController.fetchProductInfo(productRequest);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("application/octet-stream", response.getHeaderString("Content-Type"));
        assertTrue(response.getHeaderString("Content-Disposition").contains(tempFile.toFile().getName()));
        assertEquals(Files.size(tempFile), Long.parseLong(response.getHeaderString("Content-Length")));

        Files.deleteIfExists(tempFile);
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    public void testFetchAllProductInvalidInput() throws IOException{
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(null);

        Response response = supportToolController.fetchProductInfo(productRequest);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    public void testFetchAllProductEmptyProductList() throws IOException{
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(List.of());

        Response response = supportToolController.fetchProductInfo(productRequest);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    public void testFetchAllProductServiceException() throws IOException {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(List.of(productInfo));

        Mockito.when(productInfoService.getProductInfo(productRequest)).thenThrow(new RuntimeException("Service error"));

        Response response = supportToolController.fetchProductInfo(productRequest);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Service error"));
    }

    @TestSecurity(user = "test", roles = "user")
    @Test
    public void testFetchAllProductUnauthorizedAccess() {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(List.of(productInfo));

        Assertions.assertThrows(ForbiddenException.class, () -> {
            supportToolController.fetchProductInfo(productRequest);
        });
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    public void testFetchAllProductNullRequest() throws IOException{
        Response response = supportToolController.fetchProductInfo(null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    public void testFetchAllProductSpecialCharacters() throws IOException {
        ProductRequest productRequest = new ProductRequest();
        ProductInfo specialCharProduct = new ProductInfo();
        specialCharProduct.setProductName("Special@#%Name");
        specialCharProduct.setClientIds(List.of("Client1"));
        productRequest.setProducts(List.of(specialCharProduct));

        Path tempFile = Files.createTempFile("SpecialCharacter", ".zip");
        Mockito.when(productInfoService.getProductInfo(productRequest)).thenReturn(tempFile);

        Response response = supportToolController.fetchProductInfo(productRequest);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Files.deleteIfExists(tempFile);
    }

    @TestSecurity(user = "test", roles = "user")
    @Test
    public void testSystemInfoUnauthorizedAccess() {
        Assertions.assertThrows(ForbiddenException.class, () -> {
            supportToolController.systemInfo();
        });
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    void testSystemInfoSuccess() throws Exception {

        String zipFileName = "system-info.zip";
        Path tempFile = Files.createTempFile("system-info", ".zip");
        Files.writeString(tempFile, "dummy content");

        Mockito.when(systemInfoService.generateZipName(anyString()))
                .thenReturn(zipFileName);

        Mockito.when(systemInfoService.collectAndZip(anyString(), Mockito.eq(zipFileName)))
                .thenReturn(tempFile);


        Response response = supportToolController.systemInfo();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        String contentDisposition = (String) response.getHeaderString("Content-Disposition");
        assertTrue(contentDisposition.contains(zipFileName));

        long contentLength = Long.parseLong(response.getHeaderString("Content-Length"));
        assertEquals(Files.size(tempFile), contentLength);
    }

    @TestSecurity(user = "test", roles = "admin")
    @Test
    void testSystemInfoFailure() throws Exception {

        Mockito.when(systemInfoService.generateZipName(anyString()))
                .thenThrow(new RuntimeException("Simulated failure"));

        Response response = supportToolController.systemInfo();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        String entity = (String) response.getEntity();
        assertTrue(entity.contains("Error while generating ZIP"));
    }
}
