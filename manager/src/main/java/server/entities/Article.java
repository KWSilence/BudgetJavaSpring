package server.entities;

import com.google.gson.annotations.Expose;

import javax.persistence.*;

@Entity
@Table(name = "articles")
public class Article
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Expose
  private Long id;

  @Column(unique = true)
  @Expose
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