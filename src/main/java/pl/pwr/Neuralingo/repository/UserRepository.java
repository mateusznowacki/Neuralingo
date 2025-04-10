package pl.pwr.Neuralingo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.pwr.Neuralingo.entity.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
}
