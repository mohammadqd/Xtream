package xtream.structures;

public abstract class AAggregation {

	/**
	 * @param tuples tuples to be aggregated
	 * @return aggregation tuple
	 */
	public abstract Object ComputeAggregation(Object... tuples);

}
