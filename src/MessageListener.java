import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class MessageListener implements Runnable {
	// Use the XML Factory class to create a XML pull-parser
	XMLStreamReader parser = XmppConnection.parser;

	private synchronized void handleMessage(String from) throws XMLStreamException {
		boolean done = false;

        while ( !done ) {

            // Get the next XML event from the parser
            int eventType = parser.next();

            // Check if the parse event is a XML start tag	
            if ( eventType == XMLStreamConstants.START_ELEMENT ) {

                if ( parser.getLocalName().equals( "body" ) ) {
                    // Add the authentication mechanism to the list
                    System.out.println(from.substring(0, from.indexOf("/")) + " : "+parser.getElementText());
                }
            }
            else if ( eventType == XMLStreamConstants.END_ELEMENT ) {
                if ( parser.getLocalName().equals( "message" ) ) {
                    done = true;
                }
            }
            
           //eventType = parser.next();
            
        }
	}
	@Override
	public synchronized void run() {
		while ( true ) {
			int eventType = parser.getEventType();

            // Check if the parse event is a XML start tag
            if ( eventType == XMLStreamConstants.START_ELEMENT ) {
            	
                if ( parser.getLocalName() != null && parser.getLocalName().equals( "message" ) ) {
					try {
						eventType= parser.next();
						System.out.println("Handle message now");
						int count = parser.getAttributeCount();
						String from = null;
						for(int i=0;i<count;i++){
							if(parser.getAttributeLocalName(i).equals("from")){
								from = parser.getAttributeValue(i);
							}
						}
						
						handleMessage(from);

					} catch (XMLStreamException e) {
				
						System.out.println("Errorrorororororororororororororor");
						e.printStackTrace();
					}
					
					System.out.println("Done message");
					 
                }else{
                	//System.out.println("start but not message");
                }
                

            }
            
            // Get the next XML event from the parser
        	//System.out.println("asda");
                 }

	}
	
}
