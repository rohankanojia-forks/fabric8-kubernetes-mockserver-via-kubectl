package io.fabric8.kubernetes.server.mock.kubectl;

import io.fabric8.kubernetes.api.model.APIGroupListBuilder;
import io.fabric8.kubernetes.api.model.APIResourceBuilder;
import io.fabric8.kubernetes.api.model.APIResourceList;
import io.fabric8.kubernetes.api.model.APIResourceListBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.eclipse.jkube.kit.common.KitLogger;
import org.eclipse.jkube.kit.common.util.KubernetesHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

@EnableKubernetesMockClient
class KubectlMockServerTest {
  @TempDir
  private Path temporaryFolder;
  private KubernetesMockServer server;
  private KubernetesClient kubernetesClient;

  @Test
  void listPods() throws IOException {
    // Given
    // Need to set token otherwise kubectl starts giving "Enter username:" prompt
    kubernetesClient.getConfiguration().setOauthToken("sha256~secret");
    server.expect().get().withPath("/api?timeout=32s")
      .andReturn(200, String.format("{\"kind\":\"APIVersions\",\"versions\":[\"v1\"],\"serverAddressByClientCIDRs\":[{\"clientCIDR\":\"0.0.0.0/0\",\"serverAddress\":\"%s:%d\"}]}", server.getHostName(), server.getPort()))
      .always();
    server.expect().get().withPath("/apis?timeout=32s")
      .andReturn(200, new APIGroupListBuilder().build())
      .always();
    server.expect().get().withPath("/api/v1?timeout=32s")
      .andReturn(200, createAPIResourceList())
      .always();
    server.expect().get().withPath("/api/v1/namespaces/test/pods?limit=500")
      .andReturn(200, new PodListBuilder()
        .addToItems(new PodBuilder()
          .withNewMetadata().withName("p1").withCreationTimestamp("2024-06-13T10:19:18Z").endMetadata()
          .build())
        .build())
      .always();
    Path kubeconfig = KubernetesHelper.exportKubernetesClientConfigToFile(kubernetesClient.getConfiguration(), temporaryFolder.resolve("config"));
    KubectlListExternalCommand kubectlListPods = new KubectlListExternalCommand(new KitLogger.SilentLogger(), kubeconfig);

    // When + Then
    kubectlListPods.execute();
  }

  private APIResourceList createAPIResourceList() {
    APIResourceListBuilder apiResourceListBuilder = new APIResourceListBuilder();
    apiResourceListBuilder.addToResources(new APIResourceBuilder()
        .withName("pods")
        .withSingularName("pod")
        .withNamespaced()
        .withKind("Pod")
        .addToVerbs("create", "delete", "deletecollection", "get", "list", "patch", "update", "watch")
        .withShortNames("po")
      .build());
    return apiResourceListBuilder.build();
  }
}
