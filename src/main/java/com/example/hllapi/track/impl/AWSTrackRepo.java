package com.example.hllapi.track.impl;

import java.io.InputStream;
import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;
import com.example.hllapi.track.TrackUseCases.CreateTrackParams;

import software.amazon.awssdk.services.s3.S3Client;

public class AWSTrackRepo implements TrackRepo {
	
	private S3Client s3;
	private AmazonDynamoDB dynamoDB;
	
	public AWSTrackRepo(
			S3Client s3,
			AmazonDynamoDB dynamoDB
	) {
		this.s3 = s3;
		this.dynamoDB = dynamoDB;
	}

	public Track getTrackById(String id) {
		return null;
	}
	
	public List<Track> getTracksByArtist(String artistId) {
		return null;
	}
	
	public InputStream getTrackStream(String id) {
		return null;
	}
	
	public Track saveTrack(TrackUseCases.CreateTrackParams params) {
		return null;
	}
	
	public Track deleteTrack(String id) {
		return null;
	}
	
}
