package in.zapr.druid.druidry.client.apache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.zapr.druid.druidry.client.DruidClient;
import in.zapr.druid.druidry.client.DruidException;
import in.zapr.druid.druidry.client.RuntimeIoException;
import in.zapr.druid.druidry.query.DruidQuery;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

public class ApacheDruidClient implements DruidClient {

    private static final int MAX_RETRY = 10;

    private final String url;

    private final CloseableHttpClient http;

    private final ObjectMapper jsonMapper;

    public ApacheDruidClient(String url) {
        this(url, HttpClients.createDefault());
    }

    public ApacheDruidClient(String url, CloseableHttpClient http) {
        this.url = url;
        this.http = http;
        jsonMapper = new ObjectMapper();
    }

    @Override
    public void close() {
        try {
            http.close();
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public String query(DruidQuery query) {
        try {
            String body = jsonMapper.writeValueAsString(query);
            while (true) {
                int tryCount = 1;
                ClassicHttpRequest req = ClassicRequestBuilder.post(url)
                        .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                        .setEntity(body, ContentType.APPLICATION_JSON)
                        .build();
                // TODO: Replace deprecated execute() API usage.
                try (CloseableHttpResponse resp = http.execute(req)) {
                    switch (resp.getCode()) {
                        case HttpStatus.SC_OK:
                            return readResponse(resp);
                        default:
                            String respBody = readResponse(resp);
                            if (tryCount == MAX_RETRY || !retryableException(body)) {
                                throw new IOException(String.format("%d: %s", resp.getCode(), respBody));
                            }
                    }
                }
                try {
                    Thread.sleep(tryCount == 1 ? 1000 : 5000);
                } catch (InterruptedException e) {
                    throw new RuntimeIoException(e);
                }
                tryCount++;
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public CloseableHttpResponse queryAsInputStream(DruidQuery query) {
        return runQueryAsInputStream(url, query);
    }

    @Override
    public CloseableHttpResponse queryAsInputStream(String host, DruidQuery query) throws RuntimeIoException, DruidException {
        try {
            URL baseUrl = new URL(url);
            return runQueryAsInputStream(baseUrl.getProtocol() + "://" + host + baseUrl.getPath(), query);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    private CloseableHttpResponse runQueryAsInputStream(String host, DruidQuery query) throws RuntimeIoException, DruidException {
        try {
            String body = jsonMapper.writeValueAsString(query);
            ClassicHttpRequest req = ClassicRequestBuilder.post(host)
                    .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .setEntity(body, ContentType.APPLICATION_JSON)
                    .build();
            // TODO: Replace deprecated execute() API usage.
            CloseableHttpResponse resp = http.execute(req);
            switch (resp.getCode()) {
                case HttpStatus.SC_OK:
                    return resp;
                default:
                    throw new IOException(String.format("%d: %s", resp.getCode(), readResponse(resp)));
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public <T> List<T> query(DruidQuery query, Class<T> clazz) {
        try {
            return jsonMapper.readValue(query(query), new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return TypeUtils.parameterize(List.class, clazz);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeIoException(e);
        }
    }

    private static String readResponse(CloseableHttpResponse response) throws IOException {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private boolean retryableException(String body) {
        return body != null && body.contains("SegmentMissingException");
    }
}
