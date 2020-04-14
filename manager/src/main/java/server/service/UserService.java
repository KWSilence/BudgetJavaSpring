package server.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import server.config.WebSecurityConfig;
import server.entities.Role;
import server.entities.User;
import server.repos.UserRepo;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService
{
  private final UserRepo userRepo;

  public UserService(UserRepo userRepo)
  {
    this.userRepo = userRepo;
    if (userRepo.findByUsername("root") == null)
    {
      User root = new User();
      root.setActive(true);
      root.setUsername("root");
      root.setPassword(WebSecurityConfig.bCryptPasswordEncoder().encode("kws"));
      root.setRoles(Collections.singleton(Role.ADMIN));
      userRepo.save(root);
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
  {
    return userRepo.findByUsername(username);
  }
}
