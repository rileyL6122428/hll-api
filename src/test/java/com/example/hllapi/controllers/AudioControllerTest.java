package com.example.hllapi.controllers;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.hllapi.repository.AudioRepo;

import software.amazon.awssdk.services.s3.S3Client;

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
		
		@Test
		void looksUpTrackInAudioRepoWithProvidedTrackId() {
			String trackId = "EXAMPLE_TRACK_ID";
			audioController.streamTrack(trackId);
			verify(audioRepo).getTrackById(trackId);
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
