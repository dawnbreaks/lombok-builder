package lombok.test.grpcclient;

import lombok.Data;

import static lombok.test.grpcclient.GrpcClient.DEFAULT_DNS_MIN_TTL_SECONDS;

/*
 *author: lubin
 *Date:    2019/4/22
 */

//@Validated
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
