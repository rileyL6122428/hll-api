package com.example.hllapi.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.hllapi.model.Dog;
import com.example.hllapi.repository.DogRepo;

@Controller()
public class DogController {

	@Autowired
	private DogRepo dogRepo;
	
	@GetMapping(value="/api/public/dogs")
	@ResponseBody
	public List<Dog> getDogs() {
		return dogRepo.findAll();
	}
	
}
