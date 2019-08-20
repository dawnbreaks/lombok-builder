package cn.xiaoman.spring.autoconfigure.grpcclient;

import cn.xiaoman.spring.autoconfigure.grpcclient.ClientSettings.ClientConfig;
import io.grpc.stub.AbstractStub;
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

    private B createStub() {
        return null;
    }

    public static <B extends AbstractStub<B>> GrpcClient<B> createByClientConfig(ClientConfig clientConfig, Class<B> stubClass) {
        GrpcClientBuilder<B> builder = GrpcClient.<B>builder()
          .host(clientConfig.getHost())
          .dnsDiscoveryFlag(clientConfig.getDnsDiscoveryFlag())
          .stubClass(stubClass);
        return builder.build();
    }

}
