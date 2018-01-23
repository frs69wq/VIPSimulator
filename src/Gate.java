/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)
 *                  Anchen CHAI (2016)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;

import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Mutex;

public class Gate extends Job {
	private long simulateForNsec(long nSec) throws HostFailureException {
		double nbPart;

		Process.sleep(nSec);
		nbPart = VIPSimulator.eventsPerSec * nSec;
		Msg.info("simulateForNsec: '" + getName() + "' simulated " + (long) nbPart + " particles");
		// WARNING TEMPORARY HACK FOR FIRST TEST
		return 1;
		// SHOULD BE REPLACED BY
		// return (long) (nbPart);
	}

	private void connect() {
		// Use of some simulation magic here, every worker knows the mailbox of the VIP server
		GateMessage.sendTo("VIPServer", "GATE_CONNECT", 0);
	}

	private void sendProgress(long simulatedParticles) {
		// Use of some simulation magic here, every worker knows the mailbox of the VIP server
		GateMessage.sendTo("VIPServer", "GATE_PROGRESS", simulatedParticles);
	}

	private void disconnect() {
		// Use of some simulation magic here, every worker knows the mailbox of the VIP server
		GateMessage.sendTo("VIPServer", "GATE_DISCONNECT", 0);
	}
	
	private String cp_dynamic(String logicalFileName, String localFileName, LFC lfc, SE closeSE) throws HostFailureException {
		
		int i=0;
		String info = "";
		String SE_file = "";
		double min_bandwidth = 4e6; // minimum bandwidth acceptable is 4Mbps
		double timeout;
		int num_retry = 3; 	// maximum retry of lcg-rep		
		
		Timer duration = new Timer();
		Timer cr_timer = new Timer();
		double cr_duration = 0.0;
		
		Vector<SE> replicaLocations;
		Msg.info("Dynamic lcg-cp '" + logicalFileName + "' to '" + localFileName + "' using '" + lfc.getName() + "'");
		// get Logical File from the LFC
		LogicalFile file = lfc.getLogicalFile(logicalFileName);
		Msg.info("LFC '" + lfc.getName() + "' replied: " + file.toString());
		
		timeout = (file.getSize()*8)/ min_bandwidth + 1;
		duration.start();
		// get all replicas locations by lcg-lr
		replicaLocations = LCG.lr(lfc,logicalFileName);
		
		if(replicaLocations.contains(closeSE)) {
			info = LCG.cp(logicalFileName, localFileName, closeSE);
		}
		else{
			SE_file = closeSE.getName() +"_" + logicalFileName; 
			// if some job has already created lock for replicating file in SE
			// check the status of file in SE
			if(lfc.getTransferLock(SE_file) == 1){	
				int status = lfc.getReplicaInfo(SE_file);
				switch (status) {
				case 0: // file is replicating to closeSE in progress	
					
					Msg.info("case0 : replicate file into closeSE in progress");
					// Wait timeout before normal lcg-cp 
					while(status != 1 && i< timeout){		
						Process.sleep(2000);
						status = lfc.getReplicaInfo(SE_file);
						i = i+2;
						Msg.debug("Retry the file whether exists in closeSE");
					}	
					Msg.debug("Timeout, no more retry");
					if(status == 1){	
						Msg.debug("Succeed to replicate file in CloseSE");
						info = LCG.cp(logicalFileName, localFileName,closeSE);

					}		
					else{
						Msg.debug("File still not available in CloseSE, do a normal lcg-cp");
						info = LCG.cp1(logicalFileName, localFileName,lfc);					
					}
					break;
				case 1: // file exists in closeSE	
					Msg.debug("case1: copy from closeSE");
					info = LCG.cp(logicalFileName, localFileName,closeSE);
					break;
				case 2: // fail to replicate file in closeSE
					
					// need to simulate some transfer errors,
					// otherwise this case will never be reached
					info = LCG.cp1(logicalFileName, localFileName,lfc);
					break;
				default:
					break;	
				}			
			}	
			else{	
				int flag;
				flag = lfc.createTransferLock(SE_file);
				// first job will try to replicate file into closeSE
				if(flag == 0){		
					SE src = null;
					boolean flag_lcg_cp_cr;
					GfalFile gf = new GfalFile(file);	
					lfc.fillsurls(gf);	
	
					for(int j = 0 ; j < Math.min(gf.getNbreplicas(), num_retry); j++){
						SE se = gf.getCurrentReplica();
						Msg.info("Lcg-cp for :"+ se.getName());
						flag_lcg_cp_cr = LCG.cp(logicalFileName, localFileName, se, lfc, timeout);
						
						cr_timer.start();
						LCG.cr(localFileName, file.getSize() ,logicalFileName, closeSE, lfc);
						cr_timer.stop();
						cr_duration = cr_timer.getValue();
						
						if(flag_lcg_cp_cr){
							src = se;
							break;				
						}
						gf.NextReplica();
					}
					if(src == null){
						// If no SE response before timeout
						// We consider that we failed to lcg_cp_cr file into closeSE
						// update status to 2
						lfc.modifyReplicaInfo(SE_file, 2);
						// then all jobs will do a normal lcg-cp
						// No more retry of lcg_cp_cr
						info = LCG.cp1(logicalFileName, localFileName,lfc);		
					}
					else{
						Msg.info("lcg-rep complete, "+ "SE used is :"+ src.getName());		
						//if lcg_cp_cr succeeds, update status of SE_FILE to 1
						lfc.modifyReplicaInfo(SE_file, 1);	
						info = closeSE + "," +file.getSize() + "," + 0;
					}
				}		
				else{
					int status = lfc.getReplicaInfo(SE_file);
					while(status != 1 && i< timeout){		
						Process.sleep(2000);
						status = lfc.getReplicaInfo(SE_file);
						i = i+2;
						Msg.debug("Retry the file whether exists in closeSE");
					}	
					if(status !=1){
						Msg.debug("do a normal lcg-cp");	
						info = LCG.cp1(logicalFileName, localFileName,lfc);	
					}
					else info = LCG.cp(logicalFileName, localFileName,closeSE);
					
				}				
			}	
		}
		duration.stop();
		String[] log = info.split(",");
		Msg.info("cp_dynamic complete!");
		double dyn_duration = duration.getValue() - cr_duration;
		
		return  log[0] + "," + log[1] + "," + dyn_duration;
	
	}
	
