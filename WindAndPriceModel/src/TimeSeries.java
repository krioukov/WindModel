import java.util.ArrayList;
import java.util.Date;
import java.util.Date;
import java.util.Iterator;

public class TimeSeries {
	protected ArrayList<Date> times;
	protected ArrayList<Double> values;
	
	public TimeSeries() {
		times = new ArrayList<Date>();
		values = new ArrayList<Double>();
	}
	
	public ArrayList<Date> get_times() {
		return times;
	}
	
	public double[] get_values() {
		double vals[] = new double[this.length()];
		for (int i = 0; i < this.length(); i++)
			vals[i] = values.get(i);
		return vals;
	}
	
	public int length() {
		return times.size();
	}
	
	public void add(Date time, Double value) {
		times.add(time);
		values.add(value);
	}
	
	public Date get_time(int index) {
		return times.get(index);
	}
	
	public Double get_value(int index) {
		return values.get(index);
	}
}