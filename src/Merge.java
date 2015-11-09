/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import java.util.Vector;

import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;

public class Merge extends Job {

	private void connect() {
		// Use of some simulation magic here, every worker knows the mailbox of
		// the VIP server
		GateMessage.sendTo("VIPServer", "MERGE_CONNECT", 0);
	}

	private void disconnect() {
		// Use of some simulation magic here, every worker knows the mailbox of
		// the VIP server
		GateMessage.sendTo("VIPServer", "MERGE_DISCONNECT", 0);
	}

	public Merge(Host host, String name, String[] args) {
		super(host, name, args);
	}

	public void main(String[] args) throws MsgException {
		// TODO have to set the name here, might be a bug in simgrid
		setName();
		long nbParticles = 0;

		String transfer_info;

		int jobId = (args.length > 0 ? Integer.valueOf(args[0]).intValue() : 1);
		long executionTime = (args.length > 1 ? 1000 * Long.valueOf(args[1])
				.longValue() : VIPSimulator.sosTime);
		long uploadFileSize = (args.length > 2 ? Long.valueOf(args[2])
				.longValue() : 1000000);

		Msg.info("Register Merge on '" + getName() + "'");
		this.connect();

		while (true) {
			GateMessage message = (GateMessage) Message.getFrom(getName());

			switch (message.getType()) {
			case "BEGIN":
				Msg.info("Processing Merge");

				Vector<String> fileNameList = null;
				while (fileNameList == null) {
					fileNameList = LCG
							.ls(VIPServer.getDefaultLFC(), "results/");
					if (fileNameList.size() == 0) {
						Msg.warn("No files to merge. Sleeping 100ms and retry");
						Process.sleep(100);
					}
				}

				Msg.info("Files to merge:" + fileNameList.toString());
				for (String fullName : fileNameList) {
					downloadTime.start();
					// Get the file name, without the directory name
					String fileName = fullName.split("/")[1];

					transfer_info = LCG.cp(fullName, "/scratch/" + fileName,
							VIPServer.getDefaultLFC());

					Msg.info(fullName + " " + fileName + "="
							+ fileName.split("-")[0]);
					nbParticles += Long.valueOf(fileName.split("-")[0])
							.longValue();

					Msg.info("Received " + nbParticles + " particles to merge");
					System.err.println(jobId + "," + getHost().getName() + ","
							+ transfer_info + ",2");
					downloadTime.stop();
				}

				computeTime.start();
				Process.sleep(executionTime);
				computeTime.stop();

				String logicalFileName = "results/"
						+ Long.toString(nbParticles) + "-merged-" + getName()
						+ "-" + Double.toString(Msg.getClock()) + ".tgz";

				uploadTime.start();
				LCG.cr("local_file.tgz", uploadFileSize, logicalFileName,
						getCloseSE(), VIPServer.getDefaultLFC());
				uploadTime.stop();
				System.err.println(jobId + "," + getCloseSE() + ","
						+ getHost().getName() + "," + uploadFileSize +","
						+ uploadTime.getValue() + ",1");

				Msg.info("Disconnecting MERGE job. Inform VIP server.");
				this.disconnect();
				System.out.println(jobId + "," + downloadTime.getValue() + ","
						+ uploadTime.getValue());
				break;
			default:
				break;
			}
		}
	}
}
