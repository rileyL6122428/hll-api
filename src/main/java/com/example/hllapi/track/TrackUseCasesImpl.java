package com.example.hllapi.track;

import java.io.InputStream;
import java.util.List;

public class TrackUseCasesImpl implements TrackUseCases {
	
	private TrackRepo trackRepo;
	
	public TrackUseCasesImpl(TrackRepo trackRepo) {
		this.trackRepo = trackRepo;
	}
	
	public List<Track> getTracksByArtist(String artistId) {
		return trackRepo.getTracksByArtist(artistId);
	}
	
	public Track getTrackById(String trackId) {
		return trackRepo.getTrackById(trackId);
	}
	
	public InputStream streamTrack(String trackId) {
		return trackRepo.getTrackStream(trackId);
	}
	
	public Track createTrack(CreateTrackParams track) {
		return trackRepo.saveTrack(track);
	};
	
	public Track deleteTrack(String trackId) {
		return trackRepo.deleteTrack(trackId);
	}
	
}
