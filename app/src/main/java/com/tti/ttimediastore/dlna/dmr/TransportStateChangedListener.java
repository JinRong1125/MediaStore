package com.tti.ttimediastore.dlna.dmr;

import org.fourthline.cling.support.model.TransportState;

public interface TransportStateChangedListener {
	
	public void transportStateChanged(TransportState state);
}
