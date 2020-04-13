package server.entities;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "operations")
public class Operation
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "article_id")
  private Article article;

  private Integer debit;
  private Integer credit;

  @CreationTimestamp
  private Timestamp create_date;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "balance_id")
  private Balance balance;

  Operation(Article article, Integer debit, Integer credit, Balance balance)
  {
    this.article = article;
    this.debit = debit;
    this.credit = credit;
    this.balance = balance;
  }

  Operation()
  {

  }

  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public Article getArticle()
  {
    return article;
  }

  public void setArticle(Article article)
  {
    this.article = article;
  }

  public Integer getDebit()
  {
    return debit;
  }

  public void setDebit(Integer debit)
  {
    this.debit = debit;
  }

  public Integer getCredit()
  {
    return credit;
  }

  public void setCredit(Integer credit)
  {
    this.credit = credit;
  }

  public Balance getBalance()
  {
    return balance;
  }

  public void setBalance(Balance balance)
  {
    this.balance = balance;
  }
}
