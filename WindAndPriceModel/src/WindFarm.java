
public class WindFarm extends TimeSeries {
	
	public WindFarm(TimeSeries trace, double current_capacity, double scaled_capacity) {
		//Scale the trace
		for (int i = 0; i < trace.length(); i++) {
			this.add(trace.get_time(i), trace.get_value(i) * scaled_capacity / current_capacity );
		}
	}
}
