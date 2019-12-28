package com.example.hllapi.config;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.example.hllapi.track.TrackMetadataParser;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;
import com.example.hllapi.track.impl.AWSTrackRepo;
import com.example.hllapi.track.impl.FfmpegTrackParser;
import com.example.hllapi.track.impl.MongoDBTrackRepo;
import com.example.hllapi.track.impl.MongoS3TrackRepo;
import com.example.hllapi.track.impl.TrackUseCasesImpl;

import net.bramp.ffmpeg.FFprobe;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class DependencyInjectionConfig {
	
	@Value("${fileparser.tempFilePath}")
	private String tempFilepath;
	
	@Value("${ffprobe.bin.path}")
	private String ffprobePath;
	
	@Bean
	public FFprobe provideFFProbe() throws Exception {
		return new FFprobe(ffprobePath);
	}
	
	@Bean
	public TrackMetadataParser provideTrackMetadataParser(
		FFprobe ffprobe,
		Random random
	) throws Exception {
		
		return new FfmpegTrackParser(
			tempFilepath, 
			ffprobe,
			random
		);
	}
	
	@Bean
	public Random provideRandom() {
		return new Random();
	}
	
	@Value("#{'${aws.s3.approvedFileTypes}'.split(',')}")
	private List<String> approvedFileTypesList;
	
	private Set<String> approvedFileTypesSet;
	
	public Set<String> getApprovedFileTypesSet() {
		if (approvedFileTypesSet == null) {
			this.approvedFileTypesSet = new HashSet<String>(approvedFileTypesList);
		}
		return approvedFileTypesSet;
	}
	
	@Bean
	public S3Client provideS3Client() {
		return S3Client.builder()
			.region(Region.US_EAST_2)
			.build();
	}
	
	@Bean
	public AmazonDynamoDB provideDynamoDB() {
		return AmazonDynamoDBClientBuilder.standard()
			.withRegion(Regions.US_EAST_2)
			.build();
	}
	
	@Value("${aws.s3.bucketName}")
	private String bucketName;
	
	@Value("${aws.dynamodb.trackTableName}")
	private String trackTableName;
	
	@Value("${aws.dynamodb.userIdIndexName}")
	private String userIdIndexName;
	
	@Bean(name="awsTrackRepo")
	public TrackRepo provideAWSTrackRepo(
		S3Client s3Client,
		AmazonDynamoDB dynamoDB
	) {
		return new AWSTrackRepo(
			s3Client,
			dynamoDB,
			trackTableName,
			userIdIndexName,
			bucketName
		); 
	}
	
	@Bean(name="mongoS3TrackRepo")
	public TrackRepo provideMongoAndS3TrackRepo(
//		S3Client s3Client,
//		MongoDBTrackRepo mongoDBTrackRepo
	) {
//		return new MongoS3TrackRepo(
//			s3Client,
//			bucketName,
//			mongoDBTrackRepo
//		);
		return null;
	}
	
	@Bean
	public TrackUseCases provideTrackUseCases(
		@Qualifier("awsTrackRepo") TrackRepo trackRepo
	) {
		return new TrackUseCasesImpl(
			trackRepo,
			getApprovedFileTypesSet()
		);
	}
	
}
