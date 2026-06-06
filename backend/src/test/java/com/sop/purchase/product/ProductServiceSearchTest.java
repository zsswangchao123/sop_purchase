package com.sop.purchase.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.sop.purchase.product.dto.ProductDtos.CreateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceSearchTest {
    @Autowired
    private ProductService productService;

    @Test
    void searchMatchesCodeOrNameAndPaginates() {
        productService.create(new CreateProductRequest("SEARCH-TOMATO", "Tomato", null, "box", null));
        productService.create(new CreateProductRequest("SEARCH-CUCUMBER", "Cucumber", null, "box", null));

        var page = productService.search("tomato", true, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).code()).isEqualTo("SEARCH-TOMATO");
    }
}
