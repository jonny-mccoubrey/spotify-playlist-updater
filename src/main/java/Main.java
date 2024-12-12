import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.hc.core5.http.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main {
    @Option(name = "--client-id", required = true)
    private String clientId = "";

    @Option(name = "--client-secret", required = true)
    private String clientSecret = "";

    @Option(name = "--refresh-token", required = true)
    private String refreshToken = "";

    @Option(name = "--active-playlist-id", required = true)
    private String activePlaylistId = "";

    @Option(name = "--archive-playlist-id", required = true)
    private String archivePlaylistId = "";

    @Option(name = "--song-lifetime")
    private long songLifetime = 30;

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            new Main().doMain(args);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
            System.exit(1);
        }
    }

    public void doMain(String[] args)
    {
        parseCommandLineOptions(args);

        final SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        final AuthorizationCodeRefreshRequest refreshRequest = spotifyApi.authorizationCodeRefresh().build();

        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = refreshRequest.execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            LOGGER.info("Successfully set access token");
        } catch (final IOException | SpotifyWebApiException | ParseException ex) {
            LOGGER.error("Error getting access token: {}", ex.getMessage());
        }

        final GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(activePlaylistId).build();

        Paging<PlaylistTrack> playlistItems = null;
        try {
            playlistItems = getPlaylistsItemsRequest.execute();
        } catch (final Exception ex) {
            LOGGER.error("Error getting playlist items from active playlist: {}", ex.getMessage());
            System.exit(1);
        }

        if (playlistItems == null) {
            LOGGER.error("No playlist found");
            System.exit(1);
        }

        final List<PlaylistTrack> filteredTracks = Arrays.stream(playlistItems.getItems())
                        .filter(item -> {
                            final Date currentDate = new Date();
                            final long diffInMillis = currentDate.getTime() - item.getAddedAt().getTime();
                            final long daysSinceAddition = diffInMillis / (1000 * 60 * 60 * 24);
                            return daysSinceAddition >= songLifetime;
                        }).toList();

        if (filteredTracks.isEmpty()) {
            LOGGER.info("No tracks to be moved");
        } else {
            filteredTracks.forEach(item -> {
                        final String[] track = new String[]{item.getTrack().getUri()};
                        final JsonArray jsonTrack = JsonParser.parseString("[{\"uri\":\"" + track[0] + "\"}]").getAsJsonArray();

                        final AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
                                .addItemsToPlaylist(archivePlaylistId, track)
                                .build();

                        final RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest = spotifyApi
                                .removeItemsFromPlaylist(activePlaylistId, jsonTrack)
                                .build();

                        try {
                            addItemsToPlaylistRequest.execute();
                            removeItemsFromPlaylistRequest.execute();
                            LOGGER.info("Successfully moved track {}", item.getTrack());
                        } catch (IOException | SpotifyWebApiException | ParseException ex) {
                            LOGGER.error("Error moving track: {}", ex.getMessage());
                        }
            });
        }
    }

    private void parseCommandLineOptions(final String[] args) throws IllegalArgumentException
    {
        final CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (final CmdLineException ex) {
            parser.printUsage(System.err);
            LOGGER.error("Error parsing arguments: {}", ex.getMessage());
            throw new IllegalArgumentException();
        }
    }
}
