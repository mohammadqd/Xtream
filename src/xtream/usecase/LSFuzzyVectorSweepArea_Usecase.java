package xtream.usecase;

import java.util.Iterator;
import java.util.Vector;

import xtream.Globals;
import xtream.core.Core;
import xtream.core.loadshedding.LSFuzzyVectorSweepArea;
import xtream.core.loadshedding.LSOffer;
import xtream.interfaces.ITuple;
import xtream.plr.PPos;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.AggTuple;
import xtream.structures.FuzzyQueryResult;

public class LSFuzzyVectorSweepArea_Usecase {

	public LSFuzzyVectorSweepArea_Usecase() {
	}
	
	/**
	 * To find tuple size practically
	 * @param args
	 */
	public static void main(String[] args) {
		double[] mem = {0,0};
		mem[0] = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		System.out.println("\nUSED Mem: "+mem[0]);
		int tplsCnt = 1000;
		// Creating tuples
		ITuple[] agg = new ITuple[tplsCnt];
		for (int i = 0; i < tplsCnt; i++) {
//			agg[i] = new AggTuple(new Integer(i), i, ((double)i+1)/tplsCnt);
			agg[i] = new PPos(1);
		}		
		
		mem[1] = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		System.out.println("\nUSED Mem: "+mem[1]);
		System.out.println("\nDIFF: "+(mem[1]-mem[0])+" Per Tuple: "+((mem[1]-mem[0])/tplsCnt));

		// Show All members
		double sum = 0;
		for (ITuple nextTpl:agg) {
			sum += nextTpl.GetConf();
		}
		System.out.format("\nSum: "+sum);
		
		Vector<ITuple> v = new Vector<ITuple>();
		for (int j=0; j<10000; j++) 
			v.add(new PPos(3434));
		System.out.format("\nVector Size: "+v.size());


		
		}

	/**
	 * Usecase for LSFuzzyVectorSweepArea
	 * @param args
	 */
	public static void main2(String[] args) {
		Globals.core = new Core();
		double[] proposedOffers = {0.1,0.6,0.3,1,0.2};


		
		LSFuzzyVectorSweepArea vsa = new LSFuzzyVectorSweepArea(
				new AFuzzyPredicate() {

					@Override
					public double Predicate(ITuple... tpls) {
						AggTuple curTpl = (AggTuple) tpls[0];
						AggTuple queryTpl = (AggTuple) tpls[1];
						int diff = Math.abs(curTpl.getValue().intValue()
								- queryTpl.getValue().intValue());
						if (diff < 3) {
							return (1d / (diff + 1));
						} else
							return 0;
					}
				}, new ABooleanPredicate() {

					@Override
					public boolean Predicate(ITuple... tpls) {
//						return tpls[0].GetTimestamp()[1]<tpls[1].GetTimestamp()[0];
						return tpls[0].GetTimestamp()[0]<tpls[1].GetTimestamp()[0]-3;
					}
				}, null);

		int tplsCnt = 100000;

		// Creating tuples
		AggTuple[] agg = new AggTuple[tplsCnt];
		for (int i = 0; i < tplsCnt; i++) {
			agg[i] = new AggTuple(new Integer(i), i, ((double)i+1)/tplsCnt);
		}

//		vsa.SetPT(0.4);
		// Testing insert tuple
		for (int i = 0; i < tplsCnt; i++) {
			vsa.Insert(agg[i]);
		}
		System.out.println("Count after insertion " + vsa.GetCount());

		Iterator<ITuple> it; // iterator

		// Show All members
//		it = vsa.GetIterator();
//		System.out.format("\nShow All... \n");
//		while (it.hasNext()) {
//			System.out.println("In SweepArea: "
//					+ it.next());
//		}
		
		// Show Offers
		System.out.println("\n LS OFFERS ");
		LSOffer[] offers = vsa.getLSOffers(proposedOffers);
		for (LSOffer of:offers) {
			System.out.println(" >> " + of);
		}

		// Test Query
//		vsa.SetPT(0);
		ITuple queryTuple = agg[99000];
		Iterator<FuzzyQueryResult> rit = vsa.FQuery(queryTuple, 1);
		System.out.println("\n Test FQuery with: "+queryTuple+ "\n");
		while (rit.hasNext()) {
			FuzzyQueryResult nextResult = rit.next();
			System.out.println("\nQuery Result: " + nextResult.tpl + " CONF: "
					+ nextResult.conf);
		}

		// Test purge
//		System.out.format("\nPurging... ");
//		vsa.PurgeElements(agg[6], 1);

		// Test Extract
//		System.out.format("\nExtracting... ");
//		it = vsa.ExtractElements(agg[6], 1);
//		while (it.hasNext()) {
//			System.out.println("In Extract List: "
//					+ it.next());
//		}

		// LS Command
		LSOffer offer = offers[3];
		System.out.format("\nLS Command... Performing "+offer);
		vsa.LSCommand(offer);
		
		System.out.println("\nCount after LS " + vsa.GetCount());
		

		// Show All members
//		it = vsa.GetIterator();
//		System.out.format("\nShow All... \n");
//		while (it.hasNext()) {
//			System.out.println("In SweepArea: "
//					+ it.next());
//		}

	}

}
