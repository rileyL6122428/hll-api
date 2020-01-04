# Hey Look Listen API

An API for uploading and streaming Mpeg/Mp3 files.

## Schemas

* Track

```JSON
{
  "id": "string",
  "s3Key": "string",	
  "userId": "string",
  "name": "string",
  "duration": "number"
}
```

## Operations

* Get Track Metadata By Artist(Uploader)

- Path: /api/public/tracks?artist-id={artist-id}
- Returns: A list of Tracks.

* Stream Track

- Path: /api/public/track/{trackId}/stream
- Returns: An audio/mpeg.

* Upload Track

- Path: /api/private/track
- Headers: Authorization -> An Oauth 2.0 bearer token.
- Form Parameter: audio-file -> An audio/mpeg file.
- Returns: The uploaded track.

* Delete Track

- Path: /api/private/track/{trackId}
- Headers: Authorization -> An Oauth 2.0 bearer token.
- Returns: The deleted track.
