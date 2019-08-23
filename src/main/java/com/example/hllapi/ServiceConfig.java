package com.example.hllapi;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.bramp.ffmpeg.FFprobe;

@Configuration
public class ServiceConfig {
	
	@Bean
	public FFprobe provideFFProbe() throws Exception {
		return new FFprobe(
			"/usr/local/bin/ffprobe"
		);
	}
	
	@Bean
	public Random provideRandom() {
		return new Random();
	}

}
