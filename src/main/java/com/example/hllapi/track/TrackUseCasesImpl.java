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
	
	public Track createTrack(CreateTrackParams params) {
		return trackRepo.saveTrack(params);
	};
	
	public Track deleteTrack(DeleteTrackParams params) {
		Track track = trackRepo.getTrackById(params.getTrackId());
		
		if (track.getUserId().equalsIgnoreCase(params.getRequesterId())) {
			track = trackRepo.deleteTrack(track.getId());
		} else {
			track = null;
		}
		return track;
	}
	
}
