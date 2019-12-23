package com.example.hllapi.track.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.example.hllapi.track.Track;
import com.example.hllapi.track.TrackRepo;
import com.example.hllapi.track.TrackUseCases;
import com.example.hllapi.track.TrackUseCases.CreateTrackOutcomes;
import com.example.hllapi.track.TrackUseCases.CreateTrackParams;
import com.example.hllapi.track.TrackUseCases.DeleteTrackOutcomes;
import com.example.hllapi.track.TrackUseCases.DeleteTrackParams;
import com.example.hllapi.track.TrackUseCases.FetchTracksOutcomes;
import com.example.hllapi.track.TrackUseCases.StreamTrackOutcomes;
import com.example.hllapi.track.TrackUseCases.TrackCreation;
import com.example.hllapi.track.TrackUseCases.TrackDeletion;
import com.example.hllapi.track.TrackUseCases.TrackStreamInit;
import com.example.hllapi.track.TrackUseCases.TracksRetrieval;

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
		boolean fileFormatIsAcceptable = allowedFileTypes.contains(params.fileType.toLowerCase());
		Track createdTrack = null;

		if (fileFormatIsAcceptable) {
			createdTrack = trackRepo.saveTrack(params);			
		}
		
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
