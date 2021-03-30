package eu.sshopencloud.marketplace.domain.common.util;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.experimental.UtilityClass;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


@UtilityClass
public class WebClientUtils {

    private static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 100;


    public WebClient create(WebClient.Builder webClientBuilder, int connectTimeoutMs, int readTimeoutMs, Optional<Long> maxResponseBytes) {
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
                });

        HttpClient httpClient = HttpClient.from(tcpClient);

        webClientBuilder = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        if (maxResponseBytes.isPresent())
            webClientBuilder = webClientBuilder.filter(ExchangeFilterFunctions.limitResponseSize(maxResponseBytes.get()));

        return webClientBuilder.build();
    }
}
