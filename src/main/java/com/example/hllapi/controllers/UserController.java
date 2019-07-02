package com.example.hllapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.hllapi.model.User;
import com.example.hllapi.repository.UserRepo;

@Controller
public class UserController {

	@Autowired
	private UserRepo userRepo;
	
	@GetMapping(value="/api/public/user/{name}")
	@ResponseBody
	public User getUser(@PathVariable String name) {
		return this.userRepo.findByName(name);
	}
	
}
