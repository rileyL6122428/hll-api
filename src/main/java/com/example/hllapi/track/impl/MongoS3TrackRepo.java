package com.example.hllapi.track.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Arrays;

import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class MongoS3TrackRepo implements TrackRepo {
	
	private S3Client s3;
	private String bucketName;
	private MongoDBTrackRepo mongoTrackRepo;
	
	public MongoS3TrackRepo(
		S3Client s3,
		String bucketName,
		MongoDBTrackRepo mongoTrackRepo
	) {
		this.s3 = s3;
		this.bucketName = bucketName;
		this.mongoTrackRepo = mongoTrackRepo;
	}
	
	public Track getTrackById(String id) {
		Track track = null;
		try {
			track = this.mongoTrackRepo.byId(id);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return track;
	}
	
	public List<Track> getTracksByArtist(String artistId) {
		ArrayList<Track> tracks = new ArrayList<Track>();
		try {
			Iterable<MongoDBTrack> tracksIterable = this.mongoTrackRepo.findAllByUserId(artistId);
			Iterator<MongoDBTrack> tracksIterator = tracksIterable.iterator();
			while(tracksIterator.hasNext()) {
				tracks.add(tracksIterator.next());
			}			
		} catch (Exception exception) {
			exception.printStackTrace();
			tracks = null;
		}
		return tracks;
	}
	
	public InputStream getTrackStream(String id) {
		InputStream trackInputStream = null;
		
		try {
			Track track = mongoTrackRepo.byId(id);
			
			trackInputStream = s3.getObject(
				GetObjectRequest.builder()
					.bucket(bucketName)
					.key(track.getS3Key())
					.build()
			);
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return trackInputStream;
	}
	
	public Track saveTrack(TrackUseCases.CreateTrackParams params) {
		Track track = null;
		
		try {
			String s3Key = "audio/" + params.trackName + ".mp3";
			
			s3.putObject(
				PutObjectRequest.builder()
					.bucket(bucketName)
					.key(s3Key)
					.build(),
					
				RequestBody.fromBytes(params.trackBytes)
			);
			
			track = mongoTrackRepo.save(
				new MongoDBTrack.Builder()
					.setS3Key(s3Key)
					.setUserId(params.artistName)
					.setName(params.trackName)
					.setDuration(params.duration)
					.build()
			);
				
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return track;
	}
	
	public Track deleteTrack(String id) {
		MongoDBTrack track = null;
		boolean trackDeletedFromMongo = false;
		
		try {
			track = mongoTrackRepo.byId(id);
			mongoTrackRepo.delete(track);
			trackDeletedFromMongo = true;
			
			s3.deleteObject(
				DeleteObjectRequest.builder()
					.key(track.getS3Key())
					.bucket(bucketName)
					.build()
			);
			
		} catch (Exception exception) {
			exception.printStackTrace();
			
		} finally {
			if (!trackDeletedFromMongo) {
				track = null;
			}
		}
		
		return track;
	}
}
