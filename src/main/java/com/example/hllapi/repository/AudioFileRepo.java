package com.example.hllapi.repository;

import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

@Component
public class AudioFileRepo {
	
	@Autowired
	private GridFsTemplate gridFs;
	
	public String store(InputStream inputStream) {
		return gridFs.store(inputStream, "TEST_NAME", "audio/mpeg").toString();
	}
	
	public GridFsResource getExample() throws Exception {
//		GridFSFile gridFsFile = gridFs.findOne(new Query(Criteria.where("_id").is(id)));
//		GridFSFile gridFsFile = gridFs.findOne(new Query(Criteria.where("files_id").exists(true)));
//		return new GridFsResource(gridFsFile, getGridFs().openDownloadStream(gridFsFile.getObjectId()));
//		GridFsResource resource = gridFs.getResource(gridFsFile.getFilename());
//		return resource;
//		return gridFs.getResources("hey-look")[0];
		
		String heyLookMaId = "5ce37827a78ab9068c6cb67d";
		String godzillaRoarId = "5d0ec39100d01106c3d22995";
		GridFSFile file = gridFs.findOne(new Query(Criteria.where("_id").is(godzillaRoarId)));
		
		GridFsResource resource = gridFs.getResource(file);
		printFileLength(resource);
		return resource;
	}
	
	private void printFileLength(GridFsResource resource) throws Exception {
//		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
//		AudioFormat format = audioInputStream.getFormat();
//		long frames = audioInputStream.getFrameLength();
//		double durationInSeconds = (frames+0.0) / format.getFrameRate();  
		
//		FFprobe ffprobe = new FFprobe("/usr/local/bin/ffprobe");
//		FFmpegProbeResult probeResult = ffprobe.probe(resource.getFilename());
		Runtime.getRuntime().exec("echo 'hello world'");
//
//		FFmpegFormat format = probeResult.getFormat();
//		System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs", 
//			format.filename, 
//			format.format_long_name,
//			format.duration
//		);
//
//		FFmpegStream stream = probeResult.getStreams().get(0);
//		System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",
//			stream.codec_long_name,
//			stream.width,
//			stream.height
//		);
	}
	
}
