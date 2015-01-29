package edu.brown.cs.h2r.real_sense_ros;

import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMCapture.Device;
import intel.rssdk.PXCMCapture.Device.StreamOption;
import intel.rssdk.PXCMCapture.Device.StreamProfile;
import intel.rssdk.PXCMCapture.Sample;
import intel.rssdk.PXCMCapture.StreamType;
import intel.rssdk.PXCMCaptureManager;
import intel.rssdk.PXCMImage;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.PXCMVideoModule;
import intel.rssdk.pxcmStatus;

import java.util.EnumSet;

import ros.Publisher;
import ros.RosBridge;

public class PublishImages {
	public static void main(String[] args) {
		System.out.println("Starting image publisher.");
		if (args.length != 1) {
			System.err.println("Usage: RealSenseRos ws://ROSBRIDGE_URL:9090");
		}
		System.out.println("Connecting to rosbridge at " + args[0]);
		RosBridge bridge = RosBridge.createConnection(args[0]);
		bridge.waitForConnection();
		Publisher strpub = new Publisher("/bridge", "std_msgs/String", bridge);

		Publisher markerpub = new Publisher("/rssdk/hand_marker",
				"visualization_msgs/Marker", bridge);

		// Create session
		PXCMSession session = PXCMSession.CreateInstance();
		if (session == null) {
			System.out.print("Failed to create a session instance\n");
			System.exit(3);
		}



		PXCMSenseManager senseMgr;
		//senseMgr = session.CreateSenseManager();
		senseMgr = PXCMSenseManager.CreateInstance();
		if (senseMgr == null) {
			System.out.print("Failed to create a SenseManager instance\n");
			System.exit(3);
		}

		PXCMCaptureManager captureMgr = senseMgr.QueryCaptureManager();

		// captureMgr.FilterByDeviceInfo("RealSense", null, 0);

		pxcmStatus sts;

		System.out.println("Enable stream");

		PXCMVideoModule.DataDesc ddesc = new PXCMVideoModule.DataDesc();
		ddesc.deviceInfo.streams = StreamType.STREAM_TYPE_COLOR;
		int width = 640;
		int height = 480;
		int fps = 30;

		EnumSet<StreamOption> set = EnumSet.of(StreamOption.STREAM_OPTION_ANY);
		set = EnumSet.noneOf(StreamOption.class);

		// ddesc.streams.color.options = set;

		// ddesc.streams.color.sizeMin.width = ddesc.streams.color.sizeMax.width
		// = width;
		// ddesc.streams.color.sizeMin.height =
		// ddesc.streams.color.sizeMax.height = height;
		// ddesc.streams.color.frameRate.min = fps;
		// ddesc.streams.color.frameRate.max = fps;

		// sts = senseMgr.EnableStreams(ddesc);

		//senseMgr.EnableStream(PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32, 0, 0);
		senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR,0,0,0);

		System.out.println("Done enabling stream");

		// if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
		// System.out.print("Failed to enable HandAnalysis\n");
		// System.exit(3);
		// }

		sts = senseMgr.Init();
		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
			Device device = captureMgr.QueryDevice();

			Sample sample = new Sample();
			device.ReadStreams(PXCMCapture.StreamType.STREAM_TYPE_COLOR, sample);

		} else {
			System.err.println("Error: " + sts);
		}
		System.out.println("Exiting");
	}

	public static void main1(String args[]) {
		System.out.println("Starting image publisher.");
		if (args.length != 1) {
			System.err.println("Usage: RealSenseRos ws://ROSBRIDGE_URL:9090");
		}
		System.out.println("Connecting to rosbridge at " + args[0]);
		RosBridge bridge = RosBridge.createConnection(args[0]);
		bridge.waitForConnection();
		Publisher rgbpub = new Publisher("/rssdk/camera/image_rgb",
				"sensor_msgs/Image", bridge);

		// Create session
		PXCMSession session = PXCMSession.CreateInstance();
		if (session == null) {
			System.out.print("Failed to create a session instance\n");
			System.exit(3);
		}
		Device device = null;

		StreamProfile sp = null;

		PXCMSenseManager senseMgr = session.CreateSenseManager();
		PXCMCaptureManager captureMgr = senseMgr.QueryCaptureManager();
		captureMgr.FilterByDeviceInfo("RealSense", null, 0);

		System.out.println("Connected:  " + senseMgr.IsConnected());

		// PXCMSenseManager senseMgr = PXCMSenseManager.CreateInstance();

		if (senseMgr == null) {
			System.out.print("Failed to create a SenseManager instance\n");
			System.exit(3);
		}

		// senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, 640,
		// 480, 30);

		// senseMgr.EnableStream(StreamType.STREAM_TYPE_ANY, 0, 0, 0);

		// senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_DEPTH,320,240,60);

		System.out.println("Init");
		pxcmStatus result = senseMgr.Init();
		System.out.println("Init: " + result);
		System.out.println("Looping");

		System.out.println("Enabling stream");
		senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, 640,
				480, 30);

		while (true) {
			System.out.println("Inside loop");
			if (senseMgr.AcquireFrame(true).compareTo(
					pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
				System.err.println("Error acquiring frame");
				break;
			}

			PXCMCapture.Sample sample = senseMgr.QuerySample();
			senseMgr.ReleaseFrame();

		}
	}

}
