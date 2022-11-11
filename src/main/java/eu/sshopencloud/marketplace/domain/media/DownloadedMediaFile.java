package eu.sshopencloud.marketplace.domain.media;

import java.io.InputStream;
import java.util.function.Function;


interface DownloadedMediaFile {
    <T> T consumeFile(Function<InputStream, T> mediaConsumer);
}
