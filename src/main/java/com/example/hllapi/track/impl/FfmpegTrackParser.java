package com.example.hllapi.track.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

import com.example.hllapi.track.TrackMetadataParser;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

public class FfmpegTrackParser implements TrackMetadataParser {
	
//	@Value("${fileparser.tempFilePath}")
	private String tempFilepath;
	private FFprobe ffprobe;
	private Random random;
	
	public FfmpegTrackParser(
		String tempFilepath,
		FFprobe ffprobe,
		Random random
	) {
		this.tempFilepath = tempFilepath;
		this.ffprobe = ffprobe;
		this.random = random;
	}
	
	public double getDuration(byte[] fileBytes) throws Exception {
		String tempFilePath = tempFilepath + "/temp." + random.nextFloat() + ".test";
		File tempFile = new File(tempFilePath);
		Files.write(tempFile.toPath(), fileBytes);
		
		FFmpegProbeResult probeResult = ffprobe.probe(tempFilePath);
		FFmpegFormat format = probeResult.getFormat();
		
		Files.delete(tempFile.toPath());
		return format.duration;
	}

}
