package com.vn.actions;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vn.K8sNotifications;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@ApplicationScoped
public class K8sNotificationsDelete {
	
	private final Logger log = LoggerFactory.getLogger(K8sNotificationsDelete.class);

	@ConfigProperty(name = "operator.service-account")
	String operatorServiceAccount;

	@ConfigProperty(name = "operator.image-pull-secrets")
	String operatorImagePullSecrets;
	

	public void deleteCR(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String name = spec.getName();
		String nameObjectDeployed = name.concat("-").concat(namespace);
		
		log.info(String.format("# borrando cr %s, namespace %s", nameObjectDeployed, namespace));
		
		K8sNotifications k8sn = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			k8sn = kubernetesClient.customResources(K8sNotifications.class).inNamespace(namespace)
					.withName(nameObjectDeployed).get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			if (k8sn != null) {
				Boolean crDeleted = kubernetesClient.customResources(K8sNotifications.class).inNamespace(namespace)
						.delete();
				log.info(String.format("# borrado cr %s, namespace %s, result %s", nameObjectDeployed,
						namespace, crDeleted));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}

	public void deleteDeployment(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String name = spec.getName();
		String nameObjectDeployed = name.concat("-").concat(namespace);
		
		log.info(String.format("# borrando deploy %s, namespace %s", nameObjectDeployed, namespace));
		
		Deployment deployment = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			deployment = kubernetesClient.apps().deployments().inNamespace(namespace)
					.withName(nameObjectDeployed).get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			if (deployment != null) {
				Boolean deploymentDeleted = kubernetesClient.apps().deployments().inNamespace(namespace)
						.withName(nameObjectDeployed).delete();
				log.info(String.format("# borrado deploy %s, namespace %s, result %s", nameObjectDeployed,
						namespace, deploymentDeleted));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void deleteConfigMap(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String name = spec.getName();
		String nameObjectDeployed = name.concat("-").concat(namespace);
		
		log.info(String.format("# borrando configmap %s, namespace %s", nameObjectDeployed, namespace));
		
		ConfigMap configMap = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			configMap = kubernetesClient.configMaps().inNamespace(namespace).withName(nameObjectDeployed)
					.get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			if (configMap != null) {
				Boolean configMapDeleted = kubernetesClient.configMaps().inNamespace(namespace)
						.withName(nameObjectDeployed).delete();
				log.info(String.format("# borrado configmap %s, namespace, %s result %s", nameObjectDeployed,
						namespace, configMapDeleted));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void deleteSecret(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String name = spec.getName();
		String nameObjectDeployed = name.concat("-").concat(namespace);
		
		log.info(String.format("# borrando secret %s, namespace %s", nameObjectDeployed, namespace));
		
		Secret secret = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			secret = kubernetesClient.secrets().inNamespace(namespace).withName(nameObjectDeployed).get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			if (secret != null) {
				Boolean secretDeleted = kubernetesClient.secrets().inNamespace(namespace).withName(nameObjectDeployed)
						.delete();
				log.info(String.format("# borrado secret %s, namespace %s, result %s", nameObjectDeployed,
						namespace, secretDeleted));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void deleteClusterRoleBinding(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		String nameObjectToDeploy = spec.getName().concat("-").concat(namespace);
		
		log.info(String.format("# borrando clusterrolebinding %s, namespace %s", nameObjectToDeploy, namespace));
		
		List<ClusterRoleBinding> clusterRoleBindings = new ArrayList<ClusterRoleBinding>();
		try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			ClusterRoleBinding clusterRoleBinding = kubernetesClient.rbac().clusterRoleBindings().withName(nameObjectToDeploy).get();
			clusterRoleBindings.add(clusterRoleBinding);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}	
		if(clusterRoleBindings.size() > 0) {			
			try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
				Boolean result = kubernetesClient.rbac().clusterRoleBindings().delete(clusterRoleBindings);
				log.info(String.format("# borrado clusterrolebinding %s, namespace %s, result %s", nameObjectToDeploy, namespace, result));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void deleteServiceAccount(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		
		log.info(String.format("# borrando serviceaccount %s, namespace %s", operatorServiceAccount, namespace));
		
		ServiceAccount sa = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			sa = kubernetesClient.serviceAccounts().inNamespace(namespace).withName(operatorServiceAccount).get();
		}
		if(sa != null) {			
			try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
				Boolean result = kubernetesClient.serviceAccounts().inNamespace(namespace).withName(operatorServiceAccount).delete();
				log.info(String.format("# borrado serviceaccount %s, namespace %s, result %s", operatorServiceAccount, namespace, result));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void deleteSecretRegistry(K8sNotifications k8sNotificationsRequest) {
		final var spec = k8sNotificationsRequest.getSpec();
		String namespace = spec.getNamespace();
		
		log.info(String.format("# borrando secretregistry %s, namespace %s", operatorImagePullSecrets, namespace));
		
		Secret secret = null;
		try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			secret = kubernetesClient.secrets().inNamespace(namespace).withName(operatorImagePullSecrets).get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		if(secret != null) {		
			try (final KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
				Boolean result = kubernetesClient.secrets().inNamespace(namespace).withName(operatorImagePullSecrets).delete();
				log.info(String.format("# borrado secretregistry %s, namespace %s, result %s", operatorImagePullSecrets, namespace, result));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
