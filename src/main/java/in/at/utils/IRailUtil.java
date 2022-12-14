package in.at.utils;

import in.at.exceptions.IRailException;
import in.at.response.IRailErrorResponse;
import in.at.response.LiveBoardResponse;
import in.at.response.StationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

public final class IRailUtil {

    private static final String BASE_URL = "https://api.irail.be";

    public static StationResponse fetchStations() {
        return get("/stations?format=json&lang=en", StationResponse.class);
    }

    public static LiveBoardResponse fetchLiveBoard(String stationId, String date, String arrdep) {
        return get(String.format("/liveboard?id=%s&format=json&lang=en&date=%s&arrdep=%s", stationId, date, arrdep), LiveBoardResponse .class);
    }

    private static <T> T get(String uri, Class<T> response) {
        WebClient.RequestHeadersSpec<?> spec = WebClient.builder().baseUrl(BASE_URL).clientConnector(new ReactorClientHttpConnector(
                HttpClient.create().followRedirect(true)
        )).build().get().uri(uri);

        return spec.retrieve()
                .onStatus(HttpStatus::isError, r -> r.bodyToMono(IRailErrorResponse.class).map(IRailException::new))
                .toEntity(response).block().getBody();
    }
}
