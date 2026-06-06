CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  specification VARCHAR(128),
  unit VARCHAR(32) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  remark VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE support_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  unit VARCHAR(32) NOT NULL,
  default_price DECIMAL(12,2) NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  remark VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE product_support_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  support_item_id BIGINT NOT NULL,
  calc_type VARCHAR(16) NOT NULL,
  base_quantity DECIMAL(12,2),
  support_quantity DECIMAL(12,2) NOT NULL,
  rounding_mode VARCHAR(16) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  remark VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_rule_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_rule_support_item FOREIGN KEY (support_item_id) REFERENCES support_item(id)
);

CREATE TABLE purchase_list (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  list_no VARCHAR(64) NOT NULL UNIQUE,
  status VARCHAR(16) NOT NULL,
  source_type VARCHAR(16) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  remark VARCHAR(512),
  created_by VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  confirmed_at DATETIME,
  purchased_at DATETIME
);

CREATE TABLE purchase_list_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  purchase_list_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_code_snapshot VARCHAR(64) NOT NULL,
  product_name_snapshot VARCHAR(128) NOT NULL,
  unit_snapshot VARCHAR(32) NOT NULL,
  quantity DECIMAL(12,2) NOT NULL,
  remark VARCHAR(512),
  CONSTRAINT fk_list_product_list FOREIGN KEY (purchase_list_id) REFERENCES purchase_list(id),
  CONSTRAINT fk_list_product_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE purchase_list_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  purchase_list_id BIGINT NOT NULL,
  support_item_id BIGINT NOT NULL,
  support_item_code_snapshot VARCHAR(64) NOT NULL,
  support_item_name_snapshot VARCHAR(128) NOT NULL,
  unit_snapshot VARCHAR(32) NOT NULL,
  quantity DECIMAL(12,2) NOT NULL,
  default_unit_price_snapshot DECIMAL(12,2) NOT NULL,
  actual_unit_price DECIMAL(12,2) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  remark VARCHAR(512),
  CONSTRAINT fk_list_item_list FOREIGN KEY (purchase_list_id) REFERENCES purchase_list(id),
  CONSTRAINT fk_list_item_support_item FOREIGN KEY (support_item_id) REFERENCES support_item(id)
);

CREATE TABLE purchase_list_item_source (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  purchase_list_item_id BIGINT NOT NULL,
  purchase_list_product_id BIGINT NOT NULL,
  product_support_rule_id BIGINT NOT NULL,
  calculated_quantity DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_item_source_item FOREIGN KEY (purchase_list_item_id) REFERENCES purchase_list_item(id),
  CONSTRAINT fk_item_source_product FOREIGN KEY (purchase_list_product_id) REFERENCES purchase_list_product(id),
  CONSTRAINT fk_item_source_rule FOREIGN KEY (product_support_rule_id) REFERENCES product_support_rule(id)
);
