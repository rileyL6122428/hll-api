package com.example.hllapi.track.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

class FfmpegTrackParserTest {
	
	FfmpegTrackParser trackParser;
	
	String tempFilepath;
	FFprobe ffprobe;
	Random random;
	
	@BeforeEach
	void setup() throws Exception {
		InputStream inputStream = ClassLoader.getSystemResourceAsStream("unittest.properties");
		Properties properties = new Properties();
		properties.load(inputStream);
		tempFilepath = properties.getProperty("fileparser.tempfile.location");
		
		ffprobe = mock(FFprobe.class);
		random = mock(Random.class);
		
		trackParser = new FfmpegTrackParser(
			tempFilepath,
			ffprobe,
			random
		);
	}
	
	@Nested
	public class GetDurationMethod {
		
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
			verify(ffprobe).probe(tempFilepath + "/temp." + exampleRandomFloat + ".test");
		}
		
		@Test
		void returnsTheDurationFoundOnTheMpegFormat() throws Exception {
			double duration = trackParser.getDuration(fileBytes);
			assertEquals(exampleMpegDuration, duration);
		}
		
		@Test
		void deletesTempFileFromFilesystem() throws Exception {		
			trackParser.getDuration(fileBytes);
			
			File folder = new File(tempFilepath);
			for(File file : folder.listFiles()) {
				assertNotEquals(tempFilepath + "/temp." + exampleRandomFloat + ".test", file.getAbsolutePath());
			}
		}
	}
}
