package group7Crypto;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

//this is the parent object for the rest of the block chain
class BCThread extends Thread {

	ThreadGroup Children; // Thread group that the worker threads go to so that they can be cleaned up first
	Boolean wait; //this is a Boolean object that's only used for it's object monitor for wait() to provide a 0 overhead lock

	static ProcessBlock[] Nodes = new ProcessBlock[config.getNumProcesses()]; //this is the array that stores the port and public key for each node in the chain
	static BlockLedger blockchain; //this is the BlockLedger object that contains the blockchain info

	static int PID = 0; //The PID (Process ID) of this process

	BCThread(int pid) {
		PID = pid; 
		blockchain = new BlockLedger();//new ledger
		Children = new ThreadGroup("workers"); // creates a new thread group for worker thread tracking
		wait = new Boolean(false); //new boolean object doesn't matter the value
	}

	//child objects held so they can be shutdown cleanly
	PublicKeyServer PKS; 
	UnverifiedBlockServer UBS;
	BlockchainServer BcS;
	UnverifiedBlockConsumer UBC;
	
	//below are several implementations of multi cast so I don't have to repeat the code everywhere they are needed	
	
	//this one sends a block record as XML
	public static void multicastXML(int portBase, BlockRecord br) throws JAXBException, UnknownHostException, IOException
	{
		Socket sock;
		PrintStream toServer;
	
		String message = XMLHelper.Marshal(br, br.getClass());
		
		for (int i = 0; i < config.getNumProcesses(); i++) {// Send our key to all servers.
			sock = new Socket(config.getDefaultHost(), portBase + (i * 1000));
			toServer = new PrintStream(sock.getOutputStream());
			toServer.println(message);
			toServer.flush();
			sock.close();
		}
	}
	
	//this one sends a BlockLedger as xml
	public static void multicastXML(int portBase, BlockLedger bl) throws JAXBException, UnknownHostException, IOException
	{
		Socket sock;
		PrintStream toServer;
		
		String message = XMLHelper.Marshal(bl, bl.getClass());
		
		for (int i = 0; i < config.getNumProcesses(); i++) {// Send our key to all servers.
			sock = new Socket(config.getDefaultHost(), portBase + (i * 1000));
			toServer = new PrintStream(sock.getOutputStream());
			toServer.println(message);
			toServer.flush();
			sock.close();
		}
	}
	
	//this one sends a ProcessBlock with an object stream
	public void multiCast(int portBase, ProcessBlock pb) throws UnknownHostException, IOException
	{
		Socket sock;
		ObjectOutputStream toServer;
		for (int i = 0; i < config.getNumProcesses(); i++) { // send to each process in group, including us:
			
			sock = new Socket(config.getDefaultHost(), portBase + (i * 1000));
			toServer = new ObjectOutputStream(sock.getOutputStream());
			toServer.writeObject(pb);
			toServer.flush(); // make the multicast
			sock.close();
		}
	}

