package group7Crypto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

//This class is responsible for receiving peer information and storing it.
class PublicKeyWorker extends Thread { // Class definition
	
	Socket sock; //socket to recieve data

	PublicKeyWorker(ThreadGroup children, String string, Socket s) {
		super(children, string); //pass children and string to thread constructor for thread grouping
		sock = s;
	} 

	public void run() { //called on start() of thread
		try {
			
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream()); //object input stream should be removed if extended beyond java
			ProcessBlock data = (ProcessBlock) in.readObject(); //read in the process block containing process ID and public key

			BCThread.Nodes[data.processID] = data; //put the process info into the Nodes array using the proper index
			
			OutputManager.log("Got key: " + data.pubKey.toString() + " from process " + data.processID); //logging
			sock.close(); //close socket for cleanup
			//error handling defaults
		} catch (IOException x) { 
			x.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}

//waits for connections to accept public keys
class PublicKeyServer extends ServerThread {

	public PublicKeyServer(ThreadGroup children, String string) {
		super(children, string, "PKWorkers");//pass everything to the base class
	}

	@Override
	public void loopBody() throws SocketTimeoutException, IOException {
		sock = servsock.accept(); //wait for a connections 
		new PublicKeyWorker(Children, "PKWorkers", sock).start(); //start a thread to handle it
	}

	@Override
	public int getPort() { //port that this should listen on
		return Ports.KeyServerPort;
	}

	@Override
	public String getTName() { //name for debugging
		return "PK Server Thread";
	}
}