package edu.brown.cs.h2r.real_sense_ros;

import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMCaptureManager;
import intel.rssdk.PXCMHandConfiguration;
import intel.rssdk.PXCMHandData;
import intel.rssdk.PXCMHandModule;
import intel.rssdk.PXCMPoint3DF32;
import intel.rssdk.PXCMPointF32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.pxcmStatus;

public class RealSenseRos {
	public static void main(String s[]) {
		System.out.println("Starting hand tracker.");
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

		/*
		 * PXCMHandModule handModule = senseMgr.QueryHand();
		 * PXCMHandConfiguration handConfig =
		 * handModule.CreateActiveConfiguration();
		 * handConfig.EnableAllGestures(); handConfig.EnableAllAlerts();
		 * handConfig.ApplyChanges(); handConfig.Update();
		 */

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

			for (int nframes = 0; nframes < 3000; nframes++) {
				//System.out.println("Frame # " + nframes);
				sts = senseMgr.AcquireFrame(true);
				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0)
					break;

				PXCMCapture.Sample sample = senseMgr.QueryHandSample();

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
				}

				// alerts
				int nalerts = handData.QueryFiredAlertsNumber();
				//System.out.println("# of alerts at frame " + nframes + " is "
				//		+ nalerts);

				// gestures
				int ngestures = handData.QueryFiredGesturesNumber();
				//System.out.println("# of gestures at frame " + nframes + " is "
				//		+ ngestures);

				senseMgr.ReleaseFrame();
			}
		}
	}
}
