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
package xtream.lsrm;

import java.io.IOException;

import cern.jet.random.engine.MersenneTwister;
import xtream.Globals;
import xtream.plr.PPos;
import xtream.query.IQuery;
import xtream.query.Select;
import xtream.structures.ABooleanPredicate;
import xtream.structures.ITuple;
import xtream.structures.PeriodicStatistics;

/**
 * @author mohammad
 * 
 */
public class RandomDrop extends Select implements ILSRMOP {
	
	protected PeriodicStatistics pstat; 
	
	/**
	 * @param opName
	 * @param parentQuery
	 * @param dropRatio
	 *            ratio of drop tuples in [0,1]
	 */
	public RandomDrop(String opName, IQuery parentQuery, final double dropRatio) {
		super(new ABooleanPredicate() {
			protected MersenneTwister rndEngine = new MersenneTwister(56454);
			protected double drpRtio = dropRatio;
			public boolean Predicate(ITuple... tpls) {
				return (rndEngine.nextDouble() > drpRtio);
			}
		}, opName, parentQuery);
		pstat = new PeriodicStatistics(Globals.OVERLOAD_CHECKING_TIME_PERIOD);
	}

	public void ChangeDropRatio(final double newDropRatio) {
		prediction = new ABooleanPredicate() {
			protected MersenneTwister rndEngine = new MersenneTwister(56454);
			protected double drpRtio = newDropRatio;
			public boolean Predicate(ITuple... tpls) {
				return (rndEngine.nextDouble() > drpRtio);
			}
		};
	}

	/* (non-Javadoc)
	 * @see xtream.query.Select#PutTuple(xtream.interfaces.ITuple, int)
	 */
	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		super.PutTuple(tp, i);
		pstat.newValue(tp.GetSize());
	}

	@Override
	public long getGain() {
		return Math.round(pstat.getSum());
	}

	@Override
	public double getLoss() {
		return parentQuery.GetRelativeQoSWeight();
	}

	@Override
	public int compareTo(ILSRMOP o) {
		return new Double(this.getLoss()).compareTo(new Double(o.getLoss()));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("RandomDrop: OPName: %s Gain: %d Loss: %f", this.opName,getGain(),getLoss());
	}

}
