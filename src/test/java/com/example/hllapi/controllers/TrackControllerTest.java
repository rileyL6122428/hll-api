package com.example.hllapi.controllers;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.hllapi.model.Track;
import com.example.hllapi.repository.TrackRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class TrackControllerTest {

	TrackController trackController;
	TrackRepo trackRepo;
	S3Client s3;
	String bucketName;
	
	@BeforeEach
	void setup() {
		bucketName = "hey-look-listen";
		trackRepo = mock(TrackRepo.class);
		s3 = mock(S3Client.class);
		
		trackController = new TrackController(
			trackRepo,
			s3
		);
		trackController.setBucketName(bucketName);
	}
	
	@Nested
	class StreamTrack {
		
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
		void looksUpTrackInTrackRepoWithProvidedTrackId() {
			trackController.streamTrack(track.getId());
			verify(trackRepo).byId(track.getId());
		}
		
		@Test
		void usesTrackBucketIdToPullAudioFileFromS3() {
			trackController.streamTrack(track.getId());
			
			ArgumentCaptor<GetObjectRequest> getObjArgCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
			verify(s3).getObject(getObjArgCaptor.capture());
			assertEquals(bucketName, getObjArgCaptor.getValue().bucket());
			assertEquals("EXAMPLE_TRACK_KEY", getObjArgCaptor.getValue().key());
		}
		
		@Test
		void returnsFetchedAudioFileAsAnInputStreamResource() throws Exception {
			InputStreamResource audioFileStream = trackController.streamTrack(track.getId());
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
			trackController.getTrackMetaData(trackIds);
			verify(trackRepo).findAllById(trackIds);
		}
		
		@Test
		void returnsTracksFoundByTrackRepo() {
			when(trackRepo.findAllById(trackIds)).thenReturn(asList(track1, track2));
			ResponseEntity<Iterable<Track>> response = trackController.getTrackMetaData(trackIds);
			
			Iterator<Track> tracksIterator = response.getBody().iterator();
			assertEquals(track1, tracksIterator.next());
			assertEquals(track2, tracksIterator.next());
			assertFalse(tracksIterator.hasNext());
		}
	}
	
	@Nested
	class PostTrack {
		
		MultipartFile file;
		String authHeader;
		
		@BeforeEach
		void setup() {
			file = mock(MultipartFile.class);
			authHeader = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1VTTNSVU5HUXpWQlJVSTFNMFEzUXpZd09UWXpNa1ZFTXpNeE9ETTROMEpFUkRBMlJqQTBPUSJ9.eyJuaWNrbmFtZSI6InJpbGV5bGl0dGxlZmllbGQiLCJuYW1lIjoicmlsZXlsaXR0bGVmaWVsZEB5bWFpbC5jb20iLCJwaWN0dXJlIjoiaHR0cHM6Ly9zLmdyYXZhdGFyLmNvbS9hdmF0YXIvNTgwYTA4ODMxNDRiMWNmNWM5ZjFhNTFmY2Q0MWVhZjg_cz00ODAmcj1wZyZkPWh0dHBzJTNBJTJGJTJGY2RuLmF1dGgwLmNvbSUyRmF2YXRhcnMlMkZyaS5wbmciLCJ1cGRhdGVkX2F0IjoiMjAxOS0wOC0xNFQwMjo1OTo0OS4wNjdaIiwiaXNzIjoiaHR0cHM6Ly9kZXYta2ZhYXQ4LTguYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDVjYmEyZTJkODFhYzkwMTAzM2JhMDQ0ZiIsImF1ZCI6IjhzMHN2WlZFZlMyeENOdzgyaXZnR3IzWUZVNE9ReDduIiwiaWF0IjoxNTY1ODMzNjY1LCJleHAiOjE1NjU4Njk2NjUsImF0X2hhc2giOiIyMWRFRVlaQUJmemlMSDZVcVNCa0VnIiwibm9uY2UiOiJqNDAyWn5pN3FBcTFrZFRhMVVKbWd4WVFxOXNodkZMSCJ9.B9L6Iu8Og7BbvEEem9yJnRzGdA2ZofFob_IVu3vqW2oLZ8NlJD_bBWvbilFaqcVFs3EutEc2xyzJAWcbNpU86KMYD8CVOm8Y0awd-mCt-DhPeN6wEd0j6GpSYc1-MyGW0ScD8fpMKU_jfEASeGHKVcj9r1aZIWSWIsvHTLgGtcV13MDav3IN2NF3yNJQSFPm_nIyAas2vV0Oe41e_VFFvsE8HR4o94L6kiyKZ5ZCl55jegvM7ifbbRuFTpnxMnxLB6YhFKbw8rxi9n4p960ugsfZkOfTBOj4pnCKrpJxy1rNWTRcYqKFQpc6ncAHULxE2LP1MEqBA9od-gVJhpwO2g";
		}
		
		@Test
		void returnsABadRequestStatusCodeWhenFileIsNotAnMp3() throws Exception {
			when(file.getContentType()).thenReturn("NOT_AN_MP3");
			ResponseEntity<RespBody> response = trackController.postTrack(file, authHeader);
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertEquals("UNALLOWED CONTENT TYPE", response.getBody().getMessage());
		}
		
		@Test
		void returnsAnInternalServerErrorResponseIfAnExceptionOccurs() throws Exception {
			when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
				.thenThrow(new RuntimeException("EXAMPLE_RUNTIME_EXCEPTION"));
			
			ResponseEntity<RespBody> response = trackController.postTrack(file, authHeader);
			
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
			assertEquals("UPLOAD FAILED", response.getBody().getMessage());
		}
		
		@Nested
		class HappyPath {
			
			@BeforeEach
			void setup() throws Exception {
				when(file.getContentType()).thenReturn("audio/mp3");
				when(file.getOriginalFilename()).thenReturn("EXAMPLE_ORIGINAL_FILENAME");
				when(file.getBytes()).thenReturn(new byte[] {});
			}
			
			@Test
			void returnsASuccessResponseWhenAnMp3IsSuccessfullyUploaded() throws Exception {
				Track returnedTrack = mock(Track.class);
				when(trackRepo.save(any(Track.class))).thenReturn(returnedTrack);
				
				ResponseEntity<RespBody> response = trackController.postTrack(file, authHeader);
				assertEquals(HttpStatus.OK, response.getStatusCode());
				assertEquals("UPLOAD SUCCEEDED", response.getBody().getMessage());
				assertEquals(returnedTrack, response.getBody().getTrack());
			}
			
			@Test
			void callsS3ClientWithAppropriateArguments() throws Exception {
				ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
				ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
				
				when(s3.putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture())).thenReturn(null);
				
				trackController.postTrack(file, authHeader);
				
				PutObjectRequest putObjectRequest = putObjectRequestCaptor.getValue(); 
				assertEquals(bucketName, putObjectRequest.bucket());
				assertEquals("audio/EXAMPLE_ORIGINAL_FILENAME.mp3", putObjectRequest.key());
				
				RequestBody requestBody = requestBodyCaptor.getValue();
				assertEquals(0, requestBody.contentLength()); // Verifies that the request body is the empty byte array passed in above
			}
			
			@Test
			void savesTrackIntoTrackRepo() throws Exception {
				ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);
				when(trackRepo.save(trackCaptor.capture())).thenReturn(null);
				
				trackController.postTrack(file, authHeader);
				
				Track storedTrack = trackCaptor.getValue();
				assertEquals("audio/EXAMPLE_ORIGINAL_FILENAME.mp3", storedTrack.getS3Key());
				assertEquals("rileylittlefield@ymail.com", storedTrack.getUserId());
			}
		}
		
	}

}
