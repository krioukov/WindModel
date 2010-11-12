import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import umontreal.iro.lecuyer.probdist.WeibullDist;


public class WindModel {
	private ArrayList<WindFarm> farms;
	private TimeSeries wind;
	private TimeSeries wind_hourly;
	private TimeSeries wind_norm;
	private ArrayList<ArrayList<Double>> wind_seasonal;

	private static long HOUR = 60*60*1000;
	public enum Season { SPRING, SUMMER, FALL, WINTER };
	
	public WindModel(ArrayList<WindFarm> farms) throws Exception {
		this.farms = farms;
		this.wind = new TimeSeries();
		this.wind_hourly = new TimeSeries();
		this.wind_seasonal = new ArrayList<ArrayList<Double>>(4);
		for (int s = 0; s < 4; s++) this.wind_seasonal.add(new ArrayList<Double>(24));
		this.wind_norm = new TimeSeries();
		
		sum_wind_farms();
		avg_hour();
		seasonal_avg();
		norm_wind();
		
		double[] params = WeibullDist.getMLE(this.wind_norm.get_values(), this.wind_norm.length());
		double k = params[0];
		double lambda = 1 / params[1];
		
		System.out.println("k: " + k);
		System.out.println("lambda: " + lambda);
	}
	
	/**
	 * Computes the normalized hourly wind trace. Divides trace by seasonal, hourly mean.
	 */
	private void norm_wind() {
		for (int i = 0; i < this.wind_hourly.length(); i++) {
			Date date = this.wind_hourly.get_time(i);
			int season = get_season(date).ordinal();
			int hour = get_hour(date);
			// Divide trace by seasonal, hourly mean
			double norm = this.wind_hourly.get_value(i) / this.wind_seasonal.get(season).get(hour);
			this.wind_norm.add(date, norm);
		}
		
	}

	/**
	 * Sums traces of all wind farms to produce a trace of total wind energy
	 * @throws Exception All wind farm traces must have the same times. Does not resample data.
	 */
	private void sum_wind_farms() throws Exception {
		wind = new TimeSeries();		
		for (int i = 0; i < farms.get(0).length(); i++) {
			Date time = farms.get(0).get_time(i);
			double sum = 0;			
			for (WindFarm f : farms) {
				if (!f.get_time(i).equals(time)) throw new Exception("All wind farms must have the same times");
				sum += f.get_value(i);
			}
			wind.add(time, sum);
		}
	}
	
	/**
	 * Takes the hourly average of the wind trace
	 */
	private void avg_hour() {
		/* Average measurements over hour blocks. Does not assume readings are
		   at fixed intervals, but does assume there is at least one reading
		   per hour
		*/
		Date hour_start = this.wind.get_time(0);
		double sum = 0;
		int count = 0;
		for (int i = 0; i < this.wind.length(); i++) {
			Date time = this.wind.get_time(i);
			Date next_hour = new Date(hour_start.getTime()  + HOUR);
			if (!time.before(next_hour)) {
				this.wind_hourly.add(hour_start, sum / count);
				sum = 0;
				count = 0;
				hour_start = next_hour;
			}
			sum += this.wind.get_value(i);
			count++;
		}
		this.wind_hourly.add(hour_start, sum / count);
	}
	
	/**
	 * Computes the average wind power for each hour of the day in each season
	 */
	private void seasonal_avg() {	
		//Temporary array of (seasons, hours, readings)
		ArrayList<ArrayList<ArrayList<Double>>> season_hour_reading = new ArrayList<ArrayList<ArrayList<Double>>>();		
		for (int s = 0; s < 4; s++) {
			ArrayList<ArrayList<Double>> hours = new ArrayList<ArrayList<Double>>();
			for (int h = 0; h < 24; h++) hours.add(new ArrayList<Double>());
			season_hour_reading.add(hours);
		}
		for (int i = 0; i < wind_hourly.length(); i++) {
			int season = get_season(wind_hourly.get_time(i)).ordinal();
			int hour = get_hour(wind_hourly.get_time(i));
			Double reading = wind_hourly.get_value(i);
			season_hour_reading.get(season).get(hour).add(reading);
		}
		for (int s = 0; s < 4; s++) {
			for (int h = 0; h < 24; h++) {
				double sum = 0;
				ArrayList<Double> readings = season_hour_reading.get(s).get(h);
				for (Double d : readings) sum += d;
				wind_seasonal.get(s).add(sum / readings.size());
			}
		}
	}

	/**
	 * Returns true if time is in range [start_month/start_day, end_month/end_day)
	 */
	private boolean in_range(Calendar time, int start_month, int start_day, int end_month, int end_day) {
		int month = time.get(Calendar.MONTH);
		int day = time.get(Calendar.DAY_OF_MONTH);
		return ( (month == start_month && day >= start_day) ||
				 (month > start_month && month < end_month) ||
				 (month == end_month && day < end_day) );
	}
	
	/**
	 * Returns the season of time.
	 */
	private Season get_season(Date date) {
		Calendar time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		time.setTime(date);
		//if (in_range(time, Calendar.MARCH, 21, Calendar.JUNE, 21)) return Season.SPRING;
		//else if (in_range(time, Calendar.JUNE, 21, Calendar.SEPTEMBER, 23)) return Season.SUMMER;
		//else if (in_range(time, Calendar.SEPTEMBER, 23, Calendar.DECEMBER, 21)) return Season.FALL;
		//else return Season.WINTER;
		
		if (in_range(time, Calendar.MARCH, 20, Calendar.JUNE, 21)) return Season.SPRING;
		else if (in_range(time, Calendar.JUNE, 21, Calendar.SEPTEMBER, 23)) return Season.SUMMER;
		else if (in_range(time, Calendar.SEPTEMBER, 23, Calendar.DECEMBER, 22)) return Season.FALL;
		else return Season.WINTER;		
	}

	private int get_hour(Date date) {
		Calendar time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		time.setTime(date);
		return time.get(Calendar.HOUR_OF_DAY);
	}
}
