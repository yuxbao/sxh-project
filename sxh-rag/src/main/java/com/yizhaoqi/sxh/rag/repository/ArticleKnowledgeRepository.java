package com.yizhaoqi.sxh.rag.repository;

import com.yizhaoqi.sxh.rag.model.ArticleKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArticleKnowledgeRepository extends JpaRepository<ArticleKnowledge, Long> {

    Optional<ArticleKnowledge> findByArticleId(Long articleId);

    Optional<ArticleKnowledge> findByFileMd5(String fileMd5);

    List<ArticleKnowledge> findByFileMd5In(Collection<String> fileMd5List);

    void deleteByArticleId(Long articleId);
}
