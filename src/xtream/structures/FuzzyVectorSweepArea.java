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
package xtream.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import xtream.Globals;
import xtream.interfaces.ITuple;
import xtream.usecase.VectorSweepArea_Usecase;

/**
 * @author ghalambor
 * @see VectorSweepArea_Usecase
 * 
 */
public class FuzzyVectorSweepArea extends AFuzzySweepArea {

	protected Vector<ITuple> buffer;

	/**
	 * @param queryPredicate
	 * @param removePredicate
	 * @param order
	 */
	public FuzzyVectorSweepArea(AFuzzyPredicate queryPredicate,
			ABooleanPredicate removePredicate, Comparator<ITuple> order) {
		super(queryPredicate, removePredicate, order);
		buffer = new Vector<ITuple>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.ASweepArea#Insert(xtream.interfaces.ITuple)
	 */
	@Override
	public void Insert(ITuple tpl) {
		if ((!Globals.ADAPTIVE_FLS || tpl.GetConf() >= GetPT()))
			buffer.add(tpl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.ASweepArea#Replace(xtream.interfaces.ITuple,
	 * xtream.interfaces.ITuple)
	 */
	@Override
	public void Replace(ITuple oldTpl, ITuple newTpl) {
		int oldIndex = buffer.indexOf(oldTpl);
		if (oldIndex > -1) { // if found
			if ((!Globals.ADAPTIVE_FLS || newTpl.GetConf() >= GetPT())) // if new tuple is confident enough
				buffer.add(oldIndex, newTpl); // repleace it
			else
				buffer.remove(oldIndex); // remove old tuple and also ignore new
											// one
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.ASweepArea#GetIterator()
	 */
	@Override
	public Iterator<ITuple> GetIterator() {
		return buffer.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.ASweepArea#Query(xtream.interfaces.ITuple, int)
	 */
	@Override
	public Iterator<FuzzyQueryResult> FQuery(ITuple tpl, int j, double threshold) { // implementation
																					// without
		// considering j
		List<FuzzyQueryResult> results = new ArrayList<FuzzyQueryResult>();
		for (ITuple i : buffer) {
			double newConf = i.GetConf() * tpl.GetConf()
					* queryPredicate.Predicate(i, tpl);
			if ((!Globals.ADAPTIVE_FLS || newConf > threshold)) {
				FuzzyQueryResult newResult = new FuzzyQueryResult(i, newConf);
				results.add(newResult);
			}
		}
		return results.iterator();
	}

	public Iterator<FuzzyQueryResult> FQuery(ITuple tpl, int j) {
		return FQuery(tpl,j,GetPT());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * xtream.structures.ASweepArea#ExtractElements(xtream.interfaces.ITuple,
	 * int)
	 */
	@Override
	public Iterator<ITuple> ExtractElements(ITuple tpl, int j) { // implementation
																	// without
																	// considering
																	// j
		List<ITuple> results = new ArrayList<ITuple>();
		Iterator<ITuple> it = buffer.iterator();
		while (it.hasNext()) {
			ITuple nextTpl = it.next();
			if ((Globals.ADAPTIVE_FLS && nextTpl.GetConf() < GetPT())
					|| (tpl != null && removePredicate.Predicate(nextTpl, tpl))) {
				results.add(nextTpl);
				it.remove();
			}
		}
		return results.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.ASweepArea#PurgeElements(xtream.interfaces.ITuple,
	 * int)
	 */
	@Override
	public int PurgeElements(ITuple tpl, int j) { // implementation without
													// considering j
		int purged = 0;
		Iterator<ITuple> it = buffer.iterator();
		while (it.hasNext()) {
			ITuple nextTpl = it.next();
			if ((Globals.ADAPTIVE_FLS && nextTpl.GetConf() < GetPT())
					|| (tpl != null && removePredicate.Predicate(nextTpl, tpl))) {
				it.remove();
				purged++;
			}
		}
		return purged;
	}

	/**
	 * To delete all tuples
	 * 
	 * @return num of deleted tuples
	 */
	public int PurgeAll() {
		int cnt = GetCount();
		buffer = new Vector<ITuple>();
		return cnt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.ASweepArea#GetCount()
	 */
	@Override
	public int GetCount() {
		return buffer.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.structures.AFuzzySweepArea#SetPT(double)
	 */
	@Override
	public double SetPT(double pt) {
		double oldPT = GetPT();
		super.SetPT(pt);
		if (oldPT < pt)
			PurgeElements(null, 0); // remove tuples with lower conf
		return pt;
	}

	/**
	 * @return size of structure in byte
	 */
	public long GetSize() {
		int cnt = GetCount();
		if (cnt > 0) {
			return cnt * buffer.firstElement().GetSize();
		} else
			return 0;
	}

}
