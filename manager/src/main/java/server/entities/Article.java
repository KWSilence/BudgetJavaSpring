package server.entities;

import javax.persistence.*;

@Entity
@Table(name = "articles")
public class Article
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String name;

  public Article()
  {

  }

  public Article(String name)
  {
    this.name = name;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }
}