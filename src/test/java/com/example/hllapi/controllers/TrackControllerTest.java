package com.example.hllapi.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackMetadataParser;
import com.example.hllapi.track.TrackUseCases;

class TrackControllerTest {
	
	TrackController controller;
	
	TrackUseCases useCases;
	TrackMetadataParser trackParser;

	@BeforeEach
	void setup() {
		useCases = mock(TrackUseCases.class);
		trackParser = mock(TrackMetadataParser.class);
		
		controller = new TrackController(
			useCases,
			trackParser
		);		
	}
	
	@Nested
	public class GetTracksMethod {
		
		TrackUseCases.TracksRetrieval retrieval;
		String artistId;
		
		@BeforeEach
		void setup() {
			artistId = "EXAMPLE_ARTIST_ID";
			
			retrieval = new TrackUseCases.TracksRetrieval();
			when(useCases.getTracksByArtist(artistId)).thenReturn(retrieval);
		}
		
		@Test
		void looksUpTracksRelatedToProvidedArtist() throws Exception {
			controller.getTracks(artistId);
			verify(useCases).getTracksByArtist(artistId);
		}
		
		@Test
		void returnsSuccessResponseWhenRetrievalSuccessful() throws Exception {
			retrieval.outcome = TrackUseCases.FetchTracksOutcomes.SUCESSFUL;
			retrieval.tracks = new ArrayList<Track>() {{
				add(mock(Track.class));
				add(mock(Track.class));
				add(mock(Track.class));
			}};
			
			ResponseEntity<Object> response = controller.getTracks(artistId);
			
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(retrieval.tracks, response.getBody());
		}
		
		@Test
		void returnsFailureResponseWhenRetrievalFails() throws Exception {
			retrieval.outcome = TrackUseCases.FetchTracksOutcomes.FAILURE;
			ResponseEntity<Object> response = controller.getTracks(artistId);
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		}
	}

	@Nested
	public class StreamTrackMethod {
		
		String trackId;
		TrackUseCases.TrackStreamInit streamInit;
		
		@BeforeEach
		void setup() {
			trackId = "EXAMPLE_TRACK_ID";
			
			streamInit = new TrackUseCases.TrackStreamInit();
			when(useCases.streamTrack(trackId)).thenReturn(streamInit);
		}
		
		@Test
		void returnsStreamResourceWhenStreamInitIsSuccessful() throws Exception {
			streamInit.outcome = TrackUseCases.StreamTrackOutcomes.SUCESSFUL;
			streamInit.stream = mock(InputStream.class);
			InputStreamResource streamResource = controller.streamTrack(trackId);
			assertEquals(streamInit.stream, streamResource.getInputStream());
		}
		
		@Test
		void returnsNullWhenStreamInitFails() throws Exception {
			streamInit.outcome = TrackUseCases.StreamTrackOutcomes.FAILURE;
			InputStreamResource streamResource = controller.streamTrack(trackId);
			assertNull(streamResource);
		}
	}


	@Nested
	public class PostTrackMethod {
		
		MultipartFile audioFile;
		double trackDuration;
		String authHeader;
		
		TrackUseCases.TrackCreation trackCreation;
		ArgumentCaptor<TrackUseCases.CreateTrackParams> postTrackCaptor;
		
		@BeforeEach
		void beforeEach() throws Exception {
			// name: "John Doe"
			authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
			
			audioFile = mock(MultipartFile.class);
			when(audioFile.getBytes()).thenReturn(new byte[] {});
			when(audioFile.getOriginalFilename()).thenReturn("EXAMPLE_FILE_NAME");
			when(audioFile.getContentType()).thenReturn("EXAMPLE_GET_CONTENT_TYPE");
			
			trackDuration = 123d;
			when(trackParser.getDuration(audioFile.getBytes())).thenReturn(trackDuration);
			
			trackCreation = new TrackUseCases.TrackCreation();
			postTrackCaptor = ArgumentCaptor.forClass(TrackUseCases.CreateTrackParams.class);
			when(useCases.createTrack(postTrackCaptor.capture())).thenReturn(trackCreation);
		}
		
		@Test
		void createsTrackWithProvidedParams() throws Exception {
			controller.postTrack(audioFile, authHeader);
			
			TrackUseCases.CreateTrackParams createParams = postTrackCaptor.getValue();
			assertEquals("John Doe", createParams.artistName);
			assertEquals(audioFile.getOriginalFilename(), createParams.trackName);
			assertEquals(audioFile.getBytes(), createParams.trackBytes);
			assertEquals(audioFile.getContentType(), createParams.fileType);
			assertEquals(trackDuration, createParams.duration);
		}
	}
}
