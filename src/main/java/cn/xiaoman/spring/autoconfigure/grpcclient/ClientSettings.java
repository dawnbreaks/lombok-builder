package cn.xiaoman.spring.autoconfigure.grpcclient;

import brave.Tracing;
import com.google.common.collect.Lists;
import io.grpc.stub.AbstractStub;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static cn.xiaoman.spring.autoconfigure.grpcclient.GrpcClient.*;

/*
 *author: lubin
 *Date:    2019/4/22
 */

//@ConfigurationProperties(prefix = "xiaoman.spring.client-settings")
@Validated
@Data
public class ClientSettings {
    private final Optional<Tracing> tracing;
    private Integer dnsMinTtlSeconds = DEFAULT_DNS_MIN_TTL_SECONDS;
    private Integer dnsMaxTtlSeconds = DEFAULT_DNS_MAX_TTL_SECONDS;
    private Long responseTimeOutMillis = DEFAULT_RESPONSE_TIMEOUT_MILLISECONDS;
    private List<ClientConfig> clients = Lists.newArrayList();

    public ClientSettings(Optional<Tracing> tracing) {
        this.tracing = tracing;
    }

    @Data
    public static class ClientConfig {
        @NotEmpty
        private String name;
        @NotEmpty
        private String host;
        @NotNull
        private Boolean dnsDiscoveryFlag;

        private Integer port = GrpcClient.DEFAULT_SERVER_PORT;
        private Integer dnsMinTtlSeconds;
        private Integer dnsMaxTtlSeconds;
        private Long responseTimeOutMillis;
    }

    @PostConstruct
    public void init() {
        for (ClientConfig clientConfig : clients) {
            if (clientConfig.getDnsMaxTtlSeconds() == null) {
                clientConfig.setDnsMaxTtlSeconds(dnsMaxTtlSeconds);
            }
            if (clientConfig.getDnsMinTtlSeconds() == null) {
                clientConfig.setDnsMinTtlSeconds(dnsMinTtlSeconds);
            }
            if (clientConfig.getResponseTimeOutMillis() == null) {
                clientConfig.setResponseTimeOutMillis(responseTimeOutMillis);
            }
        }
    }

    private ClientConfig getClientConfigByName(String name) {
        return clients.stream().filter(config -> config.getName().equals(name)).findFirst().get();
    }

    public <STUB extends AbstractStub<STUB>> GrpcClient<STUB> createClientByName(String name, Class<STUB> stubClass) {
        ClientConfig clientConfig = getClientConfigByName(name);
        return GrpcClient.createByClientConfig(clientConfig, tracing.isPresent() ? tracing.get() : null, stubClass);
    }
}
