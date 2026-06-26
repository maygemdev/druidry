package in.zapr.druid.druidry.client.apache;

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
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
        jsonMapper = JsonMapper.builder().build();
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
            int tryCount = 1;
            while (true) {
                ClassicHttpRequest req = ClassicRequestBuilder.post(url)
                        .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                        .setEntity(body, ContentType.APPLICATION_JSON)
                        .build();
                HttpResult result = http.execute(req,
                        response -> new HttpResult(response.getCode(), readResponse(response)));
                if (result.code() == HttpStatus.SC_OK) {
                    return result.body();
                }
                if (tryCount == MAX_RETRY || !retryableException(result.body())) {
                    throw new IOException("%d: %s.\n For request:\n %s".formatted(result.code(), result.body(), body));
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
    public CloseableHttpResponse queryAsInputStream(String host, DruidQuery query)
            throws RuntimeIoException, DruidException {
        try {
            URL baseUrl = new URL(url);
            return runQueryAsInputStream(baseUrl.getProtocol() + "://" + host + baseUrl.getPath(), query);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    // Returns an open response so the caller can stream the body; the response-handler
    // execute() overloads close the response before returning and cannot be used here,
    // and HttpClient 5 offers no non-deprecated alternative for an open response.
    @SuppressWarnings("deprecation")
    private CloseableHttpResponse runQueryAsInputStream(String host, DruidQuery query)
            throws RuntimeIoException, DruidException {
        try {
            String body = jsonMapper.writeValueAsString(query);
            ClassicHttpRequest req = ClassicRequestBuilder.post(host)
                    .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .setEntity(body, ContentType.APPLICATION_JSON)
                    .build();
            CloseableHttpResponse resp = http.execute(req);
            if (resp.getCode() == HttpStatus.SC_OK) {
                return resp;
            }
            throw new IOException(String.format("%d: %s.For request:\\n %s", resp.getCode(), readResponse(resp), body));
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public <T> List<T> query(DruidQuery query, Class<T> clazz) {
        try {
            return jsonMapper.readValue(query(query), new TypeReference<>() {
                @Override
                public Type getType() {
                    return TypeUtils.parameterize(List.class, clazz);
                }
            });
        } catch (JacksonException e) {
            throw new RuntimeIoException(e);
        }
    }

    private static String readResponse(ClassicHttpResponse response) throws IOException {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private boolean retryableException(String body) {
        return body != null && (body.contains("SegmentMissingException") || body.contains("missing segments")
                || body.contains("org.jboss.netty.channel.ChannelException"));
    }

    private record HttpResult(int code, String body) {

    }
}
