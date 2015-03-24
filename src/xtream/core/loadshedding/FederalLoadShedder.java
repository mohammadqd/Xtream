/**
 * Project: Xtream
 * Module:
 * Task:
 * Last Modify:
 * Created:
 * Developer: Mohammad Ghalambor Dezfuli (mghalambor@iust.ac.ir & @ gmail.com)
 *
 * LICENSE:
 *    
 * This file is part of the Xtream project.
 *
 * Xtream is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xtream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xtream.  If not, see <http://www.gnu.org/licenses/>.
 */
package xtream.core.loadshedding;

import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import xtream.Globals;
import xtream.Globals.Monitoring_Modes;
import xtream.interfaces.ILSStore;
import xtream.interfaces.ILoadShedder;
import xtream.interfaces.IQuery;
import xtream.structures.TxtFileOutPort;

/**
 * @author ghalambor
 * 
 */
public class FederalLoadShedder implements ILoadShedder {

	protected IQuery[] queries;
	protected LSOffer[][] offers; // offers[i][j] is jth offer from ith query
	protected int lsQueriesCount; // number of queries which implement ILSStore
									// interface
	protected double[] offerThreshold;
	protected int perQueryOffers;
	protected long lastLoadShedding;
	protected TxtFileOutPort log; // log file

	/**
	 * 
	 */
	public FederalLoadShedder(IQuery[] queries, int perQueryOffers) {
		this.queries = queries;
		offers = new LSOffer[queries.length][perQueryOffers];
		offerThreshold = new double[perQueryOffers];
		for (int i = 0; i < perQueryOffers; i++) {
			offerThreshold[i] = ((double) i + 1) / perQueryOffers;
		}
		this.perQueryOffers = perQueryOffers;
		lastLoadShedding = 0;
		log = new TxtFileOutPort("Federal_Load_Shedder.txt");
		log.Open();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILoadShedder#ReleaseMemory(long)
	 */
	public long ReleaseMemory(long memToRelease) {
		if (Globals.FEDERAL_LOADSHEDDING_IS_ACTIVE) {
			long curTime = Globals.core.GetSysCurTime();
			long releasedMem = 0;
			try {
				lastLoadShedding = curTime;
				RetrieveOffers();
				if (Globals.MONITORING_MODE == Monitoring_Modes.Partial
						|| Globals.MONITORING_MODE == Monitoring_Modes.Full)
					PrintOffers();
				switch (Globals.LOADSHEDDING_OFFERSELECTION_METHOD) {
				case SamePT:
					releasedMem = SamePTReleaseMemory(memToRelease);
					break;
				case FairThief:
					releasedMem = FairThiefReleaseMemory(memToRelease);
					break;
				case Greedy2:
				case Greedy:
					releasedMem = GreedyReleaseMemory(memToRelease);
					break;
				}
				return releasedMem;
			} finally {
				log.WriteStr("Load Shedding @(ms) " + curTime
						+ " Duration(ms): "
						+ (Globals.core.GetSysCurTime() - curTime)
						+ " MemToRelease(MB): " + (memToRelease / 1048576)
						+ " ReleasedMem(MB): " + (releasedMem / 1048576)
						+ " Method: "
						+ Globals.LOADSHEDDING_OFFERSELECTION_METHOD.name());
			}
		} else
			return 0;
	}

	/**
	 * Retrieves LSOffers from queries which implement ILSStore
	 */
	public void RetrieveOffers() {
		lsQueriesCount = 0;
		for (int i = 0; i < queries.length; i++) {
			if (queries[i] instanceof ILSStore) {
				offers[lsQueriesCount] = ((ILSStore) queries[i])
						.getLSOffers(offerThreshold);
				lsQueriesCount++;
			}
		}
	}

	protected long GreedyReleaseMemory(long memToRelease) {
		if (memToRelease > 0) {
			long totalMemRelease = 0;
			double totalCost = 0;
			Vector<LSOffer> selectedOffers = new Vector<LSOffer>();
			PriorityQueue<LSOffer> pqueue = new PriorityQueue<LSOffer>(
					lsQueriesCount * perQueryOffers);
			HashSet<IQuery> selectedQueries = new HashSet<IQuery>(
					lsQueriesCount);
			for (int i = 0; i < lsQueriesCount; i++) {
				for (int j = 0; j < perQueryOffers; j++) {
					pqueue.add(offers[i][j]);
				}
			}
			Iterator<LSOffer> it = pqueue.iterator();
			while (selectedOffers.size() < lsQueriesCount
					&& totalMemRelease < memToRelease) {
				LSOffer nextOffer = it.next();
				if (!selectedQueries.contains(nextOffer.query)) {
					selectedQueries.add(nextOffer.query);
					totalMemRelease += nextOffer.memRelease;
					selectedOffers.add(nextOffer);
					totalCost += nextOffer.totalCost;
					if (Globals.MONITORING_MODE == Monitoring_Modes.Partial // Display
							|| Globals.MONITORING_MODE == Monitoring_Modes.Full) {
						System.out.println("OFFER Query: "
								+ nextOffer.query.GetName() + " newPT: "
								+ nextOffer.newPT + " MEM_RELEASE: "
								+ nextOffer.memRelease + " COST: "
								+ nextOffer.totalCost);
					}
				}
			}
			// Send LS Command
			if (Globals.MONITORING_MODE == Monitoring_Modes.Partial // Display
					|| Globals.MONITORING_MODE == Monitoring_Modes.Full) {
				System.out
						.println("\nFederal Load Shedder(Greedy): Requested Mem to Release: "
								+ memToRelease
								+ " Released: "
								+ totalMemRelease + " TotalCost: " + totalCost);
			}
			for (LSOffer offer : selectedOffers) {
				((ILSStore) offer.query).LSCommand(offer);
			}
			return totalMemRelease;
		} else
			return 0;
	}

	/**
	 * Load Shedding based of Fair Thief Algorithm
	 * 
	 * @param memToRelease
	 *            (byte)
	 * @return real released mem
	 */
	protected long FairThiefReleaseMemory(long memToRelease) {
		if (memToRelease > 0) {
			LSOffer[] bestOffers = FairThief.FindBestOffers((int) memToRelease,
					perQueryOffers, lsQueriesCount, offers);
			if (bestOffers == null) { // no result
				bestOffers = new LSOffer[lsQueriesCount];
				for (int i = 0; i < lsQueriesCount; i++) {
					bestOffers[i] = offers[i][perQueryOffers - 1];

				}
			}
			// DEBUG
			// LSOffer[] bestOffers = new LSOffer[lsQueriesCount];
			// for (int kkk=0; kkk<lsQueriesCount; kkk++) {
			// bestOffers[kkk] = offers[kkk][6];
			// }

			if (true/* bestOffers != null */) {
				long totalMemRelease = 0;
				double totalCost = 0;
				for (int i = 0; i < bestOffers.length; i++) {
					totalMemRelease += bestOffers[i].memRelease;
					totalCost += bestOffers[i].totalCost;
					if (Globals.MONITORING_MODE == Monitoring_Modes.Partial // Display
							|| Globals.MONITORING_MODE == Monitoring_Modes.Full) {
						System.out.println("OFFER Query: "
								+ bestOffers[i].query.GetName() + " newPT: "
								+ bestOffers[i].newPT + " MEM_RELEASE: "
								+ bestOffers[i].memRelease + " COST: "
								+ bestOffers[i].totalCost);
					}
				}
				// Send LS Command
				if (Globals.MONITORING_MODE == Monitoring_Modes.Partial // Display
						|| Globals.MONITORING_MODE == Monitoring_Modes.Full) {
					System.out
							.println("\nFederal Load Shedder(FairThief): Requested Mem to Release(MB): "
									+ ((double) memToRelease / 1048576)
									+ " Released(MB): "
									+ ((double) totalMemRelease / 1048576)
									+ " TotalCost: " + totalCost);
				}
				for (LSOffer offer : bestOffers) {
					((ILSStore) offer.query).LSCommand(offer);
				}
				return totalMemRelease;
			} else
				return 0;
		} else
			return 0;
	}

	/**
	 * This is a simple implementation which forces a specific PT to all
	 * queries.
	 * 
	 * @param memToRelease
	 *            mem to release (byte)
	 * @return released mem (byte)
	 */
	protected long SamePTReleaseMemory(long memToRelease) {
		if (memToRelease > 0) {
			int i = -1; // counter for offer level
			long totalReleaseMem = 0;
			while ((i + 1) < (perQueryOffers) && totalReleaseMem < memToRelease) {
				totalReleaseMem = 0;
				i++;
				for (int q = 0; q < lsQueriesCount; q++) {
					totalReleaseMem += offers[q][i].memRelease;
				}
			}
			// Send LS Command
			if (Globals.MONITORING_MODE == Monitoring_Modes.Partial // Display
					|| Globals.MONITORING_MODE == Monitoring_Modes.Full) {
				System.out
						.println("\nFederal Load Shedder(SIMPLE): Requested Mem to Release: "
								+ memToRelease
								+ " Released: "
								+ totalReleaseMem
								+ " PTs-->"
								+ offerThreshold[i]);
			}
			for (int q = 0; q < lsQueriesCount; q++) { // LS Command
				((ILSStore) offers[q][i].query).LSCommand(offers[q][i]);
			}
			return totalReleaseMem;
		} else
			return 0;
	}

	public void PrintOffers() {
		System.out.println("\n====== GLOBALS OFFERS =====");
		int i = -1; // counter for offer level
		long totalReleaseMem = 0;
		double totalReleaseRT = 0;
		while ((i + 1) < (perQueryOffers)) {
			totalReleaseMem = 0;
			totalReleaseRT = 0;
			i++;
			for (int q = 0; q < lsQueriesCount; q++) {
				totalReleaseMem += offers[q][i].memRelease;
				totalReleaseRT += offers[q][i].rtRelease;
			}
			System.out.println("LSOffer: NewPT: " + offers[0][i].newPT
					+ " MEM_RELEASE(MB): " + (totalReleaseMem / 1048576)
					+ " RT_RELEASE: " + totalReleaseRT);
		}
	}

	@Override
	public long MTR(long um, double mslp, double islp) {
		long mtr = (long) ((2 + (Math.min(mslp, 10000000) / 600000)) * (Globals.MEMORY_MAX_ALLOWABLE - Globals.MEMORY_USE_ALERT_THRESHOLD));
		System.out.println("\n MTR: um(MB): " + (um / 1048576) + " MSLP: "
				+ mslp + " ISLP: " + islp + " MTR(MB): " + (mtr / 1048576));
		return mtr;
	}

	public void QueryQoSImprove(IQuery q) {
		double curPT = q.GetPT();
		if (curPT > 0) {
			double ptSegments = 1d / Globals.PER_OPERATOR_LS_OFFERS_COUNT;
			// q.SetPT(curPT - ptSegments);
			System.out.println("\n Query QoS Improvement From " + curPT
					+ " To " + (curPT - ptSegments));
		}
	}

	@Override
	public long GetLastLoadSheddingTime() {
		return lastLoadShedding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILoadShedder#Close()
	 */
	public void Close() {
		log.Close();
	}

}
