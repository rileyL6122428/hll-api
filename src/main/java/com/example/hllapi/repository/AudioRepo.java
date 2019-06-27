package com.example.hllapi.repository;

import com.example.hllapi.model.Track;

public interface AudioRepo {

	public Track getTrackById(String trackId);

}
