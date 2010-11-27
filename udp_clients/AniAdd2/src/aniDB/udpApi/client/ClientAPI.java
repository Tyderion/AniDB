/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package aniDB.udpApi.client;

import aniDB.udpApi.client.accountManagement.*;
import aniDB.udpApi.client.registerManagement.*;
import aniDB.udpApi.client.replies.*;
import aniDB.udpApi.client.requests.*;
import aniDB.udpApi.shared.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 *
 * @author Arokh
 */
public class ClientAPI {
	public static final int MAX_RETRY_COUNT = 3; //Drop Cmd after * retry
	public static final int MAX_REPLY_PENDING_COUNT = 3; //Delay sinding next cmd until pending replies is lower than *
	public static final int CLIENT_VERSION = 8;
	public static final int PROTOCOL_VERSION = 3;
	public static final int ANIDB_API_PORT = 9000;
	public static final String ANIDB_API_HOST = "127.0.0.1";//"api.anidb.info";
	public static final String CLIENT_TAG = "anitracker";
	public static final String NO_DELAY = "";//"FILE,ANIME,MYLISTADD"; //Cmds which do not need to respect PACKET_DELAY
	public static final TimeSpan PACKET_DELAY = new TimeSpan(2200); // TimeSpan.FromSeconds(2.2 /*0.0*/); //Delay between each packet send
	public static final TimeSpan PING_INTERVAL = new TimeSpan(5 * 60 * 1000); // TimeSpan.FromMinutes(5); //Pinginterval, to keep connection alive (NAT)
	public static final TimeSpan REPLY_TIMEOUT = new TimeSpan(20000); // TimeSpan.FromSeconds(20 /*2*/); //Time until reply is considered lost


	private final DatagramSocket com;
	private final List<CmdInfo> pendingRequests;
	private final List<ReplyInfo> serverReplies;
	private final List<Query> queries;

	private Receive receive;
	private Send send;
	private Idle idle;

	private IAction<ReplyInfo> throwEvent;
	private Object hQuery;


	private Date lastPacketOn;
	private Date lastDelayPacketOn;

	private int pendingReplyCount;

	private boolean behindNAT;
	private boolean isEncodingSet;

	private boolean apiUnavailable;
	private Date apiDownUntil;


	private RegisterManagement registerManagement;
	private AccountManagement accountManagement;

	public ClientAPI() throws Exception {
		ByReference<IAction<ReplyInfo>> throwEventRef = new ByReference<IAction<ReplyInfo>>();
		registerManagement = new RegisterManagement(throwEventRef);
		throwEvent = throwEventRef.getParam();

		accountManagement = new AccountManagement();
		accountManagement.addAccountListener(new IAccountListener() {
			public void action(UserAccount account, AccountEvent accountEvent) {
				accountListener(account, accountEvent);
			}
		});

		com = new DatagramSocket(ANIDB_API_PORT, Inet4Address.getByName(ANIDB_API_HOST));

		pendingRequests = new ArrayList<CmdInfo>();
		serverReplies = new ArrayList<ReplyInfo>();
		queries = new ArrayList<Query>();

		receive = new Receive(this);
		send = new Send(this);
		idle = new Idle(this);

		try {
			hQuery = registerManagement.register(new IAction<Reply>() { public void invoke(Reply param) { internalReplyHandling(param); } });
		} catch(Exception ex) {}
	}

	private void accountListener(UserAccount account, IAccountListener.AccountEvent accountEvent){
		switch(accountEvent) {
			case Added:
				authenticate(account);
		}
	}


	public void queryCmd(Cmd cmd, Object handle) {
		if(cmd == null) throw new NullPointerException();
		if(!(handle instanceof  IdGenerator) || handle == null) throw new IllegalArgumentException();

		queryCmd(new CmdInfo(cmd, (IdGenerator)handle, null));
	}

	private void queryCmd(CmdInfo cmdInfo) {
		cmdInfo.getCmd().setFinal();

		synchronized(pendingRequests) {
			pendingRequests.add(cmdInfo);
			if(!send.isAlive()) send.Start();
		}
	}


	private void authenticate(UserAccount account) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	private void authenticate() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void logOut(UserAccount account, boolean b) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	private void logOut(boolean b) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void processReply(String replyStr) throws Exception{
		ReplyInfo replyInfo = ReplyInfo.parseReplyString(replyStr); //TODO: refactor parseReplyString to retrun ReplyInfo
		if(replyInfo.getId() == null) {
			serverReplies.add(replyInfo);

		} else {
			Query query = queries.get(replyInfo.getIndex());

			if(query.getState() == Query.State.Success) {
				//TODO Log
				return;
			}

			replyInfo.getReply().setAccount(query.getCmdInfo().getCmd().getAccount());

			query.setReplyInfo(replyInfo);
		}

		if(internalReplyHook(replyInfo)) throwEvent.invoke(replyInfo); //RegisterManagement.throwEvent
	}


