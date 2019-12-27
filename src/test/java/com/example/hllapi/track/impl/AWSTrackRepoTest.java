package com.example.hllapi.track.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.example.hllapi.track.Track;

import software.amazon.awssdk.services.s3.S3Client;

class AWSTrackRepoTest {
	
	AWSTrackRepo trackRepo;
	
	S3Client s3;
	AmazonDynamoDB dynamoDB;
	String trackTableName;
	String userIdIndexName;

	@BeforeEach
	void setup() {
		s3 = mock(S3Client.class);
		dynamoDB = mock(AmazonDynamoDB.class);
		trackTableName = "EXAMPLE_TRACK_TABLE_NAME";
		userIdIndexName = "EXAMPLE_USER_ID_INDEX_NAME";
		
		trackRepo = new AWSTrackRepo(
			s3,
			dynamoDB,
			trackTableName,
			userIdIndexName
		);
	}
	
	@Nested
	public class GetTracksByArtistMethod {
		
		String artistName;
		
		@BeforeEach
		void setup() {
			artistName = "EXAMPLE_ARTIST_NAME";
		}
		
		@Test
		void returnsNullTrackListWhenDynamoDBThrows() {
			when(dynamoDB.query(any(QueryRequest.class))).thenThrow(new RuntimeException("EXAMPLE_RUNTIME_EXCEPTION"));
			List<Track> tracks = trackRepo.getTracksByArtist(artistName);
			assertNull(tracks);
		}
		
		@Test
		void queriesDynamoDBWithRequiredParams() {
			QueryResult queryResult = mock(QueryResult.class);
			when(queryResult.getItems()).thenReturn(new ArrayList<Map<String, AttributeValue>>());
			
			ArgumentCaptor<QueryRequest> queryCaptor = ArgumentCaptor.forClass(QueryRequest.class);
			when(dynamoDB.query(queryCaptor.capture())).thenReturn(queryResult);
			
			trackRepo.getTracksByArtist(artistName);
			
			QueryRequest queryRequest = queryCaptor.getValue();
			assertEquals(trackTableName, queryRequest.getTableName());
			assertEquals(userIdIndexName, queryRequest.getIndexName());
			assertEquals("userId = :artistId", queryRequest.getKeyConditionExpression());
			
			Map<String, AttributeValue> expressionAttributeValues = queryRequest.getExpressionAttributeValues();
			assertEquals(1, expressionAttributeValues.size());
			assertEquals(artistName, expressionAttributeValues.get(":artistId").getS());
		}
		
		@Test
		void returnsMappedTracks() {
			QueryResult queryResult = mock(QueryResult.class);
			when(queryResult.getItems()).thenReturn(new ArrayList<Map<String, AttributeValue>>(){{
				add(new HashMap<String, AttributeValue>(){{
					put("id", new AttributeValue("EXAMPLE_ID_1"));
					put("name", new AttributeValue("EXAMPLE_NAME_1"));
					put("s3Key", new AttributeValue("EXAMPLE_S3_KEY_1"));
					put("userId", new AttributeValue("EXAMPLE_USER_ID_1"));
					put("duration", new AttributeValue(){{ setN("1"); }});
				}});
				
				add(new HashMap<String, AttributeValue>(){{
					put("id", new AttributeValue("EXAMPLE_ID_2"));
					put("name", new AttributeValue("EXAMPLE_NAME_2"));
					put("s3Key", new AttributeValue("EXAMPLE_S3_KEY_2"));
					put("userId", new AttributeValue("EXAMPLE_USER_ID_2"));
					put("duration", new AttributeValue(){{ setN("2"); }});
				}});
			}});
			
			when(dynamoDB.query(any(QueryRequest.class))).thenReturn(queryResult);
			
			List<Track> tracks = trackRepo.getTracksByArtist(artistName);
			
			assertEquals(2, tracks.size());
			
			assertEquals("EXAMPLE_ID_1", tracks.get(0).getId());
			assertEquals("EXAMPLE_NAME_1", tracks.get(0).getName());
			assertEquals("EXAMPLE_S3_KEY_1", tracks.get(0).getS3Key());
			assertEquals("EXAMPLE_USER_ID_1", tracks.get(0).getUserId());
			assertEquals(1d, tracks.get(0).getDuration());
			
			assertEquals("EXAMPLE_ID_2", tracks.get(1).getId());
			assertEquals("EXAMPLE_NAME_2", tracks.get(1).getName());
			assertEquals("EXAMPLE_S3_KEY_2", tracks.get(1).getS3Key());
			assertEquals("EXAMPLE_USER_ID_2", tracks.get(1).getUserId());
			assertEquals(2d, tracks.get(1).getDuration());
		}	
	}

}
