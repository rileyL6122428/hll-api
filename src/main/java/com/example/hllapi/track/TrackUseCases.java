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
		FAILURE_FROM_IMPROPER_FILE_FORMAT,
		FAILURE
	}
	static class TrackCreation {
		public CreateTrackOutcomes outcome;
		public Track track;
	}
	
	
	public TrackDeletion deleteTrack(DeleteTrackParams params);
	static public class DeleteTrackParams {
		public String trackId;
		public String requesterId;
	}
	static public enum DeleteTrackOutcomes {
		SUCESSFUL,
		UNAUTHORIZED,
		FAILURE
	}
	static class TrackDeletion {
		public DeleteTrackOutcomes outcome;
		public Track track;
	}

}
