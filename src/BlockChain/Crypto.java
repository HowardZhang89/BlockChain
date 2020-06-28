package group7Crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

//Cryptography helper class
class Crypto
{
	//it's a singleton because I only want to generate one signer and one key pair
	static private Crypto pInstance; 
	KeyPair pair; //public private key pair
	Signature signer; //Signature class
	
	private Crypto() //private constructor
	{
		try {
			pair = generateKeyPair(ThreadLocalRandom.current().nextLong()); //make a new key pair based off a threaad local seed
			signer = Signature.getInstance("SHA1withRSA"); //get a new signer with provided algorithim
		} catch (Exception e) {
			e.printStackTrace();//error handling
		}
	}
	
	//gets a hash of a given string
	public static byte[] getHash(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		 MessageDigest md = MessageDigest.getInstance("SHA-256");
	     md.update (data.getBytes("UTF-8"));
	     return md.digest();
	}
	
	//returns the singleton instance of the crypto class
	private static Crypto getInstance()
	{
		if(pInstance == null)
		{
			pInstance = new Crypto();
		}
		return pInstance;
	}
	
	//returns the public key from the singleton instance 
	//no way to retrieve private key by design
	public static PublicKey getPublicKey()	{
		return getInstance().pair.getPublic();
	}
	
	//signs the array of bytes and encodes it as a string
	public static String signDataHelper(byte[] data) throws SignatureException, InvalidKeyException
	{
		return EncodeSignature(signData(data)); //encodes the byte[] returned by the signData class
	}
	
	//verifies a byte[] vs a signed string using a provided public key
	public static boolean verifySigHelper(byte[] data, PublicKey key, String signature) throws Exception
	{
		return verifySig(data,key,DecodeSignature(signature));
	}
	
	//theese are private for now because I found no need for them but I kept the helpers separte because I could see them being useful later
	//signs the data[]
	private static byte[] signData(byte[] data) throws SignatureException, InvalidKeyException {
		Crypto pcrpt = getInstance(); //gets the singleton
		pcrpt.signer.initSign(pcrpt.pair.getPrivate()); //sets the signer up to sign the data using the stored private key
		pcrpt.signer.update(data); //pass in the byte[] to the signature class so it can be signed
		return (pcrpt.signer.sign()); //return the signed byte[]
	}
	
	 private static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
		 	Crypto pcrpt = getInstance(); //gets the singleton
		    pcrpt.signer.initVerify(key); //set it up for verification with the provided public key
		    pcrpt.signer.update(data);  // put the byte[] data into the signature 
		    return (pcrpt.signer.verify(sig)); //verify that the provided data matches the byte[] of the signed data
	}
	 
	 public static byte[] DecodeSignature(String signature) //decode a string into a byte[]
	 {
		return Base64.getDecoder().decode(signature);
	 }
	 
	 public static String EncodeSignature(byte[] signedData) //encode a byte[] as a string
	 {
	      return Base64.getEncoder().encodeToString(signedData);     
	 }
	 
	 //generate a new keypair private method called only in constructor currently
	  private static KeyPair generateKeyPair(long seed) throws Exception {
	    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA"); //get a new key generated with the RSA algorithim
	    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN"); //get a new secure random using an algorithim and a provider of the implementation
	    rng.setSeed(seed); //set the secure random seed with the provided long seed
	    keyGenerator.initialize(1024, rng); //init the key generator with a keysize and the secure random generator 
	    return (keyGenerator.generateKeyPair()); //return the generated keypair class
	  }

	public static void Init() {//private init just calls the getInstance()
		getInstance(); //I did this cuz I had concerns about threads racing to init this.
	}	
}
