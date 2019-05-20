package com.example.hllapi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PingController {
	
	@CrossOrigin
	@GetMapping(value="/api/public/ping", produces="application/json")
	@ResponseBody
	public String getPing() {
		return "{ \"Hello\": \"World!\"}";
	}

}
