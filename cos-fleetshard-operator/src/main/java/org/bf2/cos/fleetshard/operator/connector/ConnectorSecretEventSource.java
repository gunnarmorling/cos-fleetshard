package org.bf2.cos.fleetshard.operator.connector;

import java.util.Objects;

import org.bf2.cos.fleetshard.api.ManagedConnector;
import org.bf2.cos.fleetshard.api.ManagedConnectorOperator;
import org.bf2.cos.fleetshard.operator.support.InstrumentedWatcherEventSource;
import org.bf2.cos.fleetshard.operator.support.MetricsRecorder;
import org.bf2.cos.fleetshard.support.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.javaoperatorsdk.operator.processing.event.DefaultEvent;

public class ConnectorSecretEventSource extends InstrumentedWatcherEventSource<Secret> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorSecretEventSource.class);

    private final ManagedConnectorOperator operator;
    private final String namespace;

    public ConnectorSecretEventSource(
        KubernetesClient kubernetesClient,
        ManagedConnectorOperator operator,
        String namespace,
        MetricsRecorder recorder) {

        super(kubernetesClient, recorder);

        this.operator = operator;
        this.namespace = namespace;
    }

    @Override
    protected Watch doWatch() {
        return getClient()
            .secrets()
            .inNamespace(namespace)
            .withLabel(Resources.LABEL_OPERATOR_TYPE, operator.getSpec().getType())
            .withLabel(Resources.LABEL_UOW)
            .watch(this);
    }

    @Override
    protected void onEventReceived(Action action, Secret resource) {
        LOGGER.debug("Event {} received for secret: {}/{}",
            action.name(),
            resource.getMetadata().getNamespace(),
            resource.getMetadata().getName());

        getEventHandler().handleEvent(
            new DefaultEvent(
                cr -> hasSameUoW((ManagedConnector) cr, resource),
                this));
    }

    private boolean hasSameUoW(ManagedConnector connector, Secret resource) {
        return Objects.equals(
            connector.getSpec().getDeployment().getUnitOfWork(),
            resource.getMetadata().getLabels().get(Resources.LABEL_UOW));
    }
}
