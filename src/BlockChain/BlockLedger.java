package group7Crypto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//block ledger represents the current blockchain, stored in memory as java objects but is also backed by XML that's re-written on update.
@XmlRootElement //annotation so this can be a stand alone object in XML
class BlockLedger 
{	
	@XmlElement //the only element it has is a list of block records, this works because the BlockRecords also have XML annotations
	private LinkedList<BlockRecord> BlockRecordLList;
	
	public BlockLedger() 
	{//the ledger starts with a blank linked list just to avoid null reference errors
		BlockRecordLList = new LinkedList<BlockRecord>();
	}
	
	//debugging method just dumps the blockchain number and ID's to the console
	public void dump()
	{
		int i = 0;
		for (BlockRecord t : BlockRecordLList) {
			OutputManager.log(" Block " + i + " = " + t.getABlockID());
			i++;
		}
	}

	public boolean isEmpty() //check if the blockRecord is empty
	{
		return BlockRecordLList.isEmpty();
	}
	
	public void setLedger(LinkedList<BlockRecord> bc) //set a linked list as the new block record
	{
		BlockRecordLList = bc; //in memory this removes the reference to the old blockchain and instead references the linked list passed in
		//this is an atomic operation via the java standard so no sync is necessary, if multiple threads call set, the winner is the last one that does so,
		//however those threads should be checking for a winner anyway so it's not this methods job to sync

		//below just gathers the blockchain to print to console
		String tmp = "";
		int i = 0;
		for (BlockRecord t : BlockRecordLList) {
			tmp+=" Block " + i + " = " + t.getABlockID() + "\n";
			i++;
		}
		OutputManager.log("         --NEW BLOCKCHAIN--\n" + tmp); //logging
		
		//this handles the XML file backing of the blockchain
		try {
			//make a new file to contain the ledger
			File f = new File("./BlockLedger" + Integer.toString(BCThread.PID) +".xml");
			OutputManager.log("Ledger Backed too path "+ f.getAbsolutePath()); //logging that path to console
			FileOutputStream fo = new FileOutputStream(f); //get a file outputstream for that file
			
			fo.write(XMLHelper.Marshal(this, this.getClass()).getBytes()); //write the XML representation of the ledger to the file
			
			fo.flush(); //flush from ram to disk
			
			fo.close(); //close the file handle
			
			//default error handling
		} catch (FileNotFoundException | JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//add a record to the ledger this is only done while creating a new ledger
	//so this method is to add the new block to the front of the chain
	public void Add(BlockRecord br)	{
		BlockRecordLList.add(br);
	}

	//return the current Ledger
	public LinkedList<BlockRecord> getLedger() {
		return BlockRecordLList;
	}

	//return the first block in the list, meaning the latest one added
	public BlockRecord getFirst() {
		return BlockRecordLList.getFirst();
	}

	//add a collection of Block records to the blockchain
	//this is done to add the existing blockchain behind the newly added block record
	public void addAll(LinkedList<BlockRecord> ledger) {
		BlockRecordLList.addAll(ledger);	//I have to confirm the operation of this,
		//I know it iterates over the collection but I believe it doesn't copy the data just adds references too it
		//though I know the better way would to be to prepend to the existing list and send that but this keeps me lock free.
	}
}

