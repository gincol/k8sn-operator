package com.vn;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("operators.vn.com")
@Kind("K8sNotifications")
@Plural("k8snotifications")
@ShortNames("k8sn")
public class K8sNotifications extends CustomResource<K8sNotificationsSpec, K8sNotificationsStatus>
		implements Namespaced {

	private static final long serialVersionUID = 1L;
	private K8sNotificationsSpec spec;
	private K8sNotificationsStatus status;

	public K8sNotificationsSpec getSpec() {
		return spec;
	}

	public void setSpec(K8sNotificationsSpec spec) {
		this.spec = spec;
	}

	public K8sNotificationsStatus getStatus() {
		return status;
	}

	public void setStatus(K8sNotificationsStatus status) {
		this.status = status;
	}

}
