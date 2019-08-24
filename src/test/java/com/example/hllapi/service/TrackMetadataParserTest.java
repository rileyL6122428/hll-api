package com.example.hllapi.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

class TrackMetadataParserTest {
	
	private TrackMetadataParser trackParser;
	private FFprobe ffprobe;
	private Random random;
	private String directoryOfTempFile;
	
	@BeforeEach
	public void setup() {
		ffprobe = mock(FFprobe.class);
		random = mock(Random.class);
		directoryOfTempFile = "/Users/rileylittlefield/hey-look-listen/PoC/temp-files";
		
		trackParser = new TrackMetadataParser(
			ffprobe,
			random
		);
		trackParser.setTempFilepath(directoryOfTempFile);
	}

	@Nested
	class GetDuration {
		
		FFmpegProbeResult probeResult;
		FFmpegFormat mpegFormat;
		byte[] fileBytes;
		float exampleRandomFloat;
		double exampleMpegDuration;
		
		@BeforeEach
		public void setup() throws Exception {
			probeResult = mock(FFmpegProbeResult.class);
			mpegFormat = mock(FFmpegFormat.class);
			
			when(ffprobe.probe(any(String.class))).thenReturn(probeResult);
			when(probeResult.getFormat()).thenReturn(mpegFormat);
			
			fileBytes = new byte[]{};
			
			exampleRandomFloat = 1f;
			when(random.nextFloat()).thenReturn(exampleRandomFloat);
			
			exampleMpegDuration = 2d;
			mpegFormat.duration = exampleMpegDuration;
		}
		
		@Test
		void probesConfiguredFilepathWithFFProbe() throws Exception {
			trackParser.getDuration(fileBytes);
			verify(ffprobe).probe(directoryOfTempFile + "/temp." + exampleRandomFloat + ".test");
		}
		
		@Test
		void returnsTheDurationFoundOnTheMpegFormat() throws Exception {
			double duration = trackParser.getDuration(fileBytes);
			assertEquals(exampleMpegDuration, duration);
		}
		
		@Test
		void deletesTempFileFromfilesystem() throws Exception {		
			trackParser.getDuration(fileBytes);
			
			File folder = new File(directoryOfTempFile);
			for(File file : folder.listFiles()) {
				assertNotEquals(directoryOfTempFile + "/temp." + exampleRandomFloat + ".test", file.getAbsolutePath());
			}
		}
		
	}
	

}
