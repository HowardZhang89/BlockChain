package group7Crypto;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.LinkedList;

import javax.swing.UIManager;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import java.io.FileReader;
//CDE Some other uitilities: 
import java.util.UUID;

import java.io.ObjectInputStream;


//config so I don't have to mess around with server config, need to move some more stuff like process count and such in here for cleaniness
class config { 

	public static String getDefaultHost(){return "localhost";} 
		
	public static int getAdminDefaultPort(){return 5050;}
	
	public static int getServerTimeout() { return 5000;}
	
	public static int getQLen(){return 6;}
	
	public static int getNumProcesses() {return 3;}
	
}



class BlockchainWorker extends Thread { 
	Socket sock;
	
	BlockchainWorker(ThreadGroup children, String string, Socket s) {
		super(children, string); //Thread constructor for thread group assignment
		sock = s; //socket to do work
	}

	public void run() { //run method called on thread start()
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); //grab a buffered reader to read in the XML
						
			String XML = ""; //new empty string to store the entire xml file
			String line; //each line of xml
			while((line = in.readLine()) != null) //read all the lines in the input stream
			{
			 	XML+=line; //appending each one to the XML string
			}
			
			BlockLedger data = (BlockLedger) XMLHelper.Unmarshal(XML, BlockLedger.class); //use the XML Helper to turn the XML string into a BlockLedger class

			OutputManager.log("GotData!"); //logging
			
			
			BCThread.blockchain.setLedger(data.getLedger()); //this swaps the reference inside the ledger with the new ledger
			
			sock.close(); //cleanup
			//error catching
		} catch (IOException x) {
			x.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}

//this is from your example almost entirely I was gonna roll it into config but I left it instead
class Ports {
	
	//the base ports for the port numbering scheme by process
	public static int KeyServerPortBase = 6050; 
	public static int UnverifiedBlockServerPortBase = 6051;
	public static int BlockchainServerPortBase = 6052;
	//public static int AdminPortBase = config.getAdminDefaultPort(); //for remote management later
	
	public static int KeyServerPort;
	public static int UnverifiedBlockServerPort;
	public static int BlockchainServerPort;
	public static int AdminPort;

	public void setPorts() {
		//sets the port numbers for a process
		KeyServerPort = KeyServerPortBase + (BCThread.PID * 1000);
		UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (BCThread.PID * 1000);
		BlockchainServerPort = BlockchainServerPortBase + (BCThread.PID * 1000);
		
		//AdminPort = AdminPortBase + (bcThread.PID * 1000); //for remote management later I pulled it out cuz it didn't fit with the assignment
	}
}

//Abstract parent class "Server Thread" takes care of most of the boring work

//this accepts connections for receiving new blockchains
class BlockchainServer extends ServerThread {
	
	public BlockchainServer(ThreadGroup children, String string) {
		super(children, string, "BlockChainWorkers"); //pass all of the arguments up to the ServerThread base class
	}

	@Override
	public void loopBody() throws SocketTimeoutException, IOException { 
		sock = servsock.accept(); //wait for a connection
		new BlockchainWorker(Children, ChildGroupName, sock).start(); //start a blockChain worker
	}

	@Override
	public int getPort() {
		return Ports.BlockchainServerPort; //return the port it needs to run at
	}

	@Override
	public String getTName() {
		return "BlockChainServer"; //return the name of this thread for debugging
	}	
}

class UnverifiedBlockServer extends ServerThread {

	 //queue of unverified blocks I took out the priority queue since I didn't have time to set up my objects as comparable in a clean manner
	//if I revisit i will setup priority queue
	BlockingQueue<BlockRecord> queue;

	UnverifiedBlockServer(ThreadGroup children, String string, BlockingQueue<BlockRecord> queue) {
		super(children, string, "UWorkers"); //pass up to base class
		this.queue = queue; //grabs the passed in queue to be shared
	}

	//begin nested class UnverifiedBlockWorker
	class UnverifiedBlockWorker extends Thread { 
		Socket sock;

		UnverifiedBlockWorker(ThreadGroup children, String string, Socket s) {
			super(children, string); //Thread constructor for thread management
			sock = s; //socket for work
		}
		
