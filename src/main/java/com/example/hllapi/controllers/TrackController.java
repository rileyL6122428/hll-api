package com.example.hllapi.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.hllapi.model.Track;
import com.example.hllapi.repository.TrackRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Controller
public class TrackController {

	private S3Client s3;
	private TrackRepo trackRepo;
	
	@Value("${aws.s3.bucketName}")
	private String bucketName;
	
	@Autowired
	public TrackController(
		TrackRepo trackRepo,
		S3Client s3
	) {
		this.trackRepo = trackRepo;
		this.s3 = s3;
	}
	
	@PostMapping(value="/api/private/track")
	@CrossOrigin
	public ResponseEntity<RespBody> postTrack(
		@RequestParam("audio-file") MultipartFile audioFile,
		@RequestHeader("Authorization") String authHeader
	) throws Exception {
		
		ResponseEntity<RespBody> response;
		
		try {
			if (audioFile.getContentType().equalsIgnoreCase("audio/mp3")) {
				String s3Key = "audio/" + audioFile.getOriginalFilename() + ".mp3";
				uploadToS3(audioFile, s3Key);
				
				DecodedJWT jwt = JWT.decode(authHeader.substring(6));
				Track savedTrack = trackRepo.save(new Track() {{
					setS3Key(s3Key);
					setUserId(jwt.getClaim("name").asString());
				}});
					
				response = ResponseEntity.ok(new RespBody() {{
					setMessage("UPLOAD SUCCEEDED");
					setTrack(savedTrack);
				}});
					
			} else {
				response = ResponseEntity.badRequest().body(new RespBody("UNALLOWED CONTENT TYPE"));
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
			response = ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new RespBody("UPLOAD FAILED"));
		}
		
		return response;
	}
	
	private void uploadToS3(MultipartFile file, String key) throws Exception {
		s3.putObject(
			PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build(),
				
			RequestBody.fromBytes(file.getBytes())
		);
	}
	
	@GetMapping(value="/api/public/track/{trackId}/stream", produces="audio/mpeg")
	@ResponseBody
	public InputStreamResource streamTrack(@PathVariable String trackId) {
		Track track = trackRepo.byId(trackId);
		
		ResponseInputStream<GetObjectResponse> responseInputStream = s3.getObject(
			GetObjectRequest.builder()
				.bucket(bucketName)
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

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

}

class RespBody {
	
	private String message;
	private Track track;
	
	public RespBody() { }
	
	public RespBody(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}
}
