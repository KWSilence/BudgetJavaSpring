package server.entities;

import com.google.gson.annotations.Expose;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import server.config.WebSecurityConfig;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Entity
public class User implements UserDetails
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Expose
  private Long id;
  @Expose
  private String username;
  private String password;
  private boolean active;

  @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Expose
  private Set<Role> roles;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "balance_id")
  @Expose
  private Balance balance;

  public User()
  {

  }

  public User(String username, String password, Balance balance)
  {
    this.username = username;
    this.password = WebSecurityConfig.bCryptPasswordEncoder().encode(password);
    this.balance = balance;
    this.active = true;
    this.roles = Collections.singleton(Role.USER);
  }

  public User(String username, String password)
  {
    this.username = username;
    this.password = WebSecurityConfig.bCryptPasswordEncoder().encode(password);
    this.balance = null;
    this.active = true;
    this.roles = Collections.singleton(Role.ADMIN);
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public String getUsername()
  {
    return username;
  }

  public boolean isAdmin()
  {
    return roles.contains(Role.ADMIN);
  }

  @Override
  public boolean isAccountNonExpired()
  {
    return true;
  }

  @Override
  public boolean isAccountNonLocked()
  {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired()
  {
    return true;
  }

  @Override
  public boolean isEnabled()
  {
    return isActive();
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(boolean active)
  {
    this.active = active;
  }

  public Set<Role> getRoles()
  {
    return roles;
  }

  public void setRoles(Set<Role> roles)
  {
    this.roles = roles;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities()
  {
    return getRoles();
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public Balance getBalance()
  {
    return balance;
  }

  public void setBalance(Balance balance)
  {
    this.balance = balance;
  }
}
