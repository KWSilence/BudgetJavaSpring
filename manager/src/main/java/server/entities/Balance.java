package server.entities;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "balance")
public class Balance
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @CreationTimestamp
  private Timestamp create_date;

  private Integer credit;
  private Integer debit;
  private Integer amount;

  public Balance()
  {
    this.credit = 0;
    this.debit = 0;
    this.amount = 0;
  }

  public Balance(Integer credit, Integer debit, Integer amount)
  {
    this.credit = credit;
    this.debit = debit;
    this.amount = amount;
  }

  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  public Integer getCredit()
  {
    return credit;
  }

  public void setCredit(Integer credit)
  {
    this.credit = credit;
  }

  public Integer getDebit()
  {
    return debit;
  }

  public void setDebit(Integer debit)
  {
    this.debit = debit;
  }

  public Integer getAmount()
  {
    return amount;
  }

  public void setAmount(Integer amount)
  {
    this.amount = amount;
  }
}
