package xtream.structures;

import xtream.interfaces.ITuple;

public class FuzzyQueryResult {

	/**
	 * result tuple
	 */
	public ITuple tpl;
	/**
	 * confidence in [0,1]
	 */
	public double conf;

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
