import org.simgrid.msg.Msg;


public class Timer {
	private double value;
	private double delay;

	public Timer() {
		this.value = 0;
	}

	public double getValue() {
		return value;
	}

	public void start(){
		this.delay = Msg.getClock();
	}

	public void stop(){
		this.delay = Msg.getClock() - delay;
		this.value += delay;
	}

}
