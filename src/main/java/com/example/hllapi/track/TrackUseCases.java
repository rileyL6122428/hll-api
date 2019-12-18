package com.example.hllapi.track;

import java.io.InputStream;
import java.util.List;

public interface TrackUseCases {
	
	public TracksRetrieval getTracksByArtist(String artistId);
	static public enum FetchTracksOutcomes {
		SUCESSFUL,
		FAILURE
	}
	static class TracksRetrieval {
		public FetchTracksOutcomes outcome;
		public List<Track> tracks;
	}
	
	public Track getTrackById(String trackId);
	
	public InputStream streamTrack(String trackId);
	
	public TrackCreation createTrack(CreateTrackParams params);	
	static public class CreateTrackParams {
		public String artistName;
		public String trackName;
		public byte[] trackBytes;
		public String fileType;
		public double duration;
	}
	static public enum CreateTrackOutcomes {
		SUCESSFUL,
		UNAUTHORIZED,
		FAILURE
	}
	static class TrackCreation {
		public CreateTrackOutcomes outcome;
		public Track track;
	}
	
	
	public Track deleteTrack(DeleteTrackParams params);
	static public interface DeleteTrackParams {
		public String getTrackId();
		public String getRequesterId();
	}

}
