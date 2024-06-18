package io.fabric8.kubernetes.server.mock.kubectl;

import org.eclipse.jkube.kit.common.ExternalCommand;
import org.eclipse.jkube.kit.common.KitLogger;

import java.nio.file.Path;

public class KubectlListExternalCommand extends ExternalCommand {
  private final Path kubeConfigPath;
  protected KubectlListExternalCommand(KitLogger log, Path kubeConfigPath) {
    super(log);
    this.kubeConfigPath = kubeConfigPath;
  }

  @Override
  protected String[] getArgs() {
    return new String[] {"kubectl", "--kubeconfig", kubeConfigPath.toString(), "get", "pods"};
  }

  @Override
  protected void processLine(String line) {
    System.out.println(line);
  }
}
