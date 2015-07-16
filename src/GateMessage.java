import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;

public class GateMessage extends Message {
	private long particleNumber;

	public long getParticleNumber() {
		return particleNumber;
	}

	private GateMessage(Type type, long particleNumber) {
		super(type.toString(), 1, 100);
		this.type = type;
		this.particleNumber = particleNumber;
	}

	public static void sendTo(String destination, Type type, 
			long particleNumber) {
		GateMessage m = new GateMessage(type, particleNumber);
		try{
			Msg.debug("Sending a '" + type.toString() + "' message to '" + 
					destination +"'");
			m.send(destination);
		} catch (MsgException e) {
			Msg.error("Something went wrong when emitting a '" +
				type.toString() +"' message to '" + destination + "'");
			e.printStackTrace();
		}
	}
}
