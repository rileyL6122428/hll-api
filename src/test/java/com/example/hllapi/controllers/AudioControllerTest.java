package com.example.hllapi.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.ResponseEntity;

import com.example.hllapi.model.Track;
import com.example.hllapi.repository.TrackRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

class AudioControllerTest {

	AudioController audioController;
	TrackRepo trackRepo;
	S3Client s3;
	
	@BeforeEach
	void setup() {
		trackRepo = mock(TrackRepo.class);
		s3 = mock(S3Client.class);
		
		audioController = new AudioController(
			trackRepo,
			s3
		);
	}
	
	@Nested
	class StreamTrackMethod {
		
		Track track;
		ResponseInputStream<GetObjectResponse> getS3ObjResponseStream;
		
		@BeforeEach
		void setup() {
			track = mock(Track.class);
			getS3ObjResponseStream = mock(ResponseInputStream.class);
			
			when(track.getId()).thenReturn("EXAMPLE_TRACK_ID");
			when(trackRepo.byId(track.getId())).thenReturn(track);
			when(track.getS3Key()).thenReturn("EXAMPLE_TRACK_KEY");
			when(s3.getObject(any(GetObjectRequest.class))).thenReturn(getS3ObjResponseStream);
		}
		
		@Test
		void looksUpTrackInAudioRepoWithProvidedTrackId() {
			audioController.streamTrack(track.getId());
			verify(trackRepo).byId(track.getId());
		}
		
		@Test
		void usesTrackBucketIdToPullAudioFileFromS3() {
			audioController.streamTrack(track.getId());
			
			ArgumentCaptor<GetObjectRequest> getObjArgCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
			verify(s3).getObject(getObjArgCaptor.capture());
			assertEquals("hey-look-listen", getObjArgCaptor.getValue().bucket());
			assertEquals("EXAMPLE_TRACK_KEY", getObjArgCaptor.getValue().key());
		}
		
		@Test
		void returnsFetchedAudioFileAsAnInputStreamResource() throws Exception {
			InputStreamResource audioFileStream = audioController.streamTrack(track.getId());
			assertEquals(getS3ObjResponseStream, audioFileStream.getInputStream());
		}
	}
	
	@Nested
	class GetTrackMetaData {
		
		private Track track1;
		private Track track2;
		private List<String> trackIds;

		@BeforeEach
		void setup() {
			track1 = mock(Track.class);
			track2 = mock(Track.class);			
			trackIds = asList("EXAMPLE_ID_1", "EXAMPLE_ID_2");
		}
		
		@Test
		void delgatesTrackLookupToTrackRepo() {
			audioController.getTrackMetaData(trackIds);
			verify(trackRepo).findAllById(trackIds);
		}
		
		@Test
		void returnsTracksFoundByTrackRepo() {
			when(trackRepo.findAllById(trackIds)).thenReturn(asList(track1, track2));
			ResponseEntity<Iterable<Track>> response = audioController.getTrackMetaData(trackIds);
			
			Iterator<Track> tracksIterator = response.getBody().iterator();
			assertEquals(track1, tracksIterator.next());
			assertEquals(track2, tracksIterator.next());
			assertFalse(tracksIterator.hasNext());
		}
	}

}
