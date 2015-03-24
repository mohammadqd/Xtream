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

	public PeriodicStatistics(long periodSize) {
		this.periodSize = periodSize;
		periodStartTime = System.currentTimeMillis();
	}

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

	public double getMean() {
		if (lastCount > 0)
			return lastSum / lastCount;
		else
			return 0;
	}

	public double getSum() {
		return lastSum;
	}

	public long getCount() {
		return lastCount;
	}

}
