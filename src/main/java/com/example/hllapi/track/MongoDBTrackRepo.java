package com.example.hllapi.track;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDBTrackRepo extends MongoRepository<Track, String> { 
	
	default public Track byId(String trackId) {
		Optional<Track> track = this.findById(trackId);
		return track.isPresent() ? track.get() : null;
	}
	
	public Iterable<Track> findAllByUserId(String userId);

}
