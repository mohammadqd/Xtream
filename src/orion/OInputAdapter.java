package orion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Array;
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

public class OInputAdapter extends InPort{
	

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
    protected long systemStartTime;
	protected String sqlcmd1 = "INSERT INTO positions VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	protected String sqlcmd2 = "DELETE FROM positions"; // to clear the table on start


    TxtFileOutPort outp; // out port (for prob)
    AggOutPort inpAgg; // to put statistics about input
    /*********************************
     * Constructor
     * @param name name of inport
     * @param nameOfFile name of input file
     */
    public OInputAdapter(String name, String nameOfFile,  int rndEngSeed, Connection con, long systemStartTime, long time)
    {
        super(name);
        this.time=time;
        inName = name;
        this.con = con;
        this.systemStartTime = systemStartTime;
        fileName = nameOfFile;
        totalReadTuples = 0;
        coltRandomEngine = new MersenneTwister(rndEngSeed);
        
        try
        {
            inStream = new BufferedReader(new InputStreamReader(new
                    FileInputStream(fileName)));
            Statement st = con.createStatement();
            st.executeUpdate(sqlcmd2); // clear position table
    		pst = con.prepareStatement(sqlcmd1,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);

        } catch (java.io.IOException exp) //try
        {
            exp.printStackTrace();
        } //catch
        catch (SQLException e) {
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
            inStream.close();
            pst.close();
        } catch (java.io.IOException exp) //try
        {
            exp.printStackTrace();
        } //catch
        catch (SQLException e) {
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
        try
        {
            while ((time == 0) || (System.currentTimeMillis()-startTime < time))
            {
                String nextTupleStr = inStream.readLine();
                if (nextTupleStr == null)
                {
                    return; // end of file
                }
                PPos nextTuple = new PPos(System.currentTimeMillis()-systemStartTime,coltRandomEngine);
                nextTuple.DispatchString(nextTupleStr);
//                inpAgg.WriteAggValue(nextTuple.time, 1); // input statistics
                long nextTime = startTime + nextTuple.time; // arrival time of next tuple
                long deltaTime = nextTime - System.currentTimeMillis(); // how many milliseconds remained to input time of next tuple
                if (deltaTime > 0) sleep(deltaTime); //sleep till input time of next tuple
                nextTuple.sysInTS = System.currentTimeMillis();
                Insert(nextTuple); // add nextTuple to queues
            } //while true
        } catch (java.io.EOFException exp) //try,   end of input file
        {
            System.out.println("End of input: " + inName);
            return;
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
    	try {
    		Float[] confs = (((PPos)tp).GetSegProbabilitiesf(0));
    		Float[] lane = new Float[] {(float)((PPos)tp).lane[0],(float)((PPos)tp).lane[1],(float)((PPos)tp).lane[2]};
    		Array segs = con.createArrayOf("float", confs);
    		Array lanes = con.createArrayOf("float", lane);
    		pst.clearParameters();
    		pst.setInt(1,(int)((PPos)tp).time);
    		pst.setInt(2,((PPos)tp).vid);
    		pst.setDouble(3,((PPos)tp).spd_mean);
    		pst.setDouble(4,((PPos)tp).spd_var);
    		pst.setInt(5,((PPos)tp).xway);
    		pst.setInt(6,((PPos)tp).dir);
    		pst.setDouble(7,((PPos)tp).pos_mean);
    		pst.setDouble(8,((PPos)tp).pos_var);
    		pst.setDouble(9,((PPos)tp).conf);
    		pst.setArray(10, segs);
    		pst.setArray(11, lanes);
    		pst.execute();
    	}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
    } //Insert

}
