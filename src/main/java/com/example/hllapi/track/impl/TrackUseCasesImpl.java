package com.example.hllapi.track.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;

public class TrackUseCasesImpl implements TrackUseCases {
	
	private TrackRepo trackRepo;
	Set<String> allowedFileTypes;
	
	public TrackUseCasesImpl(
		TrackRepo trackRepo,
		Set<String> allowedFileTypes
	) {
		this.trackRepo = trackRepo;
		this.allowedFileTypes = allowedFileTypes;
	}
	
	public TrackUseCases.TracksRetrieval getTracksByArtist(String artistId) {
		List<Track> tracks = trackRepo.getTracksByArtist(artistId); 
		
		TrackUseCases.TracksRetrieval retrieval = new TrackUseCases.TracksRetrieval();
		if (tracks != null) {
			retrieval.outcome = TrackUseCases.FetchTracksOutcomes.SUCESSFUL;
			retrieval.tracks = tracks;
		} else {
			retrieval.outcome = TrackUseCases.FetchTracksOutcomes.FAILURE;
		}
		
		return retrieval;
	}
	
	public TrackUseCases.TrackStreamInit streamTrack(String trackId) {
		InputStream trackStream = trackRepo.getTrackStream(trackId);
		
		TrackUseCases.TrackStreamInit streamInit = new TrackUseCases.TrackStreamInit();
		if (trackStream != null) {
			streamInit.outcome = TrackUseCases.StreamTrackOutcomes.SUCESSFUL;
			streamInit.stream = trackStream;
		} else {
			streamInit.outcome = TrackUseCases.StreamTrackOutcomes.FAILURE;
		}
		
		return streamInit;
	}
	
	public TrackUseCases.TrackCreation createTrack(CreateTrackParams params) {
		boolean fileFormatIsAcceptable = allowedFileTypes.contains(params.fileType.toLowerCase());
		Track createdTrack = null;

		if (fileFormatIsAcceptable) {
			createdTrack = trackRepo.saveTrack(params);			
		}
		
		TrackUseCases.TrackCreation trackCreation = new TrackUseCases.TrackCreation();
		if (createdTrack != null) {
			trackCreation.outcome = TrackUseCases.CreateTrackOutcomes.SUCESSFUL;
			trackCreation.track = createdTrack;
		} else if (!fileFormatIsAcceptable) {
			trackCreation.outcome = TrackUseCases.CreateTrackOutcomes.FAILURE_FROM_IMPROPER_FILE_FORMAT;
		} else {
			trackCreation.outcome = TrackUseCases.CreateTrackOutcomes.FAILURE;
		}
		
		return trackCreation;
	};
	
	public TrackUseCases.TrackDeletion deleteTrack(DeleteTrackParams params) {
		Track deletedTrack = null;
		Track targetTrack = trackRepo.getTrackById(params.trackId);
		boolean operationAuthorized = targetTrack != null && targetTrack.getUserId().equalsIgnoreCase(params.requesterId);
				
		if (operationAuthorized) {
			deletedTrack = trackRepo.deleteTrack(targetTrack.getId());			
		}
		
		TrackUseCases.TrackDeletion trackDeletion = new TrackUseCases.TrackDeletion();
		if (operationAuthorized && deletedTrack != null) {
			trackDeletion.outcome = TrackUseCases.DeleteTrackOutcomes.SUCESSFUL;
			trackDeletion.track = targetTrack;
			
		} else if (!operationAuthorized) {
			trackDeletion.outcome = TrackUseCases.DeleteTrackOutcomes.UNAUTHORIZED;
			
		} else {
			trackDeletion.outcome = TrackUseCases.DeleteTrackOutcomes.FAILURE;
		}
		
		
		return trackDeletion;
	}
	
}
