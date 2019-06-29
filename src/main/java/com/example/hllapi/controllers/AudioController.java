package com.example.hllapi.controllers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.hllapi.model.Track;
import com.example.hllapi.repository.AudioFileRepo;
import com.example.hllapi.repository.TrackRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Controller
public class AudioController {

//	private AudioFileRepo audioFileRepo;

	private S3Client s3;
	private TrackRepo trackRepo;
	
	@Autowired
	public AudioController(
//			AudioFileRepo audioFileRepo,
			TrackRepo audioRepo,
			S3Client s3
	) {
//		this.audioFileRepo = audioFileRepo;
		this.trackRepo = audioRepo;
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

	
	@GetMapping(value="/api/public/track/{trackId}/stream", produces="audio/mpeg")
	@ResponseBody
	public InputStreamResource streamTrack(@PathVariable String trackId) {
		
		
		Track track = trackRepo.byId(trackId);
		
		ResponseInputStream<GetObjectResponse> responseInputStream = s3.getObject(
			GetObjectRequest.builder()
				.bucket("hey-look-listen")
				.key(track.getS3Key())
				.build()
		);
		
		return new InputStreamResource(responseInputStream);
	}

	@GetMapping(value="/api/public/track")
	public ResponseEntity<Iterable<Track>> getTrackMetaData(@RequestParam("id") List<String> trackIds) {
		Iterable<Track> tracks = trackRepo.findAllById(trackIds);
		return ResponseEntity.ok(tracks);
	}

}
