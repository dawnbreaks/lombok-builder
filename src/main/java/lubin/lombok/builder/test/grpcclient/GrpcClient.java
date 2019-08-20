package lubin.lombok.builder.test.grpcclient;

import brave.Tracing;
import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.brave.BraveClient;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.armeria.client.endpoint.dns.DnsAddressEndpointGroup;
import com.linecorp.armeria.client.endpoint.dns.DnsAddressEndpointGroupBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.grpc.stub.AbstractStub;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lubin.lombok.builder.test.grpcclient.ClientSettings.ClientConfig;

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
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final KubernetesClient kubernetesClient = new DefaultKubernetesClient();

    public B createStub() {
        log.info("createStub|clientConfig={}", this);
        if (dnsDiscoveryFlag) {
            return createByK8sDns();
//            return createByK8sEndpoints();
        } else {
            return createByHost0();
        }
    }

    public static <B extends AbstractStub<B>> GrpcClient<B> createByClientConfig(ClientConfig clientConfig, Tracing tracing, Class<B> stubClass) {
        GrpcClientBuilder<B> builder = GrpcClient.<B>builder()
          .host(clientConfig.getHost())
          .tracing(tracing)
          .dnsDiscoveryFlag(clientConfig.getDnsDiscoveryFlag())
          .stubClass(stubClass);

        if (clientConfig.getResponseTimeOutMillis() != null) {
            builder.responseTimeOutMillis(clientConfig.getResponseTimeOutMillis());
        }
        if (clientConfig.getDnsMinTtlSeconds() != null) {
            builder.dnsMinTtlSeconds(clientConfig.getDnsMinTtlSeconds());
        }
        if (clientConfig.getDnsMaxTtlSeconds() != null) {
            builder.dnsMaxTtlSeconds(clientConfig.getDnsMaxTtlSeconds());
        }
        if (clientConfig.getPort() != null) {
            builder.port(clientConfig.getPort());
        }
        return builder.build();
    }

    private B createByK8sDns() {
        try {
            String groupId = host + "_" + port;
            DnsAddressEndpointGroup group = new DnsAddressEndpointGroupBuilder(host).port(port)
              .ttl(dnsMinTtlSeconds, dnsMaxTtlSeconds)
              .build();
            group.awaitInitialEndpoints();
            EndpointGroupRegistry.register(groupId, group, EndpointSelectionStrategy.ROUND_ROBIN);

            String serviceURI = String.format(SERVICE_ENDPOINT_GROUP_URI, groupId);
            return createClient(serviceURI, stubClass);
        } catch (Exception e) {
            throw new RuntimeException("GrpcClient.createByK8sDns", e);
        }
    }

    private B createByK8sEndpoints() {
        try {
            String groupId = host + "_" + port;
            K8sEndpointGroup group = new K8sEndpointGroup(kubernetesClient(), K8S_NAMESPACE, host, port);
            group.awaitInitialEndpoints();
            EndpointGroupRegistry.register(groupId, group, EndpointSelectionStrategy.ROUND_ROBIN);

            String serviceURI = String.format(SERVICE_ENDPOINT_GROUP_URI, groupId);
            return createClient(serviceURI, stubClass);
        } catch (Exception e) {
            throw new RuntimeException("GrpcClient.createByK8sEndpoints", e);
        }
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
        if (tracing != null) {
            builder.decorator(BraveClient.newDecorator(tracing, host));
        }
        return builder.build(stubClass);
    }
}
