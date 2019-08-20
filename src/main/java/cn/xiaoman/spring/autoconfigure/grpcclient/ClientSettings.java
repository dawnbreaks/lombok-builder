package cn.xiaoman.spring.autoconfigure.grpcclient;

import io.grpc.stub.AbstractStub;
import lombok.Data;

/*
 *author: lubin
 *Date:    2019/4/22
 */

//@Validated
@Data
public class ClientSettings {
    public static final int DEFAULT_DNS_MIN_TTL_SECONDS = 5;
    private Integer dnsMinTtlSeconds;
    private Integer dnsMaxTtlSeconds;
    private Long responseTimeOutMillis;
    @Data
    public static class ClientConfig {
        private String name;
        private String host;
        private Boolean dnsDiscoveryFlag;

        private Integer port;
        private Integer dnsMinTtlSeconds;
        private Integer dnsMaxTtlSeconds;
        private Long responseTimeOutMillis;
    }

    public <STUB extends AbstractStub<STUB>> GrpcClient<STUB> createClientByName(String name, Class<STUB> stubClass) {
        ClientConfig clientConfig = new ClientConfig();
        return GrpcClient.createByClientConfig(clientConfig, stubClass);
    }
}
