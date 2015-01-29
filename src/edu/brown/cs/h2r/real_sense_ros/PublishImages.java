package edu.brown.cs.h2r.real_sense_ros;

import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMCapture.Device;
import intel.rssdk.PXCMCapture.Device.StreamProfile;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.pxcmStatus;
import ros.Publisher;
import ros.RosBridge;

public class PublishImages {
	public static void main(String args[]) {
		System.out.println("Starting image publisher.");
		if (args.length != 1) {
			System.err.println("Usage: RealSenseRos ws://ROSBRIDGE_URL:9090");
		}
		System.out.println("Connecting to rosbridge at " + args[0]);
		RosBridge bridge = RosBridge.createConnection(args[0]);
		bridge.waitForConnection();
		Publisher rgbpub = new Publisher("/rssdk/camera/image_rgb", "sensor_msgs/Image", bridge);
		
		// Create session
		PXCMSession session = PXCMSession.CreateInstance();
		if (session == null) {
			System.out.print("Failed to create a session instance\n");
			System.exit(3);
		}
		Device device = null;	
		StreamProfile sp = null;
		
		PXCMSenseManager senseMgr = session.CreateSenseManager();
		senseMgr.Init();
		//PXCMSenseManager senseMgr = PXCMSenseManager.CreateInstance();
		
		if (senseMgr == null) {
			System.out.print("Failed to create a SenseManager instance\n");
			System.exit(3);
		}

		System.out.println("Enabling stream");
		
		//senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, 640, 480, 30);
		senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, 10, 10, 30);
		//senseMgr.EnableStream(StreamType.STREAM_TYPE_ANY, 0, 0, 0);

		//senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_DEPTH,320,240,60);

		System.out.println("Init");
		senseMgr.Init();
		System.out.println("Looping");
		while (true) {
			System.out.println("Inside loop");
			if (senseMgr.AcquireFrame(true).compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
				System.err.println("Error acquiring frame");
				break;
			}

			PXCMCapture.Sample sample=senseMgr.QuerySample();
			senseMgr.ReleaseFrame();
			   
		}
	}
		

}
