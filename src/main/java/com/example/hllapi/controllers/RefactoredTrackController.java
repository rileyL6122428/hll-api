package com.example.hllapi.controllers;

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
import com.example.hllapi.service.TrackMetadataParser;
import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackUseCases;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import static com.example.hllapi.track.TrackUseCases.*;

import java.util.List;

@Controller
public class RefactoredTrackController {

	private TrackUseCases trackUseCases;
	private TrackMetadataParser trackParser;
	
	public RefactoredTrackController(
		TrackUseCases trackUseCases,
		TrackMetadataParser trackParser
	) {
		this.trackUseCases = trackUseCases;
		this.trackParser = trackParser;
	}
	
	@GetMapping(value="/api/v2/public/tracks")
	public ResponseEntity<Object> getTracks(@RequestParam(name="artist-id") String artistId) throws Exception {
		
		TracksRetrieval retrieval = trackUseCases.getTracksByArtist(artistId); 
		
		ResponseEntity<Object> response;
		if (retrieval.outcome == FetchTracksOutcomes.SUCESSFUL) {
			response = ResponseEntity.ok(retrieval.tracks);
			
		} else {
			response = ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();
		}
		
		return response;
	}
	
	@GetMapping(value="/api/v2/public/track/{trackId}/stream", produces="audio/mpeg")
	@ResponseBody
	public InputStreamResource streamTrack(@PathVariable String trackId) {
		TrackStreamInit trackStreamInit = trackUseCases.streamTrack(trackId);
		
		InputStreamResource streamResource;
		if (trackStreamInit.outcome == StreamTrackOutcomes.SUCESSFUL) {
			streamResource = new InputStreamResource(trackStreamInit.stream);
		} else {
			streamResource = null;
		}
		
		return streamResource;
	}
	
	@PostMapping(value="/api/v2/private/track")
	@CrossOrigin
	public ResponseEntity<Object> postTrack(
		@RequestParam("audio-file") MultipartFile audioFile,
		@RequestHeader("Authorization") String authHeader
	) throws Exception {
		
		TrackCreation trackCreation = trackUseCases.createTrack(new CreateTrackParams() {{
			this.trackName = audioFile.getName();
			this.trackBytes = audioFile.getBytes();
			this.fileType = audioFile.getContentType();

			double duration = trackParser.getDuration(audioFile.getBytes());
			this.duration = duration;
			
			DecodedJWT jwt = JWT.decode(authHeader.substring(6));
			this.artistName = jwt.getClaim("name").asString();
		}});
		
		ResponseEntity<Object> response;
		
		if (trackCreation.outcome == CreateTrackOutcomes.SUCESSFUL) {
			response = ResponseEntity
				.status(HttpStatus.OK)
				.body((Object)trackCreation.track);
			
		} else if (trackCreation.outcome == CreateTrackOutcomes.UNAUTHORIZED) {
			response = ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.build();

		} else {
			response = ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();
		}
		
		return response;
	}
	
}
