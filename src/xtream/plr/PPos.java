/**
 * Project: Xtream
 * Module: Position tuples in PLR
 * Task:
 * Last Modify: May 2013
 * Created: 2011
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
package xtream.plr;

import xtream.Globals;
import xtream.structures.ITuple;
import xtream.types.BoundedNormal;

/**
 * @author mohammad
 * 
 */
public class PPos implements ITuple {

	// ------------------------------------------------------------
	// NOTE: On changing fields, update constructor & clone methods
	// ------------------------------------------------------------

	protected static final double SEGMENT_SIZE = 5280; // lenth of highway
														// segments

	// Note: Remember to update CopyValues() if you changed the variables

	public long sysInTS; // system in timestamp

	// public int type; //0:Position report 1: 2:Account Balance 3:Daily
	// Expenditure 4:Travel time
	public long time; // application time (msec)
	public int vid; // 0..150000
	// public double spd_mean; // mean speed (Gaussian Dist.) 0..100 MPH
	// public double spd_var; // variance of speed (Gaussian Dist.) 0..100 MPH^2
	public BoundedNormal speed;
	public int xway; // 0..L-1
	public int dir; // 0: to east 1:west
	public double[] lane = { 0.0d, 0.0d, 0.0d }; // lane[x]: probability that
													// car is in the lane x
													// public double pos_mean;
													// // mean position
													// (Gaussian Dist.)
													// 0..527999
	// public double pos_var; // variance of position (Gaussian Dist.)
	// 0..527999^2
	public BoundedNormal position;
	public double conf; // confidence (0,1]
	public long expirationTime; // application time for expiration (msec)
	public int numOfCoveredSegs; // number of segs that prob. of existence >
									// minp
	protected byte[] extraLoad; // this is for increasing size of tuples (for
								// experiments)
	protected long responseTime;

	/**************************************
	 * Constructor
	 */
	public PPos(long curSysTime) {
		super();
		extraLoad = new byte[Globals.TUPLE_EXTRA_SIZE]; // each tuple has this
														// extra load for
														// evaluations
		sysInTS = curSysTime; // get current system time
		responseTime = -1; // not valid and not set
	}// PosReport

	/**************************************
	 * tostring
	 */
	public String toString() {
		String outstr = "";
		outstr = "(Type: Position Report" + ",App Time:" + time + ",Sys In: "
				+ sysInTS + " ,Sys RT: " + responseTime + ",VID: " + vid
				+ ",Spd: G(" + speed.getMean() + "," + speed.getStd()
				+ "),XWay:" + xway + ",Lane:[" + lane[0] + "," + lane[1] + ","
				+ lane[2] + "]" + ",Dir:" + dir + ",Pos: G("
				+ position.getMean() + "," + position.getStd() + "), Conf: "
				+ conf + ", Seg: " + GetSeg() + ")";
		return outstr;
	}// toString

	/**
	 * to copy current tuple values into a given parameter without extraLoad
	 * 
	 * @param tpl
	 *            tuple to copy into
	 * @return true: success false: different types and impossible to copy
	 */
	public boolean CopyValues(ITuple tpl) {
		if (tpl.getClass() == this.getClass()) // of the same type
		{
			PPos newPos = (PPos) tpl;
			newPos.dir = dir;
			newPos.lane[0] = lane[0];
			newPos.lane[1] = lane[1];
			newPos.lane[2] = lane[2];
			newPos.speed = new BoundedNormal(speed.getL(), speed.getU(),
					speed.getMean(), speed.getStd(), speed.getRndeng());
			newPos.position = new BoundedNormal(position.getL(),
					position.getU(), position.getMean(), position.getStd(),
					position.getRndeng());
			newPos.sysInTS = sysInTS;
			newPos.time = time;
			newPos.vid = vid;
			newPos.xway = xway;
			newPos.conf = conf;
			newPos.expirationTime = expirationTime;
			newPos.extraLoad = (extraLoad != null) ? new byte[extraLoad.length]
					: null;
			newPos.responseTime = responseTime;
			return true;
		} else
			return false;
	}

	/**************************************
	 * DispatchString
	 * 
	 * @param argstr
	 *            a string to dispatch (e.g. "")
	 * @return 0: OK >0: Error in dispatching More Info:
	 */
	public int DispatchString(String argstr) {
		String fields[];
		try {
			fields = argstr.split(",");
			// type.SetValue(Integer.parseInt(fields[0])); // field 0 is type
			if (Globals.SYNTHETIC_INPUT_RATE)
				time = Globals.core.GetSysCurTime();
			else
				time = Long.parseLong(fields[1])
						* Globals.INPUT_TIME_GRANULARITY;
			expirationTime = time + Globals.DEFAULT_TUPLE_EXPIRATION;
			vid = Integer.parseInt(fields[2]);
			double spd_mean = Double.parseDouble(fields[3]);
			speed = new BoundedNormal(spd_mean - Globals.SPD_ERROR_BOUND,
					spd_mean + Globals.SPD_ERROR_BOUND, spd_mean,
					Math.sqrt(Double.parseDouble(fields[4])), Globals.rndEngine);
			xway = Integer.parseInt(fields[5]);
			dir = Integer.parseInt(fields[6]);
			lane[0] = Double.parseDouble(fields[7]);
			lane[1] = Double.parseDouble(fields[8]);
			lane[2] = Double.parseDouble(fields[9]);
			double pos_mean = Double.parseDouble(fields[10]) / 528;
			position = new BoundedNormal(pos_mean - Globals.POS_ERROR_BOUND,
					pos_mean + Globals.POS_ERROR_BOUND, pos_mean,
					Globals.POS_STD /*
									 * Math.sqrt( Double .parseDouble (
									 * fields[11 ]))
									 */, Globals.rndEngine);
			conf = Globals.MIN_INPUT_CONFIDENCE
					+ Globals.inputConfidenceRandEngine.nextDouble()
					* (1 - Globals.MIN_INPUT_CONFIDENCE); // Double.parseDouble(fields[12]);

		} catch (Throwable t) {
			System.out
					.println("ERROR: Problem in PPos Dispatching! "
							+ "Probably there is a format error in input position file.");
			return 1; // Unknown Error
		}
		return 0; // OK
	}

