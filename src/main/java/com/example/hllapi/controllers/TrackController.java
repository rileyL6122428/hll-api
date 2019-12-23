package com.example.hllapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.hllapi.track.TrackMetadataParser;
import com.example.hllapi.track.TrackUseCases;
import com.example.hllapi.track.TrackUseCases.CreateTrackOutcomes;
import com.example.hllapi.track.TrackUseCases.CreateTrackParams;
import com.example.hllapi.track.TrackUseCases.DeleteTrackOutcomes;
import com.example.hllapi.track.TrackUseCases.DeleteTrackParams;
import com.example.hllapi.track.TrackUseCases.FetchTracksOutcomes;
import com.example.hllapi.track.TrackUseCases.StreamTrackOutcomes;
import com.example.hllapi.track.TrackUseCases.TrackCreation;
import com.example.hllapi.track.TrackUseCases.TrackDeletion;
import com.example.hllapi.track.TrackUseCases.TrackStreamInit;
import com.example.hllapi.track.TrackUseCases.TracksRetrieval;

@Controller
public class TrackController {

	private TrackUseCases trackUseCases;
	private TrackMetadataParser trackParser;
	
	@Autowired
	public TrackController(
		TrackUseCases trackUseCases,
		TrackMetadataParser trackParser
	) {
		this.trackUseCases = trackUseCases;
		this.trackParser = trackParser;
	}
	
	@GetMapping(value="/api/public/tracks")
	public ResponseEntity<Object> getTracks(
		@RequestParam(name="artist-id") String artistId
		
	) throws Exception {
		
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
	
	@GetMapping(value="/api/public/track/{trackId}/stream", produces="audio/mpeg")
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
	
	@PostMapping(value="/api/private/track")
	@CrossOrigin
	public ResponseEntity<Object> postTrack(
		@RequestParam("audio-file") MultipartFile audioFile,
		@RequestHeader("Authorization") String authHeader
	) throws Exception {
		
		TrackCreation trackCreation = trackUseCases.createTrack(new CreateTrackParams() {{
			this.trackName = audioFile.getOriginalFilename();
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
				.body(trackCreation.track);
			
		} else {
			response = ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();
		}
		
		return response;
	}
	
	
	@DeleteMapping(value="/api/private/track/{trackId}")
	public ResponseEntity<Object> deleteTrack(
		@PathVariable String trackId,
		@RequestHeader("Authorization") String authHeader
	) {
		String selectedTrackId = trackId;
		
		TrackDeletion deletion = trackUseCases.deleteTrack(new DeleteTrackParams() {{
			this.trackId = selectedTrackId;
			
			DecodedJWT jwt = JWT.decode(authHeader.substring(6));
			this.requesterId = jwt.getClaim("name").asString();
		}});
		
		ResponseEntity<Object> response;
		if (deletion.outcome == DeleteTrackOutcomes.SUCESSFUL) {
			response = ResponseEntity
				.status(HttpStatus.OK)
				.body(deletion.track);
			
		} else if (deletion.outcome == DeleteTrackOutcomes.UNAUTHORIZED) {
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
