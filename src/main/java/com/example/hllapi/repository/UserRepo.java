package com.example.hllapi.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.hllapi.model.User;

public interface UserRepo extends MongoRepository<User, String> {

	@Query("{ 'name' : ?0 }")
	public User findByName(String name);
	
	@Query("{}")
	public List<User> findAll();
	
}
