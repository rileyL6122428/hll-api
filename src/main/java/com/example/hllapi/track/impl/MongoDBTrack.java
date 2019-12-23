package com.example.hllapi.track.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.hllapi.track.Track;

@Document(collection="track")
public class MongoDBTrack extends Track {
	
	public MongoDBTrack(
		@Value("#root._id") String id,
		String s3Key,		
		String userId,
		String name,
		double duration
	) {
		super(
			id,
			s3Key,
			userId,
			name,
			duration
		);
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	public static class Builder {
		
		private String id;
		private String s3Key;
		private String userId;
		private String name;
		private double duration;
		
		public Builder setId(String id) {
			this.id = id; return this;
		}
		
		public Builder setS3Key(String s3Key) {
			this.s3Key = s3Key; return this;
		}
		
		public Builder setUserId(String userId) {
			this.userId = userId; return this;
		}
		
		public Builder setName(String name) {
			this.name = name; return this;
		}
		
		public Builder setDuration(double duration) {
			this.duration = duration; return this;
		}
		
		public MongoDBTrack build() {
			return new MongoDBTrack(
				id,
				s3Key,
				userId,
				name,
				duration
			);
		}
	}
	
}
