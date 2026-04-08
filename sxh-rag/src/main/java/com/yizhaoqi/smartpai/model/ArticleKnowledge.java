package com.yizhaoqi.smartpai.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "article_knowledge")
public class ArticleKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false, unique = true)
    private Long articleId;

    @Column(name = "file_md5", nullable = false, length = 32, unique = true)
    private String fileMd5;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "category_name", length = 255)
    private String categoryName;

    @Column(name = "tags_text", columnDefinition = "TEXT")
    private String tagsText;

    @Column(name = "article_url", length = 1024)
    private String articleUrl;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
