package com.example.hllapi.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

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
			String artistId = "EXAMPLE_ARTIST_ID";
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

}
