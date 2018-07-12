package com.tti.ttimediastore.dlna.dmr;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

/**
 * ConnectionManagerService在本程序中显得并不是特别重要，因为本程序采用的传递数据的
 * 方式为：dmc告知dmr uri地址，dmr通过http-get的方式，将uri所对应的实际数据传输至dmr，
 * 所以并不需要特别区分每一个连接。而在其它情景下，ConnectionManagerService就会显得很
 * 重要，例如当程序采用这种方式获取数据时：dmc告知dms采用http-post方式，将数据传输至dmr
 * 时，每个连接的连接标识就会变得很重要。
 *
 */

public class ConnectionService extends ConnectionManagerService {
	
	/**
	 * 向dmr的protocolInfo中添加协议信息，目前只支持http-get的方式，暂且
	 * 只支持MP3。协议信息的格式为（详情可以查阅ConnectionManager2.5.2章节）：
	 * <protocol>’:’ <network>’:’<contentFormat>’:’<additionalInfo>。
	 */
    public ConnectionService() {

		addVideoType();
		addAudioType();
		addImageType();
    }
    
    public ProtocolInfos getSinkProtocolInfo() {
    	return super.getSinkProtocolInfo();
    }

	private void addVideoType() {
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/3gpp:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/dl:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/dv:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/fli:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/m4v:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/mpeg:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/mp4:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/quicktime:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/vnd.mpegurl:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-la-asf:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-mng:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-ms-asf:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-ms-wm:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-ms-wmv:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-ms-wmx:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-msvideo:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-sgi-movie:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-flv:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/x-matroska:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:video/MP2T:*"));
	}

	private void addAudioType() {
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/3gpp:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/basic:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/midi:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/mpeg:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/qcp:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/mpegurl:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/prs.sid:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-aiff:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-gsm:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-mpegurl:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-ms-wma:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-ms-wax:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-pn-realaudio:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-realaudio:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-scpls:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-sd2:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/x-wav:*"));
	}

	private void addImageType() {
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/bmp:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/gif:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/ico:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/ief:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/jpeg:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/pcx:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/png:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/svg+xml:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/tiff:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/vnd.djvu:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/vnd.wap.wbmp:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-cmu-raster:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-coreldraw:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-coreldrawpattern:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-coreldrawtemplate:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-corelphotopaint:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-icon:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-jg:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-jng:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-ms-bmp:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-photoshop:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-portable-anymap:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-portable-bitmap:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-portable-graymap:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-portable-pixmap:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-rgb:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-xbitmap:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-xpixmap:*"));
		sinkProtocolInfo.add(new ProtocolInfo("http-get:*:image/x-xwindowdump:*"));
	}
}
