package fr.gravelinesgrimp.pocket.member.api;

import java.util.UUID;

/** Where the app fetches an uploaded file (always through the access check of MediaController). */
public final class MediaUrls {

    private MediaUrls() {}

    public static String of(UUID mediaId) {
        return mediaId == null ? null : "/api/v1/media/" + mediaId;
    }
}
