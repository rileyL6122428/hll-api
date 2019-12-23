package com.example.hllapi.track;

public interface TrackMetadataParser {
	
	public double getDuration(byte[] fileBytes) throws Exception;
	
}
