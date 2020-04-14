package server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import server.entities.Article;
import server.entities.Balance;
import server.entities.Operation;
import server.entities.User;
import server.repos.ArticleRepo;
import server.repos.BalanceRepo;
import server.repos.OperationRepo;
import server.repos.UserRepo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ManagerController
{
  private final OperationRepo operationRepo;

  private final UserRepo userRepo;

  private final BalanceRepo balanceRepo;

  private final ArticleRepo articleRepo;

  public ManagerController(OperationRepo operationRepo, UserRepo userRepo, BalanceRepo balanceRepo,
                           ArticleRepo articleRepo)
  {
    this.operationRepo = operationRepo;
    this.userRepo = userRepo;
    this.balanceRepo = balanceRepo;
    this.articleRepo = articleRepo;
  }

  @GetMapping("/balance")
  public ResponseEntity<Balance> readBalance(@AuthenticationPrincipal User user)
  {
    if (user.isAdmin())
    {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(user.getBalance(), HttpStatus.OK);
  }

  @GetMapping("/balance/operations")
  public ResponseEntity<List<Operation>> readOperations(@AuthenticationPrincipal User user,
                                                        @RequestParam(required = false) String article,
                                                        @RequestParam(required = false) Long id)
  {
    List<Operation> operations = this.operationRepo.findByBalance(user.getBalance());
    if (article != null && !article.isEmpty())
    {
      operations = operations.stream().filter(o -> o.getArticleName().equals(article)).collect(Collectors.toList());
    }
    if (id != null)
    {
      operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    }
    return operations != null && !operations.isEmpty() ? new ResponseEntity<>(operations, HttpStatus.OK)
                                                       : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @GetMapping("/operations")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<List<Operation>> readAllOperations(@RequestParam(required = false) String username,
                                                           @RequestParam(required = false) String article)
  {
    List<Operation> operations = this.operationRepo.findAll();
    if (username != null)
    {
      User user = userRepo.findByUsername(username);
      if (user == null)
      {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      operations = operations.stream().filter(o -> o.getBalanceID().equals(user.getBalance().getId()))
                             .collect(Collectors.toList());
    }
    if (article != null && !article.isEmpty())
    {
      operations = operations.stream().filter(o -> o.getArticleName().equals(article)).collect(Collectors.toList());
    }
    return operations != null && !operations.isEmpty() ? new ResponseEntity<>(operations, HttpStatus.OK)
                                                       : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @GetMapping("/articles")
  public ResponseEntity<List<Article>> readArticles()
  {
    final List<Article> articles = (List<Article>) articleRepo.findAll();
    return articles != null && !articles.isEmpty() ? new ResponseEntity<>(articles, HttpStatus.OK)
                                                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PostMapping("/balance/operations")
  public ResponseEntity<?> addOperation(@AuthenticationPrincipal User user, @RequestParam String article,
                                        @RequestParam(required = false, defaultValue = "0.0") Double debit,
                                        @RequestParam(required = false, defaultValue = "0.0") Double credit)
  {
    List<Article> findArticle = articleRepo.findByName(article);
    if (findArticle.isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    if (debit < 0 || credit < 0)
    {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    if (debit.toString().indexOf('.') > 3 || credit.toString().indexOf('.') > 3)
    {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Balance balance = user.getBalance();
    Operation operation = new Operation(findArticle.get(0), (debit), (credit), user.getBalance());
    operationRepo.save(operation);
    balance.setCredit(balance.getCredit() + credit);
    balance.setDebit(balance.getDebit() + debit);
    balance.setAmount((balance.getDebit() * 100 - balance.getCredit() * 100) / 100);
    balanceRepo.save(balance);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/articles")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> addArticle(@RequestParam String article)
  {
    if (!articleRepo.findByName(article).isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    articleRepo.save(new Article(article));
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PatchMapping("/articles/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> changeArticle(@PathVariable Long id, @RequestParam String article)
  {
    Optional<Article> findArticle = articleRepo.findById(id);
    if (findArticle.isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (!articleRepo.findByName(article).isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    Article newArticle = findArticle.get();
    newArticle.setName(article);
    articleRepo.save(newArticle);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PatchMapping("/balance/operations/{id}")
  public ResponseEntity<?> changeOperation(@AuthenticationPrincipal User user, @PathVariable Long id,
                                           @RequestParam(required = false) String article,
                                           @RequestParam(required = false, defaultValue = "0.0") Double debit,
                                           @RequestParam(required = false, defaultValue = "0.0") Double credit)
  {
    List<Operation> operations = operationRepo.findByBalance(user.getBalance());
    operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    if (operations.isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    Operation operation = operations.get(0);
    List<Article> articleList = articleRepo.findByName(article);
    if (article != null && !articleList.isEmpty())
    {
      operation.setArticle(articleList.get(0));
    }
    double debitChange = 0.0, creditChange = 0.0;
    if (debit > 0 && debit.toString().indexOf('.') < 4)
    {
      debitChange = debit - operation.getDebit();
      operation.setDebit(debit);
    }
    if (credit > 0 && credit.toString().indexOf('.') < 4)
    {
      creditChange = credit - operation.getCredit();
      operation.setCredit(credit);
    }

    Balance balance = user.getBalance();
    balance.setDebit((balance.getDebit() * 100 + debitChange * 100) / 100);
    balance.setCredit((balance.getCredit() * 100 + creditChange * 100) / 100);
    balance.setAmount((balance.getDebit() * 100 - balance.getCredit() * 100) / 100);
    balanceRepo.save(balance);
    operationRepo.save(operation);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @DeleteMapping("/balance/operations/{id}")
  public ResponseEntity<?> deleteOperation(@AuthenticationPrincipal User user, @PathVariable Long id)
  {
    List<Operation> operations = operationRepo.findByBalance(user.getBalance());
    operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    if (operations.isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    Operation operation = operations.get(0);
    Balance balance = user.getBalance();
    balance.setCredit((balance.getCredit() * 100 - operation.getCredit() * 100) / 100);
    balance.setDebit((balance.getDebit() * 100 - operation.getDebit() * 100) / 100);
    balance.setAmount((balance.getDebit() * 100 - balance.getCredit() * 100) / 100);
    balanceRepo.save(balance);
    operationRepo.delete(operation);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @DeleteMapping("/balance/operations")
  public ResponseEntity<?> deleteAllOperation(@AuthenticationPrincipal User user)
  {
    operationRepo.deleteAll();
    Balance balance = user.getBalance();
    balance.setDebit(0.0);
    balance.setCredit(0.0);
    balance.setAmount(0.0);
    balanceRepo.save(balance);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

}
