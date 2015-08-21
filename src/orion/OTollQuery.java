package orion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import xtream.Globals;
import xtream.core.Core;
import xtream.io.AggOutPort;
import xtream.io.TxtFileOutPort;
import xtream.plr.PPos;
import xtream.structures.ITuple;
import xtream.structures.InPort;
import xtream.structures.TupleQueue;
import cern.jet.random.engine.MersenneTwister;

public class OTollQuery extends InPort{
	

    protected BufferedReader inStream;
    protected String fileName; // name of file to read data
    protected long startTime; // start time of beginning read data (millisecond). tuple t1 should be inserted in time: t1.timestamp + startTime
    protected long time; // assigned time to run 0: unlimited
    protected String inName = "UNKNOWN FileInPort";
    protected Core mainCore;
    protected long totalReadTuples; // total number of read tuples
    protected MersenneTwister coltRandomEngine;
    protected Connection con; // Postgres Connection
    protected PreparedStatement pst;
    protected CallableStatement cstmt;
    protected long systemStartTime;
	protected String sqlcmd1 = "INSERT INTO positions VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	protected String sqlcmd2 = "DELETE FROM positions"; // to clear the table on start
	protected TxtFileOutPort outp;
	protected int periods; //toll evaluation periods
    protected AggOutPort inpAgg; // to put statistics about input
    /*********************************
     * Constructor
     * @param name name of inport
     * @param outp out port to store tolls (open/close automatic)
     * @param con connection to orion DB
     * @param systemStartTime system start time in milli sec
     * @param time total time to run (millisec)
     * @param periods toll evaluation periods
     */
    public OTollQuery(String name, TxtFileOutPort outp, Connection con, long systemStartTime, long time, int periods)
    {
        super(name);
        this.time=time;
        this.periods = periods;
        inName = name;
        this.outp = outp;
        this.con = con;
        this.systemStartTime = systemStartTime;
        totalReadTuples = 0;
        
        try
        {
        	outp.Open();
			cstmt = con.prepareCall("{call \"GetAllTolls\"(?,?,?)}");

        } catch (SQLException e) {
			e.printStackTrace();
		}

    } //Constructor

    /*******************************
     *Add Queue
     * @param q Queue to add to list of queues
     */
    public void AddQueue(TupleQueue q)
    {
        queues.add(q);
    } // AddQueue
	
    
    public void AddOutPort(TxtFileOutPort outp, int index) {
        this.outp = outp;
	}

    public void AddAggOutPort(AggOutPort outp) {
        this.inpAgg = outp;
	}
    /*****************************
     * finalize
     * invoked by Java Garbage Collector
     */
    public void finalize()
    {
        try
        {
        	outp.Close();
        	cstmt.close();
        	pst.close();
        } catch (SQLException e) {
			e.printStackTrace();
		}
    } //finalize

    /**********************************
     * run
     * to run it as a thread
     */
    public void run()
    {
        startTime = System.currentTimeMillis();
        int a = 0;
        Float[] lastDir0 = new Float[100];
        Float[] lastDir1 = new Float[100];
        for (int c=0; c<100; c++)
        {
        	lastDir0[c] = 0f;
        	lastDir1[c] = 0f;
        }
        try
        {
            while ((time == 0) || (System.currentTimeMillis()-startTime < time))
            {
            	long curTime = System.currentTimeMillis() - systemStartTime;
            	cstmt.clearParameters();
            	cstmt.registerOutParameter(1, java.sql.Types.ARRAY);
                cstmt.registerOutParameter(2, java.sql.Types.ARRAY);
                cstmt.setInt(3, (int)curTime);
                long startTime = System.currentTimeMillis();
                cstmt.execute();
                long execDelay = System.currentTimeMillis() - startTime;
                Float[] dir1 = (Float[]) cstmt.getArray(2).getArray();
                Float[] dir0 = (Float[]) cstmt.getArray(1).getArray();
                for (int c=0;c<100; c++) {
                	if (dir0[c] != lastDir0[c])
                		outp.WriteStr(curTime+","+(curTime-execDelay)+",0,0,"+dir0[c]);
                	if (dir1[c] != lastDir1[c])
                		outp.WriteStr(curTime+","+(curTime-execDelay)+",1,0,"+dir1[c]);
                }
                lastDir0 = dir0;
                lastDir1 = dir1;
                if (periods != 0) 
                	sleep(Math.max(periods,execDelay)); //sleep before the next evaluation
            } //while true
        } catch (Exception exp) //catch1
        {
            exp.printStackTrace();
        } //catch2
    } //run

    /************************************
     * insert
     * new tuple to all registered queues
     * (it does shadow copy to all queues not deep copy)
     * @param tp tuple to add
     * @todo if is not necessary and should be removed
     */
    private void Insert(ITuple tp)
    {
		
    } //Insert

}