		public void run() {
			try {
				
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); //get input stream for xml
				
				String XML = ""; //empty string to build the xml line by line
				String line; //empty string for the line from the reader
				while((line = in.readLine()) != null)
				{
					XML+=line; //add each line from the buffer to the XML string
				}
				
				BlockRecord data = (BlockRecord) XMLHelper.Unmarshal(XML, BlockRecord.class); //transform the xml string into a BlockRecordLList class
				
				//now that I have a BlockRecordLList object I want to verify the sender
				if(Crypto.verifySigHelper(data.getABlockID().getBytes(), BCThread.Nodes[data.getACreatingProcessInt()].pubKey, data.getASignedBlockID()))
				{
					//OutputManager.log("BLOCK ID VERIFIED!!!"); //logging
					//OutputManager.log("Put in priority queue: " + data + "\n"); //logging
					
					queue.put(data); //put the data in the queue
				}
				

				sock.close(); //close the socket
			} catch (Exception x) { //default error handling
				x.printStackTrace();
			}
		}
	}//end of nested class UnverifiedBlockWorker		

	
	@Override
	public void loopBody() throws SocketTimeoutException, IOException {
		sock = servsock.accept(); // wait for a connection
		new UnverifiedBlockWorker(Children, "UBWorkers", sock).start(); //Start a block worker to handle it
	}

	@Override
	public int getPort() { //get the port that this is to listen on
		return Ports.UnverifiedBlockServerPort;
	}

	@Override
	public String getTName() { //get the thread name for debugging
		return "Unverified BlockServer";
	}
}


//this takes unverified blocks from the queue and works on them if need be.
class UnverifiedBlockConsumer extends Thread {

	BlockingQueue<BlockRecord> queue; //storing the list of tasks to do
	boolean run = true; //since this doesn't extend ServerThread because it's not a server It has to have it's own lifecycle management
	//this lets it know when to quit

	UnverifiedBlockConsumer(ThreadGroup children, String string, BlockingQueue<BlockRecord> queue) {
		super(children, string);//constructor for thread for thread management
		this.queue = queue; //Constructor binds our prioirty queue to the local variable. 
	} 

	//helper function to see if  the block is already in the blockchain
	private boolean isDupe(BlockRecord data) { 
		boolean dupe = false;
		for (BlockRecord i : BCThread.blockchain.getLedger()) { //iterate through every record in the ledger
			// OutputManager.log("checking " + i.BlockID + " vs " + i.BlockID);
			if (i.getABlockID().compareTo(data.getABlockID()) == 0) {  //if it already exists 
				dupe = true; //it's a duplicate
				//OutputManager.log("Dupe not gonna start work"); //logging
			}
		}
		return dupe; //return value if it is a dupe or not
	}

	//from your code to build the random seed via indexes of this string;
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	//your code as a helper function to build the random seed string
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	//do the actual work
	private void work(BlockRecord data) throws NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException {
		
		//temp vars for processing
		String randString;
		String stringOut;
		
		int workNumber = Integer.MAX_VALUE; //set to max so there's no way the work number is set to an initial value that claims to solve the puzzle
		
		//not happy about this code but this checks to see if the blockchain is new, it's annoying to waste cycles on this every block but here it is
		if (BCThread.blockchain.isEmpty()) { //if the block chain is fresh
			data.setAPreviousHash("00000000-0000-0000-0000-000000000000");// set the previous hash of the block record object to a UUID esque string of 0's
		} else {
			data.setAPreviousHash(BCThread.blockchain.getFirst().getASHA256String()); //else get the previous completed block in the blockchains hash
		}
		
		for (int i = 1; i < 5000000; i++) { //I set it to much longer 
			
			if (run == false) { //if the process is set to shutdown jump out and avoid wasting time on work
				break;
			}

			if (i % 10 == 0) { //if the try is a multiple of 10 check to see if someone else has completed the work
				if (isDupe(data)) { //use the isDupe helper to see if someone else beat you
					OutputManager.log("Another client mined the block first. Terminate mining for this block."); //logging
					break;//jump out
				}
			}
			//if it's all good do the work the work is more or less from your work example with some optimizations
			randString = randomAlphaNumeric(8); // Get a new random AlphaNumeric seed string
			
			stringOut = DatatypeConverter.printHexBinary(Crypto.getHash(data.getAPreviousHash() + randString + data.getData())); //get hash and turn into a string of hex values
			
			//OutputManager.log("Hash is: " + stringOut); //logging
			
			workNumber = Integer.parseInt(stringOut.substring(0, 4), 16); // Between 0000 (0) and FFFF (65535)
			
			//OutputManager.log("First 16 bits " + stringOut.substring(0, 4) + ": " + workNumber + "\n"); //logging
			
			if (workNumber < 100) { // lower number = more work.
				//System.out.println("Puzzle solved!"); //logging
				//System.out.println("The seed was: " + randString); //logging
				//since the puzzle is solved fill in the data needed to verify the block
				data.setASHA256String(stringOut);  //set the hash
				data.setASignedSHA256(Crypto.signDataHelper(stringOut.getBytes())); //set the signed version of the hash for verification
				data.setNonce(randString); //set the salt used to solve the puzzle for verfication
				data.setAVerificationProcessID(Integer.toString(BCThread.PID)); //set the proces id so it's public key can be used to verify it did the work
				break; //don't do anymore work cuz it's solved
			}
		}
	}

