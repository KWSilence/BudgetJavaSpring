package server.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entities.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long>
{
  User findByUsername(String username);
}
