package com.example.hllapi.track.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackUseCases;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class AWSTrackRepoTest {
	
	AWSTrackRepo trackRepo;
	
	S3Client s3;
	AmazonDynamoDB dynamoDB;
	String trackTableName;
	String userIdIndexName;
	String bucketName;

	@BeforeEach
	void setup() {
		s3 = mock(S3Client.class);
		dynamoDB = mock(AmazonDynamoDB.class);
		trackTableName = "EXAMPLE_TRACK_TABLE_NAME";
		userIdIndexName = "EXAMPLE_USER_ID_INDEX_NAME";
		bucketName = "EXAMPLE_BUCKET_NAME";
		
		trackRepo = new AWSTrackRepo(
			s3,
			dynamoDB,
			trackTableName,
			userIdIndexName,
			bucketName
		);
	}
	
	@Nested
	public class GetTrackByIdMethod {
		
		String trackId;
		Map<String, AttributeValue> resultMap;
		
		@BeforeEach
		void setup() {
			trackId = "EXAMPLE_TRACK_ID";
			
			resultMap = new HashMap<String, AttributeValue>(){{
				put(AWSTrackRepo.TableSchema.KeyNames.ID, new AttributeValue("EXAMPLE_ID"));
				put(AWSTrackRepo.TableSchema.KeyNames.NAME, new AttributeValue("EXAMPLE_NAME"));
				put(AWSTrackRepo.TableSchema.KeyNames.S3_KEY, new AttributeValue("EXAMPLE_S3_KEY"));
				put(AWSTrackRepo.TableSchema.KeyNames.USER_ID, new AttributeValue("EXAMPLE_USER_ID"));
				put(AWSTrackRepo.TableSchema.KeyNames.DURATION, new AttributeValue(){{ setN("123"); }});
			}};
		}
		
		@Test
		void returnsNullTrackWhenDynamoDBThrows() {
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenThrow(new RuntimeException("EXAMPLE_RUNTIME_EXCEPTION"));
			Track track = trackRepo.getTrackById(trackId);
			assertNull(track);
		}
		
		@Test
		void fetchesTrackViaDynamoClientWithRequiredParams() {
			ArgumentCaptor<GetItemRequest> requestCaptor = ArgumentCaptor.forClass(GetItemRequest.class);
			when(dynamoDB.getItem(requestCaptor.capture())).thenReturn(new GetItemResult().withItem(resultMap));
			
			trackRepo.getTrackById(trackId);
			
			GetItemRequest getItemRequest = requestCaptor.getValue();
			assertEquals(trackTableName, getItemRequest.getTableName());
			assertEquals(1, getItemRequest.getKey().size());
			assertEquals(trackId, getItemRequest.getKey().get("id").getS());
		}
		
		@Test
		void returnsTrackMappedFromResultMap() {
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(new GetItemResult().withItem(resultMap));
			
			Track track = trackRepo.getTrackById(trackId);
			
			assertEquals("EXAMPLE_ID", track.getId());
			assertEquals("EXAMPLE_NAME", track.getName());
			assertEquals("EXAMPLE_S3_KEY", track.getS3Key());
			assertEquals("EXAMPLE_USER_ID", track.getUserId());
			assertEquals(123d, track.getDuration());
		}
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
					put(AWSTrackRepo.TableSchema.KeyNames.ID, new AttributeValue("EXAMPLE_ID_1"));
					put(AWSTrackRepo.TableSchema.KeyNames.NAME, new AttributeValue("EXAMPLE_NAME_1"));
					put(AWSTrackRepo.TableSchema.KeyNames.S3_KEY, new AttributeValue("EXAMPLE_S3_KEY_1"));
					put(AWSTrackRepo.TableSchema.KeyNames.USER_ID, new AttributeValue("EXAMPLE_USER_ID_1"));
					put(AWSTrackRepo.TableSchema.KeyNames.DURATION, new AttributeValue(){{ setN("1"); }});
				}});
				
				add(new HashMap<String, AttributeValue>(){{
					put(AWSTrackRepo.TableSchema.KeyNames.ID, new AttributeValue("EXAMPLE_ID_2"));
					put(AWSTrackRepo.TableSchema.KeyNames.NAME, new AttributeValue("EXAMPLE_NAME_2"));
					put(AWSTrackRepo.TableSchema.KeyNames.S3_KEY, new AttributeValue("EXAMPLE_S3_KEY_2"));
					put(AWSTrackRepo.TableSchema.KeyNames.USER_ID, new AttributeValue("EXAMPLE_USER_ID_2"));
					put(AWSTrackRepo.TableSchema.KeyNames.DURATION, new AttributeValue(){{ setN("2"); }});
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

	@Nested
	public class GetTrackStreamMethod {
		
		Map<String, AttributeValue> resultMap;
		
		@BeforeEach
		void setup() {
			resultMap = new HashMap<String, AttributeValue>(){{
				put(AWSTrackRepo.TableSchema.KeyNames.ID, new AttributeValue("EXAMPLE_ID"));
				put(AWSTrackRepo.TableSchema.KeyNames.NAME, new AttributeValue("EXAMPLE_NAME"));
				put(AWSTrackRepo.TableSchema.KeyNames.S3_KEY, new AttributeValue("EXAMPLE_S3_KEY"));
				put(AWSTrackRepo.TableSchema.KeyNames.USER_ID, new AttributeValue("EXAMPLE_USER_ID"));
				put(AWSTrackRepo.TableSchema.KeyNames.DURATION, new AttributeValue(){{ setN("123"); }});
			}};
		}
		
		@Test
		void fetchesTrackFileFromS3() {
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(new GetItemResult().withItem(resultMap));
			
			ArgumentCaptor<GetObjectRequest> s3Captor = ArgumentCaptor.forClass(GetObjectRequest.class); 
			when(s3.getObject(s3Captor.capture())).thenReturn(null);
			
			trackRepo.getTrackStream("EXAMPLE_ID");
			
			GetObjectRequest getObjReq = s3Captor.getValue();
			assertEquals("EXAMPLE_S3_KEY", getObjReq.key());
			assertEquals(bucketName, getObjReq.bucket());
		}
		
		@Test
		void returnsTrackStreamFromS3Client() {
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(new GetItemResult().withItem(resultMap));
			
			ResponseInputStream<GetObjectResponse> s3Stream = mock(ResponseInputStream.class);
			when(s3.getObject(any(GetObjectRequest.class))).thenReturn(s3Stream);
			
			InputStream returnedStream = trackRepo.getTrackStream("EXAMPLE_TRACK_ID");
			
			assertEquals(s3Stream, returnedStream);
		}
		
		@Test
		void returnsNullStreamWhenS3ClientThrows() {
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(new GetItemResult().withItem(resultMap));
			when(s3.getObject(any(GetObjectRequest.class))).thenThrow(new RuntimeException());
			
			InputStream trackStream = trackRepo.getTrackStream("EXAMPLE_TRACK_ID");
			
			assertNull(trackStream);
		}
		
		@Test
		void returnsNullStreamWhenDynamoDBThrows() {
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenThrow(new RuntimeException());
			InputStream trackStream = trackRepo.getTrackStream("EXAMPLE_TRACK_ID");
			assertNull(trackStream);
		}
	}

	@Nested
	public class SaveTrackMethod {
		
		TrackUseCases.CreateTrackParams saveParams;
		
		@BeforeEach
		void setup() {
			saveParams = new TrackUseCases.CreateTrackParams() {{
				trackName = "EXAMPLE_TRACK_NAME";
				fileType = "EXAMPLE_FILE_TYPE";
				artistName = "EXAMPLE_ARTIST_NAME";
				trackBytes = new byte[] {};
				duration = 123d;
			}};			
		}
		
		@Test
		void returnsNullTrackWhenS3Throws() {
			when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(new RuntimeException());
			Track track = trackRepo.saveTrack(saveParams);
			assertNull(track);
		}
		
		@Test
		void putsTrackContentsIntoS3() {
			ArgumentCaptor<PutObjectRequest> putObjCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
			ArgumentCaptor<RequestBody> reqBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
			when(s3.putObject(putObjCaptor.capture(), reqBodyCaptor.capture())).thenReturn(null);
			
			trackRepo.saveTrack(saveParams);
			
			PutObjectRequest putObjRequest = putObjCaptor.getValue();
			assertEquals("audio/" + saveParams.trackName + ".mp3", putObjRequest.key());
			assertEquals(bucketName, putObjRequest.bucket());
			
			RequestBody requestBody = reqBodyCaptor.getValue();
			assertEquals(saveParams.trackBytes.length, requestBody.contentLength());
		}
		
		@Test
		void returnsNullTrackIfDynamoDBThrows() {
			when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
			when(dynamoDB.putItem(any(PutItemRequest.class))).thenThrow(new RuntimeException());
			
			Track track = trackRepo.saveTrack(saveParams);
			
			assertNull(track);
		}
		
		@Test
		void uploadsTrackMetadataToDynamoDB() {
			when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
			
			ArgumentCaptor<PutItemRequest> putItemCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
			when(dynamoDB.putItem(putItemCaptor.capture())).thenReturn(null);
			
			trackRepo.saveTrack(saveParams);
			
			PutItemRequest putItemRequest = putItemCaptor.getValue();
			assertEquals(trackTableName, putItemRequest.getTableName());
			
			assertNotNull(putItemRequest.getItem().get("id").getS());
			assertEquals(saveParams.trackName, putItemRequest.getItem().get("name").getS());
			assertEquals("audio/" + saveParams.trackName + ".mp3", putItemRequest.getItem().get("s3Key").getS());
			assertEquals(saveParams.artistName, putItemRequest.getItem().get("userId").getS());
			assertEquals("123.0", putItemRequest.getItem().get("duration").getN());
		}
		
		@Test
		void returnsTrackDerivedFromSaveParams() {
			when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
			when(dynamoDB.putItem(any(PutItemRequest.class))).thenReturn(null);
			
			Track track = trackRepo.saveTrack(saveParams);
			
			assertNotNull(track.getId());
			assertEquals(saveParams.trackName, track.getName());
			assertEquals("audio/" + saveParams.trackName + ".mp3", track.getS3Key());
			assertEquals(saveParams.artistName, track.getUserId());
			assertEquals(saveParams.duration, track.getDuration());
		}
	}

	@Nested
	public class DeleteTrackMethod {
		
		String trackId;
		Map<String, AttributeValue> resultMap;
		
		@BeforeEach
		void setup() {
			trackId = "EXAMPLE_TRACK_ID";
			
			resultMap = new HashMap<String, AttributeValue>(){{
				put(AWSTrackRepo.TableSchema.KeyNames.ID, new AttributeValue("EXAMPLE_ID"));
				put(AWSTrackRepo.TableSchema.KeyNames.NAME, new AttributeValue("EXAMPLE_NAME"));
				put(AWSTrackRepo.TableSchema.KeyNames.S3_KEY, new AttributeValue("EXAMPLE_S3_KEY"));
				put(AWSTrackRepo.TableSchema.KeyNames.USER_ID, new AttributeValue("EXAMPLE_USER_ID"));
				put(AWSTrackRepo.TableSchema.KeyNames.DURATION, new AttributeValue(){{ setN("123"); }});
			}};
			
			when(dynamoDB.getItem(any(GetItemRequest.class))).thenReturn(new GetItemResult().withItem(resultMap));
		}
		
		@Test
		void returnsNullTrackWhenDynamoDBThrows() {
			when(dynamoDB.deleteItem(any(DeleteItemRequest.class))).thenThrow(new RuntimeException());
			Track track = trackRepo.deleteTrack(trackId);
			assertNull(track);
		}
		
		@Test
		void delegatesTrackDeletionToDynamoDBClient() {
			ArgumentCaptor<DeleteItemRequest> deleteReqCaptor = ArgumentCaptor.forClass(DeleteItemRequest.class);
			when(dynamoDB.deleteItem(deleteReqCaptor.capture())).thenReturn(null);
			
			trackRepo.deleteTrack(trackId);
			
			DeleteItemRequest deleteItemRequest = deleteReqCaptor.getValue();
			assertEquals(trackTableName, deleteItemRequest.getTableName());
			assertEquals(1, deleteItemRequest.getKey().size());
			assertEquals(trackId, deleteItemRequest.getKey().get(AWSTrackRepo.TableSchema.KeyNames.ID).getS());
		}
		
		@Test
		void returnsTrackWhenDynamoDBDeleteOpSucceedsButS3DeleteOpFails() {
			when(dynamoDB.deleteItem(any(DeleteItemRequest.class))).thenReturn(null);
			when(s3.deleteObject(any(DeleteObjectRequest.class))).thenThrow(new RuntimeException());
			
			Track track = trackRepo.deleteTrack(trackId);
			
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.ID).getS(), track.getId());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.NAME).getS(), track.getName());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.S3_KEY).getS(), track.getS3Key());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.USER_ID).getS(), track.getUserId());
			assertEquals(Double.parseDouble(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.DURATION).getN()), track.getDuration());
		}
		
		@Test
		void returnsTrackWhenDynamoDBDeleteOpAndS3DeleteOpBothSucceed() {
			when(dynamoDB.deleteItem(any(DeleteItemRequest.class))).thenReturn(null);
			when(s3.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);
			
			Track track = trackRepo.deleteTrack(trackId);
			
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.ID).getS(), track.getId());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.NAME).getS(), track.getName());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.S3_KEY).getS(), track.getS3Key());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.USER_ID).getS(), track.getUserId());
			assertEquals(Double.parseDouble(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.DURATION).getN()), track.getDuration());
		}
		

		@Test
		void deletesTrackFromS3() {
			when(dynamoDB.deleteItem(any(DeleteItemRequest.class))).thenReturn(null);
			
			ArgumentCaptor<DeleteObjectRequest> deleteObjCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
			when(s3.deleteObject(deleteObjCaptor.capture())).thenReturn(null);
			
			Track track = trackRepo.deleteTrack(trackId);
			
			DeleteObjectRequest deleteObjRequest = deleteObjCaptor.getValue();
			assertEquals(bucketName, deleteObjRequest.bucket());
			assertEquals(resultMap.get(AWSTrackRepo.TableSchema.KeyNames.S3_KEY).getS(), deleteObjRequest.key());
		}
	}
}
