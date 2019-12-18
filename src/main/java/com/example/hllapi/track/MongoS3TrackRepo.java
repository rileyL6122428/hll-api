package com.example.hllapi.track;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class MongoS3TrackRepo implements TrackRepo {
	
	private S3Client s3;
	private String bucketName;
	private Set<String> allowedFileTypes;
	private MongoDBTrackRepo mongoTrackRepo;
	
	public MongoS3TrackRepo(
		S3Client s3,
		String bucketName,
		Set<String> allowedFileTypes,
		MongoDBTrackRepo mongoTrackRepo
	) {
		this.s3 = s3;
		this.bucketName = bucketName;
		this.allowedFileTypes = allowedFileTypes;
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
			Iterable<Track> tracksIterable = this.mongoTrackRepo.findAllByUserId(artistId);
			Iterator<Track> tracksIterator = tracksIterable.iterator();
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
			if (allowedFileTypes.contains(params.getFileType().toLowerCase())) {
				String s3Key = "audio/" + params.getTrackName() + ".mp3";
				
				s3.putObject(
					PutObjectRequest.builder()
						.bucket(bucketName)
						.key(s3Key)
						.build(),
						
					RequestBody.fromBytes(params.getTrackBytes())
				);
				
				track = mongoTrackRepo.save(
					Track.Builder()
						.setS3Key(s3Key)
						.setUserId(params.getArtistName())
						.setName(params.getTrackName())
						.setDuration(params.getDuration())
						.build()
				);
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return track;
	}
	
	public Track deleteTrack(String id) {
		Track track = null;
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
