package in.zapr.druid.druidry.client.apache;

import com.sun.net.httpserver.HttpServer;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.util.Timeout;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import in.zapr.druid.druidry.client.RuntimeIoException;
import in.zapr.druid.druidry.dataSource.TableDataSource;
import in.zapr.druid.druidry.granularity.PredefinedGranularity;
import in.zapr.druid.druidry.granularity.SimpleGranularity;
import in.zapr.druid.druidry.query.aggregation.DruidTimeSeriesQuery;
import in.zapr.druid.druidry.query.config.Interval;
import in.zapr.druid.druidry.query.config.spec.MultipleIntervalSegmentSpec;

public class ApacheDruidClientTest {

    private HttpServer server;
    private List<String> requests;
    // responses served in order; the last one repeats
    private Queue<Response> responses;
    private long delayMillis;

    private record Response(int status, String body) {
    }

    @BeforeMethod
    public void init() throws IOException {
        requests = new CopyOnWriteArrayList<>();
        responses = new ConcurrentLinkedQueue<>(List.of(new Response(202, "")));
        delayMillis = 0;
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getRawPath());
            Response response = responses.size() > 1 ? responses.poll() : responses.peek();
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(response.status(), body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
            }
            exchange.close();
        });
        server.start();
    }

    @AfterMethod
    public void stop() {
        server.stop(0);
    }

    @Test
    public void testCancelQuery() {
        try (ApacheDruidClient client = new ApacheDruidClient(url())) {
            client.cancelQuery("q-1");
        }

        Assert.assertEquals(requests, List.of("DELETE /druid/v2/q-1"));
    }

    @Test
    public void testCancelQueryEncodesId() {
        try (ApacheDruidClient client = new ApacheDruidClient(url())) {
            client.cancelQuery("a b/c");
        }

        Assert.assertEquals(requests, List.of("DELETE /druid/v2/a%20b%2Fc"));
    }

    @Test(expectedExceptions = RuntimeIoException.class)
    public void testCancelQueryRefused() {
        responses = new ConcurrentLinkedQueue<>(List.of(new Response(500, "")));
        try (ApacheDruidClient client = new ApacheDruidClient(url())) {
            client.cancelQuery("q-1");
        }
    }

    @Test
    public void testQueryAsInputStreamRetriesMissingSegments() throws IOException {
        responses = new ConcurrentLinkedQueue<>(List.of(new Response(500, "{\"error\":\"SegmentMissingException\"}"),
                new Response(200, "[]")));
        try (ApacheDruidClient client = new ApacheDruidClient(url());
             CloseableHttpResponse response = client.queryAsInputStream(query())) {
            Assert.assertEquals(response.getCode(), 200);
        }

        Assert.assertEquals(requests, List.of("POST /druid/v2/", "POST /druid/v2/"));
    }

    @Test
    public void testQueryAsInputStreamDoesNotRetryOtherErrors() {
        responses = new ConcurrentLinkedQueue<>(List.of(new Response(500, "{\"error\":\"Query timeout\"}")));
        try (ApacheDruidClient client = new ApacheDruidClient(url())) {
            Assert.assertThrows(RuntimeIoException.class, () -> client.queryAsInputStream(query()));
        }

        Assert.assertEquals(requests, List.of("POST /druid/v2/"));
    }

    @Test(expectedExceptions = RuntimeIoException.class)
    public void testQueryAsInputStreamUsesRequestConfig() {
        responses = new ConcurrentLinkedQueue<>(List.of(new Response(200, "[]")));
        delayMillis = 1000;
        try (ApacheDruidClient client = new ApacheDruidClient(url())) {
            client.queryAsInputStream(query(), RequestConfig.custom().setResponseTimeout(Timeout.ofMilliseconds(200)).build());
        }
    }

    private String url() {
        return "http://" + server.getAddress().getAddress().getHostAddress() + ":" + server.getAddress().getPort() + "/druid/v2/";
    }

    private static DruidTimeSeriesQuery query() {
        DateTime start = new DateTime(2024, 1, 1, 0, 0);
        return DruidTimeSeriesQuery.builder()
                .dataSource(new TableDataSource("ds"))
                .intervals(new MultipleIntervalSegmentSpec(Collections.singletonList(new Interval(start, start.plusDays(1)))))
                .granularity(new SimpleGranularity(PredefinedGranularity.ALL))
                .build();
    }
}
