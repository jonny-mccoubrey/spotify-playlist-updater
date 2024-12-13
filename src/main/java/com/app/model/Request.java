package com.app.model;

public record Request(String clientId, String clientSecret, String refreshToken, String activePlaylistId,
               String archivePlaylistId, long songLifetime) {
}