	private void internalReplyHandling(Reply reply) {

		if(reply.getTag().equals("auth")) {
			switch(reply.getCode()) {
				case LOGIN_ACCEPTED:
				case LOGIN_ACCEPTED_NEW_VER:
					if(reply.getAccount() == null) return; //TODO: Log failure

					if(reply.getAccount().isAuthenticated()) logOut(reply.getAccount(), false);
					reply.getAccount().setSession(reply.getMessage().substring(0, reply.getMessage().indexOf(" ")));
					reply.getAccount().setAuthenticated(true);

					apiUnavailable = false;

					try {
						InetAddress inetIP = InetAddress.getByName(Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b").matcher(reply.getMessage()).group());
						behindNAT = !InetAddress.getLocalHost().equals(inetIP);
					} catch(UnknownHostException ex) {}

					
					if(reply.getAccount().areAnyOptionsSet(UserAccount.Option.Push_Notify, UserAccount.Option.Push_Message, UserAccount.Option.Push_Buddy)) {

						boolean pushPending = false;
						synchronized(pendingRequests) {
							for(CmdInfo pendingRequest : pendingRequests) {
								if(pendingRequest.getCmd().getAccount() != null &&
								   pendingRequest.getCmd().getAccount().equals(reply.getAccount()) &&
								   pendingRequest.getCmd().getAction().equals("PUSH")) {

									pushPending = true;
									break;
								}
							}
						}

						if(!pushPending) { //TODO
							//PUSH pushCmd = new PUSH();
							//pushCmd.setAccount(reply.getAccount());
							//pushCmd.setBuddy(reply.getAccount().isOptionSet(UserAccount.Option.Push_Buddy));
							//pushCmd.setMessage(reply.getAccount().isOptionSet(UserAccount.Option.Push_Message));
							//pushCmd.setNotify(reply.getAccount().isOptionSet(UserAccount.Option.Push_Notify));

							//queryCmd(pushCmd.makeFinal(), hQuery);
						}
					}

					if(reply.getCode() == ReplyCodes.LOGIN_ACCEPTED_NEW_VER) {
					}

					break;

				case CLIENT_VERSION_OUTDATED:
				case LOGIN_FAILED:
					break;
				default:
					break;
			}

		} else if(reply.getTag().equals("logout")) {
			switch(reply.getCode()) {
				case LOGGED_OUT:
				case NOT_LOGGED_IN:
				default:
					reply.getAccount().setAuthenticated(false);
					//IsEncodingSet = false
					break;
			}
		}

		switch(reply.getCode()) {
			case NOTIFICATION_ENABLED:

			case PUSHACK_CONFIRMED:
			case UPTIME:
			case PONG:

			case NO_SUCH_PACKET_PENDING:
			case ACCESS_DENIED:
			case CLIENT_BANNED:
			case ILLEGAL_INPUT_OR_ACCESS_DENIED:
			case API_PASSWORD_NOT_DEFINED:
			case NO_SUCH_ENCRYPTION_TYPE:

			case ENCRYPTION_ENABLED:
			case ENCODING_CHANGED:
			case ENCODING_NOT_SUPPORTED:
				break;
		}
	}
	private boolean internalReplyHook(ReplyInfo replyInfo) {
		UserAccount account = replyInfo.getReply().getAccount();

		Query.State queryState = Query.State.Success;

		switch(replyInfo.getReply().getCode()) {
			case NOTIFICATION_NOTIFY:
			case NOTIFICATION_MESSAGE:
			case NOTIFICATION_BUDDY:
			case NOTIFICATION_SHUTDOWN:
				if(account != null ? account.isOptionSet(UserAccount.Option.Push_Ack) : false) { //TODO
					//PUSHACK pushAckcmd = new PUSHACK();
					//pushAckcmd.setPacketId(Integer.parseInt(replyInfo.getReply().getMessage().substring(0, replyInfo.getReply().getMessage().indexOf(" "))));
					//queryCmd(pushAckcmd.makeFinal(), hQuery);
					//return false;
				} else {
					//return true;
				}
				break;
			case INVALID_SESSION:
			case LOGIN_FIRST:

				if(account != null) {
					logOut(account, false);
					authenticate(account);

					queryState = Query.State.Pending;
					if(replyInfo.getId() != null) queryCmd(queries.get(replyInfo.getIndex()).getCmdInfo());
				} else {
					//TODO: Log
				}
				return false;

			case ACCESS_DENIED:
				break;
			case UNKNOWN_COMMAND:
				break;
			case ILLEGAL_INPUT_OR_ACCESS_DENIED:
				break;
			case CLIENT_VERSION_OUTDATED:
				break;
			case CLIENT_BANNED:
				break;
			case BANNED:
				break;
			case INTERNAL_SERVER_ERROR:
			case ANIDB_OUT_OF_SERVICE:
			case SERVER_BUSY:
			case API_VIOLATION:
				apiUnavailable = true;
				apiDownUntil = new Date(new Date().getTime() + 60*60);

				logOut(false);
				if(replyInfo.getId() != null) queryCmd(queries.get(replyInfo.getIndex()).getCmdInfo());
				return false;
		}

		queries.get(replyInfo.getIndex()).setState(queryState);

		return true;
	}


	private static class Receive extends ThreadBase<ClientAPI> {
		public Receive(ClientAPI api) { super(api); }

        private byte[] inflatePacket(byte[] buffer) throws IOException{
			ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            stream.skip(4);
            InflaterInputStream iis = new InflaterInputStream(stream, new Inflater(true));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2 * 1400);

            int readBytes;
            byte[] b = new byte[1024];
            while((readBytes = iis.read(b))!= -1) baos.write(b, 0, readBytes);

            return baos.toByteArray();
        }

		@Override
		protected void DoWork() {
			byte[] b = new byte[1400];
			DatagramPacket packet = new DatagramPacket(b, 1400);
			String replystr = "";
			
			Thread t;

			while(!terminate) {
				try {
					api.com.receive(packet);

					replystr = new String(b[0] == 0 && b[1] == 0 ? inflatePacket(b) : b, "UTF8");

					t = new Thread(new Reply(replystr));
					t.start();
				} catch(Exception ex) {

				}
			}

		}

		private class Reply implements Runnable {
            String replyStr;

            public Reply(String replyStr) {
                this.replyStr = replyStr;
            }

            public void run() {
				try {
					api.processReply(replyStr);
				} catch(Exception ex) {
					//TODO Error handling
				}
            }
        }
	}
	private static class Send extends ThreadBase<ClientAPI> {
		public Send(ClientAPI api) { super(api); }

