package server.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.entities.Balance;
import server.entities.Operation;

import java.util.List;

@Repository
public interface OperationRepo extends JpaRepository<Operation, Long>
{
  List<Operation> findByBalance(Balance balance);
}
