package xtream.structures;

public abstract class AAggregation {

	/**
	 * user-defined aggregation function for aggregate operators
	 * @param tuples tuples to be aggregated
	 * @return aggregation tuple
	 */
	public abstract Object ComputeAggregation(Object... tuples);

}
