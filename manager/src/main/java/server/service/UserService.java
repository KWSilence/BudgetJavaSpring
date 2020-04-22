package server.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import server.entities.Balance;
import server.entities.User;
import server.repos.MException;
import server.repos.UserRepo;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService
{
  private final UserRepo repository;
  private final BalanceService balanceService;

  public UserService(UserRepo repository, BalanceService balanceService)
  {
    this.repository = repository;
    this.balanceService = balanceService;
    if (repository.findByUsername("root") == null)
    {
      User root = new User("root", "kws");
      repository.save(root);
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
  {
    return repository.findByUsername(username);
  }

  public User getById(Long id) throws MException
  {
    Optional<User> user = repository.findById(id);
    if (user.isEmpty())
    {
      throw new MException("User by id not found");
    }
    return user.get();
  }

  public User getByUsername(String username) throws MException
  {
    User user = repository.findByUsername(username);
    if (user == null)
    {
      throw new MException("User by username not found");
    }
    return user;
  }

  public List<User> getAll() throws MException
  {
    List<User> users = repository.findAll();
    if (users.isEmpty())
    {
      throw new MException("Users not found");
    }
    return users;
  }

  private boolean isExistUsername(String username)
  {
    return (repository.findByUsername(username) != null);
  }

  public void add(String username, String password) throws MException
  {
    if (username.trim().isEmpty())
    {
      throw new MException("Enter username");
    }
    if (password.trim().isEmpty())
    {
      throw new MException("Enter password");
    }
    if (isExistUsername(username))
    {
      throw new MException("This user already exist");
    }

    Balance balance = new Balance();
    balanceService.addOrUpdate(balance);
    User user = new User(username, password, balance);
    repository.saveAndFlush(user);
  }
}
