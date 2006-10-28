/*
 * Created on 2005.6.19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Betalord
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.util.ArrayList;

public class Channel {
	public String name;
	private String topic; // "" represents no topic (topic is disabled for this channel)
	private String topicAuthor;
	private long topicChangedTime; // time when topic was last changed (in ms since Jan 1, 1970 UTC)
	private String key = ""; // if key is "" then this channel is not locked (anyone can join). Otherwise, user must supply correct key to join it. 
	public ArrayList clients; // clients connected to this channel
	public MuteList muteList = new MuteList(this); // contains a list of Strings (usernames) who are muted (not allowed to talk in the channel)
	
	public Channel(String channelName) {
		name = new String(channelName);
		topic = "";
		topicAuthor = "";
		clients = new ArrayList();
	}
	
	public String getTopic() {
		return topic;
	}
	
	public String getTopicAuthor() {
		return topicAuthor;
	}
	
	public long getTopicChangedTime() {
		return topicChangedTime;
	}
	
	public boolean setTopic(String newTopic, String author) {
		if (newTopic.trim().equals("*")) {
			topic = "";
			topicAuthor = author;
			topicChangedTime = System.currentTimeMillis();
			return false;
		}
		topic = newTopic.trim();
		topicAuthor = author;
		topicChangedTime = System.currentTimeMillis();
		if (TASServer.DEBUG > 1) System.out.println("* Topic for #" + name + " changed to '" + topic + "' (set by <" + author + ">)");
		return true;
	}
	
	/* sends msg as a channel message to all clients on this channel */
	public void broadcast(String msg) {
		if (msg.trim().equals("")) return ; // no message
		sendLineToClients("CHANNELMESSAGE " + name + " " + msg);
	}
	
	public boolean isTopicSet() {
		return !(topic.equals(""));
	}
	
	/* adds a new client to the listeners of this channel */
	public void addClient(Client client) {
		if (isClientInThisChannel(client)) return ; // already in the channel! 

		clients.add(client);
	}
	
	public boolean removeClient(Client client) {
		return clients.remove(client);
	}
	
	public boolean isClientInThisChannel(Client client) {
		return (clients.indexOf(client) != -1);
	}
	
	/* sends s to all clients in this channel */
	public void sendLineToClients(String s) {
		if (name.toUpperCase().equals("MAIN")) if (TASServer.LOG_MAIN_CHANNEL) {
			TASServer.writeMainChanLog(s);
		}
		for (int i = 0; i < clients.size(); i++)
			((Client)clients.get(i)).sendLine(s);
	}
	
	public boolean isLocked() {
		return !(key.equals("*"));
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
