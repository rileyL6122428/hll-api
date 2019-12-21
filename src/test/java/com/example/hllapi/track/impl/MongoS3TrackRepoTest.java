package com.example.hllapi.track.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Arrays;
import static java.util.Arrays.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.hllapi.track.Track;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;;

class MongoS3TrackRepoTest {
	
	MongoS3TrackRepo trackRepo;
	
	S3Client s3;
	String bucketName;
	Set<String> allowedFileTypes;
	MongoDBTrackRepo mongoTrackRepo;
	
	@BeforeEach
	void setup() {
		s3 = mock(S3Client.class);
		bucketName = "EXAMPLE_BUCKET_NAME";
		allowedFileTypes = new HashSet<String>() {{
			add("EXAMPLE_ALLOWED_TYPE_1");
			add("EXAMPLE_ALLOWED_TYPE_2");
		}};
		mongoTrackRepo = mock(MongoDBTrackRepo.class);
		
		trackRepo = new MongoS3TrackRepo(
			s3,
			bucketName,
			allowedFileTypes,
			mongoTrackRepo
		);
	}
	
	@Nested
	public class GetTrackByIdMethod {
		
		@Test
		void returnsTrackFromMongoTrackRepo() {
			String trackId = "EXAMPLE_TRACK_ID";
			MongoDBTrack queriedTrack = mock(MongoDBTrack.class);
			when(mongoTrackRepo.byId(trackId)).thenReturn(queriedTrack);
			
			Track track = trackRepo.getTrackById(trackId);
			
			assertEquals(queriedTrack, track);
		}
		
		@Test
		void returnsNullTrackWhenMongoTrackRepoThrows() {
			String trackId = "EXAMPLE_TRACK_ID";
			when(mongoTrackRepo.byId(trackId)).thenThrow(new RuntimeException());
			Track track = trackRepo.getTrackById(trackId);
			assertNull(track);
		}
	}
	
	@Nested
	public class GetTracksByArtistMethod {

		@Test
		void returnsTracksIterableFromMongoRepoConvertedToAList() {
			String artistId = "EXAMPLE_ARTIST_ID";
			MongoDBTrack track1 = mock(MongoDBTrack.class);
			MongoDBTrack track2 = mock(MongoDBTrack.class);
			when(mongoTrackRepo.findAllByUserId(artistId)).thenReturn(asList(track1, track2));
			
			List<Track> tracks = trackRepo.getTracksByArtist(artistId);
			
			assertEquals(2, tracks.size());
			assertEquals(track1, tracks.get(0));
			assertEquals(track2, tracks.get(1));
		}
		
		@Test
		void returnsNullTrackListWhenMongoTrackRepoThrows() {
			String artistId = "EXAMPLE_ARTIST_ID";
			when(mongoTrackRepo.findAllByUserId(artistId)).thenThrow(new RuntimeException());
			List<Track> tracks = trackRepo.getTracksByArtist(artistId);
			assertNull(tracks);
		}
	}
	
	@Nested
	public class GetTrackStreamMethod {
		
		String trackId;
		String s3Key = "EXAMPLE_S3_KEY";
		
		@BeforeEach
		void setup() {
			trackId = "EXAMPLE_TRACK_ID";
			s3Key = "EXAMPLE_S3_KEY";
		}
		
		@Test
		void fetchesTrackFileFromS3() {
			MongoDBTrack track = mock(MongoDBTrack.class);
			when(track.getS3Key()).thenReturn(s3Key);
			when(mongoTrackRepo.byId(trackId)).thenReturn(track);
			
			ArgumentCaptor<GetObjectRequest> s3Captor = ArgumentCaptor.forClass(GetObjectRequest.class); 
			when(s3.getObject(s3Captor.capture())).thenReturn(null);
			
			trackRepo.getTrackStream(trackId);
			
			GetObjectRequest getObjReq = s3Captor.getValue();
			assertEquals(s3Key, getObjReq.key());
			assertEquals(bucketName, getObjReq.bucket());
		}
		
		@Test
		void returnsTrackStreamFromS3Client() {
			MongoDBTrack track = mock(MongoDBTrack.class);
			when(mongoTrackRepo.byId(trackId)).thenReturn(track);
			
			ResponseInputStream<GetObjectResponse> s3Stream = mock(ResponseInputStream.class);
			when(s3.getObject(any(GetObjectRequest.class))).thenReturn(s3Stream);
			
			InputStream returnedStream = trackRepo.getTrackStream(trackId);
			
			assertEquals(s3Stream, returnedStream);
		}
		
		@Test
		void returnsNullStreamWhenS3ClientThrows() {
			MongoDBTrack track = mock(MongoDBTrack.class);
			when(track.getS3Key()).thenReturn(s3Key);
			when(mongoTrackRepo.byId(trackId)).thenReturn(track);
			
			when(s3.getObject(any(GetObjectRequest.class))).thenThrow(new RuntimeException());
			
			InputStream trackStream = trackRepo.getTrackStream(trackId);
			
			assertNull(trackStream);
		}
		
		@Test
		void returnsNullStreamWhenMongoTrackRepoThrows() {
			when(mongoTrackRepo.byId(trackId)).thenThrow(new RuntimeException());
			InputStream trackStream = trackRepo.getTrackStream(trackId);
			assertNull(trackStream);
		}
	}


	@Nested
	public class DeleteTrackMethod {
		
		String trackId;
		String trackS3Key;
		MongoDBTrack track;
		
		@BeforeEach
		void setup() {
			trackId = "EXAMPLE_TRACK_ID";
			trackS3Key = "EXAMPLE_TRACK_S3_KEY";
			track = mock(MongoDBTrack.class);
			when(track.getS3Key()).thenReturn(trackS3Key);
		}
		
		@Test
		void returnsNullTrackIfMongoTrackRepoThrows() {
			when(mongoTrackRepo.byId(trackId)).thenThrow(new RuntimeException());
			Track deletedTrack = trackRepo.deleteTrack(trackId);
			assertNull(deletedTrack);
		}
		
		@Test
		void returnsTrackIfTrackRemovedFromMongoButS3ClientThrows() {
			when(mongoTrackRepo.byId(trackId)).thenReturn(track);
			when(s3.deleteObject(any(DeleteObjectRequest.class))).thenThrow(new RuntimeException());
			
			Track deletedTrack = trackRepo.deleteTrack(trackId);
			
			verify(mongoTrackRepo).delete(track);
			assertEquals(track, deletedTrack);
		}
		
		@Test
		void attemptsToDeleteTrackFromS3() {
			when(mongoTrackRepo.byId(trackId)).thenReturn(track);
			ArgumentCaptor<DeleteObjectRequest> s3Captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
			when(s3.deleteObject(s3Captor.capture())).thenReturn(null);
			
			trackRepo.deleteTrack(trackId);
			
			DeleteObjectRequest deleteObjReq = s3Captor.getValue();
			assertEquals(bucketName, deleteObjReq.bucket());
			assertEquals(trackS3Key, deleteObjReq.key());
		}
	}
}
