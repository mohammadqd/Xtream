package xtream.structures;

import xtream.interfaces.ITuple;

public class FuzzyQueryResult {
	

	public ITuple tpl; // result tuple
	public double conf; // confidence in [0,1]
	
	/**
	 * @param tpl
	 * @param conf
	 */
	public FuzzyQueryResult(ITuple tpl, double conf) {
		super();
		this.tpl = tpl;
		this.conf = conf;
	}

}
