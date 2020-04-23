package server_test.controller;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import server.Application;
import server.controller.ManagerController;
import server.entities.Response;
import server.entities.User;
import server.repos.UserRepo;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ControllerTest
{
  @Autowired
  private ManagerController controller;
  @Autowired
  private UserRepo userRepo;

  @Test
  public void test1RegisterUsers()
  {
    Response response = Response.parseJson(controller.addUser("gamer", "wasd"));
    assertEquals(response.getSuccess(), true);

    response = Response.parseJson(controller.addUser("ubuntu", "poop"));
    assertEquals(response.getSuccess(), true);


    response = Response.parseJson(controller.addUser("", "wasd"));
    assertEquals(response.getSuccess(), false);
    assertEquals(response.getError(), "Enter username");

    response = Response.parseJson(controller.addUser("gamer1", ""));
    assertEquals(response.getSuccess(), false);
    assertEquals(response.getError(), "Enter password");

    response = Response.parseJson(controller.addUser("ubuntu", "nice"));
    assertEquals(response.getSuccess(), false);
    assertEquals(response.getError(), "This user already exist");
  }

  @Test
  @WithUserDetails("root")
  public void test2CheckAddingUsers()
  {
    Response response = Response.parseJson(controller.readUsers("gamer"));
    assertEquals(response.getArrayBody().get(0).get("username"), "gamer");

    response = Response.parseJson(controller.readUser(2L));
    assertEquals(response.getMapBody().get("username"), "gamer");

    response = Response.parseJson(controller.readUsers("ubuntu"));
    assertEquals(response.getArrayBody().get(0).get("username"), "ubuntu");

    response = Response.parseJson(controller.readUser(3L));
    assertEquals(response.getMapBody().get("username"), "ubuntu");

    response = Response.parseJson(controller.readUsers(null));
    assertEquals(response.getArrayBody().size(), 3);
  }

  @Test
  @WithUserDetails("root")
  public void test3AddArticles()
  {
    Response response = Response.parseJson(controller.readArticles(null));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.addArticle("games"));
    assertEquals(response.getSuccess(), true);

    response = Response.parseJson(controller.addArticle("ali"));
    assertEquals(response.getSuccess(), true);

    response = Response.parseJson(controller.addArticle("company"));
    assertEquals(response.getSuccess(), true);

    response = Response.parseJson(controller.readArticles(null));
    assertEquals(response.getArrayBody().size(), 3);

    response = Response.parseJson(controller.readArticles("games"));
    assertEquals(response.getArrayBody().get(0).get("name"), "games");
    response = Response.parseJson(controller.readArticle(1L));
    assertEquals(response.getMapBody().get("name"), "games");

    response = Response.parseJson(controller.readArticles("ali"));
    assertEquals(response.getArrayBody().get(0).get("name"), "ali");
    response = Response.parseJson(controller.readArticle(2L));
    assertEquals(response.getMapBody().get("name"), "ali");

    response = Response.parseJson(controller.readArticles("company"));
    assertEquals(response.getArrayBody().get(0).get("name"), "company");
    response = Response.parseJson(controller.readArticle(3L));
    assertEquals(response.getMapBody().get("name"), "company");

    response = Response.parseJson(controller.addArticle("company"));
    assertEquals(response.getSuccess(), false);
    assertEquals(response.getError(), "This article name already used");

    response = Response.parseJson(controller.addArticle(""));
    assertEquals(response.getSuccess(), false);
    assertEquals(response.getError(), "Article name should not be empty");
  }

  @Test
  public void test4WhoWork()
  {
    Response response = Response.parseJson(controller.who(userRepo.findByUsername("gamer")));
    assertEquals(response.getMapBody().get("username"), "gamer");
  }

  @Test
  @WithUserDetails("root")
  public void test5UpdateArticle()
  {
    Response response = Response.parseJson(controller.readArticle(2L));
    assertEquals(response.getMapBody().get("name"), "ali");

    response = Response.parseJson(controller.changeArticle("2", "AliExp"));
    assertEquals(response.getSuccess(), true);

    response = Response.parseJson(controller.readArticle(2L));
    assertEquals(response.getMapBody().get("name"), "AliExp");

    response = Response.parseJson(controller.changeArticle("a", "AliExp"));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.changeArticle("20", "AliExp"));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.changeArticle("2", ""));
    assertEquals(response.getSuccess(), false);
  }

  @Test
  @WithUserDetails("gamer")
  public void test6AddOperation()
  {
    User user1 = userRepo.findByUsername("gamer");
    User user2 = userRepo.findByUsername("ubuntu");

    Response response = Response.parseJson(controller.addOperation(user1, "", null, null));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.addOperation(user1, "a", null, null));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.addOperation(user1, "games", "-100", "0"));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.addOperation(user1, "games", "1.001", "0"));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.addOperation(user1, "games", "a", "0"));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.addOperation(user1, "games", "50", null)); //id 1
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.addOperation(user1, "games", null, "5")); //id 2
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.readBalance(user1));
    assertEquals(response.getMapBody().get("debit"), 50D);
    assertEquals(response.getMapBody().get("credit"), 5D);
    assertEquals(response.getMapBody().get("amount"), 45D);

    response = Response.parseJson(controller.addOperation(user2, "AliExp", "10", null)); //id 3
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.addOperation(user2, "AliExp", "15", null)); // id 4
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.readBalance(user2));
    assertEquals(response.getMapBody().get("debit"), 25D);
    assertEquals(response.getMapBody().get("credit"), 0D);
    assertEquals(response.getMapBody().get("amount"), 25D);

    response = Response.parseJson(controller.readOperations(user1, null));
    assertEquals(response.getArrayBody().size(), 2);
    response = Response.parseJson(controller.readOperations(user1, "games"));
    assertEquals(response.getArrayBody().size(), 2);
    assertEquals(response.getArrayBody().get(0).get("id"), 1D);
    assertEquals(response.getArrayBody().get(1).get("id"), 2D);

    response = Response.parseJson(controller.readOperations(user1, "AliExp"));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.readOperation(user1, 1L));
    assertEquals(response.getMapBody().get("debit"), 50D);

    response = Response.parseJson(controller.readOperation(user1, 3L)); //operation of another user
    assertEquals(response.getSuccess(), false);
  }

  @Test
  @WithUserDetails("root")
  public void test7ShowOperationsForAdmin()
  {
    Response response = Response.parseJson(controller.readAllOperations(null, null));
    assertEquals(response.getArrayBody().size(), 4);
    assertEquals(response.getArrayBody().get(0).get("debit"), 50D);
    assertEquals(response.getArrayBody().get(1).get("debit"), 0D);
    assertEquals(response.getArrayBody().get(2).get("debit"), 10D);
    assertEquals(response.getArrayBody().get(3).get("debit"), 15D);

    response = Response.parseJson(controller.readAllOperations(2L, null));
    assertEquals(response.getArrayBody().size(), 2);
    assertEquals(response.getArrayBody().get(0).get("debit"), 50D);
    assertEquals(response.getArrayBody().get(1).get("debit"), 0D);

    response = Response.parseJson(controller.readAllOperations(null, "AliExp"));
    assertEquals(response.getArrayBody().size(), 2);
    assertEquals(response.getArrayBody().get(0).get("debit"), 10D);
    assertEquals(response.getArrayBody().get(1).get("debit"), 15D);

    response = Response.parseJson(controller.readAllOperations(null, "A"));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.readAllOperations(20L, null));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.readOneOperation(2L));
    assertEquals(response.getMapBody().get("debit"), 0D);
    assertEquals(response.getMapBody().get("credit"), 5D);

    response = Response.parseJson(controller.readOneOperation(20L));
    assertEquals(response.getSuccess(), false);
  }

  @Test
  @WithUserDetails("gamer")
  public void test8UpdateAndDeleteOperations()
  {
    User user1 = userRepo.findByUsername("gamer");
    User user2 = userRepo.findByUsername("ubuntu");

    Response response = Response.parseJson(controller.changeOperation(user1, 2L, "a", null, null));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.changeOperation(user1, 2L, null, "a", null));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.changeOperation(user1, 2L, null, "-100", null));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.changeOperation(user1, 2L, null, "0.001", null));
    assertEquals(response.getSuccess(), false);

    response = Response.parseJson(controller.readOperation(user1, 2L));
    assertEquals(response.getMapBody().get("debit"), 0D);
    assertEquals(response.getMapBody().get("credit"), 5D);
    response = Response.parseJson(controller.changeOperation(user1, 2L, null, "5", null));
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.readOperation(user1, 2L));
    assertEquals(response.getMapBody().get("debit"), 5D);
    assertEquals(response.getMapBody().get("credit"), 5D);
    response = Response.parseJson(controller.readBalance(user1));
    assertEquals(response.getMapBody().get("debit"), 55D);
    assertEquals(response.getMapBody().get("credit"), 5D);
    assertEquals(response.getMapBody().get("amount"), 50D);

    response = Response.parseJson(controller.readOperation(user1, 2L));
    assertEquals(response.getMapBody().get("debit"), 5D);
    assertEquals(response.getMapBody().get("credit"), 5D);
    response = Response.parseJson(controller.changeOperation(user1, 2L, null, "10", "0"));
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.readOperation(user1, 2L));
    assertEquals(response.getMapBody().get("debit"), 10D);
    assertEquals(response.getMapBody().get("credit"), 0D);
    response = Response.parseJson(controller.readBalance(user1));
    assertEquals(response.getMapBody().get("debit"), 60D);
    assertEquals(response.getMapBody().get("credit"), 0D);
    assertEquals(response.getMapBody().get("amount"), 60D);

    response = Response.parseJson(controller.deleteOperation(user1, 2L));
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.readOperations(user1, null));
    assertEquals(response.getArrayBody().size(), 1);
    assertEquals(response.getArrayBody().get(0).get("id"), 1D);
    response = Response.parseJson(controller.readBalance(user1));
    assertEquals(response.getMapBody().get("debit"), 50D);
    assertEquals(response.getMapBody().get("credit"), 0D);
    assertEquals(response.getMapBody().get("amount"), 50D);

    response = Response.parseJson(controller.readBalance(user2));
    assertEquals(response.getMapBody().get("debit"), 25D);
    assertEquals(response.getMapBody().get("credit"), 0D);
    assertEquals(response.getMapBody().get("amount"), 25D);
    response = Response.parseJson(controller.deleteAllOperation(user2));
    assertEquals(response.getSuccess(), true);
    response = Response.parseJson(controller.readOperations(user2, null));
    assertEquals(response.getSuccess(), false);
    response = Response.parseJson(controller.readBalance(user2));
    assertEquals(response.getMapBody().get("debit"), 0D);
    assertEquals(response.getMapBody().get("credit"), 0D);
    assertEquals(response.getMapBody().get("amount"), 0D);
  }
}
