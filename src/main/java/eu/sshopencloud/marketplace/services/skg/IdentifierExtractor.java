package eu.sshopencloud.marketplace.services.skg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IdentifierExtractor {

    // Matches the last path segment of a URL, ignoring trailing slashes,
    // query strings and fragments.
    private static final Pattern URL_LAST_SEGMENT =
            Pattern.compile("https?://[^/]+/(?:.*/)?([^/?#]+)");

    private IdentifierExtractor() {
    }

    /**
     * Extracts the bare identifier (e.g. "87wJWo") from either:
     *  - the identifier itself:  "87wJWo"
     *  - a full URL:             "https://marketplace.sshopencloud.eu/tool-or-service/87wJWo"
     *
     * @param input raw identifier or URL
     * @return the bare identifier
     * @throws IllegalArgumentException if the input is null/blank or no identifier can be found
     */
    public static String extractPersistentId(String input) {
        input = input.replaceFirst("^/+", "");   // remove leading slash(es)

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Identifier must not be null or blank");
        }

        String trimmed = input.trim();

        // Case 1: already a bare identifier (no slashes)
        if (!trimmed.contains("/")) {
            return trimmed;
        }

        // Case 2: URL – take the last path segment
        Matcher matcher = URL_LAST_SEGMENT.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        throw new IllegalArgumentException("Cannot extract identifier from: " + input);
    }
}
