package se.kth.ssvl.tslab.wsn.general.servlib.routing.prophet;

import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleDaemon;
import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol.status_report_reason_t;
import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.BundleDeleteRequest;
import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.BundleDeliveredEvent;
import se.kth.ssvl.tslab.wsn.general.servlib.naming.endpoint.EndpointIDPattern;
import se.kth.ssvl.tslab.wsn.general.servlib.reg.Registration;
import se.kth.ssvl.tslab.wsn.general.bpf.BPF;

public class ProphetRegistration extends Registration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG = "ProphetRegistration";
	private ProphetBundleRouter router_;

	public ProphetRegistration(ProphetBundleRouter router_) {
		super(PROPHET_REGID, new EndpointIDPattern(router_.localEid()
				+ "/prophet"), Registration.failure_action_t.DEFER, 0, 0, "");
		this.router_ = router_;
		set_active(true);
	}

	@Override
	public void deliver_bundle(Bundle bundle) {
		BPF.getInstance().getBPFLogger().debug(TAG,
				"Prophet bundle from " + bundle.source());
		router_.deliver_bundle(bundle);
		BundleDaemon.getInstance().post_at_head(
				new BundleDeliveredEvent(bundle, this));
		BundleDaemon.getInstance().post_at_head(
				new BundleDeleteRequest(bundle,
						status_report_reason_t.REASON_NO_ADDTL_INFO));
	}

}
