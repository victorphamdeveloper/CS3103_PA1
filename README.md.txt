============================README============================
1. Run the program with the following argument format:
java JabberMain <googleID> <password> <XMPPServer> <XMPP port>
ex: java JabberMain bob@gmail.com password talk.google.com 5222
2. Type @roster to see the list of your contact
3. Type @chat <receiverID> to start chat session with receiver
ex: @chat bob@gmail.com
5. Type your chat message to chat with the current receiver
Note that you have to start a chat session by typing @chat command to specify a receiver. 
Else the program will output: "Please specify a buddy to chat with"
6. Typing @end will end a chat session and store all current chat session's messages to a text file in SimpleServer's directory.
Note: To store data to server, you have to start SimpleServer first
The file name format will be name_timestamp.txt
Bonus feature:
When you lose Internet connection, you can still type in the messages to send
All those unsended messages will be sent when you get connection back