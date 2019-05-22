package com.example.hllapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.hllapi.repository.AudioTrackRepo;

@Controller
public class AudioStreamController {

	@Autowired
	private AudioTrackRepo audioTrackRepo;
	
	@GetMapping(value="/api/public/stream-audio/sample", produces="audio/mpeg")
	@ResponseBody
	public GridFsResource getTrack() throws Exception {
		return audioTrackRepo.getExample();
	}
}
