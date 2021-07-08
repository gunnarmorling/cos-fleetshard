package org.bf2.cos.fleetshard.operator.it;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.bf2.cos.fleet.manager.api.model.cp.ConnectorDeployment;
import org.bf2.cos.fleetshard.api.ManagedConnector;
import org.bf2.cos.fleetshard.api.ManagedConnectorOperator;
import org.bf2.cos.fleetshard.operator.client.UnstructuredClient;
import org.bf2.cos.fleetshard.operator.it.support.camel.CamelTestSupport;

import static org.bf2.cos.fleetshard.api.ManagedConnector.DESIRED_STATE_READY;
import static org.bf2.cos.fleetshard.operator.support.ResourceUtil.asCustomResourceDefinitionContext;

public class CamelConnectorStatusTestSupport extends CamelTestSupport {
    protected void managedCamelConnectorStatusIsReported() {
        final ManagedConnectorOperator op = withCamelConnectorOperator("cm-1", "1.1.0");
        final ConnectorDeployment cd = withDefaultConnectorDeployment();
        final UnstructuredClient uc = new UnstructuredClient(ksrv.getClient());

        await(() -> {
            Optional<ManagedConnector> connector = getManagedConnector(cd);
            if (connector.isEmpty()) {
                return false;
            }

            JsonNode secret = uc.getAsNode(
                namespace,
                "v1",
                "Secret",
                connector.get().getMetadata().getName() + "-" + cd.getMetadata().getResourceVersion());

            JsonNode binding = uc.getAsNode(
                namespace,
                "camel.apache.org/v1alpha1",
                "KameletBinding",
                connector.get().getMetadata().getName());

            return secret != null && binding != null;
        });

        updateKameletBinding(mandatoryGetManagedConnector(cd).getMetadata().getName());

        awaitStatus(clusterId, cd.getId(), status -> {
            return Objects.equals(DESIRED_STATE_READY, status.getPhase());
        });
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> updateKameletBinding(String name) {
        UnstructuredClient uc = new UnstructuredClient(ksrv.getClient());
        ObjectNode binding = (ObjectNode) uc.getAsNode(
            namespace,
            "camel.apache.org/v1alpha1",
            "KameletBinding",
            name);

        binding.with("status").put("phase", "Ready");
        binding.with("status").withArray("conditions")
            .addObject()
            .put("message", "a message")
            .put("reason", "a reason")
            .put("status", "the status")
            .put("type", "the type")
            .put("lastTransitionTime", "2021-06-12T12:35:09+02:00");

        try {
            return ksrv.getClient()
                .customResource(asCustomResourceDefinitionContext(binding))
                .updateStatus(
                    namespace,
                    name,
                    Serialization.jsonMapper().treeToValue(binding, Map.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
