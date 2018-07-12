package com.tti.ttimediastore.dlna.dmr;

import org.fourthline.cling.model.types.UDN;

import java.util.UUID;

public class DMRUnity {

	public static final String MANUFACTURER = "TTI";
	public static UDN S_UDN = new UDN(UUID.randomUUID());
	public static UDN R_UDN = new UDN(UUID.randomUUID());
	public static final String MANUFACTURER_URL = "http://www.tti.tv/";
	public static final String DMS_MODEL_NAME = "TTIMediaStore";
	public static final String DMR_MODEL_NAME = "TTIMediaStore";
	public static final String DMS_MODEL_DESCRIPTION = DMS_MODEL_NAME;
	public static final String DMR_MODEL_DESCRIPTION = DMR_MODEL_NAME;
	public static final String MODEL_NUMBER = "v1.0";

	public static final String DMS = "MediaServer";
	public static final String DMC = "";
	public static final String DMR = "MediaRenderer";
	public static final int VERSION = 1;
}
