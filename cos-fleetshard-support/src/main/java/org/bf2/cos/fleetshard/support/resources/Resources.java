package org.bf2.cos.fleetshard.support.resources;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bf2.cos.fleetshard.api.DeployedResource;
import org.bf2.cos.fleetshard.api.ResourceRef;
import org.bson.types.ObjectId;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;

public final class Resources {

    public static final String LABEL_WATCH = "cos.bf2.org/watch";
    public static final String LABEL_RESOURCE_CONTEXT = "cos.bf2.org/resource.context";
    public static final String LABEL_CLUSTER_ID = "cos.bf2.org/cluster.id";
    public static final String LABEL_DEPLOYMENT_ID = "cos.bf2.org/deployment.id";
    public static final String LABEL_CONNECTOR_ID = "cos.bf2.org/connector.id";
    public static final String LABEL_CONNECTOR_TYPE_ID = "cos.bf2.org/connector.type.id";
    public static final String LABEL_CONNECTOR_OPERATOR = "cos.bf2.org/connector.operator";
    public static final String LABEL_DEPLOYMENT_RESOURCE_VERSION = "cos.bf2.org/deployment.resource.version";
    public static final String LABEL_OPERATOR_OWNER = "cos.bf2.org/operator.owner";
    public static final String LABEL_OPERATOR_ASSIGNED = "cos.bf2.org/operator.assigned";
    public static final String LABEL_OPERATOR_TYPE = "cos.bf2.org/operator.type";
    public static final String LABEL_OPERATOR_VERSION = "cos.bf2.org/operator.version";

    public static final String ANNOTATION_DELETION_MODE = "cos.bf2.org/resource.deletion.mode";
    public static final String ANNOTATION_DEPLOYMENT_RESOURCE_VERSION = "cos.bf2.org/deployment.resource.version";
    public static final String ANNOTATION_UPDATED_TIMESTAMP = "cos.bf2.org/update.timestamp";

    public static final String DELETION_MODE_CONNECTOR = "connector";
    public static final String DELETION_MODE_DEPLOYMENT = "deployment";
    public static final String CONTEXT_DEPLOYMENT = "deployment";
    public static final String CONTEXT_OPERAND = "operand";

    private Resources() {
    }

    public static ResourceRef asRef(HasMetadata from) {
        ResourceRef answer = new ResourceRef();
        answer.setApiVersion(from.getApiVersion());
        answer.setKind(from.getKind());
        answer.setName(from.getMetadata().getName());

        return answer;
    }

    public static Optional<String> getDeletionMode(HasMetadata resource) {
        if (resource.getMetadata() == null) {
            return Optional.empty();
        }

        if (resource.getMetadata().getAnnotations() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(resource.getMetadata().getAnnotations().get(ANNOTATION_DELETION_MODE));
    }

    public static String uid() {
        return ObjectId.get().toString();
    }

    public static boolean hasLabel(HasMetadata metadata, String name, String value) {
        Map<String, String> elements = metadata.getMetadata().getLabels();
        return elements != null && Objects.equals(value, elements.get(name));
    }

    public static void setLabel(HasMetadata metadata, String name, String value) {
        KubernetesResourceUtil.getOrCreateLabels(metadata).put(name, value);
    }

    public static boolean hasAnnotation(HasMetadata metadata, String name, String value) {
        Map<String, String> elements = metadata.getMetadata().getAnnotations();
        return elements != null && Objects.equals(value, elements.get(name));
    }

    public static void setAnnotation(HasMetadata metadata, String name, String value) {
        KubernetesResourceUtil.getOrCreateAnnotations(metadata).put(name, value);
    }

    public static DeployedResource asDeployedResource(HasMetadata metadata) {
        DeployedResource answer = new DeployedResource();
        answer.setApiVersion(metadata.getApiVersion());
        answer.setKind(metadata.getKind());
        answer.setName(metadata.getMetadata().getName());
        answer.setNamespace(metadata.getMetadata().getNamespace());
        answer.setGeneration(metadata.getMetadata().getGeneration());
        answer.setResourceVersion(metadata.getMetadata().getResourceVersion());

        if (metadata.getMetadata().getAnnotations() != null) {
            String version = metadata.getMetadata().getAnnotations().get(LABEL_DEPLOYMENT_RESOURCE_VERSION);
            if (version != null) {
                answer.setDeploymentRevision(Long.parseLong(version));
            }
        }

        return answer;

    }
}