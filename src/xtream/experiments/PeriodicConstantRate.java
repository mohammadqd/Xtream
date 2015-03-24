package xtream.experiments;

public class PeriodicConstantRate extends ASyntheticInputDelayGenerator {
	
	protected long rate; // tuples per period
	protected long periodSize; //millisec



	public PeriodicConstantRate(long rate, long periodSize) {
		super();
		this.rate = rate;
		this.periodSize = periodSize;
	}

	@Override
	public long nextDelay(double progress) {
		return periodSize/rate;
	}

}
