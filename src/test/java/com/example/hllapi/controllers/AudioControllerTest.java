package com.example.hllapi.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.InputStreamResource;

import com.example.hllapi.model.Track;
import com.example.hllapi.repository.AudioRepo;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

class AudioControllerTest {

	AudioController audioController;
	AudioRepo audioRepo;
	S3Client s3;
	
	@BeforeEach
	void setup() {
		audioRepo = mock(AudioRepo.class);
		s3 = mock(S3Client.class);
		
		audioController = new AudioController(
			audioRepo,
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
			when(audioRepo.getTrackById(track.getId())).thenReturn(track);
			when(track.getTrackKey()).thenReturn("EXAMPLE_TRACK_KEY");
			when(s3.getObject(any(GetObjectRequest.class))).thenReturn(getS3ObjResponseStream);
		}
		
		@Test
		void looksUpTrackInAudioRepoWithProvidedTrackId() {
			audioController.streamTrack(track.getId());
			verify(audioRepo).getTrackById(track.getId());
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
	
//	@Nested
//	class UploadTrackMethod {
//		
//		MultipartFile upload;
//		InputStream uploadInputStream;
//		
//		@BeforeEach
//		void setup() throws Exception {
//			upload = mock(MultipartFile.class);
//			uploadInputStream = mock(InputStream.class);
//			
//			when(upload.getInputStream()).thenReturn(uploadInputStream);
//		}
//		
//		@Test
//		void storesFileInputStream() throws Exception {
//			audioController.uploadTrack(upload);
//			verify(audioFileRepo).store(uploadInputStream);
//		}
//		
//		@Test
//		void returnsFileIdReturnedByFileRepo() throws Exception {
//			String storedFileId = "EXAMPLE_FILE_ID";
//			when(audioFileRepo.store(uploadInputStream)).thenReturn(storedFileId);
//			
//			ResponseEntity<String> uploadFileResponse = audioController.uploadTrack(upload);
//			String returnedFileId = uploadFileResponse.getBody();
//			
//			assertEquals(storedFileId, returnedFileId);
//		}
//	}

}
