package com.example.hllapi.track.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;

class TrackUseCasesImplTest {
	
	TrackUseCasesImpl trackUseCases;
	
	TrackRepo trackRepo;
	Set<String> allowedFileTypes;
	
	@BeforeEach
	void setup() {
		trackRepo = mock(TrackRepo.class);
		allowedFileTypes = new HashSet<String>() {{
			add("EXAMPLE_ALLOWED_FILE_TYPE_1".toLowerCase());
			add("EXAMPLE_ALLOWED_FILE_TYPE_2".toLowerCase());
		}};
		
		trackUseCases = new TrackUseCasesImpl(
			trackRepo,
			allowedFileTypes
		);
	}
	
	@Nested
	public class GetTracksByArtistMethod {
		
		String artistId;
		
		@BeforeEach
		void setup() {
			artistId = "EXAMPLE_ARTIST_ID";
		}
		
		@Test
		void returnsFailureOutcomeWhenTrackRepoReturnsNullTrackList() {
			when(trackRepo.getTracksByArtist(artistId)).thenReturn(null);
			TrackUseCases.TracksRetrieval retrieval = trackUseCases.getTracksByArtist(artistId);
			assertEquals(TrackUseCases.FetchTracksOutcomes.FAILURE, retrieval.outcome);
			assertNull(retrieval.tracks);
		}
		
		@Test
		void returnSuccessOutcomeWhenTrackRepoReturnsNonNullList() {
			ArrayList<Track> trackList = new ArrayList<Track>(){{
				add(mock(Track.class));
				add(mock(Track.class));
			}};
			when(trackRepo.getTracksByArtist(artistId)).thenReturn(trackList);
			
			TrackUseCases.TracksRetrieval retrieval = trackUseCases.getTracksByArtist(artistId);
			
			assertEquals(TrackUseCases.FetchTracksOutcomes.SUCESSFUL, retrieval.outcome);
			assertEquals(trackList, retrieval.tracks);
		}
	}

	@Nested
	public class StreamTrackMethod {
		
		String trackId;
		
		@BeforeEach
		void setup() {
			trackId = "EXAMPLE_TRACK_ID";
		}
		
		@Test
		void returnsFailureOutcomeWhenTrackRepoReturnsNullStream() {
			when(trackRepo.getTrackStream(trackId)).thenReturn(null);
			TrackUseCases.TrackStreamInit streamInit = trackUseCases.streamTrack(trackId);
			assertEquals(TrackUseCases.StreamTrackOutcomes.FAILURE, streamInit.outcome);
		}
		
		@Test
		void returnsSuccessOutcomeWhenTrackRepoReturnsNonNullStream() {
			InputStream trackStream = mock(InputStream.class);
			when(trackRepo.getTrackStream(trackId)).thenReturn(trackStream);
			TrackUseCases.TrackStreamInit streamInit = trackUseCases.streamTrack(trackId);
			assertEquals(TrackUseCases.StreamTrackOutcomes.SUCESSFUL, streamInit.outcome);
			assertEquals(trackStream, streamInit.stream);
		}
	}

	@Nested
	public class CreateTrackMethod {
		
		TrackUseCases.CreateTrackParams createParams;
		
		@BeforeEach
		void setup() {
			createParams = new TrackUseCases.CreateTrackParams();
		}
		
		@Test
		void returnsFailureOutcomeIfFiletypeIsNotAllowed() {
			createParams.fileType = "NOT_ALLOWED_FILE_TYPE";
			TrackUseCases.TrackCreation trackCreation = trackUseCases.createTrack(createParams);
			assertEquals(TrackUseCases.CreateTrackOutcomes.FAILURE_FROM_IMPROPER_FILE_FORMAT, trackCreation.outcome);
			assertNull(trackCreation.track);
		}
		