		private DatagramPacket packet;

		@Override
		protected void DoWork() {
			boolean cmdReordered, isAuthed;
			Date now;

			try {
				packet = new DatagramPacket(new byte[0], 0, InetAddress.getByName(ANIDB_API_HOST), ANIDB_API_PORT);
			} catch(UnknownHostException ex) {}

			CmdInfo cmdInfo;
			while(!api.pendingRequests.isEmpty() && !terminate) {
				try {
					now = new Date();
					cmdReordered = false;
					synchronized(api.pendingRequests) { cmdInfo = api.pendingRequests.get(0); } //TODO: Possible racing condition

					UserAccount account = cmdInfo.getCmd().getAccount();
					if(cmdInfo.getCmd().isAuthRequired() && api.accountManagement.contains(account)) {
						isAuthed = account.isAuthenticated();
					} else {
						isAuthed = false;
					}
					if(api.pendingReplyCount < MAX_REPLY_PENDING_COUNT) {
						if((!cmdInfo.getCmd().isAuthRequired() || isAuthed) && (
						  NO_DELAY.contains(cmdInfo.getCmd().getAction()) ||
						  api.lastDelayPacketOn.getTime() == 0 ||
						  DateUtil.substract(now, api.lastDelayPacketOn).compareTo(PACKET_DELAY) < 0
						 )
						) {
							//Cmd doesn't need login or client is logged in and is allowed to send
							//send cmds from top to bottom
							
							transformCmd(cmdInfo);

							api.com.send(packet);
							api.lastPacketOn = new Date(now.getTime());

							if(!ClientAPI.NO_DELAY.contains(cmdInfo.getCmd().getAction())) {
								api.lastDelayPacketOn = new Date(now.getTime());
							}
							
							synchronized(api.pendingRequests) { api.pendingRequests.remove(0); }
							
						} else {
							//Cmd needs login but client is not connected OR Cmd needs delay which has not yet passed
							//Move command without (login req./delay req.) to top
							boolean r1, r2, n1, n2, canOptimize;
							r1 = cmdInfo.getCmd().isAuthRequired();
							n1 = ClientAPI.NO_DELAY.contains(cmdInfo.getCmd().getAction());
							if((!isAuthed && r1) || !n1) {
								synchronized(api.pendingRequests) {
									for(int i = 0; i < api.pendingRequests.size(); i++) {
										r2 = api.pendingRequests.get(i).getCmd().isAuthRequired();
										n2 = NO_DELAY.contains(api.pendingRequests.get(i).getCmd().getAction());
										canOptimize = (!isAuthed && !n1 && !r1 && n2 && !r2) ||
											          (!isAuthed && !n1 &&  r1 &&       !r2) ||
													  (!isAuthed &&  n1 &&  r1 &&       !r2) ||
													  ( isAuthed && !n1 && !r1 && n2       ) ||
													  ( isAuthed && !n1 &&  r1 && n2       );
										if(canOptimize) {
											cmdInfo = api.pendingRequests.set(0, api.pendingRequests.get(i));
											api.pendingRequests.set(i, cmdInfo);
											cmdReordered = true;
											break;
										}
									}
								}
							}
						}
					}
					if(!cmdReordered) Thread.sleep(200); //Sleep to prevent CPU load
				} catch(Exception ex) {
				}
			}
		}
		private void transformCmd(CmdInfo cmdInfo) throws UnsupportedEncodingException {
			Query query;

			if(cmdInfo.getIndex() != null) {
				query = api.queries.get(cmdInfo.getIndex());
				query.setRequestOn(new Date());

			} else {
				cmdInfo.setIndex(api.queries.size());
				query = new Query();
				query.setCmdInfo(cmdInfo);

				api.queries.add(query);
			}

			String session = null;
			UserAccount account = cmdInfo.getCmd().getAccount();

			if(cmdInfo.getCmd().isAuthRequired() && api.accountManagement.contains(account)) {
				session = account.getSession();
			}

			String cmdStr = cmdInfo.toString(session);
			byte[] b = cmdStr.getBytes(api.isEncodingSet ? "UTF-8" : "US-ASCII");
			api.isEncodingSet = true;

			packet.setData(b, 0, b.length);
		}
	}
	private static class Idle extends ThreadBase<ClientAPI> {
		public Idle(ClientAPI api) { super(api); }

