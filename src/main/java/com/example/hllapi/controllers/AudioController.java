package com.example.hllapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.hllapi.repository.AudioFileRepo;

@Controller
public class AudioController {

	private AudioFileRepo audioFileRepo;
	
	@Autowired
	public AudioController(AudioFileRepo audioFileRepo) {
		this.audioFileRepo = audioFileRepo;
	}
	
	@GetMapping(value="/api/public/stream-audio/sample", produces="audio/mpeg")
	@ResponseBody
	public GridFsResource getTrack() throws Exception {
		return audioFileRepo.getExample();
	}
	
	@PostMapping(value="/api/public/audio/upload")
	@CrossOrigin
	public ResponseEntity<String> uploadTrack(@RequestParam("audio-file") MultipartFile file) throws Exception {
		String storedId = audioFileRepo.store(file.getInputStream());
		return ResponseEntity.ok().body(storedId);
	}
}
