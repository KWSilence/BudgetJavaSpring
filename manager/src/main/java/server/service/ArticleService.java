package server.service;

import org.springframework.stereotype.Service;
import server.entities.Article;
import server.repos.ArticleRepo;
import server.response.MException;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService
{
  private final ArticleRepo repository;

  public ArticleService(ArticleRepo repository)
  {
    this.repository = repository;
  }

  public Article getById(Long id) throws MException
  {
    if (!isExistById(id))
    {
      throw new MException("Article by this id not found");
    }
    return repository.findById(id).get();
  }

  public Article getById(String id) throws MException
  {
    long numId;
    try
    {
      numId = Long.parseLong(id);
    }
    catch (Exception e)
    {
      throw new MException("Article id should be num");
    }
    return getById(numId);
  }

  public Article getByName(String name) throws MException
  {
    if (!isExistByName(name))
    {
      throw new MException("Article with this name not found");
    }
    return repository.findByName(name);
  }

  public List<Article> getAll() throws MException
  {
    List<Article> articles = repository.findAll();
    if (articles.isEmpty())
    {
      throw new MException("Articles not found");
    }
    return articles;
  }

  private boolean isExistById(Long id)
  {
    Optional<Article> article = repository.findById(id);
    return article.isPresent();
  }

  public boolean isExistByName(String name)
  {
    Article article = repository.findByName(name);
    return (article != null);
  }

  public void addOrUpdate(Article article) throws MException
  {
    if (isExistByName(article.getName()))
    {
      throw new MException("This article name already used");
    }
    if (article.getName().trim().isEmpty())
    {
      throw new MException("Article name should not be empty");
    }
    repository.saveAndFlush(article);
  }
}
