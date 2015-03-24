package xtream.usecase;

import java.util.Vector;

import cern.jet.random.engine.MersenneTwister;
import xtream.Globals;
import xtream.core.loadshedding.FairThief;
import xtream.core.loadshedding.LSOffer;

public class FairThief_Usecase {

	private static int V = 100*1024*1024;
	private static int n = 10;
	private static int k = 8;
	private static LSOffer[][] offers;
	private static int bfCounter = 0;
	private static Vector<LSOffer> bfOffers;
	private static double bfMinCost = Double.MAX_VALUE;
	private static MersenneTwister rndeng = new MersenneTwister(425);//245

	public FairThief_Usecase() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {


		// ***********************
		// INITIAL OFFERS
		// ***********************

		
		long startTime;

		// ***********************
		// Bruteforce Approach
		// ***********************
		/*
		startTime = System.currentTimeMillis();
//		BruteforceApproach(1, new Vector<LSOffer>());
		System.out.println("\n************************************");
		System.out.println("***** BruteForce   O F F E R S *****");
		System.out.println("************************************");
		System.out.println("Delay(ms): "+(System.currentTimeMillis()-startTime));
		if (bfOffers != null) {
		long totalMemRelease = 0;
		double totalCost = 0;
		for (LSOffer lso : bfOffers) {
			System.out.println("OFFER  MEM_RELEASE: " + lso.memRelease
					+ " COST: " + lso.totalCost);
			totalMemRelease += lso.memRelease;
			totalCost += lso.totalCost;
		}
		System.out.println("Total Mem Release: " + totalMemRelease
				+ " Total_Cost: " + totalCost);
		} else { // no result 
			System.out.println("NO BRUTEFORCE RESULT!!");
		}
*/
		// ***********************
		// FairThief
		// ***********************
		startTime = System.currentTimeMillis();
		double duration=0;
		double deltaV = 0;
		long totalMemRelease = 0;
		LSOffer[] bestOffers = new LSOffer[n];
		int testCount = 100;
		long usedMem = 0;
		for (int i=0; i<testCount; i++) {
			totalMemRelease = 0;
			InitialOffers();
			bestOffers = FairThief.FindBestOffers(V, k, n, offers);
			for (int j = 0; j < bestOffers.length; j++) {
				totalMemRelease += bestOffers[j].memRelease;
			}
//			System.out.println(" TotalMemRelease(MB): "+(totalMemRelease/1048576));
			deltaV += Math.abs(V-totalMemRelease);
		}
		duration = (double)(System.currentTimeMillis()-startTime)/testCount;
		
		System.out.println("\n************************************");
		System.out.println("***** FAIR THIEF   O F F E R S *****");
		System.out.println("************************************");
		System.out.println("Mean Delay(ms): "+duration+ " Mean DeltaV(byte): "+ Math.round(((double)deltaV/testCount))+ " Size: "+((double)Globals.totalSize/testCount));
		if (bestOffers != null) {
		double totalCost = 0;
//		// Print Offers
//		for (int i = 0; i < bestOffers.length; i++) {
//			System.out.println("OFFER [" + i + "] MEM_RELEASE: "
//					+ bestOffers[i].memRelease + " COST: "
//					+ bestOffers[i].totalCost);
//			totalMemRelease += bestOffers[i].memRelease;
//			totalCost += bestOffers[i].totalCost;
//		}
		System.out.println("Total Mem Release: " + totalMemRelease
				+ " Total_Cost: " + totalCost);
		} else { // bestOffers == null
			System.out.println("NO FAIRTHIEF RESULT!!");
		}
	}

	public static void BruteforceApproach(int level, Vector<LSOffer> of) {
		if (level == n) {
			for (int i = 0; i <= k; i++) {
				Vector<LSOffer> newVec = new Vector<LSOffer>(of.size() + 1);
				newVec.addAll(of);
				if (i > 0)
					newVec.add(offers[level - 1][i - 1]);
				double sumCost = 0;
				int memRelease = 0;
				for (LSOffer lso : newVec) {
					sumCost += lso.totalCost;
					memRelease += lso.memRelease;
				}
				if (memRelease >= V && sumCost < bfMinCost) {
					bfMinCost = sumCost;
					bfOffers = newVec;
				}
			}
		} else {
			for (int i = 0; i <= k; i++) {
				Vector<LSOffer> newVec = new Vector<LSOffer>(of.size() + 1);
				newVec.addAll(of);
				if (i > 0)
					newVec.add(offers[level - 1][i - 1]);
				BruteforceApproach(level + 1, newVec);
			}
		}
	}
	
	public static void InitialOffers() {
		offers = new LSOffer[n][k];
		for (int i = 0; i < n; i++) {
			long lastRelease = 0;
			double lastCost = 0;
			for (int j = 0; j < k; j++) {
				offers[i][j] = new LSOffer();
				offers[i][j].totalCost = lastCost + rndeng.nextDouble();
				lastCost = offers[i][j].totalCost;
				offers[i][j].memRelease = (lastRelease + Math.round(((rndeng.nextDouble() * 5)*1024*1024)));
				lastRelease += offers[i][j].memRelease;
				// Print Offer
//				System.out.println("OFFER [" + i + "][" + j + "] MEM_RELEASE(MB): "
//						+ (offers[i][j].memRelease/1048576) + " COST: "
//						+ offers[i][j].totalCost);
			}
		}
	}

}
