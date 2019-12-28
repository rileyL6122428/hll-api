package com.example.hllapi.track.impl;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface MongoDBTrackRepo extends MongoRepository<MongoDBTrack, String> { 
	
	default public MongoDBTrack byId(String trackId) {
		Optional<MongoDBTrack> track = this.findById(trackId);
		return track.isPresent() ? track.get() : null;
	}
	
	public Iterable<MongoDBTrack> findAllByUserId(String userId);

}
