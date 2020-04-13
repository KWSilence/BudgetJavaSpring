package server.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import server.entities.Article;
import server.entities.Balance;
import server.entities.Operation;
import server.entities.User;
import server.repos.ArticleRepo;
import server.repos.BalanceRepo;
import server.repos.OperationRepo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MainController
{
  private final ArticleRepo articleRepo;
  private final BalanceRepo balanceRepo;
  private final OperationRepo operationRepo;
  private User user;

  public MainController(ArticleRepo articleRepo, BalanceRepo balanceRepo, OperationRepo operationRepo)
  {
    this.articleRepo = articleRepo;
    this.balanceRepo = balanceRepo;
    this.operationRepo = operationRepo;
  }

  @GetMapping("/")
  public String greeting()
  {
    return "starting";
  }

  @GetMapping("/main")
  public String main(@AuthenticationPrincipal User user, Map<String, Object> model)
  {
    this.user = user;
    updatePage(model, false);
    return "main";
  }

  @PostMapping("add")
  public String add(@RequestParam List<String> articles, @RequestParam String debit, @RequestParam String credit, Map<String, Object> model)
  {
    if (articles.isEmpty())
    {
      model.put("error", "Add: article field is empty");
      updatePage(model, false);
      return "main";
    }
    List<Article> article = articleRepo.findByName(articles.get(0));
    if (articleRepo.findByName(articles.get(0)).isEmpty())
    {
      model.put("error", "Add: this article not found");
      updatePage(model, false);
      return "main";
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
      model.put("error", "Add: credit and debit should be float number");
      updatePage(model, false);
      return "main";
    }

    if ((debit.indexOf('.') != -1 && (debit.length() - debit.indexOf('.')) > 3) || (credit.indexOf('.') != -1 && (credit.length() - credit.indexOf('.')) > 3))
    {
      model.put("error", "Add: credit and debit should be float number in form '*.00'");
      updatePage(model, false);
      return "main";
    }

    if (creditNum < 0 || debitNum < 0)
    {
      model.put("error", "Add: credit and debit should be positive");
      updatePage(model, false);
      return "main";
    }

    balance.setDebit(balance.getDebit() + debitNum);
    balance.setCredit(balance.getCredit() + creditNum);
    balance.setAmount((balance.getDebit() * 100 - balance.getCredit() * 100) / 100);
    balanceRepo.save(balance);
    Operation operation = new Operation(article.get(0), debitNum, creditNum, this.user.getBalance());
    operationRepo.save(operation);

    updatePage(model, false);
    return "main";
  }

  @PostMapping("filter")
  public String filter(@RequestParam List<String> articles, Map<String, Object> model)
  {
    if (articles.isEmpty())
    {
      updatePage(model, false);
      return "main";
    }
    model.put("operations", operationRepo.findByBalance(this.user.getBalance()).stream()
      .filter(o-> o.getArticleName().equals(articles.get(0)))
      .sorted(Comparator.comparing(Operation::getCreate_date).reversed())
      .collect(Collectors.toList()));
    updatePage(model, true);
    return "main";
  }

  private void updatePage(Map<String, Object> model, boolean isFind)
  {
    if (!isFind)
    {
      model.put("operations", operationRepo.findByBalance(this.user.getBalance()).stream()
        .sorted(Comparator.comparing(Operation::getCreate_date).reversed())
        .collect(Collectors.toList()));
    }
    model.put("user", this.user);
    model.put("balance", this.user.getBalance());
    model.put("articles", articleRepo.findAll());
  }
}