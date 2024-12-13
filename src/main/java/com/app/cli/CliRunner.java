package com.app.cli;

import com.app.model.Request;
import com.app.util.SpotifyUtil;
import org.apache.hc.core5.http.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

public class CliRunner {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CliRunner.class);

    public static void main(String[] args) {
        try {
            new CliRunner().doMain(args);
        } catch (final IOException | ParseException | SpotifyWebApiException ex) {
            LOGGER.error(ex.getMessage());
            System.exit(1);
        }
    }

    private void doMain(String[] args) throws IOException, ParseException, SpotifyWebApiException {
        parseCommandLineOptions(args);

        SpotifyUtil.processPlaylist(new Request(clientId, clientSecret, refreshToken, activePlaylistId, archivePlaylistId, songLifetime));
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
