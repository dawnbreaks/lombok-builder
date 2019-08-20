package lubin.lombok.builder.test.grpc;


import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import io.grpc.stub.AbstractStub;
import io.opencensus.trace.Tracing;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/*
 *author: lubin
 *Date:    2019/4/11
 */

@Builder
@Accessors(fluent = true)
@Slf4j
@ToString(exclude = {"stub"})
public class GrpcClient<B extends AbstractStub<B>> {
    private static final String K8S_NAMESPACE = "infra";
    private static final String SERVICE_ENDPOINT_GROUP_URI = "gproto+http://group:%s/";
    public static final int DEFAULT_SERVER_PORT = 8080;

    public static final int DEFAULT_DNS_MIN_TTL_SECONDS = 5;
    public static final int DEFAULT_DNS_MAX_TTL_SECONDS = 10;
    public static final long DEFAULT_RESPONSE_TIMEOUT_MILLISECONDS = 70_000;

    @Getter(lazy = true)
    private final B stub = createStub();
    @Builder.Default
    private Integer dnsMinTtlSeconds = DEFAULT_DNS_MIN_TTL_SECONDS;
    @Builder.Default
    private Integer dnsMaxTtlSeconds = DEFAULT_DNS_MAX_TTL_SECONDS;
    @Builder.Default
    private Long responseTimeOutMillis = DEFAULT_RESPONSE_TIMEOUT_MILLISECONDS;
    private Class<B> stubClass;
    private String host;
    @Builder.Default
    private Integer port = DEFAULT_SERVER_PORT;
    private Boolean dnsDiscoveryFlag;
    private Tracing tracing;

    public B createStub() {
        log.info("createStub|clientConfig={}", this);
        return createByHost0();
    }


    private B createByHost0() {
        try {
            String groupId = host + "_" + port;
            EndpointGroup group = new StaticEndpointGroup(Endpoint.of(host, port));
            EndpointGroupRegistry.register(groupId, group, EndpointSelectionStrategy.ROUND_ROBIN);

            String serviceURI = String.format(SERVICE_ENDPOINT_GROUP_URI, groupId);
            return createClient(serviceURI, stubClass);
        } catch (Exception e) {
            throw new RuntimeException("GrpcClient.createByHost", e);
        }
    }

    private B createClient(String serviceURI, Class<B> stubClass) {
        ClientBuilder builder = new ClientBuilder(serviceURI).responseTimeoutMillis(responseTimeOutMillis);
        return builder.build(stubClass);
    }
}
