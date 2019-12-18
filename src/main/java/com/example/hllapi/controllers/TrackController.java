package com.example.hllapi.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.example.hllapi.service.TrackMetadataParser;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Controller
public class TrackController {

	private S3Client s3;
	private TrackRepo trackRepo;
	private TrackMetadataParser trackFileParser;
	private Set<String> ALLOWED_FILE_TYPES = new HashSet<String>(){{
		add("audio/mpeg");
		add("audio/mp3");
	}}; 
	
	@Value("${aws.s3.bucketName}")
	private String bucketName;
	
	@Autowired
	public TrackController(
		TrackRepo trackRepo,
		S3Client s3,
		TrackMetadataParser trackFileParser
	) {
		this.trackRepo = trackRepo;
		this.s3 = s3;
		this.trackFileParser = trackFileParser;
	}
	
	@PostMapping(value="/api/private/track")
	@CrossOrigin
	public ResponseEntity<TrackController.ResponsePayload> postTrack(
		@RequestParam("audio-file") MultipartFile audioFile,
		@RequestHeader("Authorization") String authHeader
	) throws Exception {
		
		ResponseEntity<TrackController.ResponsePayload> response;
		
		try {
//			if (audioFile.getContentType().equalsIgnoreCase("audio/mp3")) {
			if (ALLOWED_FILE_TYPES.contains(audioFile.getContentType().toLowerCase())) {
				String s3Key = "audio/" + audioFile.getOriginalFilename() + ".mp3";
				s3.putObject(
						PutObjectRequest.builder()
							.bucket(bucketName)
							.key(s3Key)
							.build(),
							
						RequestBody.fromBytes(audioFile.getBytes())
					);
				
				DecodedJWT jwt = JWT.decode(authHeader.substring(6));
				Track savedTrack = trackRepo.save(
					Track.Builder()
						.setS3Key(s3Key)
						.setUserId(jwt.getClaim("name").asString())
						.setName(audioFile.getOriginalFilename())
						.setDuration(trackFileParser.getDuration(audioFile.getBytes()))
						.build()
				);
					
				response = ResponseEntity.ok(new TrackController.ResponsePayload() {{
					setMessage("UPLOAD SUCCEEDED");
					setTrack(savedTrack);
				}});
					
			} else {
				response = ResponseEntity.badRequest().body(new TrackController.ResponsePayload("UNALLOWED CONTENT TYPE"));
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
			response = ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new TrackController.ResponsePayload("UPLOAD FAILED"));
		}
		
		return response;
	}
	
	@GetMapping(value="/api/public/tracks")
	public ResponseEntity<Object> getTracks(@RequestParam(name="artist-id") String artistId) throws Exception {
		ResponseEntity<Object> response;
		
		try {
			Iterable<Track> tracks = trackRepo.findAllByUserId(artistId);
			response = ResponseEntity.ok(tracks);
			
		} catch (Exception exception) {
			response = ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new TrackController.ResponsePayload() {{
					setMessage("RETRIEVE FAILED");
				}});
		}
		
		return response;
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
	
	@DeleteMapping(value="/api/private/track/{trackId}")
	public ResponseEntity<Object> deleteTrack(@PathVariable String trackId, @RequestHeader("Authorization") String authHeader) {
		ResponseEntity<Object> response;
		DecodedJWT jwt = JWT.decode(authHeader.substring(6));
		Track track = trackRepo.byId(trackId);
		
		try {
			if (jwt.getClaim("name").asString().equals(track.getUserId())) {
				trackRepo.delete(track);
				deleteFromS3(track);
				response = successfulDeleteResponse(track);				
			} else {
				response = unauthorizedDeleteResponse();
			}
			
		} catch (AwsServiceException | SdkClientException exception) {
			System.err.println("ERROR DELETEING TRACK FROM S3 WITH KEY " + track.getS3Key());
			response = successfulDeleteResponse(track);
			
		} catch (Exception exception) {
			response = serverErrorResponse();
		}
		
		return response;
	}


	private void deleteFromS3(Track track) throws AwsServiceException, SdkClientException, S3Exception {
		s3.deleteObject(
			DeleteObjectRequest.builder()
			.key(track.getS3Key())
			.bucket(bucketName)
			.build()
		);
	}
	
	private ResponseEntity<Object> successfulDeleteResponse(Track track) {
		return ResponseEntity.ok(new ResponsePayload() {{
			setTrack(track);
		}});
	}
	
	private ResponseEntity<Object> unauthorizedDeleteResponse() {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ResponsePayload() {{
					setMessage("USER IS NOT AUTHORIZED TO DELETE REQUESTED TRACK");
				}});
	}
	
	private ResponseEntity<Object> serverErrorResponse() {
		return ResponseEntity
				.status(HttpStatusCode.INTERNAL_SERVER_ERROR)
				.body(new ResponsePayload() {{
					setMessage("UNABLE TO DELETE TRACK");
				}});
	}
	
	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	class ResponsePayload {
		
		private String message;
		private Track track;
		
		public ResponsePayload() { }
		
		public ResponsePayload(String message) {
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

}

