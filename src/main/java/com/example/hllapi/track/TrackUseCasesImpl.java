package com.example.hllapi.track;

import java.io.InputStream;
import java.util.List;

public class TrackUseCasesImpl implements TrackUseCases {
	
	private TrackRepo trackRepo;
	
	public TrackUseCasesImpl(TrackRepo trackRepo) {
		this.trackRepo = trackRepo;
	}
	
	public TrackUseCases.TracksRetrieval getTracksByArtist(String artistId) {
		TrackUseCases.TracksRetrieval retrieval = new TrackUseCases.TracksRetrieval();
		List<Track> tracks = trackRepo.getTracksByArtist(artistId); 
		
		if (tracks != null) {
			retrieval.outcome = TrackUseCases.FetchTracksOutcomes.SUCESSFUL;
			retrieval.tracks = tracks;
		} else {
			retrieval.outcome = TrackUseCases.FetchTracksOutcomes.FAILURE;
		}
		
		return retrieval;
	}
	
	public TrackUseCases.TrackStreamInit streamTrack(String trackId) {
		TrackUseCases.TrackStreamInit streamInit = new TrackUseCases.TrackStreamInit();
		InputStream trackStream = trackRepo.getTrackStream(trackId);
		
		if (trackStream != null) {
			streamInit.outcome = TrackUseCases.StreamTrackOutcomes.SUCESSFUL;
			streamInit.stream = trackStream;
		} else {
			streamInit.outcome = TrackUseCases.StreamTrackOutcomes.FAILURE;
		}
		
		return streamInit;
	}
	
	public TrackUseCases.TrackCreation createTrack(CreateTrackParams params) {
		TrackUseCases.TrackCreation trackCreation = new TrackUseCases.TrackCreation();
		Track createdTrack = trackRepo.saveTrack(params);
		
		if (createdTrack != null) {
			trackCreation.outcome = TrackUseCases.CreateTrackOutcomes.SUCESSFUL;
			trackCreation.track = createdTrack;
		} else {
			trackCreation.outcome = TrackUseCases.CreateTrackOutcomes.FAILURE;
		}
		
		return trackCreation;
	};
	
	public TrackUseCases.TrackDeletion deleteTrack(DeleteTrackParams params) {
		TrackUseCases.TrackDeletion trackDeletion = new TrackUseCases.TrackDeletion();
		
		Track track = trackRepo.getTrackById(params.trackId);
		
		boolean operationAuthorized = track.getUserId().equalsIgnoreCase(params.requesterId);
		if (operationAuthorized) {
			track = trackRepo.deleteTrack(track.getId());			
		}
		
		if (operationAuthorized && track != null) {
			trackDeletion.outcome = TrackUseCases.DeleteTrackOutcomes.SUCESSFUL;
			trackDeletion.track = track;
			
		} else if (!operationAuthorized) {
			trackDeletion.outcome = TrackUseCases.DeleteTrackOutcomes.UNAUTHORIZED;
			
		} else {
			trackDeletion.outcome = TrackUseCases.DeleteTrackOutcomes.FAILURE;
		}
		
		
		return trackDeletion;
	}
	
}
