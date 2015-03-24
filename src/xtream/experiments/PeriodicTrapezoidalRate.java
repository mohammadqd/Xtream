/**
 * Project: Xtream
 * Module:
 * Task:
 * Last Modify:
 * Created:
 * Developer: Mohammad Ghalambor Dezfuli (mghalambor@iust.ac.ir & @ gmail.com)
 *
 * LICENSE:
 *    
 * This file is part of the Xtream project.
 *
 * Xtream is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xtream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xtream.  If not, see <http://www.gnu.org/licenses/>.
 */
package xtream.experiments;

/**
 * .........========
 * ......../........\
 * ......./..........\
 * .=====/............=====
 * .{low}{}{highrate}{}{low}
 * 
 * @author ghalambor
 *
 */
public class PeriodicTrapezoidalRate extends ASyntheticInputDelayGenerator {

	protected long totalTime; 
	protected long periodTime;
	
	protected long lowRate;
	protected long highRate;
	protected double highRatePortion; // in [0,1]
	protected double lowRatePortion; // in [0,1]
	
	protected PeriodicLinearRate part2,part4;
	protected PeriodicConstantRate part1,part3,part5;
	protected double[] progressBorders;


	/**
	 * @param totalTime 
	 * @param periodTime
	 * @param highRatePortion in [0,1]
	 * @param lowRatePortion in [0,1]
	 * @param highRate tuples in period
	 * @param lowRate tuples in period
	 */
	public PeriodicTrapezoidalRate(long totalTime,long periodTime,double highRatePortion,double lowRatePortion,long highRate,long lowRate) {
		super();
		this.periodTime = periodTime;
		this.totalTime = totalTime;
		this.lowRate = lowRate;
		this.highRate = highRate;
		this.highRatePortion = highRatePortion;
		this.lowRatePortion = lowRatePortion;
		progressBorders = new double[4];
		progressBorders[0] = lowRatePortion/2;
		progressBorders[1] = (1-lowRatePortion-highRatePortion)/2+progressBorders[0];
		progressBorders[2] = highRatePortion+progressBorders[1];
		progressBorders[3] = (1-lowRatePortion-highRatePortion)/2+progressBorders[2];
		part1 = new PeriodicConstantRate(lowRate, periodTime);
		part5 = new PeriodicConstantRate(lowRate, periodTime);
		part3 = new PeriodicConstantRate(highRate, periodTime);
		double timeForEvenParts = (1d-(highRatePortion+lowRatePortion))*totalTime/2;
		part2 = new PeriodicLinearRate((double)periodTime/timeForEvenParts, periodTime, lowRate, highRate);
		part4 = new PeriodicLinearRate((double)periodTime/timeForEvenParts, periodTime, highRate, lowRate);
	}

	/* (non-Javadoc)
	 * @see xtream.experiments.ASyntheticInputDelayGenerator#nextDelay()
	 */
	@Override
	public long nextDelay(double progress) {
		if (progress<progressBorders[0])
			return part1.nextDelay(progress/(lowRatePortion/2));
		else if (progress<progressBorders[1])
			return part2.nextDelay((progress-progressBorders[0])/((1-lowRatePortion-highRatePortion)/2));
		else if (progress<progressBorders[2])
			return part3.nextDelay((progress-progressBorders[1])/highRatePortion);
		else if (progress<progressBorders[3])
			return part4.nextDelay((progress-progressBorders[2])/((1-lowRatePortion-highRatePortion)/2));
		else
			return part5.nextDelay((progress-progressBorders[3])/(lowRatePortion/2));
	}
}
