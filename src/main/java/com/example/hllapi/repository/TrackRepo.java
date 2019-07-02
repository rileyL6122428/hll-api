package com.example.hllapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.hllapi.model.Track;


public interface TrackRepo extends MongoRepository<Track, String> { 
	
	default public Track byId(String trackId) {
		Optional<Track> track = this.findById(trackId);
		return track.isPresent() ? track.get() : null;
	}

}

