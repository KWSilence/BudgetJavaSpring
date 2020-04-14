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
  private Long id;

  @CreationTimestamp
  private Timestamp create_date;

  private Double credit;
  private Double debit;
  private Double amount;

  public Balance()
  {
    this.credit = 0.0;
    this.debit = 0.0;
    this.amount = 0.0;
  }

  public Balance(Double credit, Double debit, Double amount)
  {
    this.credit = credit;
    this.debit = debit;
    this.amount = amount;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public Double getCredit()
  {
    return credit;
  }

  public void setCredit(Double credit)
  {
    this.credit = credit;
  }

  public Double getDebit()
  {
    return debit;
  }

  public void setDebit(Double debit)
  {
    this.debit = debit;
  }

  public Double getAmount()
  {
    return amount;
  }

  public void setAmount(Double amount)
  {
    this.amount = amount;
  }

  public Timestamp getCreate_date()
  {
    return create_date;
  }
}
