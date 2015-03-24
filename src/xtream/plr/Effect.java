/**
 * 
 */
package xtream.plr;

/**
 * @author mohammad
 * effect of a new tuple on final results
 */
public class Effect {
	public double[] seg; // one value for each segment
	public long time; // system time that the effects should be applied (0: apply it now)
	
	public Effect()
	{
		seg = new double[100];
		time = 0; // now
	}
}
