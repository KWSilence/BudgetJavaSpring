package server.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import server.entities.*;
import server.repos.*;
import server.service.ArticleService;
import server.service.BalanceService;
import server.service.OperationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ManagerController
{
  private final OperationService operationService;

  private final UserRepo userRepo;

  private final BalanceService balanceService;

  private final ArticleService articleService;

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  private Gson gson;

  public ManagerController(OperationService operationService, UserRepo userRepo, BalanceService balanceService,
                           ArticleService articleService, BCryptPasswordEncoder bCryptPasswordEncoder)
  {
    this.operationService = operationService;
    this.userRepo = userRepo;
    this.balanceService = balanceService;
    this.articleService = articleService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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
                                  @RequestParam(required = false) String article)
  {
    try
    {
      List<Operation> operations = operationService.getAll();
      if (userID != null)
      {
        Optional<User> user = userRepo.findById(userID);
        if (user.isEmpty())
        {
          return gson.toJson(new Response("User by this ID not found", false));
        }
        operations = operationService.getByBalance(user.get().getBalance());
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
  public String readArticles(@RequestParam(required = false) String article,
                             @RequestParam(required = false) Long articleID)
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
      System.out.println("FUCK: " + e.getMessage());
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
    if (username.trim().isEmpty())
    {
      return gson.toJson(new Response("Enter username", false));
    }
    if (password.trim().isEmpty())
    {
      return gson.toJson(new Response("Enter password", false));
    }

    if (userRepo.findByUsername(username) != null)
    {
      return gson.toJson(new Response("This user already exist", false));
    }

    Balance balance = new Balance();
    balanceService.addOrUpdate(balance);
    User user = new User();
    user.setUsername(username);
    user.setActive(true);
    user.setPassword(bCryptPasswordEncoder.encode(password));
    user.setRoles(Collections.singleton(Role.USER));
    user.setBalance(balance);
    userRepo.save(user);

    return gson.toJson(new Response());
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
      System.out.println("FUCK: " + e.getMessage());
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
      System.out.println("FUCK: " + e.getMessage());
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
      System.out.println("FUCK: " + e.getMessage());
      return gson.toJson(new Response(e.getMessage(), false));
    }
  }
}
