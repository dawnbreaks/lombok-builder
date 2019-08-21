package lombok.test.grpcclient;

import io.grpc.stub.AbstractStub;
import lombok.Data;

import static lombok.test.grpcclient.GrpcClient.*;

/*
 *author: lubin
 *Date:    2019/8/20
 */
@Data
public class ClientSettings {
    private Integer dnsMinTtlSeconds = DEFAULT_DNS_MIN_TTL_SECONDS;
    @Data
    public static class ClientConfig {
        private String host;
        private Boolean dnsDiscoveryFlag;
    }

    public <STUB extends AbstractStub<STUB>> GrpcClient<STUB> createClientByName(String name, Class<STUB> stubClass) {
        ClientConfig clientConfig = new ClientConfig();
        return GrpcClient.createByClientConfig(clientConfig, stubClass);
    }
}
