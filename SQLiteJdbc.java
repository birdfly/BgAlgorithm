import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

class Sensor {
	
	Sensor (long started_at, long stopped_at, String uuid) {
		this.started_at = started_at;
		this.stopped_at = stopped_at;
		this.uuid = uuid;
	}

	public String toString() {
		double hours = (stopped_at - started_at) / 60000 / 60;
		double days = hours / 24; 
		DecimalFormat df = new DecimalFormat("#.00"); 
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return  dateFormat.format(started_at) + " - " +dateFormat.format(stopped_at) + " " +df.format(days);
	}
	
	long started_at;
	long stopped_at;
	String uuid;
	
}


class RawData {
	
	RawData(double raw_value, long timestamp) {
		this.raw_value = raw_value;
		this.timestamp = timestamp;		
	}

	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return  dateFormat.format(timestamp) + " " + raw_value;
	}
	
	private boolean in_range(long start, long end) {
		return timestamp >= start && timestamp <= end;
	}
	
	static List<RawData> FilterByDate(List<RawData> data, long start, long end) {
		List<RawData> RawDataList = new LinkedList <RawData>();
		for (RawData sample : data ) {
			if(sample.in_range(start, end)) {
				RawDataList.add(sample);
			}
			
		}
		return RawDataList;
	}
 	
	double raw_value;
	long timestamp;
}


class Calibration {
	
	Calibration(double measured_bg, long timestamp) {
		this.measured_bg = measured_bg;
		this.timestamp = timestamp;
	}
	
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return  dateFormat.format(timestamp) + " " + measured_bg;
	}
	
	private boolean in_range(long start, long end) {
		return timestamp >= start && timestamp <= end;
	}
	
	static List<Calibration> FilterByDate(List<Calibration> data, long start, long end) {
		List<Calibration> RawDataList = new LinkedList <Calibration>();
		for (Calibration sample : data ) {
			if(sample.in_range(start, end)) {
				RawDataList.add(sample);
			}
		}
		return RawDataList;
	}
	
	double measured_bg;
	long timestamp;
}

// A simple class to read SensorData from xDrip database
public class SQLiteJDBC
{
	public static void main( String args[] ) {
		ReadSensors("export20150814-184324.sqlite");
		ReadRawBg("export20150814-184324.sqlite");
		ReadCalibrations("export20150814-184324.sqlite");
	}
	public static List<Sensor> ReadSensors(String dbName )
	{
		Connection c = null;
		Statement stmt = null;
		List<Sensor> Sensors = new LinkedList <Sensor>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM SENSORS;" );
			while ( rs.next() ) {
				int id = rs.getInt("_id");
				String  uuid = rs.getString("uuid");
				long started_at= (long)rs.getDouble("started_at");
				long stopped_at= (long)rs.getDouble("stopped_at");
				System.out.println( "ID = " + id );
				System.out.println( "started_at = " + started_at );
				System.out.println();
				// TODO(tzachi) Fix the old sensor read time based on new sensor start time.
				Sensor sensor = new Sensor(started_at, stopped_at, uuid);
				Sensors.add(sensor);
				System.out.println(sensor);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Sensors read successfully");
		return Sensors;
	}

	public static List<RawData> ReadRawBg(String dbName )
	{
		Connection c = null;
		Statement stmt = null;
		List<RawData> RawDataList = new LinkedList <RawData>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM BGREADINGS;" );
			while ( rs.next() ) {
				double raw = rs.getDouble("raw_data");

				
				long timestamp = (long)rs.getDouble("timestamp");
				RawData rawData = new RawData(raw, timestamp);
				System.out.println(rawData);
				RawDataList.add(rawData);
			
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Rawdata read successfully");
		return RawDataList;
	}

	public static List<Calibration> ReadCalibrations(String dbName )
	{
		Connection c = null;
		Statement stmt = null;
		List<Calibration> Calibrations = new LinkedList <Calibration>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM CALIBRATION;" );
			while ( rs.next() ) {
				double measured_bg = rs.getDouble("bg");
				long timestamp = (long)rs.getDouble("timestamp");
				Calibration calibration = new Calibration(measured_bg, timestamp);
				System.out.println(calibration);
				Calibrations.add(calibration);
			
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Calibrations read successfully");
		return Calibrations;
	}

	

}