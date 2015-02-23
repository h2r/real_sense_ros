package edu.brown.cs.h2r.real_sense_ros;

import intel.rssdk.PXCMCaptureManager;
import intel.rssdk.PXCMFaceConfiguration;
import intel.rssdk.PXCMFaceData;
import intel.rssdk.PXCMFaceData.Face;
import intel.rssdk.PXCMFaceData.PoseData;
import intel.rssdk.PXCMFaceData.PoseEulerAngles;
import intel.rssdk.PXCMFaceModule;
import intel.rssdk.PXCMPoint3DF32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.pxcmStatus;
import ros.Publisher;
import ros.RosBridge;

public class PublishFace {
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

		pxcmStatus sts = senseMgr.EnableFace(null);
		
		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
			throw new IllegalStateException("Failed to enable face");
		}

		PXCMFaceModule face = senseMgr.QueryFace();
		PXCMFaceConfiguration cfg = face.CreateActiveConfiguration();
		cfg.EnableAllAlerts();

		cfg.ApplyChanges();
		cfg.close();
		
		sts = senseMgr.Init();

		while (senseMgr.AcquireFrame(true) == pxcmStatus.PXCM_STATUS_NO_ERROR) {	
			  PXCMFaceModule face2 = senseMgr.QueryFace();
			  
			  PXCMFaceData faceData = face2.CreateOutput();
			  faceData.Update();
			  int numFaces = faceData.QueryNumberOfDetectedFaces();
			  System.out.println("Saw: " + numFaces + " faces.");
			  for (int i = 0; i < numFaces; i++)  {
				  Face f = faceData.QueryFaceByIndex(i);
				  PoseData p = f.QueryPose();
				  PXCMPoint3DF32 outHeadPosition = new PXCMPoint3DF32();
				  p.QueryHeadPosition(outHeadPosition);
				  System.out.println("Out: " + outHeadPosition);
				  
				  PoseEulerAngles outPoseEulerAngles = new PoseEulerAngles();
				  p.QueryPoseAngles(outPoseEulerAngles);
				  System.out.println("Rotation: " + outPoseEulerAngles.roll + " " + outPoseEulerAngles.pitch + " " + outPoseEulerAngles.yaw);
				  
			  }
			  faceData.close();	
			  senseMgr.ReleaseFrame();
		} 

		System.out.println("Exiting");
	}
}
