package orion;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.rowset.serial.SerialArray;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Orion Interface...");
		try {
			String url = "jdbc:postgresql://localhost/PLR";
			Connection con = DriverManager.getConnection(url,"postgres","123");
			String sqlcmd1 = "INSERT INTO positions (time,vid,seg) VALUES (?,?,?)";
			//PreparedStatement st = con.prepareStatement(sqlcmd1,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
//			st.executeUpdate("INSERT INTO utable1 VALUES ('5','norm(3,3)');");
			CallableStatement cstmt = con.prepareCall("{call \"GetAllTolls\"(?,?,?)}");
            cstmt.registerOutParameter(1, java.sql.Types.ARRAY);
            cstmt.registerOutParameter(2, java.sql.Types.ARRAY);
            cstmt.setInt(3, 60000);
            long startTime = System.currentTimeMillis();
            cstmt.execute();
            long delay = System.currentTimeMillis() - startTime;
            Float[] dir1 = (Float[]) cstmt.getArray(2).getArray();
            Float[] dir0 = (Float[]) cstmt.getArray(1).getArray();
            System.out.println("Delay: "+delay);
            for (int c=0;c<100; c++) {
            	System.out.println("dir0["+c+"]: "+(Float)(dir0[c])+"   dir1["+c+"]: "+(Float)(dir1[c]));
            }
            cstmt.close();

//			Float[] a = new Float[10];
//			a[4]=0.67f;
//			Array segs = con.createArrayOf("float", a);
//			st.setInt(1,16);
//			st.setInt(2, 25);
//			st.setArray(3, segs);
//			st.execute();
//			st.clearParameters();
//			st.setInt(1,17);
//			st.setString(2, "Farhad2");
//			st.execute();
			
			// Print table
//			rs.beforeFirst();
//			while (rs.next())
//			{
//				System.out.println("ID: " + rs.getString("id")+ " Name: "+ rs.getString("name"));
//			}
//
//			rs.close();
			con.close();

		} catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

}
