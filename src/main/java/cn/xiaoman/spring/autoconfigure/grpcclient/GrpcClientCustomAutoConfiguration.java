package cn.xiaoman.spring.autoconfigure.grpcclient;

/*
 *author: lubin
 *Date:    2019-08-13
 */

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnClass(com.linecorp.armeria.client.grpc.GrpcClientFactoryProvider.class)
//@ConditionalOnProperty(name = "xiaoman.spring.client-settings")
@EnableConfigurationProperties(ClientSettings.class)
public class GrpcClientCustomAutoConfiguration {
}
