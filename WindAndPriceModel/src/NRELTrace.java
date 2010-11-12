import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;


public class NRELTrace extends TimeSeries {
	public NRELTrace(String filename) throws IOException, ParseException {
		// Assume all dates are in US/Pacific time
		//TimeZone tz = TimeZone.getTimeZone("US/Pacific");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(tz);	
		
		BufferedReader input = new BufferedReader(new FileReader(filename));
		String line;
		input.readLine(); //Skip header
		while ((line = input.readLine()) != null) {
			StringTokenizer token = new StringTokenizer(line, ",");

			//Calendar cal = new GregorianCalendar(tz);
			//cal.setTime(sdf.parse(token.nextToken().trim()));
			Date date = sdf.parse(token.nextToken().trim());
			double windSpeed = Double.parseDouble(token.nextToken().trim());
			double ratedPower = Double.parseDouble(token.nextToken().trim());
			double scoreLight = Double.parseDouble(token.nextToken().trim());
			double correctedScore = Double.parseDouble(token.nextToken().trim());			
			this.times.add(date);
			this.values.add(correctedScore);
		}
		
	}

}
