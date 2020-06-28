package group7Crypto;

import java.io.PrintStream;

class OutputManager { //output manager does 

	private static OutputManager instance; //singleton instance
	//private OutputStream fileLog; 	   //I was going to do a file output
	private String pid$;
	
	private OutputManager(String pid) //constructor
	{
		pid$ = pid + " "; //the process ID so I can mark all messages with this prefix so I know what process sent what line
	}
	
	public static void Init(String pid) //init sets singleton instance with the process ID
	{
		if(instance == null)
		{
			instance = new OutputManager(pid); 
		}
	}
	
	//log to console with the string prefix
	public static void log(String x)
	{
		OutputManager l = getInstance();
		String output = "";
		
			output+=l.pid$;
		
		output+=(x);
		System.out.println(output);
	}
	
	public static void Out(PrintStream out, String message) //log to arbitrary print stream with prefix
	{
		OutputManager l = getInstance();
		String output = "";
		
			output+=l.pid$;
		
		output+=(message);
		out.println(output);
		out.flush();
	}
	
	//get the singleton instance no check because I want a null reference to be thrown if used but not initialized.
	private static OutputManager getInstance() 
	{
		return instance;
	}
}

