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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import xtream.Globals;
import xtream.interfaces.IFuzzySweepArea;
import xtream.interfaces.ILSStore;
import xtream.interfaces.ITuple;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.FuzzyQueryResult;
import xtream.structures.FuzzyVectorSweepArea;
import xtream.structures.MultiIterator;

/**
 * @author ghalambor
 * 
 */
public class LSFuzzyVectorSweepArea implements ILSStore, IFuzzySweepArea {

	protected FuzzyVectorSweepArea[] buffers;
	protected long[] bufferDelays; // operation delay for each buffer (millisec)
	protected long totalOperations;
	protected double currentPT; // current probability-threshold
	protected final long buffersCount = Globals.PER_OPERATOR_LS_OFFERS_COUNT;

	/**
	 * @param queryPredicate
	 * @param removePredicate
	 * @param order
	 */
	public LSFuzzyVectorSweepArea(AFuzzyPredicate queryPredicate,
			ABooleanPredicate removePredicate, Comparator<ITuple> order) {
		buffers = new FuzzyVectorSweepArea[(int) buffersCount];
		bufferDelays = new long[(int) buffersCount];
		for (int i = 0; i < buffersCount; i++) {
			buffers[i] = new FuzzyVectorSweepArea(queryPredicate,
					removePredicate, order);
			bufferDelays[i] = 0;
		}
		totalOperations = 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ILSCStore#LSCommand(xtream.structures.LSOffer)
	 */
	@Override
	public void LSCommand(LSOffer offer) {
		SetPT(offer.newPT);
	}

	@Override
	public void Insert(ITuple tpl) {
		if (!Globals.ADAPTIVE_FLS || tpl.GetConf() >= GetPT()) {
			totalOperations++;
			long index = Math.min(Math.round(tpl.GetConf() * buffersCount),
					buffersCount - 1);
			long curTime = System.currentTimeMillis();
			buffers[(int) index].Insert(tpl);
			bufferDelays[(int) index] += System.currentTimeMillis() - curTime;
		}
	}

	@Override
	public void Replace(ITuple oldTpl, ITuple newTpl) {
		totalOperations++;
		for (int i = 0; i < buffersCount; i++) {
			long curTime = System.currentTimeMillis();
			buffers[i].Replace(oldTpl, newTpl);
			bufferDelays[i] += System.currentTimeMillis() - curTime;
		}
	}

	@Override
	public Iterator<ITuple> GetIterator() {
		List<Iterator<ITuple>> iterators = new LinkedList<Iterator<ITuple>>();
		for (int i = 0; i < buffersCount; i++) {
			iterators.add(buffers[i].GetIterator());
		}
		MultiIterator<ITuple> mit = new MultiIterator<ITuple>(iterators);
		return mit;
	}

	@Override
	public Iterator<ITuple> ExtractElements(ITuple tpl, int j) {
		List<Iterator<ITuple>> iterators = new LinkedList<Iterator<ITuple>>();
		for (int i = 0; i < buffersCount; i++) {
			iterators.add(buffers[i].ExtractElements(tpl, j));
		}
		MultiIterator<ITuple> mit = new MultiIterator<ITuple>(iterators);
		return mit;
	}

	@Override
	public int PurgeElements(ITuple tpl, int j) {
		int totalPurged = 0;
		for (int i = 0; i < buffersCount; i++) {
			long curTime = System.currentTimeMillis();
			totalPurged += buffers[i].PurgeElements(tpl, j);
			bufferDelays[i] += System.currentTimeMillis() - curTime;
		}
		totalOperations += totalPurged;
		return totalPurged;
	}

	@Override
	public int GetCount() {
		int count = 0;
		for (int i = 0; i < buffersCount; i++) {
			count += buffers[i].GetCount();
		}
		return count;
	}

	@Override
	public Iterator<FuzzyQueryResult> FQuery(ITuple tpl, int j, double threshold) {
		List<Iterator<FuzzyQueryResult>> iterators = new LinkedList<Iterator<FuzzyQueryResult>>();
		for (int i = 0; i < buffersCount; i++) {
			long curTime = System.currentTimeMillis();
			iterators.add(buffers[i].FQuery(tpl, j, threshold));
			bufferDelays[i] += System.currentTimeMillis() - curTime;
		}
		MultiIterator<FuzzyQueryResult> mit = new MultiIterator<FuzzyQueryResult>(
				iterators);
		totalOperations++;
		return mit;
	}

	public Iterator<FuzzyQueryResult> FQuery(ITuple tpl, int j) {
		return FQuery(tpl, j, GetPT());
	}

	@Override
	public double SetPT(double pt) {
		if (pt > currentPT) {
			long index = Math.round(pt * buffersCount);
			for (int i = 0; i < index; i++) {
				buffers[i].PurgeAll();
			}
		}
		currentPT = pt;
		return currentPT;
	}

	@Override
	public double GetPT() {
		return currentPT;
	}

	@Override
	public LSOffer[] getLSOffers(double[] newPTs) {
		Arrays.sort(newPTs);
		assert (newPTs[newPTs.length - 1] <= 1d && newPTs[0] >= 0d) : "Wrong Input!";
		LSOffer[] offers = new LSOffer[newPTs.length];
		long delayReleased = 0;
		long memRelease = 0;
		int i = 0; // leaf index
		int j = 0; // offer index
		for (double pt : newPTs) {
			while (i < buffersCount && ((double) i + 1) / buffersCount <= pt) {
				memRelease += buffers[i].GetSize();
				delayReleased += bufferDelays[i];
				i++;
			}
			offers[j] = new LSOffer(null, pt,
					(double)delayReleased / totalOperations, memRelease);
			j++;
		}
		return offers;
	}

}
