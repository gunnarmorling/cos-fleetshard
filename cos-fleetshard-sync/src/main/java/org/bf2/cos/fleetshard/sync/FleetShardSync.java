package org.bf2.cos.fleetshard.sync;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.bf2.cos.fleetshard.sync.client.FleetShardClient;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class FleetShardSync {
    @Inject
    FleetShardClient client;

    void onStart(@Observes StartupEvent ignored) {
        client.createManagedConnectorCluster();
    }
}