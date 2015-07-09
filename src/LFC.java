import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;

public class LFC extends GridService {

	// A Logical File Catalog service is defined by:
	// hostName: the name of the host that runs the service
	// catalog: a vector of logical files
	private Vector<LogicalFile> catalog;

	// A simulation can begin with some logical files referenced in the LFC.
	// In that case, the LFC process is launched with an argument which is the
	// name of a CSV file stored in working directory that contains logical 
	// file description in the following format:
	// name,size,se_1<:se_2:...:se_n>
	// The populate function reads and parses that file, create LogicalFile 
	// objects and add them to the local catalog.
	private void populate(String csvFile){
		Msg.info("Population of LFC '"+ name + "' from '"+ csvFile + "'");

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
	private void addToCatalog(LogicalFile newFile){
		Msg.debug("Inserting '"+ newFile.toString() + "'into the catalog");
		if (catalog.contains((Object) newFile)){
			LogicalFile file = catalog.get(catalog.indexOf(newFile));
			if (!file.getLocations().contains((Object) newFile.getLocation())){
				// This has to be a new replica
				Msg.debug("New replica for '" + newFile.getName () + "' on '" + 
						newFile.getLocation() + "'");
				file.addLocation(newFile.getLocation());
			} else {
				Msg.debug(file.toString() + "is already registered");
			}
		} else {
			// This file is not registered yet, create and add it
			Msg.debug ("'" + newFile.getName() + "' is not registered yet");
			catalog.add(newFile);
		}
		Msg.debug("LFC '"+ name + "' registered " + newFile.toString());
	}

	private void sendAckTo (String mailbox) {
		LFCMessage.sendTo(mailbox, LFCMessage.Type.REGISTER_ACK, null, null);
		Msg.debug("'LFC@" + getName()+ "' sent an ACK on '" + mailbox + "'");
	}

	private void sendLogicalFile (String mailbox, LogicalFile file) {
		Vector<LogicalFile> list = new Vector<LogicalFile>();
		list.add(file);
		LFCMessage.sendTo(mailbox, Message.Type.SEND_LOGICAL_FILE, null, list);
		Msg.debug("'LFC@"+ name + "' sent logical " + file.toString() + 
				" back on '" + mailbox + "'");
	}

	private void sendLogicalFileList (String mailbox, 
			Vector<LogicalFile> list) {
		LFCMessage.sendTo(mailbox, Message.Type.SEND_LOGICAL_FILE, null, list);
	}

	public LFC(Host host, String name, String[]args) {
		super(host,name,args);
		this.catalog = new Vector<LogicalFile>();
	}

	public void main(String[] args) throws MsgException {
		Msg.debug("Register LFC on "+ name);
		VIPSimulator.getLFCList().add(this);

		// If this LFC process is started with an argument, we populate the
		// catalog from the CSV file given as args[0]
		String csvFile = (args.length > 0 ? args[0] : null);

		if (csvFile != null){
			populate(csvFile);
			Msg.debug(this.toString());
		}

		for (int i=0; i<3; i++){
			mailboxes.add(new Process(name, name+"_"+i) {
				public void main(String[] args) throws MsgException {
					String mailbox = getName();

					Msg.debug("Create a new mailbox on: " + mailbox);
					while (true){
						LFCMessage message = 
								(LFCMessage) Message.getFrom(mailbox);

						switch(message.getType()){
						case REGISTER_FILE:
							// Add an entry in the catalog for the received 
							// logical file, if needed.
							addToCatalog(message.getFile());
							// Then send back an ACK to the the sender.
							sendAckTo("return-" + mailbox);
							break;
						case ASK_LOGICAL_FILE:
							LogicalFile file = 
								getLogicalFileByName(message.getFileName());

							// If this logical file is available on several 
							// locations, one is selected before returning 
							// the file to the worker asking for it.
							file.selectLocation();

							// Send this file back to the sender
							sendLogicalFile ("return-" + mailbox, file);
							break;
						case ASK_LS:
							Vector<LogicalFile> directoryContents = 
							new Vector<LogicalFile>();
							for (LogicalFile f : catalog) 
								if (f.getName().matches(
										message.getFileName()+"(.*)"))
									directoryContents.add(f);
							// Send the directory contents back to the sender
							sendLogicalFileList ("return-" + mailbox,
									directoryContents);
							break;
						default:
							break;
						}
					}
				}
			});
			mailboxes.lastElement().start();
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

	public String toString () {
		return catalog.toString();
	}

	public void register (LogicalFile file) {
		String mailbox = this.findAvailableMailbox(10);
		Vector<LogicalFile> list = new Vector<LogicalFile>();
		list.add(file);
		LFCMessage.sendTo(mailbox, Message.Type.REGISTER_FILE, null, list);
		Message.getFrom("return-"+mailbox);
	}

	public LogicalFile getLogicalFile (String logicalFileName) {
		String mailbox = this.findAvailableMailbox(10);
		LFCMessage.sendTo(mailbox, Message.Type.ASK_LOGICAL_FILE, 
				logicalFileName, null);
		Msg.info("Asked about '" + logicalFileName + 
				"'. Waiting for information ...");

		LFCMessage m = (LFCMessage) Message.getFrom("return-"+mailbox);
		return m.getFile();
	}

	public Vector<LogicalFile> getLogicalDirectoryContents(
			String directoryName){
		String mailbox = this.findAvailableMailbox(10);
		LFCMessage.sendTo(mailbox, Message.Type.ASK_LS, directoryName, null);
		Msg.info("Asked for list of files to merge in '" + directoryName + 
				"'. Waiting for reply ...");
		LFCMessage m = (LFCMessage) Message.getFrom("return-"+mailbox);
		return m.getFileList();
	}


}