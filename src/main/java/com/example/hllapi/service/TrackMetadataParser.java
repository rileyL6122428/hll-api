package com.example.hllapi.service;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

@Service
public class TrackMetadataParser {
	
	@Value("${fileparser.tempFilePath}")
	private String tempFilepath;
	
	private FFprobe ffprobe;
	private Random random;
	
	public TrackMetadataParser(
		FFprobe ffprobe,
		Random random
	) {
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

	public String getTempFilepath() {
		return tempFilepath;
	}

	public void setTempFilepath(String tempFilepath) {
		this.tempFilepath = tempFilepath;
	}
	
}
