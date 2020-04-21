package server.service;


import org.springframework.stereotype.Service;
import server.entities.Balance;
import server.repos.BalanceRepo;
import server.repos.MException;

import java.util.Optional;

@Service
public class BalanceService
{
  private final BalanceRepo repository;

  public BalanceService(BalanceRepo repository)
  {
    this.repository = repository;
  }

  public Balance getById(Long id) throws MException
  {
    Optional<Balance> balance = repository.findById(id);
    if (balance.isEmpty())
    {
      throw new MException("Balance by this id does not exist");
    }
    return balance.get();
  }

  public void addOrUpdate(Balance balance)
  {
    repository.saveAndFlush(balance);
  }

}