	public Gate(Host host, String name, String[] args) {
		super(host, name, args);
	}

	public void main(String[] args) throws MsgException {
		// TODO have to set the name here, might be a bug in simgrid
		setName();
		long nbParticles = 0;
		long simulatedParticles = 0;
		long uploadFileSize = 0;
		String transferInfo;
		Vector<SE> actualSources = new Vector<SE>();
		int jobId = (args.length > 0 ? Integer.valueOf(args[0]).intValue() : 1);
		
		long executionTime = (args.length > 1 ? 1000 * Long.valueOf(args[1]).longValue() : VIPSimulator.sosTime);
		if (VIPSimulator.version == 1) {
			uploadFileSize = VIPSimulator.fixedFileSize;
		} else {
			uploadFileSize = (args.length > 2 ? Long.valueOf(args[2]).longValue() : 1000000);
			if (VIPSimulator.version == 3){
				actualSources.add(VIPServer.getSEbyName(args[3]));
				actualSources.add(VIPServer.getSEbyName(args[4]));
				actualSources.add(VIPServer.getSEbyName(args[5]));
			}
		}
		Msg.info("Register GATE on '" + getName() + "'");
		this.connect();

		while (true) {
			GateMessage message = (GateMessage) Message.getFrom(getName());

			switch (message.getType()) {
			case "BEGIN":
				Msg.info("Processing GATE");
				if (VIPSimulator.version == 1){
					// The first version of the GATE simulator does a single download whose size was given as input
					downloadTime.start();
					transferInfo = LCG.cp("input.tgz", "/scratch/input.tgz", VIPServer.getDefaultLFC());
					logDownload(jobId, transferInfo, 0, "gate");
					downloadTime.stop();
				} else {
					// upload-test
					// TODO to be factored at some point
					uploadTestTime.start();
					LCG.cr("output-0.tar.gz-uploadTest", 12, "output-0.tar.gz-uploadTest", getCloseSE(),
							VIPServer.getDefaultLFC());
					uploadTestTime.stop();
					System.err.println(jobId + "," + getHost().getName() +  "," + getCloseSE() + ",12,"
							+ uploadTestTime.getValue() + ",gate,0");

					// Downloading inputs:
					//   1) wrapper script
					//   2) Gate release
					//   3) workflow specific parameters
					// If less than 3 files were found in the catalog, exit.
					downloadTime.start();
					if (VIPSimulator.gateInputFileNames.size() < 3){
						Msg.error("Some input files are missing. Exit!");
						System.exit(1);
					}
					for (String logicalFileName: VIPSimulator.gateInputFileNames){
						Timer lrDuration = new Timer();
						//Gate job first do lcg-lr to check whether input file exists in closeSE
						lrDuration.start();
						Vector<SE> replicaLocations = LCG.lr(VIPServer.getDefaultLFC(),logicalFileName);
						lrDuration.stop();
						double lr_time = lrDuration.getValue();
						
						if (VIPSimulator.version == 2){
							
							if(VIPSimulator.algorithm.equals("lcg_cp")){
							// lcg-cp in production 
							transferInfo = LCG.cp1(logicalFileName,
									"/scratch/"+logicalFileName.substring(logicalFileName.lastIndexOf("/")+1),
									VIPServer.getDefaultLFC());
							
							}	
							else{
								// dynamic replication
								if(logicalFileName.contains("release") || logicalFileName.contains("opengate")){							
									transferInfo = cp_dynamic(logicalFileName,
										"/scratch/"+logicalFileName.substring(logicalFileName.lastIndexOf("/")+1),
										VIPServer.getDefaultLFC(), getCloseSE());							
									// lr is already included in cp_dynamic
									lr_time = 0.0;			
								}
								else{
									transferInfo = LCG.cp1(logicalFileName,
									"/scratch/"+logicalFileName.substring(logicalFileName.lastIndexOf("/")+1),
									VIPServer.getDefaultLFC());
																	
								} 
							}	
													
						} else {
							transferInfo = LCG.cp(logicalFileName, 
									"/scratch/"+logicalFileName.substring(logicalFileName.lastIndexOf("/")+1),
									(SE) actualSources.remove(0));
						}
						// Write download info to logs	
						logDownload(jobId, transferInfo, lr_time , "gate");
					}
					downloadTime.stop();
				}
			case "CARRY_ON":
				// Compute for sosTime seconds
				computeTime.start();

				// TODO Discuss what we can do here. Make the process just sleep for now
				simulatedParticles = simulateForNsec(executionTime);

				computeTime.stop();

				nbParticles += simulatedParticles;

				// if dynamic, Gate send Progress to VIPServer; otherwise, enter "END" directly 
				if(VIPSimulator.workflowVersion.equals("dynamic")){
					Msg.info("Sending computed number of particles to 'VIPServer'");
					sendProgress(simulatedParticles);
					break;
				}

			case "END":
				Msg.info("Stopping Gate job and uploading results. " + nbParticles + 
						" particles have been simulated by '" + getName() + "'");

				// The size of the file to upload is retrieve from the logs
				String logicalFileName = "results/" + Long.toString(nbParticles) + "-partial-" + getName() + "-" 
						+ Double.toString(Msg.getClock()) + ".tgz";

				uploadTime.start();
				LCG.cr("local_file.tgz", uploadFileSize, logicalFileName, getCloseSE(), VIPServer.getDefaultLFC());
				uploadTime.stop();
				System.err.println(jobId + "," + getHost().getName() + "," + getCloseSE() + "," + uploadFileSize +","
						+ uploadTime.getValue() + ",gate,1");

				Msg.info("Disconnecting GATE job. Inform VIP server.");
				this.disconnect();

				Msg.info("Spent " + downloadTime.getValue() + "s downloading, " + computeTime.getValue() 
						+ "s computing, and " + uploadTime.getValue() + "s uploading.");
				System.out.println(jobId + "," + downloadTime.getValue() + "," + uploadTime.getValue());
				break;
			default:
				break;
			}
		}
	}
}
