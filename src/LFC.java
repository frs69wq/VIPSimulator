import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class LFC extends Process {

	// A Logical File Catalog service is defined by:
	// hostName: the name of the host that runs the service
	// catalog: a vector of logical files
	protected String hostName;
	private Vector<LogicalFile> catalog;

	private Vector<String> mailboxes;

	// TODO WIP: try to have several listeners on the LFC to handle more than 
	// TODO one request at a time.
	public Vector<String> getMailboxes() {
		return mailboxes;
	}

	// A simulation can begin with some logical files referenced in the LFC.
	// In that case, the LFC process is launched with an argument which is the
	// name of a CSV file stored in working directory that contains logical 
	// file description in the following format:
	// name,size,se_1<:se_2:...:se_n>
	// The populate function reads and parses that file, create LogicalFile 
	// objects and add them to the local catalog.
	private void populate(String csvFile){
		Msg.info("Population of LFC '"+ hostName + "' from '"+ csvFile + "'");

		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			String line = "";

			while ((line = br.readLine()) != null) {
				String[] fileInfo = line.split(",");
				String[] seNames = fileInfo[2].split(":");
				LogicalFile file = new LogicalFile(fileInfo[0], 
						Long.valueOf(fileInfo[1]).longValue(), seNames);
				Msg.info("Importing file '" + file.toString());
				catalog.add(file);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// A worker might want to register a new logical file, or a replica on an 
	// existing file in the catalog. This function first checks if a file with
	// the same name already exists. If it does, it determines whether it is a
	// new replica or not. Otherwise, it creates a new entry in the catalog for
	// that file.
	private void register(LogicalFile newFile){
		if (catalog.contains((Object) newFile)){
			LogicalFile file = catalog.get(catalog.indexOf(newFile));
			if (!file.getLocations().contains((Object) newFile.getSEName())){
				// This has to be a new replica
				Msg.info("New replica for '" + newFile.getName () + "' on '" + 
						newFile.getSEName() + "'");
				file.addLocation(newFile.getSEName());
			} else {
				Msg.debug(file.toString() + "is already registered");
			}
		} else {
			// This file is not registered yet, create and add it
			Msg.debug ("'" + newFile.getName() + "' is not registered yet");
			catalog.add(newFile);
			Msg.debug("LFC '"+ hostName + "' registered " + newFile.toString());
		}
	}

	public LFC(Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = getHost().getName();
		this.catalog = new Vector<>();
		this.mailboxes = new Vector<>();
		// TODO WIP: try to have several listeners on the LFC to handle more 
		// TODO than one request at a time.
		for (int i=0; i<3; this.mailboxes.add(this.hostName+"_"+i++));
		this.mailboxes.add(this.hostName);
	}

	public void main(String[] args) {
		boolean stop = false;

		Msg.debug("Register LFC on "+ hostName);
		VIPSimulator.getLFCList().add(this);

		// If this LFC process is started with an argument, we populate the
		// catalog from the CSV file given as args[0]
		String csvFile = (args.length > 0 ? args[0] : null);

		if (csvFile != null){
			populate(csvFile);
			Msg.debug(this.toString());
		}

		while (!stop){
			LFCMessage message = LFCMessage.getFrom(hostName);
			// To prevent message mixing, a specific mailbox is used whose name
			// is the concatenation of LFC's hostName and sender mailbox
			String returnMailbox = hostName+message.getSenderMailbox();
			
			switch(message.getType()){
			case REGISTER_FILE:
				// Add an entry for the received logical file, if needed
				// Then send back an ACK to the the sender
				register(message.getFile());

				LFCMessage.sendTo(returnMailbox, LFCMessage.Type.REGISTER_ACK);
				Msg.debug("LFC '"+ hostName + "' sent back an ACK to '" +
						message.getSenderMailbox() + "'");
				break;
			case ASK_LOGICAL_FILE:
				LogicalFile file = 
					getLogicalFileByName(message.getLogicalName());

				// If this logical file is available on several locations, a 
				// single one is selected before returning the file to the 
				// worker asking for it.
				file.selectLocation();
				LFCMessage.sendTo(returnMailbox, 
						LFCMessage.Type.SEND_LOGICAL_FILE, file);
				Msg.debug("LFC '"+ hostName + "' returned Logical " + 
						file.toString() + " back to '" + 
						message.getSenderMailbox() + "'");
				break;
			case ASK_LS:
				Vector<LogicalFile> directoryContents = 
					new Vector<LogicalFile>();
				for (LogicalFile f : catalog) 
					if (f.getName().matches(message.getLogicalName()+"(.*)"))
						directoryContents.add(f);
				LFCMessage.sendTo(returnMailbox, LFCMessage.Type.SEND_LS,
						directoryContents);
				break;
			default:
				break;
			}
		}
	}

	public LogicalFile getLogicalFileByName (String logicalFileName) {
		LogicalFile file = catalog.get(catalog.indexOf((Object) 
				new LogicalFile (logicalFileName, 0, "")));
		if(file == null){
			Msg.error("File '" + logicalFileName + 
					"' is stored on no SE. Exiting with status 1");
			System.exit(1);
		}

		return file;
	}

	public String getSEName (String logicalFileName){
		return getLogicalFileByName(logicalFileName).getSEName();
	}

	public String toString () {
		return catalog.toString();
	}
}