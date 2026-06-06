package com.sop.purchase.supportitem.dto;

import com.sop.purchase.supportitem.SupportItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class SupportItemDtos {
    public record CreateSupportItemRequest(
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String unit,
            @NotNull @PositiveOrZero BigDecimal defaultPrice,
            String remark
    ) {
    }

    public record UpdateSupportItemRequest(String name, String unit, BigDecimal defaultPrice,
                                           Boolean enabled, String remark) {
    }

    public record EnabledRequest(Boolean enabled) {
    }

    public record SupportItemResponse(Long id, String code, String name, String unit, BigDecimal defaultPrice,
                                      Boolean enabled, String remark) {
        public static SupportItemResponse from(SupportItem item) {
            return new SupportItemResponse(item.getId(), item.getCode(), item.getName(), item.getUnit(),
                    item.getDefaultPrice(), item.getEnabled(), item.getRemark());
        }
    }
}
