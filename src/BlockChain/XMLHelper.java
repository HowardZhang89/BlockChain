package group7Crypto;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//XML Helper class to simplify working with XML
class XMLHelper
{
	//takes a untyped object and it's class info as input to marshall it into an XML string
	public static String Marshal(Object ObjectToMarshall, @SuppressWarnings("rawtypes") Class ObjectType) throws JAXBException
	{
		  JAXBContext jaxbContext = JAXBContext.newInstance(ObjectType); //gets a JAXB context for the given class type
	      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();    //creates a marshaller from that context
	      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); //pretty formatting enable
	      StringWriter sw = new StringWriter(); //new string writer to write the object too
	      jaxbMarshaller.marshal(ObjectToMarshall,sw); //passing the untyped object and the string writer 
	      //this ^ works because I told the JXABContext what class the object is I don't need to cast the object before I give it to the marshaller 
	      return sw.toString(); //get the string from the stringwriter and return it to the caller
	}
	
	//turn a string XML into an object
	public static Object Unmarshal(String XML, @SuppressWarnings("rawtypes") Class TargetClass) throws JAXBException
	{
		  JAXBContext jaxbContext = JAXBContext.newInstance(TargetClass); //make a new JXABContext passing in the targetclass 
		  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller(); //get an unmarshaller from the context
	      StringReader reader = new StringReader(XML); //wrap the raw XML string in a character stream
	      return jaxbUnmarshaller.unmarshal(reader); //pass the reader into the unmarshaller and return the untyped object back to the caller 
	}	//this works because the caller knows desired target class so it can also cast the Object to the proper class
}


