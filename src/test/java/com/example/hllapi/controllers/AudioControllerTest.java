package com.example.hllapi.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.hllapi.repository.AudioFileRepo;

class AudioControllerTest {

	AudioController audioController;
	AudioFileRepo audioFileRepo;
	
	@BeforeEach
	void setup() {
		audioFileRepo = mock(AudioFileRepo.class);
		
		audioController = new AudioController(
			audioFileRepo
		);
	}
	
	@Nested
	class UploadTrackMethod {
		
		MultipartFile upload;
		InputStream uploadInputStream;
		
		@BeforeEach
		void setup() throws Exception {
			upload = mock(MultipartFile.class);
			uploadInputStream = mock(InputStream.class);
			
			when(upload.getInputStream()).thenReturn(uploadInputStream);
		}
		
		@Test
		void storesFileInputStream() throws Exception {
			audioController.uploadTrack(upload);
			verify(audioFileRepo).store(uploadInputStream);
		}
		
		@Test
		void returnsFileIdReturnedByFileRepo() throws Exception {
			String storedFileId = "EXAMPLE_FILE_ID";
			when(audioFileRepo.store(uploadInputStream)).thenReturn(storedFileId);
			
			ResponseEntity<String> uploadFileResponse = audioController.uploadTrack(upload);
			String returnedFileId = uploadFileResponse.getBody();
			
			assertEquals(storedFileId, returnedFileId);
		}
	}

}
