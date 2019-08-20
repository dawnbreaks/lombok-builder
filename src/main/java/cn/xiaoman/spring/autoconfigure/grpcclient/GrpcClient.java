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


    @Getter(lazy = true)
    private final B stub = createStub();
    private Class<B> stubClass;
    private String host;
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
