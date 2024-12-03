import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.util.Arrays;
import java.util.Date;

public class Main {
    @Option(name = "--client-id", required = true)
    private String clientId = "";

    @Option(name = "--client-secret", required = true)
    private String clientSecret = "";

    @Option(name = "--active-playlist", required = true)
    private String activePlaylistId = "";

    @Option(name = "--archive-playlist", required = true)
    private String archivePlaylistId = "";

    @Option(name = "--song-lifetime")
    private long songLifetime = 30;

    public static void main(String[] args) {
        try {
            new Main().doMain(args);
        } catch (final Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            System.exit(1);
        }
    }

    public void doMain(String[] args)
    {
        parseCommandLineOptions(args);

        final ClientCredentialsRequest request = new ClientCredentialsRequest.Builder(clientId, clientSecret)
                .grant_type("client_credentials")
                .build();

        ClientCredentials credentials = null;
        try {
            credentials = request.execute();
        } catch (final Exception ex) {
            System.out.println("Error getting credentials: " + ex.getMessage());
            System.exit(1);
        }

        final String accessToken;
        if (credentials == null)
        {
            System.out.println("No credentials found");
            throw new RuntimeException("No credentials found");
        } else {
            accessToken = credentials.getAccessToken();
        }

        final SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(activePlaylistId).build();

        Paging<PlaylistTrack> playlistItems = null;
        try {
            playlistItems = getPlaylistsItemsRequest.execute();
        } catch (final Exception ex) {
            System.out.println("Error getting playlist items: " + ex.getMessage());
            System.exit(1);
        }

        if (playlistItems == null) {
            System.out.println("No playlist found");
            throw new RuntimeException("No playlist found");
        }

        Arrays.stream(playlistItems.getItems())
                .filter(item -> {
                    final Date currentDate = new Date();
                    final long diffInMillis = currentDate.getTime() - item.getAddedAt().getTime();
                    final long daysSinceAddition = diffInMillis / (1000 * 60 * 60 * 24);
                    return daysSinceAddition >= songLifetime;
                }).forEach(item -> System.out.println("Item: " + item.getTrack().getName()));
    }

    private void parseCommandLineOptions(final String[] args) throws IllegalArgumentException
    {
        final CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (final CmdLineException ex) {
            parser.printUsage(System.err);
            System.out.println("Error parsing arguments: " + ex.getMessage());
            throw new IllegalArgumentException();
        }
    }
}
