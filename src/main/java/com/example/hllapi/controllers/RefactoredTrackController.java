package com.example.hllapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.hllapi.service.TrackMetadataParser;
import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackUseCases;
import static com.example.hllapi.track.TrackUseCases.*;

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