	@Override
	public long GetSize() {
		return 15 * Double.SIZE / 8
				+ ((extraLoad != null) ? extraLoad.length : 0);
	}

	public static int GetPPosSize() {
		return (4 * Long.SIZE + 8 * Double.SIZE + 4 * Integer.SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#EqualValue(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean EqualValue(ITuple tp) {
		PPos newPos = (PPos) tp;
		if (newPos.dir == dir && newPos.lane[0] == lane[0]
				&& newPos.lane[1] == lane[1] && newPos.lane[2] == lane[2]
				&& newPos.position.Equal(position) && newPos.speed.Equal(speed)
				&& newPos.vid == vid && newPos.xway == xway
				&& newPos.conf == conf)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xtream.interfaces.ITuple#FullyEqual(xtream.interfaces.ITuple)
	 */
	@Override
	public boolean FullyEqual(ITuple tp) {
		PPos newPos = (PPos) tp;
		if (newPos.dir == dir && newPos.lane[0] == lane[0]
				&& newPos.lane[1] == lane[1] && newPos.lane[2] == lane[2]
				&& newPos.position.Equal(position) && newPos.speed.Equal(speed)
				&& newPos.sysInTS == sysInTS
				&& newPos.responseTime == responseTime && newPos.vid == vid
				&& newPos.xway == xway && newPos.conf == conf
				&& newPos.time == time
				&& newPos.expirationTime == expirationTime)
			return true;
		else
			return false;
	}

	@Override
	public int GetIndex() {
		return vid;
	}

	@Override
	public int GetType() {
		return 0;
	}

	@Override
	public long[] GetTimestamp() {
		long[] timestamp = new long[2];
		timestamp[0] = time;
		timestamp[1] = expirationTime;
		return timestamp;
	}

	/**
	 * @return segment of the pos report, regarding the mean value for position
	 */
	public int GetSeg() {
		return (int) (position.getMean() / 5280);
	}

	/**
	 * @param i
	 *            index of desired segment (0-99)
	 * @return probability of pos being in that seg
	 */
	public double GetSegP(int i) {
		return position.Prob(((double) (i) * SEGMENT_SIZE),
				((double) (i + 1) * SEGMENT_SIZE));
	}

	/**
	 * @param minP
	 *            minimum important probability (probabilities less than this
	 *            would be presented as 0)
	 * @return an array of 100 double values representing probability of pos
	 *         being in each seg
	 */
	public double[] GetSegProbabilities(double minP) {
		numOfCoveredSegs = 1;
		double[] out = new double[100];
		int meanSeg = GetSeg();
		out[meanSeg] = GetSegP(meanSeg);
		// -- Go Forward
		int i = meanSeg;
		while (i < 99 && out[i] > 0) {
			i++;
			out[i] = GetSegP(i);
			numOfCoveredSegs++;
			if (out[i] < minP) {
				out[i] = 0;
				numOfCoveredSegs--;
			}
		}
		// -- Go Backward
		i = meanSeg;
		while (i > 0 && out[i] > 0) {
			i--;
			out[i] = GetSegP(i);
			numOfCoveredSegs++;
			if (out[i] < minP) {
				out[i] = 0;
				numOfCoveredSegs--;
			}
		}

		return out;
	}

	/**
	 * @param minP
	 *            minimum important probability (probabilities less than this
	 *            would be presented as 0)
	 * @return an array of 100 double values representing probability of pos
	 *         being in each seg
	 */
	public Float[] GetSegProbabilitiesf(double minP) {
		numOfCoveredSegs = 100;
		Float[] out = new Float[100];
		for (int i = 0; i < 100; i++) {
			out[i] = (float) GetSegP(i);
		}
		return out;
	}

	/**
	 * @return the path number (XWay*2 + Dir)
	 */
	public int GetPath() {
		return xway * 2 + dir;
	}

	@Override
	public double GetConf() {
		return conf;
	}

	@Override
	public long GetResponseTime() {
		assert responseTime >= 0 : "Getting response time of an incomplete tuple!";
		if (responseTime < 0) // responseTime is not set
			return (Globals.core.GetSysCurTime() - sysInTS); // calculate and
																// return a
																// temporal
																// response time
		else
			return responseTime;
	}

	@Override
	public void SetResponseTime(long rt) {
		responseTime = rt;
	}

	@Override
	public ITuple Clone() {
		PPos newPos = new PPos(sysInTS);
		CopyValues(newPos);
		return newPos;
	}

	@Override
	public int compareTo(ITuple o) {
		return (new Long(GetTimestamp()[0])).compareTo(new Long(o
				.GetTimestamp()[0]));
	}

}
