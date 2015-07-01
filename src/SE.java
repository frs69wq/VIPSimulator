import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class SE extends Process {

	protected String hostName;

	public SE (Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = this.getHost().getName();
	}

	public void main(String[] args) {
		boolean stop = false;
		Msg.debug("Register SE on "+ hostName);
		VIPSimulator.seList.add(hostName);

		while (!stop){
			Message message = Message.getFrom(hostName);

			switch(message.getType()){
			case DOWNLOAD_REQUEST:
				Message.sendAsynchronouslyTo(message.getSenderMailbox(), 
						Message.Type.SEND_FILE,
						message.getLogicalFileSize());

				Msg.debug("SE '"+ hostName + "' sent file '" +
						message.getLogicalFileName() + "' of size " +
						message.getLogicalFileSize() + " to '" +
						message.getSenderMailbox() + "'");
				break;
			case UPLOAD_REQUEST:
				Message.sendTo(message.getSenderMailbox(), 
						Message.Type.UPLOAD_ACK);

				Msg.debug("SE '"+ hostName + "' sent ack back to '" +
						message.getSenderMailbox() + "'");
				break;
			case FINALIZE:
				Msg.verb("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}
}