	public void broadcastPublicKeys(){

		try {
			//send the public keys out
			multiCast(Ports.KeyServerPortBase, new ProcessBlock(PID,Crypto.getPublicKey()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		//this section waits to receive every key
		boolean ready = false;
		while(!ready)
		{
			ready = true;
			for(ProcessBlock i : Nodes)
			{
				if(i == null)
				{
					OutputManager.log(" Not ready yet waiting for keys ");
					ready = false;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //while it's not ready sleep for .1 second to give things a chance to show up
		}

		//everyone should now have everyone else's keys

	}
	
	private static BlockRecord createTransaction(int pnum, String sender, String recipient, String amount){

		BlockRecord transaction = new BlockRecord();  // create transaction as a BlockRecordLList
		
		try {
			String suuid;             //string representation of a UUID
			suuid = new String(UUID.randomUUID().toString()); //uuid
			transaction.setABlockID(suuid); //set the uuid of the block
			
			transaction.setASignedBlockID(Crypto.signDataHelper(suuid.getBytes()));
			
			//sign the uuid with executing processes private key
			transaction.setACreatingProcess(Integer.toString(pnum)); //set the creating process
			transaction.setAVerificationProcessID("To be set later..."); //for the verfying process to set
	
			//this is all for the "Data" of the block in this case the medical record line
			//tokens = InputLineStr.split(" +"); // split a line of input from the text file into an array of tokens via space or + characters
			//uses the predetrmined mapping to fill in the data fields of the block record at index n
	
			transaction.setSenderID(sender);
			transaction.setRecipientID(recipient);
			transaction.setAmount(amount);
			//hash and sign the data hash
	
			byte[] bytesHash = Crypto.getHash(transaction.getData()); // Get the hash value
	
			transaction.setADataHash(Crypto.signDataHelper(DatatypeConverter.printHexBinary(bytesHash).getBytes())); //sign and encode the signature as a string and set it in the block
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return transaction; // return the transaction as a BlockRecord
	}

	public void sendMoney(String recipient, String amount){
		
		String userPID = Integer.toString(PID);
		BlockRecord transaction = createTransaction(PID, userPID, recipient, amount);

		try {
			if (transaction != null) //if it's not null this needs to be here because each process reads 4 blocks into a larger buffer
			{
				multicastXML(Ports.UnverifiedBlockServerPortBase, transaction); //cast the blocks as XML
			}
		}catch(Exception x){
			x.printStackTrace();
		}
		
	}


	public void run() // run method called by thread.start()
	{

		Crypto.Init(); //setup the crypto so every other thread can use it
		
		final BlockingQueue<BlockRecord> queue = new LinkedBlockingQueue<>(); //blocking queue
		new Ports().setPorts(); // Establish OUR port number scheme, based on PID

		PKS = new PublicKeyServer(Children, "PKSParent"); // New thread to process incoming public keys
		PKS.start();

		UBS = new UnverifiedBlockServer(Children, "UBSParent", queue); // New thread to process incoming unverified
		UBS.start();
		
		// blocks
		BcS = new BlockchainServer(Children, "BCSParent"); // New thread to process incoming new blockchains
		BcS.start();

		try {
			Thread.sleep(1000); //sleep 1 second 
		} catch (Exception e) {
		} // Wait for servers to start.

		//broadcastPublicKeys(); // Broadcast public keys to all other processes

		try {
			Thread.sleep(1000); //sleep for a second to let things settle
		} catch (Exception e) {
		} 

		UBC = new UnverifiedBlockConsumer(Children, "UBCParent", queue); 
		UBC.start(); // Start listening for transactions from other clients

		try {
			synchronized (wait) { //this is kinda cool so what I do is use the monitor on the boolean object as a mechanism to wait and wake this thread
				//OutputManager.log("Wait Start"); //logging
				wait.wait(); //this waits on the wait Objects monitor for a notification
				//OutputManager.log("Wait Over"); //logging
			}//catch for intterrupted
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally { //no matter what cleanup

			OutputManager.log("Telling everything to shutdown"); //logging
			
			//calls the shutdown for each of it's children
			PKS.shutdown();
			UBS.shutdown();
			BcS.shutdown();
			UBC.shutdown();

			//OutputManager.log("bcThread checking for children"); //logging

			int active = Children.activeCount(); // this gets the approximate number of active worker threads
													// (approximate
													// because they can be shutting down as the thread group is counting
													// it.
			while (active > 0) // while there are still active workers in the children thread group
			{
				OutputManager.log("bc ServerThread waiting on " + active + " Children"); // log it so you know why the
														
				Thread[] list = new Thread[32];// make a buffer to contain the list of children, it's large because this is all the children under this thread and thier children and so on
				Children.enumerate(list);
				
				for(Thread t : list)
				{
					if(t != null)
					{
						OutputManager.log(t.getName()); //log the name of the sleeping threads for debugging purposes 
					}
				}
				
				try { // try because Thread.sleep can throw interrupted exception if deprecated
						// operations have taken place like pause stop or interrupt.
					Thread.sleep(1000); // sleep for 1000 mills or 1 second to give the children a chance to complete
										// their work and shutdown
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // pukes stack trace to console
				active = Children.activeCount(); // when the thread wakes back up after ~1 second (it's approximate because
												//the scheduler may not put it back immediately but that's a whole separate issue.
			} // after all the workers are done it lets the ServerThread object complete it's
				// execution
		}
		
		if(PID == 0) //if the PID is 0
		{
			blockchain.dump(); //dump the blockchain to console on exit
		}
			
		OutputManager.log("bc ServerThread Exiting Gracefully\n"); // logs that the system is cleaned up properly.
	}

	public void shutdown() // this is called by the main thread when the admin signals shutdown of the
							// server.
	{
		synchronized (wait) {
			wait.notifyAll(); // this uses the monitor on the wait object to notify all threads waiting on this object
		} //this allows the BC thread to sleep and not use processor time. no spinning and checking for shutdown
	}
}
