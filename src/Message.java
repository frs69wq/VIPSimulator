import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Task;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public abstract class Message  extends Task {
	protected enum Type{
		// Messages to and from SE
		DOWNLOAD_REQUEST,
		FILE_TRANSFER,
		UPLOAD_ACK,
		// Messages to and from LFC
		ASK_LOGICAL_FILE,
		SEND_LOGICAL_FILE,
		REGISTER_FILE,
		REGISTER_ACK,
		ASK_LS,
		SEND_LS,
		// Message to and from GATE jobs
		GATE_CONNECT,
		GATE_PROGRESS,
		GATE_DISCONNECT,
		// Message to and from Merge jobs
		MERGE_CONNECT,
		MERGE_DISCONNECT,
		// Message to Jobs
		BEGIN,
		CARRY_ON,
		END
	};

	protected Type type;
	protected String fileName = null;

	public Type getType() {
		return type;
	}

	public String getFileName() {
		return fileName;
	}

	public static Message getFrom (String mailbox) {
		Message message = null;
		try {
			message = (Message) Task.receive(mailbox);
			Msg.debug("Received a '" + message.getType().toString() + 
					"' message from " + mailbox);
			// Simulate the cost of the local processing of the request.
			// Depends on the value set when the Message was created
			message.execute();
		} catch (MsgException e) {
			e.printStackTrace();
		}

		return message;
	}

	protected Message(String name, double flopAmount, double byteAmount){
		super(name, flopAmount, byteAmount);
	}

	public void execute() throws  HostFailureException, TaskCancelledException{
		super.execute();
	}

}
