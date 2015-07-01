import org.simgrid.msg.Msg;

public class LCG {

	public static void crInput(String mailbox, String logicalFileName,
			long logicalFileSize, String SEName, String LFCName) {
		LogicalFile file = new LogicalFile(logicalFileName, logicalFileSize, 
				SEName);
		Msg.info("Ask '"+ LFCName + "' to register " + file.toString());

		Message importFile = new Message(Message.Type.CR_INPUT, file);
		importFile.sendTo(LFCName);

		Msg.debug("lcg-cr-input of '" + logicalFileName +"' on LFC '" + 
				LFCName +"' completed");
	}

	public static void cr(String mailbox, String SEName, String localFileName,
			long localFileSize, String logicalFileName, String LFCName) {
		Msg.debug("lcg-cr '" + logicalFileName + "' from '" + localFileName + 
				"' using lfc '" + LFCName +"'");

		Message uploadRequest = new Message(Message.Type.UPLOAD_REQUEST, 
				localFileSize);

		uploadRequest.sendAsynchronouslyTo(SEName);

		Msg.info("Sent upload request to SE '" + SEName + "' for file '" + 
				logicalFileName +"' of size " + localFileSize);

		//waiting for upload to finish
		Msg.debug("Host '" + mailbox + 
				"' is waiting for upload-reply from SE '" + SEName +"'");
		Message.getFrom(mailbox);
		Msg.info("SE '"+ SEName + "' replied with an ack");


		// Register file into LFC
		Msg.info("Ask '"+ LFCName + "' to register file '" + logicalFileName + 
				"' stored on SE '" + SEName + "'");
		LogicalFile file = new LogicalFile(logicalFileName, localFileSize, 
				SEName);

		Message askToRegisterFile = new Message(Message.Type.REGISTER_FILE, 
				file);
		askToRegisterFile.sendTo(LFCName);

		Message.getFrom(mailbox);
		Msg.info("LFC '"+ LFCName + "' registered file '" + 
				logicalFileName + "', stored on SE '" +	SEName + "'");

		Msg.debug("lcg-cr of '" + logicalFileName +"' on LFC '" + LFCName +
				"' completed");
	}

	public static void cp(String mailbox, String logicalFileName,
			String localFileName, String LFCName){
		String SEName = null;
		long logicalFileSize = 0;

		Msg.info("lcg-cp '" + logicalFileName + "' to '" + localFileName +
				"' using LFC '" + LFCName + "'");

		//get SE name from LFC
		Message askFileInfo = new Message(Message.Type.ASK_LOGICAL_FILE,
				logicalFileName);
		askFileInfo.sendTo(LFCName);
		Msg.info("Asked SE name to LFC '" + LFCName + "' for file '" +
				logicalFileName + "'");

		Msg.info("Waiting for LFC '" + LFCName +
				"' to reply with SE name for file '" + logicalFileName +"'");
		Message getFileInfo = Message.getFrom(mailbox);

		if (getFileInfo.getType() != Message.Type.SEND_LOGICAL_FILE){
			Msg.warn("While waiting for a reply from LFC, received a " +
					getFileInfo.getType().toString() + " message");
			//TODO should we retry the receive?
		} else {
			SEName = getFileInfo.getFile().getSEName();
			logicalFileSize = getFileInfo.getFile().getSize();

			Msg.info("LFC '"+ LFCName + "' replied with SE name '" + 
					SEName +
					"' for file '" + logicalFileName +"' of size " +
					logicalFileSize);
		}

		Msg.info("Downloading file '" + logicalFileName + "' from SE '" + 
				SEName + "' using LFC '" + LFCName +"'");
		Message downloadRequest = new Message(Message.Type.DOWNLOAD_REQUEST, 
				logicalFileName, logicalFileSize);

		downloadRequest.sendTo(SEName);
		Msg.info("Sent download request to SE '" + SEName + "' for file '" +
				logicalFileName +"' of size " + logicalFileSize);

		Msg.debug("Receiving file '" + logicalFileName + "' from SE '" + 
				SEName + "'");
		Message.getFrom(mailbox);
		Msg.info("SE '"+ SEName + "' sent file named '" + logicalFileName +"'");
	};
}
