package com.vn;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vn.actions.K8sNotificationsCreate;
import com.vn.actions.K8sNotificationsDelete;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;

@Controller
public class K8sNotificationsController implements ResourceController<K8sNotifications> {

	private final Logger log = LoggerFactory.getLogger(K8sNotificationsController.class);

	@Inject K8sNotificationsCreate k8sNotificationsCreate;
	@Inject K8sNotificationsDelete k8sNotificationsDelete;

	public K8sNotificationsController(KubernetesClient kubernetesClient) {
	}

    @Override
    public void init(EventSourceManager eventSourceManager) {
    }

	@Override
	public UpdateControl<K8sNotifications> createOrUpdateResource(K8sNotifications k8sNotificationsRequest,
			Context<K8sNotifications> context) {

		log.info("## creando recursos");
		
		createSecretRegistry(k8sNotificationsRequest);
		createServiceAccount(k8sNotificationsRequest);
		createClusterRoleBinding(k8sNotificationsRequest);

		ConfigMap configMap = createConfigMap(k8sNotificationsRequest);
		Secret secret = createSecret(k8sNotificationsRequest);

		if(configMap != null && secret != null) {
			createDeployment(k8sNotificationsRequest, configMap, secret);
			log.info("## recursos creados");
		} else {
			log.info(String.format("No se puede crear deployment %s, configMap: %s, secret: %s"), 
					k8sNotificationsRequest.getSpec().getName(), configMap, secret);
		}
		
		return UpdateControl.updateCustomResource(k8sNotificationsRequest);
	}

	@Override
	public DeleteControl deleteResource(K8sNotifications k8sNotificationsRequest, Context<K8sNotifications> context) {
		
		log.info("## eliminando recursos");
		
		deleteCR(k8sNotificationsRequest);
		deleteDeployment(k8sNotificationsRequest);
		deleteConfigMap(k8sNotificationsRequest);
		deleteSecret(k8sNotificationsRequest);
		deleteClusterRoleBinding(k8sNotificationsRequest);
		deleteServiceAccount(k8sNotificationsRequest);
		deleteSecretRegistry(k8sNotificationsRequest);
		
		log.info("## recursos eliminados");
		
		return DeleteControl.DEFAULT_DELETE;
	}

	private void createSecretRegistry(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsCreate.createSecretRegistry(k8sNotificationsRequest);	
	}
	
	private void createServiceAccount(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsCreate.createServiceAccount(k8sNotificationsRequest);
	}
	
	private void createClusterRoleBinding(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsCreate.createClusterRoleBinding(k8sNotificationsRequest);
	}
	
	private ConfigMap createConfigMap(K8sNotifications k8sNotificationsRequest) {
		return k8sNotificationsCreate.createConfigMap(k8sNotificationsRequest);
	}

	private Secret createSecret(K8sNotifications k8sNotificationsRequest) {
		return k8sNotificationsCreate.createSecret(k8sNotificationsRequest);
	}

	private void createDeployment(K8sNotifications k8sNotificationsRequest, ConfigMap configMap, Secret secret) {
		k8sNotificationsCreate.createDeployment(k8sNotificationsRequest, configMap, secret);
	}

	private void deleteCR(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteCR(k8sNotificationsRequest);		
	}

	private void deleteDeployment(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteDeployment(k8sNotificationsRequest);
	}

	private void deleteConfigMap(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteConfigMap(k8sNotificationsRequest);
	}

	private void deleteSecret(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteSecret(k8sNotificationsRequest);
	}
	
	private void deleteClusterRoleBinding(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteClusterRoleBinding(k8sNotificationsRequest);
	}
	
	private void deleteServiceAccount(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteServiceAccount(k8sNotificationsRequest);
	}
	
	private void deleteSecretRegistry(K8sNotifications k8sNotificationsRequest) {
		k8sNotificationsDelete.deleteSecretRegistry(k8sNotificationsRequest);
	}
}
