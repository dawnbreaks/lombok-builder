package lubin.lombok.builder.test;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/*
 *author: lubin
 *Date:    2019-08-20
 */
@Builder
@Accessors(fluent = true)
@ToString(exclude = {"stub"})
@Slf4j
public class GrpcClient<T extends AbstractStub<T>>  {
    public static final int DEFAULT_SERVER_PORT = 8080;

    public static final int DEFAULT_DNS_MIN_TTL_SECONDS = 5;
    public static final int DEFAULT_DNS_MAX_TTL_SECONDS = 10;
    public static final long DEFAULT_RESPONSE_TIMEOUT_MILLISECONDS = 70_000;

    @Builder.Default
    private Integer dnsMinTtlSeconds = DEFAULT_DNS_MIN_TTL_SECONDS;
    @Builder.Default
    private Integer dnsMaxTtlSeconds = DEFAULT_DNS_MAX_TTL_SECONDS;
    @Builder.Default
    private Long responseTimeOutMillis = DEFAULT_RESPONSE_TIMEOUT_MILLISECONDS;
    private Class<T> stubClass;
    private String host;
    @Builder.Default
    private Integer port = DEFAULT_SERVER_PORT;
    private Boolean dnsDiscoveryFlag;
    @Getter(lazy = true)
    private final T stub = createStub();

    private T createStub() {
        try {
            return stubClass.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static GrpcClient<Stub> create() {
        return GrpcClient.<Stub>builder().dnsDiscoveryFlag(true)
          .dnsMaxTtlSeconds(20)
          .dnsMinTtlSeconds(10)
          .stubClass(Stub.class)
          .build();
    }
}
