package group7Crypto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//BlockRecordLList is an object representation of a single block of a block chain
//it doesn't really do anything but store data
@XmlRootElement //Annotation to allow this to be a stand alone element of an XML document
class BlockRecord {

	//fields containing data
private String BlockNumber;
private String SHA256String;
private String SignedSHA256;
private String BlockID;
private String BlockIDSigned;
private String VerificationProcessID;
private String CreatingProcess;
private String PreviousHash;
private String nonce;
private String DataHash;

//The new variables for our Group7Coin
private String SenderID;
private String RecipientID; 
private String Amount;

//My original BlockChain was for medical records. This changing it to work for sending money. 
//private String Fname;
//private String Lname;
//private String SSNum;
//private String DOB;
//private String Diag;
//private String Treat;
//private String Rx;

//getters and setters for information about the block
public String getBlockNum() { return BlockNumber;}

public int getBlockNumInt() {return Integer.parseInt(BlockNumber);}

public int getACreatingProcessInt() {	return Integer.parseInt(CreatingProcess);}

@XmlElement
public void setBlockNum(String bn) { BlockNumber = bn;}

public String getADataHash() { return DataHash;}

@XmlElement
public void setADataHash(String DH) { this.DataHash = DH; }

public String getASHA256String() {return SHA256String;}
@XmlElement
public void setASHA256String(String SH){this.SHA256String = SH;}

public String getASignedSHA256() {return SignedSHA256;}
@XmlElement
public void setASignedSHA256(String SH){this.SignedSHA256 = SH;}

public String getNonce() { return nonce;}
@XmlElement
	public void setNonce(String seed) { this.nonce = seed;}

public String getAPreviousHash() {return PreviousHash;}
@XmlElement
public void setAPreviousHash(String PH){this.PreviousHash = PH;}

public String getACreatingProcess() {return CreatingProcess;}
@XmlElement
public void setACreatingProcess(String CP){this.CreatingProcess = CP;}

public String getAVerificationProcessID() {return VerificationProcessID;}
@XmlElement
public void setAVerificationProcessID(String VID){this.VerificationProcessID = VID;}

public String getABlockID() {return BlockID;}
@XmlElement
public void setABlockID(String BID){this.BlockID = BID;}

public String getASignedBlockID() {return BlockIDSigned;}
@XmlElement void setASignedBlockID(String SBID) {this.BlockIDSigned = SBID;}

public String getSenderID() {return this.SenderID;}
@XmlElement void setSenderID(String SID) {this.SenderID = SID;}

public String getRecipientID() {return this.RecipientID;}
@XmlElement void setRecipientID(String RID) {this.RecipientID = RID;}

public String getAmount() {return this.Amount;}
@XmlElement void setAmount(String AMT) {this.Amount = AMT;}


public String getData()
{
	  //return Fname + Lname + SSNum + DOB + Diag + Treat + Rx;
	  return SenderID + RecipientID + Amount;
}

public String toString()
{
	  return getData() + BlockID + " Creating process " + CreatingProcess + " Verification process " + VerificationProcessID;
}

}