package server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.service.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
  private final UserService userService;

  public WebSecurityConfig(UserService userService)
  {
    this.userService = userService;
  }

  @Bean
  public static BCryptPasswordEncoder bCryptPasswordEncoder()
  {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception
  {
    http.csrf().disable().authorizeRequests().antMatchers("/", "/registration").permitAll().anyRequest().authenticated()
        .and().formLogin().loginPage("/login").permitAll().and().logout().permitAll().logoutSuccessUrl("/").and()
        .httpBasic();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception
  {
    try
    {
      auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
    }
    catch (Exception e)
    {
      throw new Exception("Authentication failed...");
    }
  }
}