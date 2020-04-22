package server.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import server.entities.Article;
import server.entities.Operation;
import server.entities.Response;
import server.entities.User;
import server.repos.MException;
import server.service.ArticleService;
import server.service.BalanceService;
import server.service.OperationService;
import server.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ManagerController
{
  private final OperationService operationService;

  private final UserService userService;

  private final BalanceService balanceService;

  private final ArticleService articleService;

  private Gson gson;

  public ManagerController(OperationService operationService, UserService userService, BalanceService balanceService,
                           ArticleService articleService)
  {
    this.operationService = operationService;
    this.userService = userService;
    this.balanceService = balanceService;
    this.articleService = articleService;
    this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  }

  @GetMapping("me")
  public String who(@AuthenticationPrincipal User user)
  {
    try
    {
      return gson.toJson(new Response(userService.getById(user.getId())));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }

  }

  @GetMapping("/balance")
  public String readBalance(@AuthenticationPrincipal User userUp)
  {
    try
    {
      return gson.toJson(new Response(balanceService.getByUser(userUp)));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }

  }

  @GetMapping("/balance/operations")
  public String readOperations(@AuthenticationPrincipal User userUp, @RequestParam(required = false) String article)
  {
    try
    {
      List<Operation> operations = operationService.getByBalance(userUp.getBalance());
      operations = operationService.filterByArticleName(article, operations);
      return gson.toJson(new Response(operations));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/balance/operations/{id}")
  public String readOperation(@AuthenticationPrincipal User userUp, @PathVariable Long id)
  {
    try
    {
      return gson.toJson(new Response(operationService.getById(id, userUp)));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/users")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String readUsers(@RequestParam(required = false) String username)
  {
    try
    {
      List<User> users = new ArrayList<>();
      if (username != null)
      {
        User user = userService.getByUsername(username);
        users.add(user);
      }
      else
      {
        users = userService.getAll();
      }
      return gson.toJson(new Response(users));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/users/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String readUser(@PathVariable Long id)
  {
    try
    {
      return gson.toJson(new Response(userService.getById(id)));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/operations")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String readAllOperations(@RequestParam(required = false) Long userID,
                                  @RequestParam(required = false) String article)
  {
    try
    {
      List<Operation> operations = operationService.getAll();
      if (userID != null)
      {
        operations = operationService.getByBalance(userService.getById(userID).getBalance());
      }
      operations = operationService.filterByArticleName(article, operations);
      return gson.toJson(new Response(operations));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/operations/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String readOneOperation(@PathVariable Long id)
  {
    try
    {
      return gson.toJson(new Response(operationService.getOneById(id)));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/articles")
  public String readArticles(@RequestParam(required = false) String article)
  {
    try
    {
      List<Article> articles = new ArrayList<>();
      if (article != null)
      {
        articles.add(articleService.getByName(article));
      }
      else
      {
        articles = articleService.getAll();
      }
      return gson.toJson(new Response(articles));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @GetMapping("/articles/{id}")
  public String readArticle(@PathVariable Long id)
  {
    try
    {
      return gson.toJson(new Response(articleService.getById(id)));
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @PostMapping("/balance/operations")
  public String addOperation(@AuthenticationPrincipal User userUp, @RequestParam String article,
                             @RequestParam(required = false, defaultValue = "0.0") Double debit,
                             @RequestParam(required = false, defaultValue = "0.0") Double credit)
  {
    try
    {
      Article findArticle = articleService.getByName(article);
      operationService.add(findArticle, debit, credit, userUp);
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @PostMapping("/articles")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String addArticle(@RequestParam String article)
  {
    try
    {
      articleService.addOrUpdate(new Article(article));
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @PostMapping("/registration")
  public String addUser(@RequestParam String username, @RequestParam String password)
  {
    try
    {
      userService.add(username, password);
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @PatchMapping("/articles/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String changeArticle(@PathVariable Long id, @RequestParam String article)
  {
    try
    {
      Article findArticle = articleService.getById(id);
      findArticle.setName(article);
      articleService.addOrUpdate(findArticle);
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @PatchMapping("/balance/operations/{id}")
  public String changeOperation(@AuthenticationPrincipal User userUp, @PathVariable Long id,
                                @RequestParam(required = false) String article,
                                @RequestParam(required = false) Double debit,
                                @RequestParam(required = false) Double credit)
  {
    try
    {
      Article findArticle = (article == null || article.trim().isEmpty()) ? null : articleService.getByName(article);
      operationService.update(id, findArticle, debit, credit, userUp);
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }

  @DeleteMapping("/balance/operations/{id}")
  @PreAuthorize("hasAuthority('USER')")
  public String deleteOperation(@AuthenticationPrincipal User userUp, @PathVariable Long id)
  {
    try
    {
      Operation operation = operationService.getById(id, userUp);
      operationService.delete(operation);
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }

  }

  @DeleteMapping("/balance/operations")
  public String deleteAllOperation(@AuthenticationPrincipal User userUp)
  {
    try
    {
      operationService.deleteAll(userUp);
      return gson.toJson(new Response());
    }
    catch (MException e)
    {
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }
}
