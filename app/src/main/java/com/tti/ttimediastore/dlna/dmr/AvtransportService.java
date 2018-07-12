package com.tti.ttimediastore.dlna.dmr;

import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.util.Xml;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tti.ttimediastore.activity.ContentActivity;
import com.tti.ttimediastore.application.TTIMediaStore;
import com.tti.ttimediastore.constants.Constants;
import com.tti.ttimediastore.constants.Constants.MEDIA_TYPE;
import com.tti.ttimediastore.model.Image;
import com.tti.ttimediastore.model.Video;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.TransportStatus;
import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * 这是dmr的核心service，它继承自AbstractAVTransportService，并重写父类中的抽象方法
 * 来和实际的player交互，它实现了一个状态改变的监听器：TransportStateChangedListener，
 * 该监听器的作用是监听player需要上报给dmc的一些状态变化，例如，player数据准备完成开始播放。
 */
public class AvtransportService extends AbstractAVTransportService
		implements TransportStateChangedListener, DMCStateListener.OnStateChangedListener {
	private static final String LOG_TAG = AvtransportService.class
			.getSimpleName();
	private static final String DEFAULT_SPEED = "1";

	private SimpleExoPlayer mPlayer;
	private DMRPlayerController playerController;

	private MediaInfo mMediaInfo = new MediaInfo();
	private TransportInfo mTransportInfo = new TransportInfo(
			TransportState.NO_MEDIA_PRESENT, TransportStatus.OK, DEFAULT_SPEED);
	private TransportSettings mTransportSettings = new TransportSettings();
	private DeviceCapabilities mDeviceCapabilities = new DeviceCapabilities(
			new StorageMedium[] { StorageMedium.NETWORK });
	private TransportAction[] mTransportActions = new TransportAction[] {};

	public AvtransportService(LastChange lastChange) {
		super(lastChange);
		playerController = DMRPlayerController.getInstance();
		DMCStateListener.getInstance().setListener(this);
	}

	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		return new UnsignedIntegerFourBytes[] { getDefaultInstanceID() };
	}

	@Override
	protected TransportAction[] getCurrentTransportActions(
			UnsignedIntegerFourBytes arg0) throws Exception {
		return mTransportActions;
	}

	@Override
	@UpnpAction(out = {
			@UpnpOutputArgument(name = "PlayMedia", stateVariable = "PossiblePlaybackStorageMedia", getterName = "getPlayMediaString"),
			@UpnpOutputArgument(name = "RecMedia", stateVariable = "PossibleRecordStorageMedia", getterName = "getRecMediaString"),
			@UpnpOutputArgument(name = "RecQualityModes", stateVariable = "PossibleRecordQualityModes", getterName = "getRecQualityModesString") })
	public DeviceCapabilities getDeviceCapabilities(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		return mDeviceCapabilities;
	}

	@Override
	@UpnpAction(out = {
			@UpnpOutputArgument(name = "NrTracks", stateVariable = "NumberOfTracks", getterName = "getNumberOfTracks"),
			@UpnpOutputArgument(name = "MediaDuration", stateVariable = "CurrentMediaDuration", getterName = "getMediaDuration"),
			@UpnpOutputArgument(name = "CurrentURI", stateVariable = "AVTransportURI", getterName = "getCurrentURI"),
			@UpnpOutputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData", getterName = "getCurrentURIMetaData"),
			@UpnpOutputArgument(name = "NextURI", stateVariable = "NextAVTransportURI", getterName = "getNextURI"),
			@UpnpOutputArgument(name = "NextURIMetaData", stateVariable = "NextAVTransportURIMetaData", getterName = "getNextURIMetaData"),
			@UpnpOutputArgument(name = "PlayMedium", stateVariable = "PlaybackStorageMedium", getterName = "getPlayMedium"),
			@UpnpOutputArgument(name = "RecordMedium", stateVariable = "RecordStorageMedium", getterName = "getRecordMedium"),
			@UpnpOutputArgument(name = "WriteStatus", stateVariable = "RecordMediumWriteStatus", getterName = "getWriteStatus") })
	public MediaInfo getMediaInfo(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		return mMediaInfo;
	}

	@Override
	@UpnpAction(out = {
			@UpnpOutputArgument(name = "Track", stateVariable = "CurrentTrack", getterName = "getTrack"),
			@UpnpOutputArgument(name = "TrackDuration", stateVariable = "CurrentTrackDuration", getterName = "getTrackDuration"),
			@UpnpOutputArgument(name = "TrackMetaData", stateVariable = "CurrentTrackMetaData", getterName = "getTrackMetaData"),
			@UpnpOutputArgument(name = "TrackURI", stateVariable = "CurrentTrackURI", getterName = "getTrackURI"),
			@UpnpOutputArgument(name = "RelTime", stateVariable = "RelativeTimePosition", getterName = "getRelTime"),
			@UpnpOutputArgument(name = "AbsTime", stateVariable = "AbsoluteTimePosition", getterName = "getAbsTime"),
			@UpnpOutputArgument(name = "RelCount", stateVariable = "RelativeCounterPosition", getterName = "getRelCount"),
			@UpnpOutputArgument(name = "AbsCount", stateVariable = "AbsoluteCounterPosition", getterName = "getAbsCount") })
	public PositionInfo getPositionInfo(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		
		PositionInfo positionInfo;
		if (mPlayer == null) {
			positionInfo = new PositionInfo();
		} else {

			String currentPosition = ModelUtil
					.toTimeString(mPlayer.getCurrentPosition() / 1000);
			String trackDuration = ModelUtil
					.toTimeString(mPlayer.getDuration() / 1000);

			positionInfo = new PositionInfo(1, trackDuration,
					mMediaInfo.getCurrentURIMetaData(),
					mMediaInfo.getCurrentURI(), currentPosition,
					currentPosition, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}
		return positionInfo;
	}

	@Override
	@UpnpAction(out = {
			@UpnpOutputArgument(name = "CurrentTransportState", stateVariable = "TransportState", getterName = "getCurrentTransportState"),
			@UpnpOutputArgument(name = "CurrentTransportStatus", stateVariable = "TransportStatus", getterName = "getCurrentTransportStatus"),
			@UpnpOutputArgument(name = "CurrentSpeed", stateVariable = "TransportPlaySpeed", getterName = "getCurrentSpeed") })
	public TransportInfo getTransportInfo(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		/**
		 * 使用bubbleUpnp测试时，它会一直获取该信息，而使用skifta却并不会如此，
		 * 难道是我回个dmc的消息有误，导致bubble无法获取想要的信息，而重复获取？ 现在还不太清楚。
		 */
		return mTransportInfo;
	}

	@Override
	@UpnpAction(out = {
			@UpnpOutputArgument(name = "PlayMode", stateVariable = "CurrentPlayMode", getterName = "getPlayMode"),
			@UpnpOutputArgument(name = "RecQualityMode", stateVariable = "CurrentRecordQualityMode", getterName = "getRecQualityMode") })
	public TransportSettings getTransportSettings(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		return mTransportSettings;
	}

	@Override
	@UpnpAction
	public void setAVTransportURI(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
			@UpnpInputArgument(name = "CurrentURI", stateVariable = "AVTransportURI") String arg1,
			@UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2)
					throws AVTransportException {
/*
		// 检查传递过来的uri是否符合规则并且有效
		URI uri = null;
		if (arg1.startsWith("http")) {
			try {
				uri = new URI(arg1);
				HttpFetch.validate(URIUtil.toURL(uri));
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new AVTransportException(ErrorCode.INVALID_ARGS,
						"CurrentURI can not be null or malformed");
			} catch (IOException e) {
				e.printStackTrace();
				throw new AVTransportException(AVTransportErrorCode.READ_ERROR,
						"Unable to read requested URI: " + uri);
			}
		} else
			throw new AVTransportException(ErrorCode.INVALID_ARGS,
					"Requested URI was not a network stream.");
*/
/*
		// 通过分析得到的media类型，来生成或者调用不同的播放器
		MEDIA_TYPE type = getMediaType(arg2);
		mPlayer = PlayerBuilder.build(this, type);
		mPlayer.setUri(arg1);
		mMediaInfo = new MediaInfo(arg1, arg2);
*/
		if (Patterns.WEB_URL.matcher(arg1).matches() && arg1.startsWith("http")) {
			try {
				new URI(arg1);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new AVTransportException(ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed");
			}
		} else
			throw new AVTransportException(ErrorCode.INVALID_ARGS, "Requested URI was not a network stream.");

		MEDIA_TYPE type = getMediaType(arg2);
		Metadata metadata = xmlToMetadata(arg2);
		mMediaInfo = new MediaInfo(arg1, arg2);

		switch (type) {
			case VIDEO:
				setDMRVideo(metadata, arg1);
				break;
			case AUDIO:
				setDMRVideo(metadata, arg1);
				break;
			case IMAGE:
				setDMRImage(metadata, arg1);
				break;
		}

		/**
		 * 通知DMC关于DMR状态的改变，这个非常重要，所有会引起DMR状态改变的操作，
		 * 都需要在DMR完成操作后主动通知DMC该操作已经改变DMR的状态，以便DMC能够
		 * 准确掌握DMR的实时状态。具体说明可以参阅UPnP-av-AVTransport-v1-Service 2.5节。
		 */
/*
		mTransportInfo = new TransportInfo(TransportState.STOPPED,
				TransportStatus.OK, DEFAULT_SPEED);
		mTransportActions = new TransportAction[] { TransportAction.Play };
		getLastChange().setEventedValue(getDefaultInstanceID(),
				new AVTransportVariable.AVTransportURI(uri),
				new AVTransportVariable.AVTransportURIMetaData(arg2),
				new AVTransportVariable.TransportState(TransportState.STOPPED),
				new AVTransportVariable.CurrentTransportActions(
						mTransportActions));

		// 这里休息1秒的原因是：可能是通过cling频繁的发送lastchange，会导致某些lastchang 无法接收到。
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		getLastChange().fire(getPropertyChangeSupport());
*/
	}

	@Override
	@UpnpAction
	public void play(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
			@UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String arg1)
					throws AVTransportException {

		Log.e(LOG_TAG, "Start to play!");
		if (mPlayer != null) {
			mTransportInfo = new TransportInfo(TransportState.PLAYING,
					TransportStatus.OK, DEFAULT_SPEED);
			mTransportActions = new TransportAction[] { TransportAction.Pause,
					TransportAction.Seek, TransportAction.Stop };
			getLastChange().setEventedValue(getDefaultInstanceID(),
					new AVTransportVariable.TransportState(
							TransportState.PLAYING),
					new AVTransportVariable.CurrentTransportActions(
							mTransportActions));
			getLastChange().fire(getPropertyChangeSupport());
			playerController.play();
		} else {
			mTransportInfo = new TransportInfo(TransportState.NO_MEDIA_PRESENT,
					TransportStatus.OK, DEFAULT_SPEED);
			throw new AVTransportException(ErrorCode.INVALID_ACTION,
					"No player created - try setting URI of media first.");
		}
	}

	@Override
	@UpnpAction
	public void pause(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		
		Log.e(LOG_TAG, "Start to pause!");
		if (mPlayer != null) {
			mTransportInfo = new TransportInfo(TransportState.PAUSED_PLAYBACK,
					TransportStatus.OK, DEFAULT_SPEED);
			mTransportActions = new TransportAction[] { TransportAction.Play,
					TransportAction.Seek, TransportAction.Stop };
			getLastChange().setEventedValue(getDefaultInstanceID(),
					new AVTransportVariable.TransportState(
							TransportState.PAUSED_PLAYBACK),
					new AVTransportVariable.CurrentTransportActions(
							mTransportActions));
			getLastChange().fire(getPropertyChangeSupport());
			playerController.pause();
		} else {
			mTransportInfo = new TransportInfo(TransportState.NO_MEDIA_PRESENT,
					TransportStatus.OK, DEFAULT_SPEED);
			throw new AVTransportException(ErrorCode.INVALID_ACTION,
					"No player created - try setting URI of media first.");
		}
	}

	@Override
	@UpnpAction
	public void seek(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
			@UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String arg1,
			@UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String arg2)
					throws AVTransportException {
		
		if (mPlayer != null) {
			if (!arg1.equalsIgnoreCase(SeekMode.REL_TIME.toString()))
				throw new AVTransportException(ErrorCode.INVALID_ARGS,
						"Only suport REL_TIME seek mode.");
/*
			mTransportInfo = new TransportInfo(TransportState.TRANSITIONING,
					TransportStatus.OK, DEFAULT_SPEED);
			getLastChange().setEventedValue(getDefaultInstanceID(),
					new AVTransportVariable.TransportState(
							TransportState.TRANSITIONING));
			getLastChange().fire(getPropertyChangeSupport());
*/
			playerController.seek(ModelUtil.fromTimeString(arg2) * 1000);
		} else {
			mTransportInfo = new TransportInfo(TransportState.NO_MEDIA_PRESENT,
					TransportStatus.OK, DEFAULT_SPEED);
			throw new AVTransportException(ErrorCode.INVALID_ACTION,
					"No player created - try setting URI of media first.");
		}

	}

	@Override
	@UpnpAction
	public void stop(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
		
		Log.e(LOG_TAG, "Start to stop!");
		if (mPlayer != null) {
			mTransportInfo = new TransportInfo(TransportState.STOPPED,
					TransportStatus.OK, DEFAULT_SPEED);
			mTransportActions = new TransportAction[] { TransportAction.Play,
					TransportAction.Seek };
			getLastChange().setEventedValue(getDefaultInstanceID(),
					new AVTransportVariable.TransportState(
							TransportState.STOPPED),
					new AVTransportVariable.CurrentTransportActions(
							mTransportActions));
			getLastChange().fire(getPropertyChangeSupport());
			playerController.stop();
		} else {
			mTransportInfo = new TransportInfo(TransportState.NO_MEDIA_PRESENT,
					TransportStatus.OK, DEFAULT_SPEED);
			throw new AVTransportException(ErrorCode.INVALID_ACTION,
					"No player created - try setting URI of media first.");
		}
	}

	@Override
	@UpnpAction
	public void previous(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
	}

	@Override
	@UpnpAction
	public void next(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
	}

	@Override
	@UpnpAction
	public void record(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0)
					throws AVTransportException {
	}

	@Override
	@UpnpAction
	public void setNextAVTransportURI(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
			@UpnpInputArgument(name = "NextURI", stateVariable = "AVTransportURI") String arg1,
			@UpnpInputArgument(name = "NextURIMetaData", stateVariable = "AVTransportURIMetaData") String arg2)
					throws AVTransportException {
	}

	@Override
	@UpnpAction
	public void setPlayMode(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
			@UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String arg1)
					throws AVTransportException {
	}

	@Override
	@UpnpAction
	public void setRecordQualityMode(
			@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes arg0,
			@UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String arg1)
					throws AVTransportException {
	}

	/**
	 * 通过从DMC传递过来的media详细信息metadata，来分析需要播放的media的类型(video、audio或者image)，
	 * metadata中通过upnp
	 * :class来判断media类型，upnp:class中会包含audioItem、videoItem或者imageItem
	 * 的字眼来判别media的类型(具体说明可以查看UPnP-av-ContentDirectory-v1-Service)中的2.4节.
	 * 
	 * @param metadata
	 *            :通过DMC从DMS上获取到的关于media的详细信息，该详细信息是以XML的形式传递。
	 * @return 返回媒体类型，audio、video或者image
	 */
	private MEDIA_TYPE getMediaType(String metadata) {
		String itemClass = "";
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(metadata));
			int eventType = parser.getEventType();

			loop: while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:

					break;
				case XmlPullParser.START_TAG:
					String tag = parser.getName();
					if (tag.equalsIgnoreCase("class")) {
						itemClass = parser.nextText().toLowerCase();
						break loop;
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				default:
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		if (itemClass.contains(Constants.AUDIO_PREFIX))
			return Constants.MEDIA_TYPE.AUDIO;
		else if (itemClass.contains(Constants.VIDEO_PREFIX))
			return MEDIA_TYPE.VIDEO;
		else
			return MEDIA_TYPE.IMAGE;
	}

	private Metadata xmlToMetadata(String xml){
		Metadata metadata = new Metadata();
		if (xml != null) {
			XmlPullParser parser = Xml.newPullParser();
			if (parser != null) {
				try {
					String title = null;
					String creator = null;
					String artist = null;
					String album = null;
					String albumArt = null;
					String duration = null;
					String size = null;

					parser.setInput(new StringReader(xml));
					int eventType = parser.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
							case XmlPullParser.START_DOCUMENT:

								break;
							case XmlPullParser.START_TAG:
								String tag = parser.getName();
								if (tag.equalsIgnoreCase("title"))
									title = parser.nextText();
								else if (tag.equalsIgnoreCase("creator"))
									creator = parser.nextText();
								else if (tag.equalsIgnoreCase("artist"))
									artist = parser.nextText();

								else if (tag.equalsIgnoreCase("album"))
									album = parser.nextText();
								else if (tag.equalsIgnoreCase("albumArtURI"))
									albumArt = parser.nextText();
								else if (tag.equalsIgnoreCase("res")) {
									duration = parser.getAttributeValue(null,
											"duration");
									size = parser.getAttributeValue(null, "size");
								}
								break;

							case XmlPullParser.END_TAG:
								break;

							default:
								break;
						}
						eventType = parser.next();
					}
					if (metadata != null) {
						metadata.setTitle(title);
						metadata.setCreator(creator);
						metadata.setArtist(artist);
						metadata.setAlbum(album);
						metadata.setAlbumArt(albumArt);
						metadata.setSize(size);
						metadata.setDuration(duration);
					}
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return metadata;
	}

	/**
	 * 这个监听器用来接收player传递过来的状态变化，以便DMR能够将这些状态 变化及时传递给DMC。
	 */
	@Override
	public void transportStateChanged(TransportState state) {
		
		Log.e(LOG_TAG, "State has changed:" + state);
		mTransportInfo = new TransportInfo(state, TransportStatus.OK,
				DEFAULT_SPEED);

		switch (state) {
		case PLAYING:
			mTransportActions = new TransportAction[] { TransportAction.Pause,
					TransportAction.Stop, TransportAction.Seek,
					TransportAction.Next, TransportAction.Previous };
			break;
		case PAUSED_PLAYBACK:
			mTransportActions = new TransportAction[] { TransportAction.Play,
					TransportAction.Stop, TransportAction.Seek,
					TransportAction.Next, TransportAction.Previous };
			break;
		case STOPPED:
			mTransportActions = new TransportAction[] { TransportAction.Play};
			break;
		case TRANSITIONING:
			mTransportActions = new TransportAction[] {};
			break;
		}
		getLastChange().setEventedValue(getDefaultInstanceID(),
				new AVTransportVariable.TransportState(state),
				new AVTransportVariable.CurrentTransportActions(
						mTransportActions));
		getLastChange().fire(getPropertyChangeSupport());
	}

	private void setDMRVideo(Metadata metadata, String uri) {
		Constants.MODEL_TYPE = Constants.INPUT_MEDIA;
		Video video = new Video();
		video.setTitle(metadata.getTitle());
		video.setPath(uri);
		Intent intent = new Intent(TTIMediaStore.getInstance(), ContentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Constants.ITEM_CONTENT, video);
		TTIMediaStore.getInstance().startActivity(intent);
	}

	private void setDMRImage(Metadata metadata, String uri) {
		Constants.MODEL_TYPE = Constants.INPUT_IMAGE;
		Image image = new Image();
		image.setId("0");
		image.setTitle(metadata.getTitle());
		image.setPath(uri);
		ArrayList<Image> imageList = new ArrayList<>();
		imageList.add(image);
		Intent intent = new Intent(TTIMediaStore.getInstance(), ContentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Constants.ITEM_CONTENT, imageList);
		TTIMediaStore.getInstance().startActivity(intent);
	}

	@Override
	public void onStreamReady(SimpleExoPlayer player) {
		mPlayer = player;
	}

	@Override
	public void onPlay() {
		mTransportInfo = new TransportInfo(TransportState.PLAYING,
				TransportStatus.OK, DEFAULT_SPEED);
		mTransportActions = new TransportAction[] { TransportAction.Pause,
				TransportAction.Seek, TransportAction.Stop };
		getLastChange().setEventedValue(getDefaultInstanceID(),
				new AVTransportVariable.TransportState(
						TransportState.PLAYING),
				new AVTransportVariable.CurrentTransportActions(
						mTransportActions));
		getLastChange().fire(getPropertyChangeSupport());
	}

	@Override
	public void onPause() {
		mTransportInfo = new TransportInfo(TransportState.PAUSED_PLAYBACK,
				TransportStatus.OK, DEFAULT_SPEED);
		mTransportActions = new TransportAction[] { TransportAction.Play,
				TransportAction.Seek, TransportAction.Stop };
		getLastChange().setEventedValue(getDefaultInstanceID(),
				new AVTransportVariable.TransportState(
						TransportState.PAUSED_PLAYBACK),
				new AVTransportVariable.CurrentTransportActions(
						mTransportActions));
		getLastChange().fire(getPropertyChangeSupport());
	}

	@Override
	public void onStop() {
		mPlayer = null;
		mTransportInfo = new TransportInfo(TransportState.STOPPED,
				TransportStatus.OK, DEFAULT_SPEED);
		mTransportActions = new TransportAction[] { TransportAction.Play,
				TransportAction.Seek };
		getLastChange().setEventedValue(getDefaultInstanceID(),
				new AVTransportVariable.TransportState(
						TransportState.STOPPED),
				new AVTransportVariable.CurrentTransportActions(
						mTransportActions));
		getLastChange().fire(getPropertyChangeSupport());
	}
}
