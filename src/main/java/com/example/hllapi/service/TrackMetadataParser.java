package com.example.hllapi.service;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

public class TrackMetadataParser {
	
	private Random random = new Random();
	
	public double getDuration(byte[] fileBytes) throws Exception {
		String tempFilePath = "/Users/rileylittlefield/hey-look-listen/PoC/temp-files/temp." + random.nextFloat() + ".test";
		File tempFile = new File(tempFilePath);
		Files.write(tempFile.toPath(), fileBytes);
		
		FFprobe ffprobe = new FFprobe("/usr/local/bin/ffprobe");
		FFmpegProbeResult probeResult = ffprobe.probe(tempFilePath);
		FFmpegFormat format = probeResult.getFormat();
		
		Files.delete(tempFile.toPath());
		return format.duration;
	}
	
}
