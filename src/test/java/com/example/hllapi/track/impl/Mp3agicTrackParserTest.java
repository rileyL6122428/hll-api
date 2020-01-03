package com.example.hllapi.track.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Mp3agicTrackParserTest {
	
	Mp3agicTrackParser trackParser;
	
	String tempFilepath;
	Random random;
	InputStream godzillaRoarTrack;
	
	@BeforeEach
	void setup() throws Exception {
		godzillaRoarTrack = ClassLoader.getSystemResourceAsStream("godzilla_roar.mp3");
		
		InputStream inputStream = ClassLoader.getSystemResourceAsStream("unittest.properties");
		Properties properties = new Properties();
		properties.load(inputStream);
		tempFilepath = properties.getProperty("fileparser.tempfile.location");
		
		random = mock(Random.class);
		
		trackParser = new Mp3agicTrackParser(
			tempFilepath,
			random
		);
	}
	
	@Nested
	class GetDurationMethod {
		
		byte[] godzillaRoarTrackBytes;
		
		@BeforeEach
		void setup() throws Exception {
			when(random.nextFloat()).thenReturn(123f);
			godzillaRoarTrackBytes = IOUtils.toByteArray(godzillaRoarTrack);
		}
		
		@Test
		void returnsCorrectDuration() throws Exception {
			double duration = trackParser.getDuration(godzillaRoarTrackBytes);
			assertEquals(13.955d, duration);
		}
		
		@Test
		void deletesTempFileAfterReadingDuration() throws Exception {
			trackParser.getDuration(godzillaRoarTrackBytes);
			File folder = new File(tempFilepath);
			assertEquals(0, folder.listFiles().length);
		}
	}

}
