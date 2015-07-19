/**
 * Project: Xtream
 * Module: ProbeCounter
 * Task: a simple counter op to count and show results on screen/log
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
package xtream.query;

import java.io.IOException;

import xtream.core.log.XLogger;
import xtream.core.log.XLogger.SeverityLevel;
import xtream.interfaces.IQuery;
import xtream.interfaces.ITuple;

/**
 * Simple Probe counter to count and show results on screen
 * @author ghalambor
 *
 */
public class ProbCounter extends AOperator {
	
	protected long counter;

	/**
	 * @param opName name of probecounter
	 * @param parentQuery parent query
	 */
	public ProbCounter(String opName, IQuery parentQuery) {
		super(opName, parentQuery);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IOperator#run(long)
	 */
	@Override
	public void run(long ts) {
		assert false : "Not Implemented!";
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IOperator#SetPT(double)
	 */
	@Override
	public double SetPT(double pt) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IInPort#hasTuple()
	 */
	@Override
	public boolean hasTuple() {
		return false;
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IInPort#nextTuple()
	 */
	@Override
	public ITuple nextTuple() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see xtream.interfaces.IOutPort#isUnary()
	 */
	@Override
	public boolean isUnary() {
		return true;
	}

	/* (non-Javadoc)
	 * @see xtream.query.AOperator#PutTuple(xtream.interfaces.ITuple, int)
	 */
	@Override
	public void PutTuple(ITuple tp, int i) throws IOException {
		counter++;
	}

	/* (non-Javadoc)
	 * @see xtream.query.AOperator#Close()
	 */
	@Override
	public void Close() {
		super.Close();
		System.out.println("\n Prob Counter: "+opName+" Value: "+counter);
		XLogger.Log("ProbeCounter:"+opName, "Value: "+counter, SeverityLevel.INFO);
	}

}