		@Test
		void returnsFailureOutcomeIfTrackRepoReturnsNull() {
			createParams.fileType = "EXAMPLE_ALLOWED_FILE_TYPE_1";
			when(trackRepo.saveTrack(createParams)).thenReturn(null);
			TrackUseCases.TrackCreation trackCreation = trackUseCases.createTrack(createParams);
			assertEquals(TrackUseCases.CreateTrackOutcomes.FAILURE, trackCreation.outcome);
			assertNull(trackCreation.track);
		}
		
		@Test
		void returnsSuccessOutcomeWhenTrackRepoReturnsNonNullTrack() {
			createParams.fileType = "EXAMPLE_ALLOWED_FILE_TYPE_1";
			Track savedTrack = mock(Track.class);
			when(trackRepo.saveTrack(createParams)).thenReturn(savedTrack);
			
			TrackUseCases.TrackCreation trackCreation = trackUseCases.createTrack(createParams);
			
			assertEquals(TrackUseCases.CreateTrackOutcomes.SUCESSFUL, trackCreation.outcome);
			assertEquals(savedTrack, trackCreation.track);
		}
	}

	@Nested
	public class DeleteTrackMethod {
		
		TrackUseCases.DeleteTrackParams deleteParams;
		
		@BeforeEach
		void setup() {
			deleteParams = new TrackUseCases.DeleteTrackParams();
		}
		
		@Test
		void returnsUnauthorizedResponseWhenTargetTrackIsNotFoundByTrackRepo() {
			when(trackRepo.getTrackById(any(String.class))).thenReturn(null);
			TrackUseCases.TrackDeletion deletion = trackUseCases.deleteTrack(deleteParams);
			assertEquals(TrackUseCases.DeleteTrackOutcomes.UNAUTHORIZED, deletion.outcome);
			assertNull(deletion.track);
		}
		
		@Test
		void returnsFailureResponseWhenNullTrackIsReturnedFromTrackRepoDeleteMethod() {
			Track track = mock(Track.class);
			when(track.getUserId()).thenReturn("TRACK_OWNER");
			when(track.getId()).thenReturn("TRACK_ID");
			when(trackRepo.getTrackById("TRACK_ID")).thenReturn(track);
			
			deleteParams.requesterId = "TRACK_OWNER";
			deleteParams.trackId = "TRACK_ID";
			
			when(trackRepo.deleteTrack("TRACK_ID")).thenReturn(null);
			
			TrackUseCases.TrackDeletion deletion = trackUseCases.deleteTrack(deleteParams);
			
			assertEquals(TrackUseCases.DeleteTrackOutcomes.FAILURE, deletion.outcome);
			assertNull(deletion.track);
		}
		
		@Test
		void returnsUnauthorizedResponseWhenRequesterIsNotAuthorizedToDeleteTrack() {
			Track track = mock(Track.class);
			when(track.getUserId()).thenReturn("TRACK_OWNER");
			when(trackRepo.getTrackById("TRACK_ID")).thenReturn(track);
			
			deleteParams.requesterId = "NOT_TRACK_OWNER";
			deleteParams.trackId = "TRACK_ID";
			
			TrackUseCases.TrackDeletion deletion = trackUseCases.deleteTrack(deleteParams);
			
			assertEquals(TrackUseCases.DeleteTrackOutcomes.UNAUTHORIZED, deletion.outcome);
			assertNull(deletion.track);
		}
		
		@Test
		void returnsSuccessResponseWhenTrackIsSuccessfullyDeletedByTrackRepo() {
			Track track = mock(Track.class);
			when(track.getUserId()).thenReturn("TRACK_OWNER");
			when(track.getId()).thenReturn("TRACK_ID");
			when(trackRepo.getTrackById("TRACK_ID")).thenReturn(track);
			
			deleteParams.requesterId = "TRACK_OWNER";
			deleteParams.trackId = "TRACK_ID";
			
			when(trackRepo.deleteTrack("TRACK_ID")).thenReturn(track);
			
			TrackUseCases.TrackDeletion deletion = trackUseCases.deleteTrack(deleteParams);
			
			assertEquals(TrackUseCases.DeleteTrackOutcomes.SUCESSFUL, deletion.outcome);
			assertEquals(track, deletion.track);
		}
	}
}
