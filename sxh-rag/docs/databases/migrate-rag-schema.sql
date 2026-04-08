SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `display_name` VARCHAR(255) DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `role` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
  `external_source` VARCHAR(255) DEFAULT NULL,
  `external_user_id` VARCHAR(255) DEFAULT NULL,
  `org_tags` VARCHAR(255) DEFAULT NULL,
  `primary_org` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG本地扩展用户表';

CREATE TABLE IF NOT EXISTS `organization_tags` (
  `tag_id` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `parent_tag` VARCHAR(255) DEFAULT NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织标签表';

CREATE TABLE IF NOT EXISTS `file_upload` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_md5` VARCHAR(32) NOT NULL,
  `file_name` VARCHAR(255) DEFAULT NULL,
  `total_size` BIGINT NOT NULL,
  `status` INT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `org_tag` VARCHAR(255) DEFAULT NULL,
  `is_public` BIT(1) NOT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `merged_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传文件元数据';

CREATE TABLE IF NOT EXISTS `chunk_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_md5` VARCHAR(255) DEFAULT NULL,
  `chunk_index` INT NOT NULL,
  `chunk_md5` VARCHAR(255) DEFAULT NULL,
  `storage_path` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分片信息';

CREATE TABLE IF NOT EXISTS `document_vectors` (
  `vector_id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_md5` VARCHAR(32) NOT NULL,
  `chunk_id` INT NOT NULL,
  `text_content` LONGTEXT,
  `model_version` VARCHAR(32) DEFAULT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `org_tag` VARCHAR(50) DEFAULT NULL,
  `is_public` BIT(1) NOT NULL,
  PRIMARY KEY (`vector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档切片向量';

CREATE TABLE IF NOT EXISTS `conversations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question` TEXT NOT NULL,
  `answer` TEXT NOT NULL,
  `timestamp` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天对话记录';

CREATE TABLE IF NOT EXISTS `article_knowledge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `file_md5` VARCHAR(32) NOT NULL,
  `author_id` BIGINT DEFAULT NULL,
  `author_name` VARCHAR(255) DEFAULT NULL,
  `title` VARCHAR(512) NOT NULL,
  `summary` TEXT,
  `category_name` VARCHAR(255) DEFAULT NULL,
  `tags_text` TEXT,
  `article_url` VARCHAR(1024) DEFAULT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_knowledge_article_id` (`article_id`),
  UNIQUE KEY `uk_article_knowledge_file_md5` (`file_md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区文章知识索引';

DROP PROCEDURE IF EXISTS `sp_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `sp_add_index_if_missing`;
DROP PROCEDURE IF EXISTS `sp_add_fk_if_missing`;

DELIMITER $$
CREATE PROCEDURE `sp_add_column_if_missing`(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_column_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

CREATE PROCEDURE `sp_add_index_if_missing`(
  IN p_table_name VARCHAR(64),
  IN p_index_name VARCHAR(64),
  IN p_index_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = p_index_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD ', p_index_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

CREATE PROCEDURE `sp_add_fk_if_missing`(
  IN p_table_name VARCHAR(64),
  IN p_constraint_name VARCHAR(64),
  IN p_fk_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND CONSTRAINT_NAME = p_constraint_name
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD CONSTRAINT `', p_constraint_name, '` ', p_fk_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL `sp_add_column_if_missing`('users', 'display_name', '`display_name` VARCHAR(255) NULL AFTER `username`');
CALL `sp_add_column_if_missing`('users', 'avatar', '`avatar` VARCHAR(255) NULL AFTER `display_name`');
CALL `sp_add_column_if_missing`('users', 'external_source', '`external_source` VARCHAR(255) NULL AFTER `role`');
CALL `sp_add_column_if_missing`('users', 'external_user_id', '`external_user_id` VARCHAR(255) NULL AFTER `external_source`');
CALL `sp_add_column_if_missing`('users', 'org_tags', '`org_tags` VARCHAR(255) NULL AFTER `external_user_id`');
CALL `sp_add_column_if_missing`('users', 'primary_org', '`primary_org` VARCHAR(255) NULL AFTER `org_tags`');
CALL `sp_add_column_if_missing`('users', 'created_at', '`created_at` DATETIME(6) NULL AFTER `primary_org`');
CALL `sp_add_column_if_missing`('users', 'updated_at', '`updated_at` DATETIME(6) NULL AFTER `created_at`');
CALL `sp_add_index_if_missing`('users', 'uk_users_username', 'UNIQUE INDEX `uk_users_username` (`username`)');
CALL `sp_add_index_if_missing`('users', 'idx_users_external_mapping', 'INDEX `idx_users_external_mapping` (`external_source`, `external_user_id`)');

CALL `sp_add_column_if_missing`('organization_tags', 'created_at', '`created_at` DATETIME(6) NULL AFTER `created_by`');
CALL `sp_add_column_if_missing`('organization_tags', 'updated_at', '`updated_at` DATETIME(6) NULL AFTER `created_at`');
CALL `sp_add_index_if_missing`('organization_tags', 'idx_organization_tags_created_by', 'INDEX `idx_organization_tags_created_by` (`created_by`)');
CALL `sp_add_fk_if_missing`('organization_tags', 'fk_organization_tags_created_by_users', 'FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)');

CALL `sp_add_column_if_missing`('file_upload', 'org_tag', '`org_tag` VARCHAR(255) NULL AFTER `user_id`');
CALL `sp_add_column_if_missing`('file_upload', 'is_public', '`is_public` BIT(1) NOT NULL DEFAULT b''0'' AFTER `org_tag`');
CALL `sp_add_column_if_missing`('file_upload', 'created_at', '`created_at` DATETIME(6) NULL AFTER `is_public`');
CALL `sp_add_column_if_missing`('file_upload', 'merged_at', '`merged_at` DATETIME(6) NULL AFTER `created_at`');
CALL `sp_add_index_if_missing`('file_upload', 'uk_file_upload_file_md5_user_id', 'UNIQUE INDEX `uk_file_upload_file_md5_user_id` (`file_md5`, `user_id`)');
CALL `sp_add_index_if_missing`('file_upload', 'idx_file_upload_user_id', 'INDEX `idx_file_upload_user_id` (`user_id`)');
CALL `sp_add_index_if_missing`('file_upload', 'idx_file_upload_org_tag', 'INDEX `idx_file_upload_org_tag` (`org_tag`)');

CALL `sp_add_index_if_missing`('chunk_info', 'idx_chunk_info_file_md5', 'INDEX `idx_chunk_info_file_md5` (`file_md5`)');
CALL `sp_add_index_if_missing`('chunk_info', 'uk_chunk_info_file_md5_chunk_index', 'UNIQUE INDEX `uk_chunk_info_file_md5_chunk_index` (`file_md5`, `chunk_index`)');

CALL `sp_add_index_if_missing`('document_vectors', 'idx_document_vectors_file_md5', 'INDEX `idx_document_vectors_file_md5` (`file_md5`)');
CALL `sp_add_index_if_missing`('document_vectors', 'idx_document_vectors_user_id', 'INDEX `idx_document_vectors_user_id` (`user_id`)');
CALL `sp_add_index_if_missing`('document_vectors', 'idx_document_vectors_org_tag', 'INDEX `idx_document_vectors_org_tag` (`org_tag`)');

CALL `sp_add_index_if_missing`('conversations', 'idx_user_id', 'INDEX `idx_user_id` (`user_id`)');
CALL `sp_add_index_if_missing`('conversations', 'idx_timestamp', 'INDEX `idx_timestamp` (`timestamp`)');
CALL `sp_add_fk_if_missing`('conversations', 'fk_conversations_user_id_users', 'FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)');

CALL `sp_add_column_if_missing`('article_knowledge', 'article_url', '`article_url` VARCHAR(1024) NULL AFTER `tags_text`');
CALL `sp_add_column_if_missing`('article_knowledge', 'created_at', '`created_at` DATETIME(6) NULL AFTER `file_name`');
CALL `sp_add_column_if_missing`('article_knowledge', 'updated_at', '`updated_at` DATETIME(6) NULL AFTER `created_at`');
CALL `sp_add_index_if_missing`('article_knowledge', 'uk_article_knowledge_article_id', 'UNIQUE INDEX `uk_article_knowledge_article_id` (`article_id`)');
CALL `sp_add_index_if_missing`('article_knowledge', 'uk_article_knowledge_file_md5', 'UNIQUE INDEX `uk_article_knowledge_file_md5` (`file_md5`)');
CALL `sp_add_index_if_missing`('article_knowledge', 'idx_article_knowledge_author_id', 'INDEX `idx_article_knowledge_author_id` (`author_id`)');

DROP PROCEDURE IF EXISTS `sp_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `sp_add_index_if_missing`;
DROP PROCEDURE IF EXISTS `sp_add_fk_if_missing`;

SET FOREIGN_KEY_CHECKS = 1;
