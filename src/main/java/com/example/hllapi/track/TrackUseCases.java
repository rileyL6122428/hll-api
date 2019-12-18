package com.example.hllapi.track;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamSource;

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
	
	public TrackStreamInit streamTrack(String trackId);
	static public enum StreamTrackOutcomes {
		SUCESSFUL,
		FAILURE
	}
	static class TrackStreamInit {
		public StreamTrackOutcomes outcome;
		public InputStream stream;
	}
	
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
