# Commandline Interface

## Running in the commandline
The utility can be run in the command-line with the following arguments:
- `--client-id` - Client ID for Spotify web app (required)
- `--client-secret` - Client Secret for Spotify web app (required)
- `--refresh-token` - API refresh token used to generate access tokens (required)
- `--active-playlist-id` - ID of the playlist that songs are removed from (required)
- `--archive-playlist-id` - ID of the playlist that songs are added to (required)
- `--song-lifetime` - Number of days before a song should be moved to archive playlist (optional, default = 30)

So an example command would look like:
```
java -jar spotify-playlist-updater-1.0.0-SNAPSHOT.jar --client-id <client ID> --client-secret <client secret> 
--refresh-token <refresh token> --active-playlist-id <active playlist ID> --archive-playlist-id <archive playlist id>
--song-lifetime <song lifetime>
```