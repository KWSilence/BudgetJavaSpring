package server.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entities.Balance;

@Repository
public interface BalanceRepo extends JpaRepository<Balance, Long>
{
}
