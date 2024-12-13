package com.app.util;

import com.app.model.Request;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.hc.core5.http.ParseException;
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

public class SpotifyUtil {

    final private static Logger LOGGER = LoggerFactory.getLogger(SpotifyUtil.class);

    public static void processPlaylist(final Request req)
            throws IOException, ParseException, SpotifyWebApiException
    {
        LOGGER.info("Starting to process tracks for archiving");

        final SpotifyApi spotifyApi = buildSpotifyApi(req);

        final List<PlaylistTrack> filteredTracks = getTracksToBeArchived(spotifyApi, req.activePlaylistId(), req.songLifetime());

        if (filteredTracks.isEmpty()) {
            LOGGER.info("No tracks to be moved");
        } else {
            archiveTracks(spotifyApi, filteredTracks, req);
        }

        LOGGER.info("Finished archiving process");
    }

    private static SpotifyApi buildSpotifyApi(final Request req)
            throws IOException, ParseException, SpotifyWebApiException
    {
        final SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(req.clientId())
                .setClientSecret(req.clientSecret())
                .setRefreshToken(req.refreshToken())
                .build();

        final AuthorizationCodeRefreshRequest refreshRequest = spotifyApi.authorizationCodeRefresh().build();

        final AuthorizationCodeCredentials authorizationCodeCredentials = refreshRequest.execute();
        spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

        LOGGER.info("Successfully set access token");

        return spotifyApi;
    }

    private static List<PlaylistTrack> getTracksToBeArchived(final SpotifyApi spotifyApi, final String activePlaylistId,
                                                            final long songLifetime)
            throws IOException, ParseException, SpotifyWebApiException
    {
        final GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(activePlaylistId).build();
        Paging<PlaylistTrack> playlistItems = null;
        playlistItems = getPlaylistsItemsRequest.execute();

        return Arrays.stream(playlistItems.getItems())
                .filter(item -> {
                    final Date currentDate = new Date();
                    final long diffInMillis = currentDate.getTime() - item.getAddedAt().getTime();
                    final long daysSinceAddition = diffInMillis / (1000 * 60 * 60 * 24);
                    return daysSinceAddition >= songLifetime;
                }).toList();
    }

    private static void archiveTracks(final SpotifyApi spotifyApi, final List<PlaylistTrack> tracks, final Request req)
    {
        tracks.forEach(item -> {
            final String[] track = new String[]{item.getTrack().getUri()};
            final JsonArray jsonTrack = JsonParser.parseString("[{\"uri\":\"" + track[0] + "\"}]").getAsJsonArray();

            final AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
                    .addItemsToPlaylist(req.archivePlaylistId(), track)
                    .build();

            final RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest = spotifyApi
                    .removeItemsFromPlaylist(req.activePlaylistId(), jsonTrack)
                    .build();

            try {
                addItemsToPlaylistRequest.execute();
                removeItemsFromPlaylistRequest.execute();
                LOGGER.info("Successfully moved track {}", item.getTrack());
            } catch (final IOException | SpotifyWebApiException | ParseException ex) {
                LOGGER.error("Error moving track: {}", ex.getMessage());
            }
        });
    }
}
