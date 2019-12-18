package com.example.hllapi.track;

import java.io.InputStream;
import java.util.List;

public interface TrackUseCases {
	
	public List<Track> getTracksByArtist(String artistId);
	public Track getTrackById(String trackId);
	public InputStream streamTrack(String trackId);
	public Track createTrack(CreateTrackParams track);
	public Track deleteTrack(String trackId);
	
	static public interface CreateTrackParams {
		public String getArtistName();
		public String getTrackName();
		public byte[] getTrackBytes();
		public String getFileType();
		public double getDuration();
	}

}
