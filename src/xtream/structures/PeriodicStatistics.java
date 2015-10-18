package xtream.structures;

/**
 * To automatically maintain periodic statistics
 * 
 * @author mohammad
 * 
 */
public class PeriodicStatistics {

	protected long periodSize; // time (millisec)
	protected long lastCount, newCount;
	protected double lastSum, newSum;
	protected long periodStartTime;

	/**
	 * @param periodSize
	 *            size of period (msec)
	 */
	public PeriodicStatistics(long periodSize) {
		this.periodSize = periodSize;
		periodStartTime = System.currentTimeMillis();
	}

	/**
	 * @param newVal
	 *            new values to add
	 */
	public void newValue(double... newVal) {
		long curTime = System.currentTimeMillis();
		if (curTime - periodStartTime > periodSize) { // new period
			lastCount = newCount;
			lastSum = newSum;
			newCount = newVal.length;
			newSum = 0;
			for (double v : newVal)
				newSum += v;
			periodStartTime = curTime;
		} else {
			newCount = newVal.length;
			for (double v : newVal)
				newSum += v;
		}
	}

	/**
	 * @return computed mean value
	 */
	public double getMean() {
		if (lastCount > 0)
			return lastSum / lastCount;
		else
			return 0;
	}

	/**
	 * @return computed sum value
	 */
	public double getSum() {
		return lastSum;
	}

	/**
	 * @return number of data elements (contributed values)
	 */
	public long getCount() {
		return lastCount;
	}

}
