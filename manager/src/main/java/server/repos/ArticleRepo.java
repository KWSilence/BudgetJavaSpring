package server.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entities.Article;

@Repository
public interface ArticleRepo extends JpaRepository<Article, Long>
{
  Article findByName(String name);
}
