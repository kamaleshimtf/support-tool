package org.imtf.siron.supporttool.service;

import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import org.imtf.siron.supporttool.exception.NotFoundException;
import org.imtf.siron.supporttool.helper.EnvironmentManager;
import org.imtf.siron.supporttool.model.ProductInfo;
import org.imtf.siron.supporttool.model.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ProductInfoServiceTest {
    ProductInfo productInfo = new ProductInfo();

    @Inject
    ProductInfoService productInfoService;

    @InjectMock
    EnvironmentManager environmentManager;

    @BeforeEach
    public void setup() {
        productInfo.setProductName("KYC");
        productInfo.setClientIds(List.of("ALL"));
    }

    @Test
    public void testProductRequestNull() {
        assertThrows(NotFoundException.class, () -> {
            productInfoService.getProductInfo(null);
        });
    }

    @Test
    public void testProductListEmpty() {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(List.of());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productInfoService.getProductInfo(productRequest);
        });
        assertTrue(exception.getMessage().contains("No products found"));
    }

    @Test
    public void testSearchProductInfo() {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProducts(List.of(productInfo));
        assertDoesNotThrow(() -> {
            productInfoService.searchProductInfo(productRequest);
        });
    }

    @Test
    public void testSearchProductInfoWithInvalidProduct() {
        ProductRequest productRequest = new ProductRequest();
        ProductInfo invalidProduct = new ProductInfo();
        invalidProduct.setProductName("INVALID_PRODUCT");
        invalidProduct.setClientIds(List.of("ALL"));
        productRequest.setProducts(List.of(invalidProduct));

        Exception exception = assertThrows(NotFoundException.class, () -> {
            productInfoService.searchProductInfo(productRequest);
        });
        assertTrue(exception.getMessage().contains("Invalid product name:"));
    }

    @Test
    public void testGetAllEnvironments() {
        when(environmentManager.getAllEnvironment()).thenReturn(Map.of("key1", "value1", "key2", "value2"));
        Map<String, String> environments = environmentManager.getAllEnvironment();
        assertNotNull(environments, "Environments map should not be null");
        assertTrue(environments.size() > 0, "Environments map should not be empty");
    }


    @Test
    public void testEnvironmentNull() {
        when(environmentManager.getAllEnvironment()).thenReturn(null);

        Map<String, String> environments = environmentManager.getAllEnvironment();
        assertNull(environments, "Environments map should be null");
    }

    @Test
    public void testEnvironmentException() {
        when(environmentManager.getAllEnvironment()).thenThrow(new RuntimeException("Environment fetch failed"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            environmentManager.getAllEnvironment();
        });
        assertTrue(exception.getMessage().contains("Environment fetch failed"));
    }

}
