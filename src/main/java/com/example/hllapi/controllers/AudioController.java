package com.example.hllapi.controllers;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.hllapi.model.Track;
import com.example.hllapi.repository.AudioFileRepo;
import com.example.hllapi.repository.AudioRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Controller
public class AudioController {

//	private AudioFileRepo audioFileRepo;

	private S3Client s3;
	private AudioRepo audioRepo;
	
	@Autowired
	public AudioController(
//			AudioFileRepo audioFileRepo,
			AudioRepo audioRepo,
			S3Client s3
	) {
//		this.audioFileRepo = audioFileRepo;
		this.audioRepo = audioRepo;
		this.s3 = s3;
	}
	
//	@GetMapping(value="/api/public/stream-audio/sample", produces="audio/mpeg")
//	@ResponseBody
//	public InputStreamResource getTrack() throws Exception {
//		ResponseInputStream<GetObjectResponse> responseInputStream = s3.getObject(
//			GetObjectRequest.builder()
//				.bucket("hey-look-listen")
//				.key("audio/godzilla_roar.mp3")
//				.build()
//		);
//		return new InputStreamResource(responseInputStream);
//	}
	
	@GetMapping(value="/api/public/stream-audio/sample", produces="audio/mpeg")
	@ResponseBody
	public InputStreamResource getTrack() throws Exception {
		ResponseInputStream<GetObjectResponse> responseInputStream = s3.getObject(
			GetObjectRequest.builder()
				.bucket("hey-look-listen")
				.key("audio/godzilla_roar.mp3")
				.build()
		);
		return new InputStreamResource(responseInputStream);
	}

	public void streamTrack(String trackId) {
		Track track = audioRepo.getTrackById(trackId);
		
		s3.getObject(
			GetObjectRequest.builder()
				.bucket("hey-look-listen")
				.key(track.getTrackKey())
				.build()
		);
	}
	
//	@PostMapping(value="/api/public/audio/upload")
//	@CrossOrigin
//	public ResponseEntity<String> uploadTrack(@RequestParam("audio-file") MultipartFile file) throws Exception {
//		String storedId = audioFileRepo.store(file.getInputStream());
//		return ResponseEntity.ok().body(storedId);
//	}
}
