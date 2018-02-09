package org.restcomm.slee.resource.http.heartbeat;

public abstract interface HttpLoadBalancerHeartBeatingListener {

	public abstract void loadBalancerAdded(HttpLoadBalancer paramLoadBalancer);

	public abstract void loadBalancerRemoved(HttpLoadBalancer paramLoadBalancer);

	public abstract void pingingloadBalancer(HttpLoadBalancer paramLoadBalancer);

	public abstract void pingedloadBalancer(HttpLoadBalancer paramLoadBalancer);
	
}
