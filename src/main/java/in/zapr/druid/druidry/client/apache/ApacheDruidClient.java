package in.zapr.druid.druidry.client.apache;

import in.zapr.druid.druidry.client.DruidClient;
import in.zapr.druid.druidry.client.DruidException;
import in.zapr.druid.druidry.client.RuntimeIoException;
import in.zapr.druid.druidry.query.DruidQuery;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class ApacheDruidClient implements DruidClient {

    private static final int MAX_RETRY = 10;

    // a cancel runs while its caller is already failing, so a hung broker must not hold it for the query timeout
    private static final Timeout CANCEL_TIMEOUT = Timeout.ofSeconds(10);

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
        return query(query, (RequestConfig) null);
    }

    private String query(DruidQuery query, RequestConfig requestConfig) {
        try {
            String body = jsonMapper.writeValueAsString(query);
            int tryCount = 1;
            while (true) {
                ClassicHttpRequest req = ClassicRequestBuilder.post(url)
                        .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                        .setEntity(body, ContentType.APPLICATION_JSON)
                        .build();
                HttpResult result = http.execute(req, context(requestConfig),
                        response -> new HttpResult(response.getCode(), readResponse(response)));
                if (result.code() == HttpStatus.SC_OK) {
                    return result.body();
                }
                if (tryCount == MAX_RETRY || !retryableException(result.body())) {
                    throw new IOException("%d: %s.\n For request:\n %s".formatted(result.code(), result.body(), body));
                }
                backOff(tryCount);
                tryCount++;
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public CloseableHttpResponse queryAsInputStream(DruidQuery query) {
        return runQueryAsInputStream(url, query, null);
    }

    @Override
    public CloseableHttpResponse queryAsInputStream(DruidQuery query, RequestConfig requestConfig) {
        return runQueryAsInputStream(url, query, requestConfig);
    }

    @Override
    public CloseableHttpResponse queryAsInputStream(String host, DruidQuery query)
            throws RuntimeIoException, DruidException {
        try {
            URL baseUrl = new URL(url);
            return runQueryAsInputStream(baseUrl.getProtocol() + "://" + host + baseUrl.getPath(), query, null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    @SuppressWarnings("deprecation")
    public void cancelQuery(String queryId) throws RuntimeIoException {
        URI base = URI.create(url);
        InetAddress[] brokers;
        String path;
        try {
            brokers = SystemDefaultDnsResolver.INSTANCE.resolve(base.getHost());
            path = StringUtils.removeEnd(base.getRawPath(), "/") + new URIBuilder().setPathSegments(queryId).build().getRawPath();
        } catch (UnknownHostException | URISyntaxException e) {
            throw new RuntimeIoException(e);
        }
        HttpClientContext context = context(RequestConfig.custom()
                .setConnectTimeout(CANCEL_TIMEOUT)
                .setConnectionRequestTimeout(CANCEL_TIMEOUT)
                .setResponseTimeout(CANCEL_TIMEOUT)
                .build());
        IOException failure = null;
        for (InetAddress broker : brokers) {
            // the address pins the connection to this broker; the name stays in the Host header
            HttpHost target = new HttpHost(base.getScheme(), broker, base.getHost(), base.getPort());
            try {
                int code = http.execute(target, ClassicRequestBuilder.delete().setPath(path).build(), context, response -> {
                    EntityUtils.consume(response.getEntity());
                    return response.getCode();
                });
                if (code / 100 != 2) {
                    throw new IOException(String.format("%d from %s for %s", code, broker, path));
                }
            } catch (IOException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw new RuntimeIoException("Could not cancel Druid query " + queryId, failure);
        }
    }

    // Returns an open response so the caller can stream the body; the response-handler
    // execute() overloads close the response before returning and cannot be used here,
    // and HttpClient 5 offers no non-deprecated alternative for an open response.
    // A retryable error comes before any row, so retrying it is as safe as in query().
    @SuppressWarnings("deprecation")
    private CloseableHttpResponse runQueryAsInputStream(String host, DruidQuery query, RequestConfig requestConfig)
            throws RuntimeIoException, DruidException {
        try {
            String body = jsonMapper.writeValueAsString(query);
            int tryCount = 1;
            while (true) {
                ClassicHttpRequest req = ClassicRequestBuilder.post(host)
                        .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                        .setEntity(body, ContentType.APPLICATION_JSON)
                        .build();
                CloseableHttpResponse resp = http.execute(req, context(requestConfig));
                if (resp.getCode() == HttpStatus.SC_OK) {
                    return resp;
                }
                String error;
                try {
                    error = readResponse(resp);
                } finally {
                    resp.close();
                }
                if (tryCount == MAX_RETRY || !retryableException(error)) {
                    throw new IOException(String.format("%d: %s.For request:\\n %s", resp.getCode(), error, body));
                }
                backOff(tryCount);
                tryCount++;
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public <T> List<T> query(DruidQuery query, Class<T> clazz) {
        return query(query, clazz, null);
    }

    @Override
    public <T> List<T> query(DruidQuery query, Class<T> clazz, RequestConfig requestConfig) {
        try {
            return jsonMapper.readValue(query(query, requestConfig), new TypeReference<>() {
                @Override
                public Type getType() {
                    return TypeUtils.parameterize(List.class, clazz);
                }
            });
        } catch (JacksonException e) {
            throw new RuntimeIoException(e);
        }
    }

    // null keeps the client's default request config
    private static HttpClientContext context(RequestConfig requestConfig) {
        HttpClientContext context = HttpClientContext.create();
        if (requestConfig != null) {
            context.setRequestConfig(requestConfig);
        }
        return context;
    }

    private static void backOff(int tryCount) {
        try {
            Thread.sleep(tryCount == 1 ? 1000 : 5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
