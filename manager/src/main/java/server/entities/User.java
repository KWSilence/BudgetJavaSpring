package server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
public class User implements UserDetails
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  @JsonIgnore
  private String password;
  @JsonIgnore
  private boolean active;

  @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @JsonIgnore
  private Set<Role> roles;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "balance_id")
  private Balance balance;

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
  @JsonIgnore
  public boolean isAccountNonExpired()
  {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked()
  {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired()
  {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled()
  {
    return isActive();
  }

  @JsonIgnore
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
  @JsonIgnore
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
