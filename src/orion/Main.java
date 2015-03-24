package orion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import xtream.plr.PLRInPort;
import xtream.structures.TxtFileOutPort;


public class Main {

	/**
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
    	// Configuration
    	long runtime = 1*60*1000; // system run time (msec)
    	int numOfXWays = 1;
    	int timeThresh = 30000;
    	double probThresh = 0.01;
    	int joinMaxTimeDiff = 0;
    	long monitoringPeriod = 60000; // for system monitor thread (msec)
    	int tollEvalPeriods = 10;
    	////////////////			
    	long systemStartTime = System.currentTimeMillis();
    	System.out.println("Orion: Starting...");
        TxtFileOutPort tollsPort = new TxtFileOutPort("OrionTolls.txt");
		try {
			String url = "jdbc:postgresql://localhost/PLR";
			Connection con = DriverManager.getConnection(url,"postgres","123");
	        OInputAdapter adp = new OInputAdapter("RoadPort","data/datafile30min_1XW_modified.dat", 154,con,systemStartTime,runtime); // 154: random seed
	        OTollQuery tollComputer = new OTollQuery("TollComputer", tollsPort, con, systemStartTime, runtime, tollEvalPeriods);
	        adp.setPriority(Thread.MAX_PRIORITY);
	        tollComputer.setPriority(Thread.NORM_PRIORITY);
	        adp.start(); // run it as a seperate thread
	        tollComputer.start();
	        while (adp.isAlive() || tollComputer.isAlive()) 
	        {
//	        	if (rport.isAlive()) rport.interrupt();
	        	Thread.currentThread().sleep(1000);
	        }
			con.close();

		} catch (SQLException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Orion: Finish...");
	}

}
