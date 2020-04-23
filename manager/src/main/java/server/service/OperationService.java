package server.service;

import org.springframework.stereotype.Service;
import server.entities.Article;
import server.entities.Balance;
import server.entities.Operation;
import server.entities.User;
import server.repos.MException;
import server.repos.OperationRepo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OperationService
{
  private final OperationRepo repository;
  private final BalanceService balanceService;
  private final ArticleService articleService;

  public OperationService(OperationRepo repository, BalanceService balanceService, ArticleService articleService)
  {
    this.repository = repository;
    this.balanceService = balanceService;
    this.articleService = articleService;
  }

  public Operation getById(Long id, User user) throws MException
  {
    Optional<Operation> operation = repository.findById(id);
    if (operation.isEmpty() || (!operation.get().getBalance().getId().equals(user.getBalance().getId())))
    {
      throw new MException("Operation by id not found");
    }
    return operation.get();
  }

  public Operation getOneById(Long id) throws MException
  {
    Optional<Operation> operation = repository.findById(id);
    if (operation.isEmpty())
    {
      throw new MException("Operation by id not found");
    }
    return operation.get();
  }

  public List<Operation> getByBalance(Balance balance) throws MException
  {
    List<Operation> operations = repository.findByBalance(balance);
    if (operations.isEmpty())
    {
      throw new MException("Operations not found");
    }
    return operations;
  }

  public List<Operation> getAll() throws MException
  {
    List<Operation> operations = repository.findAll();
    if (operations.isEmpty())
    {
      throw new MException("Operations not found");
    }
    return operations;
  }


  public void update(Long id, Article article, String debit, String credit, User user) throws MException
  {
    Operation operation = getById(id, user);
    Double numDebit = convertToDouble(debit, "Debit");
    Double numCredit = convertToDouble(credit, "Credit");
    checkOperation(article, numDebit, numCredit);

    double debitChange = (numDebit != null) ? (numDebit - operation.getDebit()) : 0;
    double creditChange = (numCredit != null) ? (numCredit - operation.getCredit()) : 0;
    if (article != null)
    {
      operation.setArticle(article);
    }

    Balance balance = user.getBalance();
    balance.changeDebit(debitChange);
    balance.changeCredit(creditChange);
    balance.updateAmount();
    balanceService.addOrUpdate(balance);
    operation.changeCredit(creditChange);
    operation.changeDebit(debitChange);
    repository.saveAndFlush(operation);
  }

  public void add(Article article, String debit, String credit, User user) throws MException
  {
    Double tmp = convertToDouble(debit, "Debit");
    Double numDebit = (tmp == null) ? 0 : tmp;
    tmp = convertToDouble(credit, "Credit");
    Double numCredit = (tmp == null) ? 0 : tmp;
    checkOperation(article, numDebit, numCredit);

    Balance balance = user.getBalance();
    balance.changeDebit(numDebit);
    balance.changeCredit(numCredit);
    balance.updateAmount();
    balanceService.addOrUpdate(balance);

    Operation operation = new Operation(article, numDebit, numCredit, balance);
    repository.saveAndFlush(operation);
  }

  public void delete(Operation operation)
  {
    Balance balance = operation.getBalance();
    balance.changeDebit(-operation.getDebit());
    balance.changeCredit(-operation.getCredit());
    balance.updateAmount();
    balanceService.addOrUpdate(balance);
    repository.delete(operation);
  }

  public void deleteAll(User user) throws MException
  {
    Balance balance = user.getBalance();
    List<Operation> operations = getByBalance(balance);
    repository.deleteAll(operations);
    balance.reset();
    balanceService.addOrUpdate(balance);
  }

  private void checkOperation(Article article, Double debit, Double credit) throws MException
  {
    if (article != null && (article.getName().trim().isEmpty() || !articleService.isExistByName(article.getName())))
    {
      throw new MException("Article name in operation not correct");
    }

    if (credit != null && (credit < 0 || (credit.toString().length() - credit.toString().indexOf('.')) > 3))
    {
      throw new MException("Operation credit not correct(negative or not *.00)");
    }

    if (debit != null && (debit < 0 || (debit.toString().length() - debit.toString().indexOf('.')) > 3))
    {
      throw new MException("Operation debit not correct(negative or not *.00)");
    }
  }

  public Double convertToDouble(String num, String name) throws MException
  {
    if (num == null)
    {
      return null;
    }

    if (num.trim().isEmpty())
    {
      return null;
    }
    double converted;
    try
    {
      converted = Double.parseDouble(num);
    }
    catch (Exception e)
    {
      throw new MException(name + " does not content double");
    }
    return converted;
  }

  public List<Operation> filterByArticleName(String article, List<Operation> operations) throws MException
  {
    if (article != null && !article.isEmpty())
    {
      operations = operations.stream().filter(o -> o.getArticleName().equals(article)).collect(Collectors.toList());
    }
    if (operations.isEmpty())
    {
      throw new MException("Operations by this article not found");
    }
    return operations;
  }
}
