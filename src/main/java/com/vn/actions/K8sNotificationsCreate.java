package com.vn.actions;

import java.util.Base64;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vn.K8sNotifications;
import com.vn.utils.Utils;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapEnvSource;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleRefBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;

@ApplicationScoped
public class K8sNotificationsCreate {

	private final Logger log = LoggerFactory.getLogger(K8sNotificationsCreate.class);

	@ConfigProperty(name = "operator.service-account")
	String operatorServiceAccount;

	@ConfigProperty(name = "operator.image-pull-secrets")
	String operatorImagePullSecrets;
	
	public void createSecretRegistry(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		
		log.info(String.format("# creando pullsecret %s, namespace %s", operatorImagePullSecrets, namespace));
		Map<String, Object> dockerConfig = Utils.createDockerConfig("http://quay.io", "gines1", "g1ncOlrh");
        String dockerConfigAsStr = null;
		try {
			dockerConfigAsStr = Serialization.jsonMapper().writeValueAsString(dockerConfig);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		}

        try (KubernetesClient client = new DefaultKubernetesClient()) {
            client.secrets().inNamespace(namespace).createOrReplace(new SecretBuilder()
                    .withNewMetadata().withName(operatorImagePullSecrets).endMetadata()
                    .withType("kubernetes.io/dockerconfigjson")
                    .addToData(".dockerconfigjson", Base64.getEncoder().encodeToString(dockerConfigAsStr.getBytes()))
                    .build());
            log.info(String.format("# pullsecret creado %s, namespace %s", operatorImagePullSecrets, namespace));
        } catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void createServiceAccount(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		
		log.info(String.format("# creando serviceaccount %s, namespace %s", operatorServiceAccount, namespace));
		ServiceAccount serviceAccount = new ServiceAccountBuilder()
				  .withNewMetadata().withName(operatorServiceAccount).endMetadata()
				  .withAutomountServiceAccountToken(true)
				  .addNewImagePullSecret().withName(operatorImagePullSecrets).endImagePullSecret()
				  .build();
		try (KubernetesClient client = new DefaultKubernetesClient()) {
			client.serviceAccounts().inNamespace(namespace).createOrReplace(serviceAccount);
			log.info(String.format("# serviceaccount creado %s, namespace %s", operatorServiceAccount, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void createClusterRoleBinding(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String nameObjectToDeploy = spec.getName().concat("-").concat(namespace);
		
		log.info(String.format("# creando clusterrolebinding %s, namespace %s", nameObjectToDeploy, namespace));
		ClusterRoleBinding clusterRoleBinding = new ClusterRoleBindingBuilder()
			      .withNewMetadata()
			        .withName(nameObjectToDeploy)
			      .endMetadata()
			      .addToSubjects(0, new SubjectBuilder()
			        .withApiGroup("")
			        .withKind("ServiceAccount")
			        .withName(operatorServiceAccount)
			        .withNamespace(namespace)
			        .build()
			      )
			      .withRoleRef(new RoleRefBuilder()
			        .withApiGroup("")
			        .withKind("ClusterRole")
			        .withName(operatorServiceAccount)
			        .build()
			      )
			      .build();

		try (KubernetesClient client = new DefaultKubernetesClient()) {
			    clusterRoleBinding = client.rbac().clusterRoleBindings().createOrReplace(clusterRoleBinding);
			    log.info(String.format("# clusterrolebinding creado %s, namespace %s", nameObjectToDeploy, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public ConfigMap createConfigMap(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String nameObjectToDeploy = spec.getName().concat("-").concat(namespace);

		log.info(String.format("# creando configMap %s, namespace %s", nameObjectToDeploy, namespace));

		// @formatter:off
		ConfigMap configMapBuild = new ConfigMapBuilder()
				.withNewMetadata().withName(nameObjectToDeploy).endMetadata()
				.addToData("TARGET_NAMESPACE", namespace)
				.addToData("SLACK_TOKEN", spec.getSlackToken())
				.addToData("SLACK_CHANNEL_ID", spec.getSlackChannelId())
				
				.addToData("DB_KIND", spec.getDatabase().getDbKind())
				.addToData("DB_URL", spec.getDatabase().getDbUrl())
				.addToData("DB_USERNAME", spec.getDatabase().getDbUsername())
				.addToData("DB_PASSWORD", spec.getDatabase().getDbPassword())
				.build();
		// @formatter:on
		ConfigMap configMap = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			configMap = kubernetesClient.configMaps().inNamespace(namespace).createOrReplace(configMapBuild);
			log.info(String.format("# configMap creado %s, namespace: %s", nameObjectToDeploy, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return configMap;
	}

	public Secret createSecret(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String nameObjectToDeploy = spec.getName().concat("-").concat(namespace);

		log.info(String.format("# creando secret %s namespace %s", nameObjectToDeploy, namespace));

		// @formatter:off
		Secret secretBuild = new SecretBuilder()
				.withNewMetadata().withName(nameObjectToDeploy).endMetadata()
				.addToStringData("DB_KIND", spec.getDatabase().getDbKind())
				.addToStringData("DB_URL", spec.getDatabase().getDbUrl())
				.addToStringData("DB_USERNAME", spec.getDatabase().getDbUsername())
				.addToStringData("DB_PASSWORD", spec.getDatabase().getDbPassword())
				.build();
		// @formatter:on
		Secret secret = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			secret = kubernetesClient.secrets().inNamespace(namespace).createOrReplace(secretBuild);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info(String.format("# secret creado %s, namespace: %s", nameObjectToDeploy, namespace));
		return secret;
	}

	public void createDeployment(K8sNotifications k8sNotificationsRequest, ConfigMap configMap, Secret secret) {
		final var spec = k8sNotificationsRequest.getSpec();

		String image = spec.getImage();
		String namespace = spec.getNamespace();
		String nameObjectToDeploy = spec.getName().concat("-").concat(namespace);
		log.info(String.format("# creando deployment %s, namespace %s", nameObjectToDeploy, namespace));

		EnvFromSource envFromSource = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			envFromSource = new EnvFromSource(
					new ConfigMapEnvSource(configMap.getMetadata().getName(), false), null, null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
//		try {
//			Thread.currentThread().sleep(1000);
//		} catch (InterruptedException e) {
//			log.error(e.getMessage(), e);
//		}
//		Deployment deployment = null;
//		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
//			deployment = kubernetesClient.apps().deployments().inNamespace(namespace)
//					.withName(nameObjectToDeploy).get();
//			log.info(String.format("comprobando existencia deployment %s en namespace %s", nameObjectToDeploy, namespace));
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//		try {
//			Thread.currentThread().sleep(1000);
//		} catch (InterruptedException e) {
//			log.error(e.getMessage(), e);
//		}
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
//			if (deployment == null) {
			// @formatter:off
			Deployment deployment = new DeploymentBuilder()
				.withNewMetadata()
					.withName(nameObjectToDeploy)
					.addToLabels("app",nameObjectToDeploy)
				.endMetadata()
				.withNewSpec()
					.withReplicas(1)
					.withNewTemplate()
						.withNewMetadata()
						.addToLabels("app", nameObjectToDeploy)
						.endMetadata()
						.withNewSpec()
							.withServiceAccount(operatorServiceAccount)
							.withImagePullSecrets(new LocalObjectReferenceBuilder().withName(operatorImagePullSecrets).build())
							.addNewContainer()
								.withName(nameObjectToDeploy)
								.withImage(image)
								.withImagePullPolicy("Always")
								.addNewPort().withContainerPort(8080).endPort()
								.withEnvFrom(envFromSource)
							.endContainer()
						.endSpec()
					.endTemplate()
					.withNewSelector()
						.addToMatchLabels("app",nameObjectToDeploy)
					.endSelector()
				.endSpec()
			.build();
			// @formatter:on
			kubernetesClient.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
			log.info(String.format("# deploy creado %s, namespace %s", nameObjectToDeploy, namespace));
//			} else {
//				log.info("recurso previamente creado");
//			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
}
