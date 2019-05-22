package com.example.hllapi.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.hllapi.model.Dog;
import com.example.hllapi.repository.AudioTrackRepo;
import com.example.hllapi.repository.DogRepo;

@Controller
public class DogController {

	@Autowired
	private DogRepo dogRepo;
	
	@Autowired
	private AudioTrackRepo trackRepo;
	
	@GetMapping(value="/api/public/dogs")
	@ResponseBody
	public List<Dog> getDogs() {
		return dogRepo.findAll();
	}
	
//	@GetMapping(value="/api/public/save-track")
//	@ResponseBody
//	public String saveTrack() throws Exception {
//		InputStream inputStream = new FileInputStream(
//			new File(
//				"/Users/rileylittlefield/hey-look-listen/PoC/browser-audio/panic-at-the-disco-hey-look-ma-i-made-it-official-video.mp3"
//			)
//		);
//		return trackRepo.store(inputStream, "asbel", "hey-look");
//	}
	
	@GetMapping(value="/api/public/get-track", produces="audio/mpeg")
	@ResponseBody
	public GridFsResource getTrack() throws Exception {
		return trackRepo.getExample();
	}
	
}
