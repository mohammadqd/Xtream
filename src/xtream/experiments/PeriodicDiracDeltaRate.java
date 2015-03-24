package xtream.experiments;

public class PeriodicDiracDeltaRate extends ASyntheticInputDelayGenerator {

	public class Interval {
		public double low;
		public double high;

		public Interval(double low, double high) {
			this.low = low;
			this.high = high;
		}

		public boolean contains(double value) {
			if (value >= low && value <= high)
				return true;
			else
				return false;
		}
	}

	protected long lowRate;
	protected long highRate;
	protected PeriodicConstantRate lowRateGenerator, highRateGenerator;
	protected Interval[] highRateIntervals;

	/**
	 * @param periodSize
	 * @param lowRate tuples per period
	 * @param highRate tuples per period
	 * @param impactSize size of impact in [0,1] (in total progress from 0 to 1)
	 * @param impactPoints location of impacts in [0,1] (in total progress from 0 to 1)
	 */
	public PeriodicDiracDeltaRate(long periodSize, long lowRate, long highRate,
			double impactSize, double... impactPoints) {
		this.lowRate = lowRate;
		this.highRate = highRate;
		lowRateGenerator = new PeriodicConstantRate(lowRate, periodSize);
		highRateGenerator = new PeriodicConstantRate(highRate, periodSize);
		highRateIntervals = new Interval[impactPoints.length];
		int i = 0;
		for (double d : impactPoints) {
			highRateIntervals[i] = new Interval(d - impactSize/2, d + impactSize/2);
			i++;
		}
	}

	@Override
	public long nextDelay(double progress) {
		for (Interval i : highRateIntervals) {
			if (i.contains(progress))
				return highRateGenerator.nextDelay(progress);
		}
		return lowRateGenerator.nextDelay(progress);
	}

}
