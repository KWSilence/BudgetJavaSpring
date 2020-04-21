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
    if (!isExistById(id))
    {
      throw new MException("Balance by this id does not exist");
    }
    return repository.findById(id).get();
  }

  private boolean isExistById(Long id)
  {
    Optional<Balance> balance = repository.findById(id);
    return balance.isPresent();
  }

  public void addOrUpdate(Balance balance)
  {
    repository.saveAndFlush(balance);
  }

}
