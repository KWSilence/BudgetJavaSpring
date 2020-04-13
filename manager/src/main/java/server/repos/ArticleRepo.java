package server.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import server.entities.Article;

import java.util.List;

@Repository
public interface ArticleRepo extends CrudRepository<Article, Long>
{
  List<Article> findByName(String name);
}
