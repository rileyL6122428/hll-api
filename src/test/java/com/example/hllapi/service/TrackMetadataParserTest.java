package com.example.hllapi.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;

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
	private String tempFilepath;
	
	@BeforeEach
	public void setup() {
		ffprobe = mock(FFprobe.class);
		random = mock(Random.class);
		tempFilepath = "/Users/rileylittlefield/hey-look-listen/PoC/temp-files";
		
		trackParser = new TrackMetadataParser(
			ffprobe,
			random
		);
		trackParser.setTempFilepath(tempFilepath);
	}

	@Nested
	class GetDuration {
		
		FFmpegProbeResult probeResult;
		FFmpegFormat mpegFormat;
		
		@BeforeEach
		public void setup() throws Exception {
			probeResult = mock(FFmpegProbeResult.class);
			mpegFormat = mock(FFmpegFormat.class);
			
			when(ffprobe.probe(any(String.class))).thenReturn(probeResult);
			when(probeResult.getFormat()).thenReturn(mpegFormat);
		}
		
		@Test
		void probesConfiguredFilepathWithFFProbe() throws Exception {
			byte[] fileBytes = {};
			
			float exampleRandomFloat = 1f;
			when(random.nextFloat()).thenReturn(exampleRandomFloat);
			
			trackParser.getDuration(fileBytes);
			
			verify(ffprobe).probe(tempFilepath + "/temp." + exampleRandomFloat + ".test");
		}
		
		@Test
		void returnsTheDurationFoundOnTheMpegFormat() throws Exception {
			byte[] fileBytes = {};
			
			float exampleRandomFloat = 1f;
			when(random.nextFloat()).thenReturn(exampleRandomFloat);
			
			double exampleMpegDuration = 2d;
			mpegFormat.duration = exampleMpegDuration;
			
			double duration = trackParser.getDuration(fileBytes);
			
			assertEquals(exampleMpegDuration, duration);
		}
		
	}
	

}
