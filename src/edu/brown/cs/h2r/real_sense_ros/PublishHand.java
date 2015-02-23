package edu.brown.cs.h2r.real_sense_ros;

import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMCaptureManager;
import intel.rssdk.PXCMHandConfiguration;
import intel.rssdk.PXCMHandData;
import intel.rssdk.PXCMHandModule;
import intel.rssdk.PXCMImage;
import intel.rssdk.PXCMImageShared.Access;
import intel.rssdk.PXCMPoint3DF32;
import intel.rssdk.PXCMPointF32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.pxcmStatus;

import java.util.HashMap;
import java.util.Map;

import ros.Publisher;
import ros.RosBridge;

public class PublishHand {
	public static void main(String args[]) {
		System.out.println("Starting hand tracker.");
		if (args.length != 1) {
			System.err.println("Usage: RealSenseRos ws://ROSBRIDGE_URL:9090");
		}
		System.out.println("Connecting to rosbridge at " + args[0]);
		RosBridge bridge = RosBridge.createConnection(args[0]);
		bridge.waitForConnection();
		Publisher strpub = new Publisher("/bridge", "std_msgs/String", bridge);
		
		Publisher markerpub = new Publisher("/rssdk/hand_marker", "visualization_msgs/Marker", bridge);
		
		// Create session
		PXCMSession session = PXCMSession.CreateInstance();
		if (session == null) {
			System.out.print("Failed to create a session instance\n");
			System.exit(3);
		}

		PXCMSenseManager senseMgr = session.CreateSenseManager();
		if (senseMgr == null) {
			System.out.print("Failed to create a SenseManager instance\n");
			System.exit(3);
		}

		PXCMCaptureManager captureMgr = senseMgr.QueryCaptureManager();
		captureMgr.FilterByDeviceInfo("RealSense", null, 0);

		pxcmStatus sts = senseMgr.EnableHand(null);
		
		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
			System.out.print("Failed to enable HandAnalysis\n");
			System.exit(3);
		}

		sts = senseMgr.Init();
		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
			PXCMHandModule handModule = senseMgr.QueryHand();
			PXCMHandConfiguration handConfig = handModule
					.CreateActiveConfiguration();
			handConfig.EnableAllGestures();
			handConfig.EnableAllAlerts();
			handConfig.ApplyChanges();
			handConfig.Update();

			PXCMHandData handData = handModule.CreateOutput();
			int nframes = 0;
			while (true) {
				nframes++;
				sts = senseMgr.AcquireFrame(true);
				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
					System.err.println("Error acquiring frame: " + sts);
					break;
				}

				PXCMCapture.Sample sample = senseMgr.QueryHandSample();
				//HashMap<String, Object> imageData = new HashMap<String, Object>();
				
				//PXCMImage.ImageData data = new PXCMImage.ImageData();
				//sample.depth.AcquireAccess(Access.ACCESS_READ, data);
				//System.out.println("Data: " + data.format);
				//System.out.println("Data pitch lengtH: " + data.pitches.length);
				//System.out.println("Data p0: " + data.pitches[0]);
				//System.out.println("Data p1: " + data.pitches[1]);
				//System.out.println("Data p2: " + data.pitches[2]);
				//System.out.println("Data p3: " + data.pitches[3]);
					
					
				// Query and Display Joint of Hand or Palm
				handData.Update();

				PXCMHandData.IHand hand = new PXCMHandData.IHand();
				sts = handData.QueryHandData(
						PXCMHandData.AccessOrderType.ACCESS_ORDER_NEAR_TO_FAR,
						0, hand);

				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) >= 0) {
					PXCMPointF32 image = hand.QueryMassCenterImage();
					PXCMPoint3DF32 world = hand.QueryMassCenterWorld();
					

					System.out
							.println("Palm Center at frame " + nframes + ": ");
					System.out.print("   Image Position: (" + image.x + ","
							+ image.y + ")");
					System.out.println("   World Position: (" + world.x + ","
							+ world.y + "," + world.z + ")");
					final Map<String, String> strData = new HashMap<String, String>();
					strData.put("data", "x: " + world.x + ", " + world.y + "," + world.z );
					strpub.publish(strData);
					
					Map<String, Object> markerData = new HashMap<String, Object>();
					Map<String, String> markerHeader = new HashMap<String, String>();
					long currentTimeMicros = System.currentTimeMillis() * 1000 ;
					markerHeader.put("frame_id", "base");
					System.out.println("Time: " + currentTimeMicros);
					markerHeader.put("stamp", String.valueOf(currentTimeMicros));
					markerData.put("header", markerHeader);
					markerData.put("type", 0);
					markerData.put("action", 0);
					markerData.put("lifetime", 1);
					
					Map<String, Object> poseStamped = new HashMap<String, Object>();
					
					Map<String, Object> pose = new HashMap<String, Object>();
					pose.put("x", Float.valueOf(world.x) + 1);
					pose.put("y", Float.valueOf(world.y));
					pose.put("z", Float.valueOf(world.z));
					poseStamped.put("position", pose);
					
					Map<String, Float> orientation = new HashMap<String, Float>();
					orientation.put("x", (float) 0.0);
					orientation.put("y", (float) 0.0);
					orientation.put("z", (float) 0.0);
					orientation.put("w", (float) 1.0);
					poseStamped.put("orientation", orientation);
					
					markerData.put("pose", poseStamped);

					Map<String, Float> scale = new HashMap<String, Float>();
					
					int openness = hand.QueryOpenness();
					System.out.println("open: " + openness);
					
					scale.put("x", (float) 0.1);
					scale.put("y", (float) 0.1);
					scale.put("z", (float) 0.1);
					markerData.put("scale",  scale);
					
					
					Map<String, Float> color = new HashMap<String, Float>();
					color.put("r", (float) (1.0 * openness / 100.0));
					color.put("g", (float) (1.0 * openness / 100.0));
					color.put("b", (float) (1.0 * openness / 100.0));
					color.put("a", (float) 1.0);
					markerData.put("color", color);
					
					markerpub.publish(markerData);
				} else {
					// No hand was found.
				}

				// alerts
				//int nalerts = handData.QueryFiredAlertsNumber();
				//System.out.println("# of alerts at frame " + nframes + " is "
				//		+ nalerts);

				// gestures
				//int ngestures = handData.QueryFiredGesturesNumber();
				//System.out.println("# of gestures at frame " + nframes + " is "
				//		+ ngestures);

				senseMgr.ReleaseFrame();
			} 
		} else {
			System.err.println("Error");
		}
		System.out.println("Exiting");
	}
}
