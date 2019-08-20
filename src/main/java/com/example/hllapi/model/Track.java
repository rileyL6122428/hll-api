package com.example.hllapi.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="track")
public class Track {
	
	static public Track.Builder Builder() {
		return new Track.Builder();
	}

	@Id
	private String id;
	
	private String s3Key;
	
	private String userId;
	
	private String name;
	
	private double duration;
	
	public Track(
		@Value("#root._id") String id,
		String s3Key,		
		String userId,
		String name,
		double duration
	) {
		this.id = id;
		this.s3Key = s3Key;
		this.userId = userId;
		this.name = name;
		this.duration = duration;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getDuration() {
		return duration;
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
		
		public Track build() {
			return new Track(
				id,
				s3Key,
				userId,
				name,
				duration
			);
		}
		
	}
}
