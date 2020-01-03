package com.example.hllapi.track.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

import com.example.hllapi.track.TrackMetadataParser;
import com.mpatric.mp3agic.Mp3File;

public class Mp3agicTrackParser implements TrackMetadataParser {

	private String tempFilepath;
	private Random random;
	
	public Mp3agicTrackParser(
		String tempFilepath,
		Random random
	) {
		this.tempFilepath = tempFilepath;
		this.random = random;
	}
	
	@Override
	public double getDuration(byte[] fileBytes) throws Exception {
		String tempFilePath = tempFilepath + "/temp." + random.nextFloat() + ".test";
		File tempFile = new File(tempFilePath);
		Files.write(tempFile.toPath(), fileBytes);
		
		Mp3File mp3File = new Mp3File(tempFile.getAbsolutePath());
		
		double duration = (double)mp3File.getLengthInMilliseconds() / 1000d;
		
		Files.delete(tempFile.toPath());
		
		return duration;
	}

}
