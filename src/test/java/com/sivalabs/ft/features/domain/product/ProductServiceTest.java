package com.sivalabs.ft.features.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sivalabs.ft.features.DatabaseConfiguration;
import com.sivalabs.ft.features.EventPublisherTestConfiguration;
import com.sivalabs.ft.features.domain.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import({DatabaseConfiguration.class, EventPublisherTestConfiguration.class})
@TestPropertySource("classpath:application-test.properties")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testFindProductByCode() {
        String code = "intellij";
        Optional<Product> result = productService.findProductByCode(code);
        assertThat(result)
                .as("Product with code '%s' should exist".formatted(code))
                .isPresent();
        assertThat(result.get().getCode())
                .as("Product code does not match the expected value")
                .isEqualTo(code);
    }

    @Test
    void testFindAllProducts() {
        var products = productService.findAllProducts();
        assertThat(products).as("The product list should not be null").isNotNull();
        assertThat(products).as("The product list should not be empty").isNotEmpty();
    }

    @Test
    void testCreateProduct() {
        var command = new CreateProductCommand("new-code", "New Product", "Description", "image-url", "user");
        long prev = productRepository.findAll().size();
        Long productId = productService.createProduct(command);
        assertThat(productId).as("Product ID should not be null after creation").isNotNull();
        var createdProduct = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        assertThat(createdProduct.getCode())
                .as("Product code should match the command")
                .isEqualTo(command.code());

        assertThat(productRepository.findAll().size())
                .as("Product repository size should increase by 1 after creation")
                .isEqualTo(prev + 1);
    }

    @Test
    void testUpdateProduct() {
        String productCode = "intellij";
        var updateCommand = new UpdateProductCommand(
                productCode, "Updated Name", "Updated Description", "updated-image-url", "updater");
        productService.updateProduct(updateCommand);
        var updatedProduct = productRepository
                .findByCode(productCode)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product with code '%s' not found".formatted(productCode)));
        assertThat(updatedProduct)
                .usingRecursiveComparison()
                .comparingOnlyFields("name", "description", "imageUrl", "updatedBy")
                .isEqualTo(updateCommand);
    }

    @Test
    void testUpdateNonExistingProduct() {
        String nonExistentCode = "non-existent";
        var updateCommand =
                new UpdateProductCommand(nonExistentCode, "Some Name", "Some Description", "some-image-url", "user");
        assertThatThrownBy(() -> productService.updateProduct(updateCommand))
                .isInstanceOf(EntityNotFoundException.class)
                .as("Error message must contain product code")
                .hasMessageContaining(updateCommand.code());
    }
}