	//helper to verify the data of a given block
	private boolean Verified(BlockRecord data) throws NumberFormatException, Exception 
	{	
		boolean retval = false; //return value false to assume block fails verfication
		if(Crypto.verifySigHelper(data.getASHA256String().getBytes(), BCThread.Nodes[Integer.parseInt(data.getAVerificationProcessID())].pubKey, data.getASignedSHA256())) //check to make sure the signatures and hashs are good
		{
			//recreate the work function using the provided solution to ensure the work was actually completed
			
			byte[] bytesHash = Crypto.getHash(data.getAPreviousHash() + data.getNonce() + data.getData());// Get the hash value
			
			int workNumber = Integer.parseInt(DatatypeConverter.printHexBinary(bytesHash).substring(0, 4), 16); // Between 0000 (0) and FFFF (65535)
			
			//if the solution works
			if (workNumber < 100)
			{
				retval = true; //block is verified
			}
		}
		return retval; //return true or false
	}

	public void run() { //run function of thread called on start()
		
		BlockRecord data; //temp var for a BlockRecordLList

		OutputManager.log("Starting the Unverified Block Priority Queue Consumer thread.\n");//logging
		try {
			while (run) {
				data = queue.poll(1000, TimeUnit.MILLISECONDS); //only tempoary block so it can be shutdown cleanly.
				if (data != null) { //if there is something in the queue (because of timeout added it can be null) 

					// OutputManager.log("Consumer got unverified: " + data.BlockID); //logging

					if (!isDupe(data)) {//check to see if the block has already been verified
						
						work(data); //do the work because the block needs to be verified
						
						if(!Verified(data)) //check to ensure that the solution works before multicasting it out 
						{//this is just because there is a limit on tries for the work function
							OutputManager.log("Verification Failed!!!");//logging
						}
						else if ( !isDupe(data)) {//if the solution is good AND it's still not in the blockchain
							 
							BlockLedger tmp = new BlockLedger(); //build a new blockchain ledger
							
							//if the existing blockchain ledger is empty OR the previous hash in the blockchain still matches
							if(BCThread.blockchain.isEmpty() || data.getAPreviousHash().compareTo(BCThread.blockchain.getFirst().getAPreviousHash()) != 0) 
							{//add it to the front of the blockchain
								tmp.Add(data);
								tmp.addAll(BCThread.blockchain.getLedger()); //add the rest of the leadger behind it
								BCThread.multicastXML(Ports.BlockchainServerPortBase, tmp); //multi cast the ledger as XML to the rest of the processes and itself
							}
							else 
							{  //re-add the data in the event that the blockchain changed by adding a block that didn't match what this one was working on
								//it means another process posted a different block and this data might not be in the accepted blockchain so re-add for later processing
								//so that no blocks that need to be verified are dropped from the blockchain.
								queue.add(data); 
							}
						}
					}
					
					Thread.sleep(1500); // wait for blockchain to populate

				}
			}
			//default error handling
		} catch (Exception e) {
			e.printStackTrace();
		}

		OutputManager.log("UBC Exiting Gracefully\n"); //logging
	}

	//called by it's parent to let it know it's time to shutdown cleanly
	public void shutdown() {
		run = false;
	}
}



//this stores information about the other workers in the blockchain currently process id and public key only
//does nothing just is used as a package to serialize to other workers.
class ProcessBlock implements Serializable {
  
	public ProcessBlock(int pID, PublicKey publicKey) {
	processID = pID;
	pubKey = publicKey;
	}

