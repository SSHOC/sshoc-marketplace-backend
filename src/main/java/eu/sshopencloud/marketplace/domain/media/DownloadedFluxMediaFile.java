package eu.sshopencloud.marketplace.domain.media;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.function.Function;


class DownloadedFluxMediaFile implements DownloadedMediaFile {
    private final Flux<DataBuffer> content;

    public DownloadedFluxMediaFile(Flux<DataBuffer> content) {
        this.content = content;
    }

    @Override
    public <T> T consumeFile(Function<InputStream, T> mediaConsumer) {
        return content.map(buffer -> buffer.asInputStream(true))
                .reduce(SequenceInputStream::new)
                .map(mediaConsumer)
                .block();
    }
}
