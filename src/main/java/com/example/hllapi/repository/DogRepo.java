package com.example.hllapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.hllapi.model.Dog;

public interface DogRepo extends MongoRepository<Dog, String> {

}