	private static final long serialVersionUID = -8774413277908960333L;
	int processID;
    PublicKey pubKey;
} 




public class Blockchain {
	
	//verified helper for verifying blocks
	private static boolean Verified(BlockRecord data) throws NumberFormatException, Exception 
	{	
		boolean retval = false; //assume it will fail verification
		
		//check if the block id matches the signed version and the hash matches the signed version using the public keys for creating process and verfication process
		if(Crypto.verifySigHelper(data.getASHA256String().getBytes(), BCThread.Nodes[Integer.parseInt(data.getAVerificationProcessID())].pubKey, data.getASignedSHA256()) &&
				Crypto.verifySigHelper(data.getABlockID().getBytes(), BCThread.Nodes[Integer.parseInt(data.getACreatingProcess())].pubKey, data.getASignedBlockID())) //check to make sure the signatures and hashs are good
		{
			//if the signatures are legit emulate the work function supplying the provided result from the block
			
			
			byte [] bytesHash = Crypto.getHash(data.getAPreviousHash() + data.getNonce() + data.getData()); //get the hash value
			
			int workNumber = Integer.parseInt(DatatypeConverter.printHexBinary(bytesHash).substring(0, 4), 16); // Between 0000 (0) and FFFF (65535)
			
			if (workNumber < 100) //if the none solves the puzzle
			{
				retval = true; //block is verified
			}
		}
		return retval;
	}
	
	public static void main(String args[]) throws NumberFormatException, Exception {

		OutputManager.Init(args[0]); //setup the output manager with the PID
		
		OutputManager.log("Thread starting with "+ args[0]); //logging

		BCThread runner = new BCThread((args.length < 1) ? 0 : Integer.parseInt(args[0])); //start up the block chain runner with the provided pid
		runner.start();
		
		// initialize the GUI
		try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI(runner).setVisible(true);
            }
        });

		boolean run = true; // sets the run to true so it can loop on user inputs

		while (run) //while not told to "quit"
		{
			try {
				
				
				BufferedReader adminIn = new BufferedReader(new InputStreamReader(System.in));  //wrap standard in with a buffered reader
				String msg = adminIn.readLine(); //read a line from the buffered input
				if (msg.indexOf("quit") != -1) // if quit
				{
					OutputManager.log("Shutdown \nShuttingDown PleaseWait... ");
					run = false; // sets run to false so it knows to shutdown instead of looping again.
				} else if(msg.indexOf("V") != -1) //if it's not quitting and has V verify the block chain
				{
					LinkedList<BlockRecord> tmp = BCThread.blockchain.getLedger(); //grab a snapshot of the block chain as it stands in case it gets updated during verification
					
					OutputManager.log("Verifying blockchain"); //logging
					boolean verified = true; //assume the blockchain is valid
					for(BlockRecord c : tmp) //iterate through each record in the leadger snapshot
					{
						if(!Verified(c)) //if any block fails verification
						{
							verified = false; //set to false
							break; //break
						}
					}
					
					
					if(verified) //if the blockchain is valid
					{
						OutputManager.log("Blockchain Verified Successfully");
					} else
					{
						OutputManager.log("Blockchain issue: Verification Failed!");
					}
					
					
				} else if(msg.indexOf("L") != -1) //if L list the blocks instead
				{
					
					LinkedList<BlockRecord> tmp =  BCThread.blockchain.getLedger(); //grab a snapshot of the block chain as it stands in case it gets updated during verification this works because the block chain is swapped so this reference is good even if a new ledger is submitted instantly after.
					
					OutputManager.log("Listing blockchain:-------------------- ");
					
					for(BlockRecord c : tmp) //iterate through each record in the ledger snapshot
					{
						OutputManager.log(c.toString()); //call the tostring I put in the blockrecord
					}
					
					OutputManager.log("End of List --------------------");
				}
				
			} catch (SocketTimeoutException e) {
			} catch (IOException e) { e.printStackTrace();	}

		}
		//OutputManager.log("Notifying runner\n"); //logging 
		runner.shutdown(); //this calls the bcThread.shutdown() method to notify the system to stop accepting connections and clean up.
		
		try {
			runner.join(); //waits for the runner to finish
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		OutputManager.log("Main Thread Exiting Gracefully"); //logging that everything shutdown cleanly.
	}
}