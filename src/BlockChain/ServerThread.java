package group7Crypto;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

//abstract class for a server thread pulls all of the common behavior from the 3 different subclasses simplifying the code greatly. 
//including the connection, timeout catching, shutdown logic and thread management logic into one abstract class.
abstract class ServerThread extends Thread {

	//thread group for it's worker threads
	ThreadGroup Children; 
	ServerSocket servsock = null; //the server socket for listening for connections
	Socket sock; //the socket created by accept();
	String ChildGroupName;
	boolean run = true; //if the server should continue accepting connections or shutdown

	ServerThread(ThreadGroup children, String string, String childGroupName) {
		super(children, string);//the server thread is iself a thread so it's managed by it's parents thread group
		ChildGroupName = childGroupName;
		Children = new ThreadGroup(ChildGroupName); //but it also has it's own thread group
	}//this is so I can have tiered priority system and thread management,
	//a server can't have a higher priority than it's parent and it's children can't have a higher priority than it.
	//which means new connections get higher priority than long running tasks so the connection Queue doesn't overflow and miss connections
	//and the managing thread can be scheduled as soon as it has work to do
	
	//this sets the socket to use for this serverThread before use
	public void setSocket(int port) {
		OutputManager.log("Starting using port" + Integer.toString(port));
		try {
			servsock = new ServerSocket(port, config.getQLen());
			servsock.setSoTimeout(config.getServerTimeout());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//this does all the cleanup of it's children so a parent is never garbage collected while it's children might be using it's data
	//all threads are freed for garbage collection bottom up in this tree system.
	public void cleanUp(String name) {
		
		try {
			servsock.close(); //cleanup the serversock so I can use it again later
		} catch (IOException e) {
			e.printStackTrace();
		}

		OutputManager.log(name + " Thread exiting"); //logging

		int active = Children.activeCount(); // this gets the approximate number of active worker threads (approximate
												// because they can be shutting down as the thread group is counting it.
		while (active > 0) // while there are still active workers in the children thread group
		{
			OutputManager.log(name + " Thread " + active + " Children"); // log it so you know why the server
																			// isn't shutdown
			try { // try because Thread.sleep can throw interrupted exception if deprecated
					// operations have taken place like pause stop or interrupt.
				Thread.sleep(1000); // sleep for 1000 mills or 1 second to give the children a chance to complete
									// their work and shutdown
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // pukes stack trace to console
			finally {
				active = Children.activeCount(); // when the thread wakes back up after ~1 second (it's approximate because
				// the scheduler may not put it back immediately but that's a whole
				// separate issue.	
			}
			
		} // after all the workers are done it lets the ServerThread object complete it's
			// execution

		OutputManager.log(name + " Thread Exiting Gracefully\n"); // logs that the server thread made it run without
	}

	//called by it's parent to let the server know it's time to stop accepting connections and cleanup.
	public void shutdown() {
		run = false;
	}

	//this is a kind of cool system, I override the run method in Thread here and handle all of the setup, looping and cleanup here
	//so the connection can be setup to time out and resume after checking run or quit and cleanup and the subclasses don't need to worry about
	//any of the standard responsibilities of a server.
	public void run() {

		setSocket(getPort());
		try {

			while (run) {
				try {
					this.loopBody(); //the subclasses loopBody() class that does the specfic work that the serverThread needs to do.
				    } catch (SocketTimeoutException e) {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanUp(getTName());
		}
	}

	//overridden by the subclass to implement the specfic work the server needs to do.
	public abstract void loopBody() throws SocketTimeoutException, IOException; 

	//overridden by the subclass to tell the serverThread class how to setup the sockets
	public abstract int getPort();

	//this is so I can return a thread name for admin and debugging purposes.
	public abstract String getTName();

}
