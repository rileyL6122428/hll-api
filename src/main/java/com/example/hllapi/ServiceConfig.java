package com.example.hllapi;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.bramp.ffmpeg.FFprobe;

@Configuration
public class ServiceConfig {
	
	@Value("${ffprobe.bin.path}")
	private String ffprobePath;
	
	@Bean
	public FFprobe provideFFProbe() throws Exception {
		return new FFprobe(ffprobePath);
	}
	
	@Bean
	public Random provideRandom() {
		return new Random();
	}

}
