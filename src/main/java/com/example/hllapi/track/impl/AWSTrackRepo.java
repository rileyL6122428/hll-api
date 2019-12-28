package com.example.hllapi.track.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AWSTrackRepo implements TrackRepo {
	
	static class TableSchema {
		private TableSchema() {}
		
		static class KeyNames {
			private KeyNames() {}
			
			static final String ID = "id";
			static final String NAME = "name";
			static final String S3_KEY = "s3Key";
			static final String USER_ID = "userId";
			static final String DURATION = "duration";
		}
	}
	
	private S3Client s3;
	private AmazonDynamoDB dynamoDB;
	private String trackTableName; // SHOULD BE "hey-look-listen-track"?
	private String userIdIndexName;
	private String bucketName;
	
	public AWSTrackRepo(
			S3Client s3,
			AmazonDynamoDB dynamoDB,
			String trackTableName,
			String userIdIndexName,
			String bucketName
	) {
		this.s3 = s3;
		this.dynamoDB = dynamoDB;
		this.trackTableName = trackTableName;
		this.userIdIndexName = userIdIndexName;
		this.bucketName = bucketName;
	}

	public Track getTrackById(String id) {
		Track track = null;
		
		try {
			GetItemRequest getItemRequest = new GetItemRequest()
				.withTableName(trackTableName)
				.withKey(new HashMap<String, AttributeValue>(){{
					put(TableSchema.KeyNames.ID, new AttributeValue(id));
				}});
			
			GetItemResult queryResult = dynamoDB.getItem(getItemRequest);
			
			Map<String, AttributeValue> resultMap = queryResult.getItem();
			
			track = trackFromResultMap(resultMap);
			
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		
		return track;
	}

	
	public List<Track> getTracksByArtist(String artistId) {
		List<Track> tracks = null;
		
		try {
			QueryRequest queryRequest = new QueryRequest()
        		.withTableName(trackTableName)
        		.withIndexName(userIdIndexName)
        		.withKeyConditionExpression(TableSchema.KeyNames.USER_ID + " = :artistId")
        		.withExpressionAttributeValues(new HashMap<String, AttributeValue>(){{
        			put(":artistId", new AttributeValue(artistId));
        		}});
			
			QueryResult queryResult = dynamoDB.query(queryRequest);
			
			List<Map<String, AttributeValue>> resultMaps = queryResult.getItems();
			
			tracks = resultMaps.stream()
				.map(this::trackFromResultMap)
				.collect(Collectors.toList());
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return tracks;
	}
	
	private Track trackFromResultMap(Map<String, AttributeValue> resultMap) {
		return Track.Builder()
				.setId(resultMap.get(TableSchema.KeyNames.ID).getS())
				.setName(resultMap.get(TableSchema.KeyNames.NAME).getS())
				.setS3Key(resultMap.get(TableSchema.KeyNames.S3_KEY).getS())
				.setUserId(resultMap.get(TableSchema.KeyNames.USER_ID).getS())
				.setDuration(Double.parseDouble(resultMap.get(TableSchema.KeyNames.DURATION).getN()))
				.build();
	}
	
	public InputStream getTrackStream(String id) {
		InputStream trackInputStream = null;
		
		try {
			Track track = getTrackById(id);
			
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
			
			PutItemRequest putItemRequest = new PutItemRequest()
				.withTableName(trackTableName)
				.withItem(new HashMap<String, AttributeValue>(){{
					put(TableSchema.KeyNames.ID, new AttributeValue(UUID.randomUUID().toString()));
					put(TableSchema.KeyNames.NAME, new AttributeValue(params.trackName));
					put(TableSchema.KeyNames.USER_ID, new AttributeValue(params.artistName));
					put(TableSchema.KeyNames.S3_KEY, new AttributeValue(s3Key));
					put(TableSchema.KeyNames.DURATION, new AttributeValue(){{ setN(Double.toString(params.duration)); }});
				}});
			
			dynamoDB.putItem(putItemRequest);
			
			track = trackFromResultMap(putItemRequest.getItem());
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return track;
	}
	
	public Track deleteTrack(String id) {
		Track track = null;
		boolean trackDeletedFromDynamoDB = false;
		
		try {
			track = getTrackById(id);
			
			DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
				.withTableName(trackTableName)
				.withKey(new HashMap<String, AttributeValue>(){{
					put(TableSchema.KeyNames.ID, new AttributeValue(id));
				}});
			
			dynamoDB.deleteItem(deleteItemRequest);
			
			trackDeletedFromDynamoDB = true;
			
			s3.deleteObject(
				DeleteObjectRequest.builder()
					.key(track.getS3Key())
					.bucket(bucketName)
					.build()
			);
			
		} catch (Exception exception) {
			exception.printStackTrace();
			
		} finally {
			if (!trackDeletedFromDynamoDB) {
				track = null;
			}
		}
		
		return track;
	}
	
}
