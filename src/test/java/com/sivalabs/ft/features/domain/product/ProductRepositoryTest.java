package com.sivalabs.ft.features.domain.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.sivalabs.ft.features.DatabaseConfiguration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import(DatabaseConfiguration.class)
@TestPropertySource("classpath:application-test.properties")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testFindByCode() {
        String code = "intellij";
        Optional<Product> productByCode = productRepository.findByCode(code);
        assertThat(productByCode)
                .as("Product with code '%s' should exists".formatted(code))
                .isPresent();
        assertThat(productByCode.get().getCode())
                .as("Product code does not match condition")
                .isEqualTo(code);
    }
}
