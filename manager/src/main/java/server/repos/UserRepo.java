package server.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import server.entities.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
