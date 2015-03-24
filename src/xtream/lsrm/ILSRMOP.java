package xtream.lsrm;

/**
 * @author mohammad
 *
 */
public interface ILSRMOP extends Comparable<ILSRMOP> {

	/**
	 * @param newDropRatio in [0,1]
	 */
	public void ChangeDropRatio(final double newDropRatio);

	/**
	 * @return total periodic mem release capability (byte)
	 */
	public long getGain();
	
	/**
	 * @return total periodic TQoS loss
	 */
	public double getLoss();

}
