package server.entities;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "balance")
public class Balance
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Expose
  private Long id;

  @CreationTimestamp
  @Expose
  private Timestamp create_date;

  @Expose
  private Double credit;
  @Expose
  private Double debit;
  @Expose
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

  public void changeCredit(Double credit)
  {
    this.credit = (this.credit * 100 + credit * 100) / 100;
  }

  public Double getDebit()
  {
    return debit;
  }

  public void setDebit(Double debit)
  {
    this.debit = debit;
  }

  public void changeDebit(Double debit)
  {
    this.debit = (this.debit * 100 + debit * 100) / 100;
  }

  public Double getAmount()
  {
    return amount;
  }

  public void setAmount(Double amount)
  {
    this.amount = amount;
  }

  public void updateAmount()
  {
    this.amount = (this.debit * 100 - this.credit * 100) / 100;
  }

  public Timestamp getCreate_date()
  {
    return create_date;
  }

  public void reset()
  {
    this.amount = 0.0;
    this.debit = 0.0;
    this.credit = 0.0;
  }
}
