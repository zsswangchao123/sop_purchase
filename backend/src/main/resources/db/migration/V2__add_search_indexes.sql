CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_product_enabled ON product(enabled);
CREATE INDEX idx_support_item_name ON support_item(name);
CREATE INDEX idx_support_item_enabled ON support_item(enabled);
CREATE INDEX idx_purchase_list_status_created ON purchase_list(status, created_at);
CREATE INDEX idx_purchase_list_remark ON purchase_list(remark);
