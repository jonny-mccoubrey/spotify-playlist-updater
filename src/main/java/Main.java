import org.kohsuke.args4j.Option;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;

public class Main {
    @Option(name = "--client-id", required = true)
    private String clientId = "";

    @Option(name = "--client-secret", required = true)
    private String clientSecret = "";

    @Option(name = "--active-playlist", required = true)
    private String activePlaylistId = "7fLOrnMe4gg4RkS8Ax9EiE";

    @Option(name = "--archive-playlist", required = true)
    private String archivePlaylistId = "5a9m0Ayb8S9b0qBlMNXsdO";

    @Option(name = "--song-lifetime")
    private String songLifetime = "30";

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

        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(activePlaylistId).build();

        Playlist playlist = null;
        try {
            playlist = getPlaylistRequest.execute();
        } catch (final Exception ex) {
            System.out.println("Error getting playlist: " + ex.getMessage());
            System.exit(1);
        }

        if (playlist == null) {
            System.out.println("No playlist found");
            throw new RuntimeException("No playlist found");
        } else {
            System.out.println("Playlist name: " + playlist.getName());
        }
    }
}
