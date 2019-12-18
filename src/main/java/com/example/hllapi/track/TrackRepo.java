package com.example.hllapi.track;

import java.io.InputStream;
import java.util.List;

public interface TrackRepo {
	
	public Track getTrackById(String id);
	
	public List<Track> getTracksByArtist(String artistId);
	
	public InputStream getTrackStream(String id);
	
	public Track saveTrack(TrackUseCases.CreateTrackParams params);
	
	public Track deleteTrack(String id);

}
