package com.example.hllapi.repository;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

@Component
public class AudioTrackRepo {
	
	@Autowired
	private GridFsTemplate gridFs;
	
	public String store(InputStream inputStream, String username, String filename) {
		DBObject metaData = new BasicDBObject();
		metaData.put("user", username);
		return gridFs.store(inputStream, filename, "audio/mpeg").toString();
	}
	
	public GridFsResource getExample() {
//		GridFSFile gridFsFile = gridFs.findOne(new Query(Criteria.where("_id").is(id)));
//		GridFSFile gridFsFile = gridFs.findOne(new Query(Criteria.where("files_id").exists(true)));
//		return new GridFsResource(gridFsFile, getGridFs().openDownloadStream(gridFsFile.getObjectId()));
//		GridFsResource resource = gridFs.getResource(gridFsFile.getFilename());
//		return resource;
//		return gridFs.getResources("hey-look")[0];
		GridFSFile file = gridFs.findOne(new Query(Criteria.where("_id").is("5ce37827a78ab9068c6cb67d")));
		return gridFs.getResource(file);
	}
	
}
