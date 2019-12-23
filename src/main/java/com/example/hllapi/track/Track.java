package com.example.hllapi.track;

public class Track {
	
	static public Track.Builder Builder() {
		return new Track.Builder();
	}

	protected String id;
	
	protected String s3Key;
	
	protected String userId;
	
	protected String name;
	
	protected double duration;
	
	public Track(
		String id,
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

	public String getS3Key() {
		return s3Key;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getName() {
		return name;
	}
	
	public double getDuration() {
		return duration;
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
