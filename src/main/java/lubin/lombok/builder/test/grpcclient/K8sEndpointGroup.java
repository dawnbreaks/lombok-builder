package lubin.lombok.builder.test.grpcclient;

import com.google.common.collect.Lists;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.DynamicEndpointGroup;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/*
 *author: lubin
 *Date:    2019/5/29
 */
@Slf4j
public class K8sEndpointGroup extends DynamicEndpointGroup {
    private final String namespace;
    private final String serviceName;
    private final int port;
    private final KubernetesClient kubernetesClient;

    private volatile boolean watching = false;

    public K8sEndpointGroup(KubernetesClient kubernetesClient, String namespace, String serviceName, int port) {
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.port = port;
        this.kubernetesClient = kubernetesClient;
        start();
    }

    @Override
    public void close() {
        super.close();
        kubernetesClient.close();
    }

    private void start() {
        Endpoints k8sEndpoints = kubernetesClient.endpoints().inNamespace(namespace)
          .withName(serviceName)
          .get();

        if (k8sEndpoints == null) {
            throw new RuntimeException(String.format("k8s endpoints %s not found", serviceName));
        }

        updateLocalEndpoints(k8sEndpoints);
        watch(k8sEndpoints.getMetadata().getResourceVersion());
    }

    private void updateLocalEndpoints(Endpoints k8sEndpoints) {
        List<Endpoint> readyEndpoints = Lists.newArrayList();
        if (k8sEndpoints.getSubsets() == null || k8sEndpoints.getSubsets().isEmpty()) {
            onEndpoints(Collections.emptyList());
            return;
        }
        k8sEndpoints.getSubsets().forEach(subset -> {
            boolean hasServicePort = subset.getPorts().stream().anyMatch(endpointPort -> endpointPort.getPort() == port);
            if (hasServicePort) {
                subset.getAddresses().stream().map(address -> Endpoint.of(address.getIp(), port)).forEach(readyEndpoints::add);
            }
        });
        onEndpoints(readyEndpoints);
    }

    private void onEndpoints(List<Endpoint> endpoints) {
        if (!initialEndpointsFuture().isDone()) {
            initialEndpointsFuture().complete(endpoints);
        }
        setEndpoints(endpoints);
        log.info("k8sService={}|port={}|endpoints={}", serviceName, port, endpoints);
    }

    protected void watch(String resourceVersion) {
        if (watching) {
            return;
        }
        watching = true;
        kubernetesClient.endpoints().inNamespace(namespace)
          .withName(serviceName)
          .withResourceVersion(resourceVersion)
          .watch(new Watcher<Endpoints>() {
              @Override
              public void eventReceived(Action action, Endpoints endpoints) {
                  switch (action) {
                      case MODIFIED:
                      case ADDED:
                          updateLocalEndpoints(endpoints);
                          return;
                      case DELETED:
                          onEndpoints(Collections.emptyList());
                          return;
                  }
              }

              @Override
              public void onClose(KubernetesClientException e) {
                  watching = false;
                  if (e != null) {
                      log.error(String.format("serviceName=%s|watcher closed", K8sEndpointGroup.this.serviceName), e);
                  } else {
                      log.warn("serviceName={}|watcher closed", K8sEndpointGroup.this.serviceName);
                  }
              }
          });
    }
}
