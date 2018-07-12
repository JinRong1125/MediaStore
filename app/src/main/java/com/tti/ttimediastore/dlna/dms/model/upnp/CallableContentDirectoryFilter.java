package com.tti.ttimediastore.dlna.dms.model.upnp;

public class CallableContentDirectoryFilter implements ICallableFilter {

	private IUpnpDevice device;

	public void setDevice(IUpnpDevice device)
	{
		this.device = device;
	}

	@Override
	public Boolean call() throws Exception
	{
		return device.asService("ContentDirectory");
	}
}
