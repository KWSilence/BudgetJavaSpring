package server.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.entities.Operation;
import server.entities.User;
import server.repos.ArticleRepo;
import server.repos.BalanceRepo;
import server.repos.OperationRepo;
import server.repos.UserRepo;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ManagerController
{
  private final OperationRepo operationRepo;

  private final UserRepo userRepo;

  private final BalanceRepo balanceRepo;

  private final ArticleRepo articleRepo;

  public ManagerController(OperationRepo operationRepo, UserRepo userRepo, BalanceRepo balanceRepo, ArticleRepo articleRepo)
  {
    this.operationRepo = operationRepo;
    this.userRepo = userRepo;
    this.balanceRepo = balanceRepo;
    this.articleRepo = articleRepo;
  }

  @GetMapping("/operations")
  public ResponseEntity<List<Operation>> readOperations(@AuthenticationPrincipal User user)
  {
    final List<Operation> operations = this.operationRepo.findByBalance(user.getBalance());
    return operations != null && !operations.isEmpty()
      ? new ResponseEntity<>(operations, HttpStatus.OK)
      : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


}
