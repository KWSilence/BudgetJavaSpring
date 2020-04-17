package server.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import server.entities.*;
import server.repos.ArticleRepo;
import server.repos.BalanceRepo;
import server.repos.OperationRepo;
import server.repos.UserRepo;

import java.util.ArrayList;
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

  private Gson gson;

  public ManagerController(OperationRepo operationRepo, UserRepo userRepo, BalanceRepo balanceRepo,
                           ArticleRepo articleRepo)
  {
    this.operationRepo = operationRepo;
    this.userRepo = userRepo;
    this.balanceRepo = balanceRepo;
    this.articleRepo = articleRepo;
    this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  }

  @GetMapping("me")
  public String who(@AuthenticationPrincipal User user)
  {
    Optional<User> findUser = userRepo.findById(user.getId());
    if (findUser.isEmpty())
    {
      return gson.toJson(new Response("Something wrong", false));
    }
    return gson.toJson(new Response(findUser.get()));
  }

  @GetMapping("/balance")
  public String readBalance(@AuthenticationPrincipal User userUp)
  {
    if (userUp.isAdmin())
    {
      return gson.toJson(new Response("Admin have not balance", false));
    }
    return gson.toJson(new Response(userRepo.findById(userUp.getId()).get().getBalance()));
  }

  @GetMapping("/balance/operations")
  public String readOperations(@AuthenticationPrincipal User userUp, @RequestParam(required = false) String article,
                               @RequestParam(required = false) Long id)
  {
    List<Operation> operations = this.operationRepo.findByBalance(userRepo.findById(userUp.getId()).get().getBalance());
    if (article != null && !article.isEmpty())
    {
      operations = operations.stream().filter(o -> o.getArticleName().equals(article)).collect(Collectors.toList());
    }
    if (id != null)
    {
      operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    }
    return operations != null && !operations.isEmpty() ? gson.toJson(new Response(operations))
                                                       : gson.toJson(new Response("Operations not found", false));
  }

  @GetMapping("/users")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String readUsers(@RequestParam(required = false) Long userID, @RequestParam(required = false) String username)
  {
    List<User> users = new ArrayList<>();
    boolean readed = false;
    if (username != null)
    {
      User user = userRepo.findByUsername(username);
      if (user != null)
      {
        users.add(user);
      }
      readed = true;
    }
    if (userID != null)
    {
      if (readed)
      {
        return gson.toJson(new Response("Search should be by ID or by Name", false));
      }
      Optional<User> user = userRepo.findById(userID);
      user.ifPresent(users::add);
      readed = true;
    }
    if (!readed)
    {
      users = userRepo.findAll();
    }
    return users != null && !users.isEmpty() ? gson.toJson(new Response(users))
                                             : gson.toJson(new Response("Users not found", false));
  }

  @GetMapping("/operations")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String readAllOperations(@RequestParam(required = false) Long userID,
                                  @RequestParam(required = false) String article,
                                  @RequestParam(required = false) Long id)
  {
    List<Operation> operations = this.operationRepo.findAll();
    if (userID != null)
    {
      Optional<User> user = userRepo.findById(userID);
      if (user.isEmpty())
      {
        return gson.toJson(new Response("User by this ID not found", false));
      }
      operations = operations.stream().filter(o -> o.getBalanceID().equals(user.get().getBalance().getId()))
                             .collect(Collectors.toList());
    }
    if (article != null && !article.isEmpty())
    {
      operations = operations.stream().filter(o -> o.getArticleName().equals(article)).collect(Collectors.toList());
    }

    if (id != null)
    {
      operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    }

    return operations != null && !operations.isEmpty() ? gson.toJson(new Response(operations))
                                                       : gson.toJson(new Response("Operations not found", false));
  }

  @GetMapping("/articles")
  public String readArticles(@RequestParam(required = false) String article,
                             @RequestParam(required = false) Long articleID)
  {
    List<Article> articles = new ArrayList<>();
    boolean readed = false;
    if (article != null && !article.isEmpty())
    {
      articles = articleRepo.findByName(article);
      readed = true;
    }
    if (articleID != null)
    {
      if (readed)
      {
        return gson.toJson(new Response("Search should be by ID or by Name", false));
      }
      Optional<Article> findArticle = articleRepo.findById(articleID);
      findArticle.ifPresent(articles::add);
      readed = true;
    }
    if (!readed)
    {
      articles = (List<Article>) articleRepo.findAll();
    }
    return articles != null && !articles.isEmpty() ? gson.toJson(new Response(articles))
                                                   : gson.toJson(new Response("Articles not found", false));
  }

  @PostMapping("/balance/operations")
  public String addOperation(@AuthenticationPrincipal User userUp, @RequestParam String article,
                             @RequestParam(required = false, defaultValue = "0.0") Double debit,
                             @RequestParam(required = false, defaultValue = "0.0") Double credit)
  {
    List<Article> findArticle = articleRepo.findByName(article);
    if (findArticle.isEmpty())
    {
      return gson.toJson(new Response("This Article name not found", false));
    }
    if (debit < 0 || credit < 0)
    {
      return gson.toJson(new Response("Debit and Credit should be positive", false));
    }

    if (debit.toString().indexOf('.') > 3 || credit.toString().indexOf('.') > 3)
    {
      return gson.toJson(new Response("Debit and Credit should be format *.00", false));
    }

    Balance balance = userRepo.findById(userUp.getId()).get().getBalance();
    Operation operation = new Operation(findArticle.get(0), debit, credit, balance);
    operationRepo.save(operation);
    balance.changeDebit(debit);
    balance.changeCredit(credit);
    balance.updateAmount();
    balanceRepo.save(balance);
    return gson.toJson(new Response());
  }

  @PostMapping("/articles")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String addArticle(@RequestParam String article)
  {
    if (!articleRepo.findByName(article).isEmpty())
    {
      return gson.toJson(new Response("This article name already used", false));
    }
    articleRepo.save(new Article(article));
    return gson.toJson(new Response());
  }

  @PatchMapping("/articles/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String changeArticle(@PathVariable Long id, @RequestParam String article)
  {
    Optional<Article> findArticle = articleRepo.findById(id);
    if (findArticle.isEmpty())
    {
      return gson.toJson(new Response("Article by this id not found", false));
    }

    if (!articleRepo.findByName(article).isEmpty())
    {
      return gson.toJson(new Response("This article name already used", false));
    }
    Article newArticle = findArticle.get();
    newArticle.setName(article);
    articleRepo.save(newArticle);

    return gson.toJson(new Response());
  }

  @PatchMapping("/balance/operations/{id}")
  public String changeOperation(@AuthenticationPrincipal User userUp, @PathVariable Long id,
                                @RequestParam(required = false) String article,
                                @RequestParam(required = false, defaultValue = "-1.0") Double debit,
                                @RequestParam(required = false, defaultValue = "-1.0") Double credit)
  {
    List<Operation> operations = operationRepo.findByBalance(userRepo.findById(userUp.getId()).get().getBalance());
    operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    if (operations.isEmpty())
    {
      return gson.toJson(new Response("Operation by this id not found", false));
    }
    Operation operation = operations.get(0);
    List<Article> articleList = articleRepo.findByName(article);
    if (article != null && !articleList.isEmpty())
    {
      operation.setArticle(articleList.get(0));
    }
    double debitChange = 0.0, creditChange = 0.0;
    if (debit >= 0 && debit.toString().indexOf('.') < 4)
    {
      debitChange = debit - operation.getDebit();
      operation.setDebit(debit);
    }
    if (credit >= 0 && credit.toString().indexOf('.') < 4)
    {
      creditChange = credit - operation.getCredit();
      operation.setCredit(credit);
    }

    Balance balance = userRepo.findById(userUp.getId()).get().getBalance();
    balance.changeDebit(debitChange);
    balance.changeCredit(creditChange);
    balance.updateAmount();
    balanceRepo.save(balance);
    operationRepo.save(operation);
    return gson.toJson(new Response());
  }

  @DeleteMapping("/balance/operations/{id}")
  @PreAuthorize("hasAuthority('USER')")
  public String deleteOperation(@AuthenticationPrincipal User userUp, @PathVariable Long id)
  {
    List<Operation> operations = operationRepo.findByBalance(userRepo.findById(userUp.getId()).get().getBalance());
    operations = operations.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
    if (operations.isEmpty())
    {
      return gson.toJson(new Response("Operation by this id not found", false));
    }
    Operation operation = operations.get(0);
    Balance balance = userRepo.findById(userUp.getId()).get().getBalance();
    balance.changeDebit(-operation.getDebit());
    balance.changeCredit(-operation.getCredit());
    balance.updateAmount();
    balanceRepo.save(balance);
    operationRepo.delete(operation);
    return gson.toJson(new Response());
  }

  @DeleteMapping("/balance/operations")
  public String deleteAllOperation(@AuthenticationPrincipal User userUp)
  {
    Balance balance = userRepo.findById(userUp.getId()).get().getBalance();
    balance.reset();
    balanceRepo.save(balance);
    List<Operation> operations = operationRepo.findByBalance(balance);
    operationRepo.deleteAll(operations);
    return gson.toJson(new Response());
  }
}
