package com.sop.purchase.product.dto;

import com.sop.purchase.product.Product;
import jakarta.validation.constraints.NotBlank;

public class ProductDtos {
    public record CreateProductRequest(
            @NotBlank String code,
            @NotBlank String name,
            String specification,
            @NotBlank String unit,
            String remark
    ) {
    }

    public record UpdateProductRequest(String name, String specification, String unit, Boolean enabled, String remark) {
    }

    public record EnabledRequest(Boolean enabled) {
    }

    public record ProductResponse(Long id, String code, String name, String specification, String unit,
                                  Boolean enabled, String remark) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(product.getId(), product.getCode(), product.getName(),
                    product.getSpecification(), product.getUnit(), product.getEnabled(), product.getRemark());
        }
    }
}
