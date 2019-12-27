package com.example.hllapi.track.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemResult;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;

import software.amazon.awssdk.services.s3.S3Client;

public class AWSTrackRepo implements TrackRepo {
	
	private S3Client s3;
	private AmazonDynamoDB dynamoDB;
	private String trackTableName;
	private String userIdIndexName;
	
	public AWSTrackRepo(
			S3Client s3,
			AmazonDynamoDB dynamoDB,
			String trackTableName,
			String userIdIndexName
	) {
		this.s3 = s3;
		this.dynamoDB = dynamoDB;
		this.trackTableName = trackTableName;
		this.userIdIndexName = userIdIndexName;
	}

	public Track getTrackById(String id) {
		Track track = null;
		
		try {
			GetItemRequest getItemRequest = new GetItemRequest()
				.withTableName("hey-look-listen-track")
				.withKey(new HashMap<String, AttributeValue>(){{
					put("id", new AttributeValue(id));
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
        		.withKeyConditionExpression("userId = :artistId")
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
		Track track;
		track = Track.Builder()
				.setId(resultMap.get("id").getS())
				.setName(resultMap.get("name").getS())
				.setS3Key(resultMap.get("s3Key").getS())
				.setUserId(resultMap.get("userId").getS())
				.setDuration(Double.parseDouble(resultMap.get("duration").getN()))
				.build();
		return track;
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
