package server.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import server.entities.Article;
import server.entities.Balance;
import server.entities.Operation;
import server.entities.User;
import server.repos.ArticleRepo;
import server.repos.BalanceRepo;
import server.repos.OperationRepo;
import server.repos.UserRepo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class MainController
{
  private final ArticleRepo articleRepo;
  private final BalanceRepo balanceRepo;
  private final OperationRepo operationRepo;
  private final UserRepo userRepo;
  private User user;
  private String error;
  private List<Operation> filteredOperations;
  private boolean filter;

  public MainController(ArticleRepo articleRepo, BalanceRepo balanceRepo, OperationRepo operationRepo,
                        UserRepo userRepo)
  {
    this.articleRepo = articleRepo;
    this.balanceRepo = balanceRepo;
    this.operationRepo = operationRepo;
    this.userRepo = userRepo;
    filter = false;
    error = "";
    filteredOperations = null;
  }

  @GetMapping("/")
  public String greeting()
  {
    return "starting";
  }

  @GetMapping("/main")
  public String main(@AuthenticationPrincipal User user, Map<String, Object> model)
  {
    this.user = userRepo.findById(user.getId()).get();
    updatePage(model);
    return "main";
  }

  @PostMapping("add")
  public RedirectView add(@RequestParam List<String> articles, @RequestParam String debit, @RequestParam String credit)
  {
    resetParam();
    if (articles.isEmpty())
    {
      error = "Add: article field is empty";
      return new RedirectView("/main");
    }
    List<Article> article = articleRepo.findByName(articles.get(0));
    if (articleRepo.findByName(articles.get(0)).isEmpty())
    {
      error = "Add: this article not found";
      return new RedirectView("/main");
    }
    Balance balance = this.user.getBalance();

    Double debitNum, creditNum;
    debit = debit.trim();
    credit = credit.trim();

    try
    {
      if (debit.isEmpty())
      {
        debitNum = 0.0;
      }
      else
      {
        debitNum = Double.parseDouble(debit);
      }

      if (credit.isEmpty())
      {
        creditNum = 0.0;
      }
      else
      {
        creditNum = Double.parseDouble(credit);
      }
    }
    catch (Exception e)
    {
      error = "Add: credit and debit should be float number";
      return new RedirectView("/main");
    }

    if ((debit.indexOf('.') != -1 && (debit.length() - debit.indexOf('.')) > 3) ||
        (credit.indexOf('.') != -1 && (credit.length() - credit.indexOf('.')) > 3))
    {
      error = "Add: credit and debit should be float number in form '*.00'";
      return new RedirectView("/main");
    }

    if (creditNum < 0 || debitNum < 0)
    {
      error = "Add: credit and debit should be positive";
      return new RedirectView("/main");
    }

    Operation operation = new Operation(article.get(0), debitNum, creditNum, this.user.getBalance());
    operationRepo.save(operation);
    balance.changeDebit(debitNum);
    balance.changeCredit(creditNum);
    balance.updateAmount();
    ;
    balanceRepo.save(balance);

    return new RedirectView("/main");
  }

  @PostMapping("filter")
  public RedirectView filter(@RequestParam List<String> articles)
  {
    resetParam();
    if (articles.isEmpty())
    {
      return new RedirectView("/main");
    }
    filteredOperations = operationRepo.findByBalance(this.user.getBalance()).stream()
                                      .filter(o -> o.getArticleName().equals(articles.get(0)))
                                      .sorted(Comparator.comparing(Operation::getCreate_date).reversed())
                                      .collect(Collectors.toList());
    filter = true;
    return new RedirectView("/main");
  }

  @PostMapping("addArticle")
  @PreAuthorize("hasAuthority('ADMIN')")
  public RedirectView addArticle(@RequestParam String article)
  {
    resetParam();
    if (!articleRepo.findByName(article).isEmpty())
    {
      error = "Add Article: this Article is exist";
      return new RedirectView("/main");
    }

    Article newArticle = new Article();
    newArticle.setName(article);
    articleRepo.save(newArticle);

    return new RedirectView("/main");
  }

  @PostMapping("changeArticle")
  @PreAuthorize("hasAuthority('ADMIN')")
  public RedirectView changeArticle(@RequestParam List<String> articles, @RequestParam String article)
  {
    resetParam();
    if (articles.isEmpty())
    {
      error = "Change: article field is empty";
      return new RedirectView("/main");
    }

    List<Article> findArticle = articleRepo.findByName(articles.get(0));
    if (findArticle.isEmpty())
    {
      error = "Change: this article not found";
      return new RedirectView("/main");
    }

    if (!articleRepo.findByName(article).isEmpty())
    {
      error = "Change Article: this Article is exist";
      return new RedirectView("/main");
    }

    Article newArticle = findArticle.get(0);
    newArticle.setName(article);
    articleRepo.save(newArticle);

    return new RedirectView("/main");
  }

  @PostMapping("tableChange")
  public RedirectView tableChange(@AuthenticationPrincipal User userUp, @RequestParam Long id,
                                  @RequestParam String action, @RequestParam(required = false) List<String> articles,
                                  @RequestParam(required = false, defaultValue = "0.0") Double debit,
                                  @RequestParam(required = false, defaultValue = "0.0") Double credit)
  {
    resetParam();
    Operation operation = operationRepo.findById(id).get();
    if (action.equals("delete"))
    {
      Balance balance = this.user.getBalance();
      balance.changeCredit(-operation.getCredit());
      balance.changeDebit(-operation.getDebit());
      balance.updateAmount();
      balanceRepo.save(balance);
      operationRepo.deleteById(id);
    }
    if (action.equals("edit"))
    {
      if (!articles.isEmpty())
      {
        List<Article> articleList = articleRepo.findByName(articles.get(0));
        if (!articleList.isEmpty())
        {
          operation.setArticle(articleList.get(0));
        }
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
    }
    return new RedirectView("/main");
  }

  @PostMapping("deleteAll")
  private RedirectView deleteAll(@AuthenticationPrincipal User userUp)
  {
    if (userRepo == null)
    {
      System.out.println("UserRepo Her'");
      return new RedirectView("/main");
    }
    Optional<User> user = userRepo.findById(userUp.getId());
    if (user.isEmpty())
    {
      System.out.println("Her'");
      return new RedirectView("/main");
    }
    Balance balance = user.get().getBalance();
    balance.reset();
    balanceRepo.save(balance);
    List<Operation> operations = operationRepo.findByBalance(balance);
    operationRepo.deleteAll(operations);

    return new RedirectView("/main");
  }

  private void updatePage(Map<String, Object> model)
  {
//    if (filter)
//    {
//      model.put("operations", filteredOperations);
//    }
//    else
//    {
//      model.put("operations", operationRepo.findByBalance(this.user.getBalance()).stream()
//                                           .sorted(Comparator.comparing(Operation::getCreate_date).reversed())
//                                           .collect(Collectors.toList()));
//    }
    if (user.isAdmin())
    {
      model.put("isAdmin", true);
//      model.put("articleNames", articleRepo.findAll());
    }
    else
    {
      model.put("isUser", true);
    }
    if (!error.isEmpty())
    {
      model.put("error", error);
    }
//    model.put("user", this.user);
//    model.put("balance", this.user.getBalance());
    model.put("articles", articleRepo.findAll());
  }

  private void resetParam()
  {
    filteredOperations = null;
    filter = false;
    error = "";
  }
}
