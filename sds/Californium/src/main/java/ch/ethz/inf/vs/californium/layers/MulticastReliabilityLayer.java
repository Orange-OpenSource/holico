/**
 * 
 */
package ch.ethz.inf.vs.californium.layers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import java.util.TimerTask;

import ch.ethz.inf.vs.californium.coap.Message;
import ch.ethz.inf.vs.californium.coap.Message.messageType;

/**
 * @author GOUL5436
 *
 */
public class MulticastReliabilityLayer extends UpperLayer
{
	// Nested Classes //////////////////////////////////////////////////////////////

	/*
	 * Entity class to keep state of requests
	 */
	private static class PendingRequest
	{
		public Message msg;
		public long timestamp;
		public long delay;
		public ArrayList<InetAddress> peers;

		public PendingRequest(Message msg, long timestamp, long delay, Map<InetAddress, Long> connectedPeer)
		{
			this.msg = msg;
			this.timestamp = timestamp;
			this.delay = delay;
			this.peers = new ArrayList<InetAddress>();
			peers.addAll(connectedPeer.keySet());
		}
	}

	// Members /////////////////////////////////////////////////////////////////////

	public static long RETRANSMISSION_DELAY = 1000; // 1 s
	public static long MAX_RETRANSMISSION_DELAY = 30000; // 5 mn
	public static long MAX_PENDING_REQUESTS = 100;

	private static String localHostName = null;

	static
	{
		try
		{
			localHostName = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException e)
		{
		}
	}

	private boolean discovered = false;
	private Map<InetAddress, Long> connectedPeers = new ConcurrentHashMap<InetAddress, Long>();

	private Map<Integer, PendingRequest> pendingRequests = new ConcurrentHashMap<Integer, PendingRequest>();
	private Timer timer = null;

	private void addPendingMessage(Message msg)
	{
		if (pendingRequests.size() > MAX_PENDING_REQUESTS)
		{
			System.err.println("TROP DE MESSAGES EN ATTENTE");
		}
		else if (!connectedPeers.isEmpty() || !discovered) // si vide rien à attendre
		{
			if (pendingRequests.containsKey(msg.getMID())) // temporaire (ne doit pas se produire) !!
			{
				System.err.println("ATTENTION NOUVEL ENVOI AVEC MEME ID : "+msg.getMID());
			}
			long now = System.currentTimeMillis();
			pendingRequests.put(msg.getMID(), new PendingRequest(msg, now, RETRANSMISSION_DELAY, connectedPeers));
			if (timer == null)
			{
				timer = new Timer();
				timer.schedule(new TimerTask()
				{
					public void run()
					{
						long now = System.currentTimeMillis();
						Iterator it = pendingRequests.values().iterator();
						while (it.hasNext())
						{
							PendingRequest request = (PendingRequest) it.next();
							if (request.delay > MAX_RETRANSMISSION_DELAY)
							{
								for (InetAddress address : request.peers)
								{
									Long lastTimestamp = connectedPeers.get(address);
									if ((lastTimestamp != null) && (now - lastTimestamp > MAX_RETRANSMISSION_DELAY)) // l'équipement ne répond plus => on le suppose déconnecté
									{
										connectedPeers.remove(address);
									}
								}
								it.remove();
							}
							else if (now >= request.timestamp + request.delay)
							{
System.out.println("*** RETRANSMISSION:"+request.msg.key());
			                    request.delay *= 2;
							    try
							    {
									sendMessageOverLowerLayer(request.msg);
								}
							    catch (IOException e)
								{
								}
							}
						}
						if (pendingRequests.isEmpty())
						{
							cancel();
							timer = null;
						}
					}
				}, RETRANSMISSION_DELAY, RETRANSMISSION_DELAY);
			}
		}
	}

	private void removePendingMessage(Message msg)
	{
		PendingRequest request = pendingRequests.get(msg.getMID());
		if (request != null)
		{
		   request.peers.remove(msg.getPeerAddress().getAddress());
		   if (request.peers.isEmpty())
		   {
		      pendingRequests.remove(msg.getMID());
		   }
		}
	}

	/**
	 * 
	 */
	public MulticastReliabilityLayer()
	{
	}

	// I/O implementation //////////////////////////////////////////////////////

	@Override
	protected void doSendMessage(Message msg) throws IOException
	{ 
		// set message ID
		if (msg.getMID() < 0)
		{
			msg.setMID(TransactionLayer.nextMessageID());
		}

		if (msg.getType() == messageType.CON)
		{
			addPendingMessage(msg);
		}

System.out.println("*** Send:"+msg.key()+" +++ " + pendingRequests.size());
	    sendMessageOverLowerLayer(msg);
	}	

	@Override
	protected void doReceiveMessage(Message msg)
	{
		InetAddress peerAddress = msg.getPeerAddress().getAddress();
		if (!peerAddress.getCanonicalHostName().equals(localHostName)) // on laisse tomber l'adresse locale, message envoyé par moi-même
		{
			discovered = true;
            connectedPeers.put(peerAddress, System.currentTimeMillis());

			if (msg.getType() == messageType.ACK)
			{
				removePendingMessage(msg);
			}
System.out.println("*** Receive:"+msg.key()+" --- " + pendingRequests.size());
            deliverMessage(msg); // Ne permet pas 2 nodes sur la même interface réseau !!
		}
	}
}
