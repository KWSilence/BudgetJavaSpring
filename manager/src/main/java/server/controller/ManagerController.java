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
                                                        @RequestParam(required = false) String article)
  {
    List<Operation> operations = this.operationRepo.findByBalance(user.getBalance());
    if (article != null && !article.isEmpty())
    {
      operations = operations.stream().filter(o -> o.getArticleName().equals(article)).collect(Collectors.toList());
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

  @PostMapping("/add/operation")
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

    debit *= 100;
    credit *= 100;
    if (debit.toString().indexOf('.') + 1 == debit.toString().length() ||
        credit.toString().indexOf('.') + 1 == credit.toString().length())
    {
      System.out.println(debit.toString());
      System.out.println(credit.toString());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    credit /= 100;
    debit /= 100;

    Balance balance = user.getBalance();
    Operation operation = new Operation(findArticle.get(0), (debit), (credit), user.getBalance());
    operationRepo.save(operation);
    balance.setCredit(balance.getCredit() + credit);
    balance.setDebit(balance.getDebit() + debit);
    balance.setAmount((balance.getDebit() * 100 - balance.getCredit() * 100) / 100);
    balanceRepo.save(balance);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/add/article")
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

  @PatchMapping("/change/article")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> changeArticle(@RequestParam String oldArticle, @RequestParam String newArticle)
  {
    if (articleRepo.findByName(oldArticle).isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (!articleRepo.findByName(newArticle).isEmpty())
    {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    Article article = articleRepo.findByName(oldArticle).get(0);
    article.setName(newArticle);
    articleRepo.save(article);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
