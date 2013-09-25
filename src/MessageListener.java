import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class MessageListener implements Runnable {
	// Use the XML Factory class to create a XML pull-parser
	XMLStreamReader parser = XmppConnection.parser;

	private void handleMessage(String from)
			throws XMLStreamException {
		boolean done = false;

		while (!done) {

			// Get the next XML event from the parser
			int eventType = parser.next();

			// Check if the parse event is a XML start tag
			if (eventType == XMLStreamConstants.START_ELEMENT) {

				if (parser.getLocalName().equals("body")) {
					// Add the authentication mechanism to the list
					System.out.println(from.substring(0, from.indexOf("/"))
							+ " : " + parser.getElementText());
				}
			} else if (eventType == XMLStreamConstants.END_ELEMENT) {
				if (parser.getLocalName().equals("message")) {
					done = true;
				}
			}

			// eventType = parser.next();

		}
	}

	@Override
	public void run() {

		while (true) {
			int eventType = parser.getEventType();
			// Check if the parse event is a XML start tag
			if (eventType == XMLStreamConstants.START_ELEMENT) {
				if (parser.getLocalName() != null
						&& parser.getLocalName().equals("message")) {
					try {

						System.out.println("Handle message now");
						int count = parser.getAttributeCount();
						String from = null;
						for (int i = 0; i < count; i++) {
							if (parser.getAttributeLocalName(i).equals("from")) {
								from = parser.getAttributeValue(i);
							}
						}

						handleMessage(from);

					} catch (XMLStreamException e) {
						e.printStackTrace();
					}

				} else if (parser.getLocalName() != null
						&& parser.getLocalName().equals("query")) {
					try {
						handleIQQueryTag();
					} catch (XMLStreamException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			} else if (eventType == XMLStreamConstants.END_ELEMENT) {
			}

			// Get the next XML event from the parser
			// This code get conflicted with the main thread when reading the
			// socket stream
			try {
				eventType = parser.next();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void handleIQQueryTag() throws XMLStreamException {
		boolean done = false;
		System.out.println("Contact list:");
		while (!done) {

			// Get the next XML event from the parser
			int eventType = parser.next();
			// Check if the parse event is a XML start tag
			if (eventType == XMLStreamConstants.START_ELEMENT) {

				if (parser.getLocalName().equals("item")) {
					// Add the authentication mechanism to the list
					System.out.println(parser.getAttributeValue(0));
					XmppConnection.contactList.add(parser.getAttributeValue(0));
				}
			} else if (eventType == XMLStreamConstants.END_ELEMENT) {
				if (parser.getLocalName().equals("query")) {
					done = true;
				}
			}
		}

	}

}
