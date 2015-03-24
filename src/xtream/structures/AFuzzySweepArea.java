package xtream.structures;

import java.util.Comparator;
import xtream.interfaces.IFuzzySweepArea;
import xtream.interfaces.ITuple;

public abstract class AFuzzySweepArea implements
		IFuzzySweepArea {
	
	protected AFuzzyPredicate queryPredicate;
	protected ABooleanPredicate removePredicate;
	protected Comparator<ITuple> order;
	protected double currentPT; // current probability-threshold


	/**
	 * @param queryPredicate
	 * @param removePredicate
	 * @param order
	 * @param pt probability-threshold
	 */
	public AFuzzySweepArea(AFuzzyPredicate queryPredicate,
			ABooleanPredicate removePredicate, Comparator<ITuple> order) {
		this.removePredicate = removePredicate;
		this.order = order;				
		this.queryPredicate = queryPredicate;
		currentPT = 0; //default (no filtering)
	}


	/* (non-Javadoc)
	 * @see xtream.interfaces.IFuzzySweepArea#SetPT(double)
	 */
	@Override
	public double SetPT(double pt) {
		return (currentPT = pt);
	}


	/* (non-Javadoc)
	 * @see xtream.interfaces.IFuzzySweepArea#GetPT()
	 */
	@Override
	public double GetPT() {
		return currentPT;
	}

}
