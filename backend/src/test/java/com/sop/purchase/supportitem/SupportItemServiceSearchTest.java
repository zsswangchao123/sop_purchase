package com.sop.purchase.supportitem;

import static org.assertj.core.api.Assertions.assertThat;

import com.sop.purchase.supportitem.dto.SupportItemDtos.CreateSupportItemRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SupportItemServiceSearchTest {
    @Autowired
    private SupportItemService supportItemService;

    @Test
    void searchFiltersByEnabledStatus() {
        var enabled = supportItemService.create(new CreateSupportItemRequest(
                "SEARCH-ENABLED", "Enabled item", "pcs", BigDecimal.ONE, null));
        var disabled = supportItemService.create(new CreateSupportItemRequest(
                "SEARCH-DISABLED", "Disabled item", "pcs", BigDecimal.ONE, null));
        supportItemService.setEnabled(disabled.id(), false);

        var page = supportItemService.search("search", true, PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting("id").contains(enabled.id()).doesNotContain(disabled.id());
    }
}
