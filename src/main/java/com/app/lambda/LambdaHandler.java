package com.app.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.app.model.Request;
import com.app.util.SpotifyUtil;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

public class LambdaHandler implements RequestHandler<Request, String> {

    @Override
    public String handleRequest(Request request, Context context)
    {
        LambdaLogger LOGGER = context.getLogger();

        try {
            SpotifyUtil.processPlaylist(request);

            return "Successfully processed playlist";
        } catch (final IOException | ParseException | SpotifyWebApiException ex) {
            LOGGER.log("Error processing playlist: " + ex.getMessage(), LogLevel.ERROR);
            return "Error processing playlist: " + ex.getMessage();
        }
    }
}
