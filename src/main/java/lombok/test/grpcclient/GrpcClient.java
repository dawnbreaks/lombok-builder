package lombok.test.grpcclient;

import io.grpc.stub.AbstractStub;
import lombok.Builder;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.test.grpcclient.ClientSettings.ClientConfig;

/*
 *author: lubin
 *Date:    2019/4/11
 */

@Builder
@Accessors(fluent = true)
@Slf4j
public class GrpcClient<B extends AbstractStub<B>> {
    public static final int DEFAULT_DNS_MIN_TTL_SECONDS = 5;

    private String host;
    private Boolean dnsDiscoveryFlag;

    public static <B extends AbstractStub<B>> GrpcClient<B> createByClientConfig(ClientConfig clientConfig, Class<B> stubClass) {
        GrpcClientBuilder<B> builder = GrpcClient.<B>builder()
          .host(clientConfig.getHost())
          .dnsDiscoveryFlag(clientConfig.getDnsDiscoveryFlag());
        return builder.build();
    }

}
