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

	@Autowired
	private AudioFileRepo audioTrackRepo;
	
	@GetMapping(value="/api/public/stream-audio/sample", produces="audio/mpeg")
	@ResponseBody
	public GridFsResource getTrack() throws Exception {
		return audioTrackRepo.getExample();
	}
	
	@PostMapping(value="/api/public/audio/upload")
	@CrossOrigin
	public ResponseEntity<Object> uploadTrack(@RequestParam("audio-file") MultipartFile file) throws Exception {
		audioTrackRepo.store(file.getInputStream());
		return ResponseEntity.ok().body(null);
	}
}
