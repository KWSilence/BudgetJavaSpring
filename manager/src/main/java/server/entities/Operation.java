package server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "operations")
public class Operation
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "article_id")
  @JsonIgnore
  private Article article;

  private Double debit;
  private Double credit;

  @CreationTimestamp
  private Timestamp create_date;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "balance_id")
  @JsonIgnore
  private Balance balance;

  public Operation(Article article, Double debit, Double credit, Balance balance)
  {
    this.article = article;
    this.debit = debit;
    this.credit = credit;
    this.balance = balance;
  }

  Operation()
  {

  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public Article getArticle()
  {
    return article;
  }

  public String getArticleName()
  {
    return article.getName();
  }

  public Long getBalanceID()
  {
    return balance.getId();
  }

  public void setArticle(Article article)
  {
    this.article = article;
  }

  public Double getDebit()
  {
    return debit;
  }

  public void setDebit(Double debit)
  {
    this.debit = debit;
  }

  public Double getCredit()
  {
    return credit;
  }

  public void setCredit(Double credit)
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

  public Timestamp getCreate_date()
  {
    return create_date;
  }
}
