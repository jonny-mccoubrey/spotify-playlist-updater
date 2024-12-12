package com.app.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;

public class LambdaHandler implements RequestHandler<Request, Response> {

    @Override
    public Response handleRequest(Request request, Context context)
    {
        LambdaLogger LOGGER = context.getLogger();
        LOGGER.log("Processing question from " + request.name(), LogLevel.INFO);

        return new Response("Successfully processed request from " + request.name());
    }

}

record Request(String name, String question) {}

record Response(String answer) {}