		private static final int RESEND_REQUEST = 1;
		private static final int REQUEST_FAILED = 2;
		private static final int PENDING_REPLY = 4;

		@Override
		protected void DoWork() {
			Date now;
			int repliesPending;

			Query query;
			int state;
			do {
				try {
					now = new Date();
					if(!api.apiUnavailable) {
						if(SendPing(now)) api.queryCmd(PING.createRequest(null, null, null), api.hQuery);
						repliesPending = 0;
						for(int i = 0; i < api.queries.size(); i++) {
							query = api.queries.get(i);
							state = PendingReplyState(now, query);
							if((state & RESEND_REQUEST) != 0) {
								query.raiseRetryCount();
								query.setRequestOn(new Date());
								api.queryCmd(query.getCmdInfo());
							} else if((state & REQUEST_FAILED) != 0) {
								query.setState(Query.State.Failed);
							}
							if((state & PENDING_REPLY) != 0) repliesPending++;
						}
						api.pendingReplyCount = repliesPending;
					} else if(api.apiDownUntil.getTime() != 0 && DateUtil.substract(api.apiDownUntil, now).getTotalSeconds() <= 0) {
						api.authenticate();
					}
					Thread.sleep(500);
				} catch(InterruptedException ex) {}
			} while(!terminate);
		}

		private boolean SendPing(Date now) {
			boolean sendPing = false;
			
			for(UserAccount account : api.accountManagement) {
				sendPing |= account.isOptionSet(UserAccount.Option.KeepAlive);
			}

			sendPing &= api.behindNAT && api.lastPacketOn != null && DateUtil.substract(now, api.lastPacketOn).compareTo(PING_INTERVAL) > 0;

			return sendPing;
		}
		private int PendingReplyState(Date now, Query query) {
			boolean hasPendingReply, hasTimedOut, isAuthed, hasFailed;
			int state = 0;

			hasPendingReply = query.getState() == Query.State.Pending && query.getRequestOn() != null;
			if(hasPendingReply) {
				hasTimedOut = DateUtil.substract(now, query.getRequestOn()).compareTo(REPLY_TIMEOUT) > 0;
				isAuthed = api.accountManagement.contains(query.getCmdInfo().getCmd().getAccount()) && query.getCmdInfo().getCmd().getAccount().isAuthenticated();

				if(hasTimedOut && (!query.getCmdInfo().getCmd().isAuthRequired() || isAuthed)) {
					hasFailed = query.getRetryCount() >= MAX_RETRY_COUNT;

					if(hasFailed) {
						state |= REQUEST_FAILED;
					} else {
						state |= RESEND_REQUEST;
					}

				} else {

				}

				hasPendingReply = query.getRetryCount() <= ClientAPI.MAX_RETRY_COUNT && query.getState() != Query.State.Failed;
				if(!hasPendingReply) state |= PENDING_REPLY;

			} else {
				//None
			}

			return state;
		}
	}
}