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

Path: /api/public/tracks?artist-id=ARTIST_ID

Returns: A list of Tracks.




