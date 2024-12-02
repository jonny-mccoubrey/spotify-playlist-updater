# Spotify Playlist Updater

A utility that uses the [Spotify public API](https://developer.spotify.com/documentation/web-api) to
move songs from a given playlist to a separate "archive" playlist.

The utility will use the [spotify-web-api-java-client](https://github.com/spotify-web-api-java/spotify-web-api-java)
to interact with the Spotify Web API more easily. 


### How to use the tool

#### Expected arguments
- `--client-id` - Client ID for Spotify web app
- `--client-secret` - Client Secret for Spotify web app
- `--active-playlist-id` - Active playlist ID that songs are removed from after x days
- `--archive-playlist-id` - Archive playlist ID where songs are moved to
- `--song-lifetime` - Number of days until a song is to be moved to archive playlist
