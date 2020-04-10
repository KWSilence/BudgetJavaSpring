package server.repos;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import server.entities.Article;

import java.util.List;

public interface ArticleRepo extends CrudRepository<Article, Long> {
    List<Article> findByName(String name);

    @Modifying
    @Transactional
    @Query(value = "truncate table article;", nativeQuery = true)
    void truncateTable();
}
