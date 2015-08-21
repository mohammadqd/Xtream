/**
 * Project: Xtream
 * Module: Vector Sweep Area Usecase
 * Task: as a usecase and test module for VectorSweepArea
 * Last Modify:
 * Created: 2013
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
package xtream.usecase;

import java.util.Iterator;

import xtream.Globals;
import xtream.core.Core;
import xtream.structures.ABooleanPredicate;
import xtream.structures.AFuzzyPredicate;
import xtream.structures.AggTuple;
import xtream.structures.FuzzyQueryResult;
import xtream.structures.FuzzyVectorSweepArea;
import xtream.structures.ITuple;

/**
 * @author ghalambor
 * 
 */
public final class VectorSweepArea_Usecase {

	/**
	 * 
	 */
	public VectorSweepArea_Usecase() {
		Globals.core = new Core();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FuzzyVectorSweepArea vsa = new FuzzyVectorSweepArea(
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
						// return
						// tpls[0].GetTimestamp()[1]<tpls[1].GetTimestamp()[0];
						return tpls[0].GetTimestamp()[0] < tpls[1]
								.GetTimestamp()[0] - 3;
					}
				}, null);

		int tplsCnt = 10;

		// Creating tuples
		AggTuple[] agg = new AggTuple[tplsCnt];
		for (int i = 0; i < tplsCnt; i++) {
			agg[i] = new AggTuple(new Integer(i), i, ((double) i + 1) / tplsCnt);
		}

		// vsa.SetPT(0.4);
		// Testing insert tuple
		for (int i = 0; i < tplsCnt; i++) {
			vsa.Insert(agg[i]);
		}
		System.out.println("Count after insertion " + vsa.GetCount());

		Iterator<ITuple> it; // iterator

		// Show All members
		it = vsa.GetIterator();
		System.out.format("\nShow All... \n");
		while (it.hasNext()) {
			System.out.println("In SweepArea: " + it.next());
		}

		// Test Query
		// vsa.SetPT(0);
		ITuple queryTuple = agg[5];
		Iterator<FuzzyQueryResult> rit = vsa.FQuery(queryTuple, 1);
		System.out.println("\n Test FQuery with: " + queryTuple + "\n");
		while (rit.hasNext()) {
			FuzzyQueryResult nextResult = rit.next();
			System.out.println("\nQuery Result: " + nextResult.tpl + " CONF: "
					+ nextResult.conf);
		}

		// Test purge
		System.out.format("\nPurging... ");
		// vsa.PurgeElements(agg[6], 1);

		// Test Extract
		System.out.format("\nExtracting... ");
		it = vsa.ExtractElements(agg[6], 1);
		while (it.hasNext()) {
			System.out.println("In Extract List: " + it.next());
		}

		// Show All members
		it = vsa.GetIterator();
		System.out.format("\nShow All... \n");
		while (it.hasNext()) {
			System.out.println("In SweepArea: " + it.next());
		}

	}
}
