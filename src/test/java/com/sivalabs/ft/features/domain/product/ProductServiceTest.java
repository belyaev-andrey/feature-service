package com.sivalabs.ft.features.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.sivalabs.ft.features.DatabaseConfiguration;
import com.sivalabs.ft.features.EventPublisherTestConfiguration;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import({DatabaseConfiguration.class, EventPublisherTestConfiguration.class})
@TestPropertySource("classpath:application-test.properties")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void testFindProductByCode() {
        String code = "intellij";
        Product product = new Product();
        product.setCode(code);

        when(productRepository.findByCode(code)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findProductByCode(code);

        assertThat(result)
                .as("Product with code '%s' should exist".formatted(code))
                .isPresent();
        assertThat(result.get().getCode())
                .as("Product code does not match the expected value")
                .isEqualTo(code);

        verify(productRepository).findByCode(code);
    }

    @Test
    void testFindAllProducts() {
        List<Product> mockProducts = new ArrayList<>();
        Product product = new Product();
        product.setCode("intellij");
        mockProducts.add(product);

        when(productRepository.findAll()).thenReturn(mockProducts);

        var products = productService.findAllProducts();

        assertThat(products).as("The product list should not be null").isNotNull();
        assertThat(products).as("The product list should not be empty").isNotEmpty();

        verify(productRepository).findAll();
    }

    @Test
    void testCreateProduct() {
        var command = new CreateProductCommand("new-code", "New Product", "Description", "image-url", "user");

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setCode(command.code());
        savedProduct.setName(command.name());
        savedProduct.setDescription(command.description());
        savedProduct.setImageUrl(command.imageUrl());
        savedProduct.setCreatedBy(command.createdBy());

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));

        Long productId = productService.createProduct(command);

        assertThat(productId).as("Product ID should not be null after creation").isNotNull();
        assertThat(productId).isEqualTo(1L);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() {
        String productCode = "intellij";
        var updateCommand = new UpdateProductCommand(
                productCode, "Updated Name", "Updated Description", "updated-image-url", "updater");

        Product existingProduct = new Product();
        existingProduct.setCode(productCode);
        existingProduct.setName("Original Name");
        existingProduct.setDescription("Original Description");
        existingProduct.setImageUrl("original-image-url");

        Product updatedProduct = new Product();
        updatedProduct.setCode(productCode);
        updatedProduct.setName(updateCommand.name());
        updatedProduct.setDescription(updateCommand.description());
        updatedProduct.setImageUrl(updateCommand.imageUrl());
        updatedProduct.setUpdatedBy(updateCommand.updatedBy());

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        productService.updateProduct(updateCommand);

        verify(productRepository).findByCode(productCode);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateNonExistingProduct() {
        String nonExistentCode = "non-existent";
        var updateCommand =
                new UpdateProductCommand(nonExistentCode, "Some Name", "Some Description", "some-image-url", "user");

        when(productRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(updateCommand))
                .isInstanceOf(EntityNotFoundException.class)
                .as("Error message must contain product code")
                .hasMessageContaining(updateCommand.code());

        verify(productRepository).findByCode(nonExistentCode);
    }
}
