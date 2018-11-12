package cn.xyz.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.vo.User;

@Component
public class KXMPPServiceImpl implements ApplicationContextAware  {
	List<String> sysUserList=new ArrayList<String>();
	Map<String,XMPPConnection> connMap=new HashMap<String,XMPPConnection>();
	//收红包
	//{
	//  "type":83
	//	"fromUserId":""
	//	"fromUserName":""
	//	"ObjectId":"如果是群聊，则为房间Id"
	//	"timeSend":123
	public static final int  OPENREDPAKET=83;
	//上传文件
	//{
	//"type":401,
	//"content":"文件名",
	//"fromUserId":"上传者",
	//"fromUserName":"",
	//"ObjectId":"文件Id"
	//"timeSend":123
	//}
	public static final int FILEUPLOAD=401;
	
	//删除文件
	//{
	//"type":402,
	//"content":"文件名",
	//"fromUserId":"删除者",
	//"fromUserName":"",
	//"ObjectId":"文件Id",
	//"timeSend":123
	//}
	public static final int DELETEFILE=402;
	
	
	// 修改昵称
	// {
	// "type": 901,
	// "objectId": "房间Id",
	// "fromUserId": 10005,
	// "fromUserName": "10005",
	// "toUserId": 用户Id,
	// "toUserName": "用户昵称",
	// "timeSend": 123
	// }
	public static final int CHANGE_NICK_NAME = 901;

	// 修改房间名
	// {
	// "type": 902,
	// "objectId": "房间Id",
	// "content": "房间名",
	// "fromUserId": 10005,
	// "fromUserName": "10005",
	// "timeSend": 123
	// }
	public static final int CHANGE_ROOM_NAME = 902;

	// 删除成员
	// {
	// "type": 904,
	// "objectId": "房间Id",
	// "fromUserId": 0,
	// "fromUserName": "",
	// "toUserId": 被删除成员Id,
	// "timeSend": 123
	// }
	public static final int DELETE_MEMBER = 904;
	// 删除房间
	// {
	// "type": 903,
	// "objectId": "房间Id",
	// "content": "房间名",
	// "fromUserId": 10005,
	// "fromUserName": "10005",
	// "timeSend": 123
	// }
	public static final int DELETE_ROOM = 903;
	// 禁言
	// {
	// "type": 906,
	// "objectId": "房间Id",
	// "content": "禁言时间",
	// "fromUserId": 10005,
	// "fromUserName": "10005",
	// "toUserId": 被禁言成员Id,
	// "toUserName": "被禁言成员昵称",
	// "timeSend": 123
	// }
	public static final int GAG = 906;
	// 新成员
	// {
	// "type": 907,
	// "objectId": "房间Id",
	// "fromUserId": 邀请人Id,
	// "fromUserName": "邀请人昵称",
	// "toUserId": 新成员Id,
	// "toUserName": "新成员昵称",
	// "content":"是否显示阅读人数",  1:开启  0：关闭
	// "timeSend": 123
	// }
	public static final int NEW_MEMBER = 907;
	// 新公告
	// {
	// "type": 905,
	// "objectId": "房间Id",
	// "content": "公告内容",
	// "fromUserId": 10005,
	// "fromUserName": "10005",
	// "timeSend": 123
	// }
	public static final int NEW_NOTICE = 905;
	//用户离线
	//
	//{
	// "type": 908,
	// "userId":"用户ID"
	// "name":"用户昵称"
	// "coment":"用户离线"
	//}
	public static final int OFFLINE = 908;
	//用户上线
	//{
	// "type": 909,
	// "userId":"用户ID"
	// "name":"用户昵称"
	// "coment":"用户上线"
	//}
	public static final int ONLINE = 909;
	
	//弹幕
	//{
	//	"type":910,
	//	"formUserId":"用户ID"
	//	"fromUserName":"用户昵称"
	//	"content":"弹幕内容"
	//	"timeSend": 123
	//}
	public static final int BARRAGE= 910;
	
	//送礼物
	//{
	//	"type":911
	//	"fromUserId":"用户ID"
	//	"fromUserName":"用户昵称"
	//	"content":"礼物"
	//	"timeSend":123
	//}
	public static final int GIFT=911;
	
	//直播点赞
	//{
	//	"type":912
	//	}
	public static final int LIVEPRAISE=912;
	
	//设置管理员
	//{
	//	"type":913
	//	"fromUserId":"发送者Id"
	//	"fromUserName":"发送者昵称"
	//	"content":"1为启用  0为取消管理员"
	// 	"timeSend":123
	//}
	public static final int SETADMIN=913;
	
	//进入直播间
	// {
	//	"type":914
	//	"fromUserId":"发送者Id"
	//	"fromUserName":"发送者昵称"
	//	"objectId":"房间的JID"
	//	"timeSend":123
	//}
	public static final int JOINLIVE=914;
	
	
	/**
	 显示阅读人数
		 {
		  "type":915
			"objectId":"房间JId"
			"content":"是否显示阅读人数" 1：开启 2：关闭
		}
	 */
	public static final int SHOWREAD=915;
	public static final int TRANSFER=916;//群主转让
	public static final int APPLY=917;//申请进群
	public static final int ISNEED=918;//修改入群需要群主验证
	public static final int NONEED=919;//修改入群不需要群主验证
	public static final int AllowAdd=926;//修改群内可见成员名片
	public static final int NotAllow=927;//修改群内除群主外不可见成员名片
	
	
	
/*	*//**
	群组是否需要验证
	 {
	  "type":916
		"objectId":"房间JId"
		"content": 1：开启验证   0：关闭验证
	}
	 *//*
	public static final int RoomNeedVerify=916;
	
	*//**
		 房间是否公开
		 {
		  "type":917
			"objectId":"房间JId"
			"content": 1：不公开 隐私群   0：公开
		}
	 *//*
	public static final int RoomIsPublic=917;
	
	*//**
	 普通成员 是否可以看到 群组内的成员  
	 关闭 即普通成员 只能看到群主
	 {
	  "type":918
		"objectId":"房间JId"
		"content": 1：可见   0：不可见
	}
	 *//*
	public static final int RoomShowMember=918;
	*//**
	群组允许发送名片
	 {
	  "type":919
		"objectId":"房间JId"
		"content": 1：   允许发送名片   0：不允许发送
	}
	 *//*
	public static final int RoomAllowSendCard=919;*/
	
	/**
	群组全员禁言
	 {
	  "type":920
		"objectId":"房间JId"
		"content": tailTime   禁言截止时间
	}
	 */
	public static final int RoomAllBanned=920;
	
	/**
	群组允许成员邀请好友
	 {
	  "type":921
		"objectId":"房间JId"
		"content": 1：  允许成员邀请好友   0：不允许成员邀请好友
	}
	 */
	public static final int RoomAllowInviteFriend=921;
	
	/**
	群组允许成员上传群共享文件
	 {
	  "type":922
		"objectId":"房间JId"
		"content": 1：  允许成员上传群共享文件   0：不允许成员上传群共享文件
	}
	 */
	public static final int RoomAllowUploadFile=922;
	/**
	群组允许成员召开会议

	 {
	  "type":923
		"objectId":"房间JId"
		"content": 1：  允许成员召开会议   0：不允许成员召开会议
	}
	 */
	public static final int RoomAllowConference=923;
	
	/**
	群组允许成员开启 讲课
	 {
	  "type":924
		"objectId":"房间JId"
		"content": 1：  允许成员开启 讲课   0：不允许成员开启 讲课
	}
	 */
	public static final int RoomAllowSpeakCourse=924;
	/**
	群组转让 接口
	 {
	 fromUserId:旧群主ID
	  "type":925
		"objectId":"房间JId"
		"toUserId": 新群组用户ID
	}
	 */
	public static final int RoomTransfer=925;
	
	
	
	//点赞
	//{
	//	"type":301
	//
	//
	//}
	public static final int PRAISE=301;
	//评论
	//{
	//	"type":302
	//}
	public static final int COMMENT=302;
	
	//朋友圈的提醒
	//{
	//"type":304
	//}
	public static final int REMIND=304;
	
	private static ApplicationContext context;
	
	private static final Logger log = Logger.getLogger(KXMPPServiceImpl.class
			.getName());
	public static KXMPPServiceImpl getInstance() {
		return context.getBean(KXMPPServiceImpl.class);
	}
	
	
	@Resource(name = "xmppConfig")
	private XMPPConfig xmppConfig;
	
	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;

	private XMPPConnection connection;
	private ConnectionConfiguration config;
	private String from;
	private XMPPConnection userConnection;

	private String userFrom;
	
	private ConnectionConfiguration getConfig(){
		if (null == config) {
				config = new ConnectionConfiguration(xmppConfig.getHost(), xmppConfig.getPort());
				config.setSASLAuthenticationEnabled(false);
				config.setDebuggerEnabled(false);
				config.setSecurityMode(SecurityMode.disabled);
			
		}
		return config;
	}
	public synchronized XMPPConnection createConnection() throws Exception {
		
		if (null == connection) {
			connection = new XMPPConnection(getConfig());
			connection.connect();
			try {
				examineTigaseUser(xmppConfig.getUsername(), DigestUtils.md5Hex(xmppConfig.getPassword()),1);
				connection.login(xmppConfig.getUsername(), DigestUtils.md5Hex(xmppConfig.getPassword()),"mobileClient");
			} catch (XMPPException e) {
				//登陆失败 可能是系统 账号不存在  重新注册
				register();
				connection.login(xmppConfig.getUsername(), DigestUtils.md5Hex(xmppConfig.getUsername()),"mobileClient");
			}
			
			/** 设置状态 */ 
	        Presence presence = new Presence(Presence.Type.available);  
	        presence.setStatus("Q我吧");  
	       
	        connection.sendPacket(presence);  
	        connection.addConnectionListener(new MyConnectionListener(connection));
			
			from = xmppConfig.getUsername()+"@" + connection.getServiceName();
		}
		return connection;
	}
	public XMPPConnection getConnection() throws Exception {
		if (null == connection)
			connection=createConnection();
		
		/*else if (!connection.isConnected()){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					log.info("xmpp链接断了   重新链接========>");
					while (!connection.isConnected()) {
						try {
							Thread.currentThread().sleep(2000);
							connection.connect();
						}catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}*/
		
		else if(!connection.isConnected()){
			 connection.connect();
			 connection.login(xmppConfig.getUsername(),Md5Util.md5Hex(xmppConfig.getPassword()),"mobileClient");
		}else if(!connection.isAuthenticated())
			 connection.login(xmppConfig.getUsername(),Md5Util.md5Hex(xmppConfig.getPassword()),"mobileClient");
		
		return connection;
	}

public XMPPConnection getConnection(String username) throws Exception {
	XMPPConnection conn=null;
	conn=connMap.get(username);
		if(conn!=null&&conn.isConnected()){
			if(conn.isAuthenticated()){
				return conn;
				//PingManager.getInstanceFor(conn).setPingInterval(5);
			}else {
				conn.login(username, Md5Util.md5Hex(username));
				connMap.put(username, conn);
				return conn;
			}
		}
		conn = new XMPPConnection(getConfig());
		conn.connect();
		examineTigaseUser(username, Md5Util.md5Hex(username),0);
		conn.login(username, Md5Util.md5Hex(username));
		connMap.put(username, conn);
		conn.addConnectionListener(new MyConnectionListener(conn));
			
		
		return conn;
	}

	public XMPPConnection getConnection(String username, String password) throws Exception {

		
		if(userConnection!=null&&userConnection.isConnected())
			userConnection.disconnect();
			
			userConnection = new XMPPConnection(getConfig());
			userConnection.connect();
			 examineTigaseUser(username, password, 1);
			userConnection.login(username, password);
			userConnection.addConnectionListener(new MyConnectionListener(userConnection));
			userFrom = username + "@" + userConnection.getServiceName();
		
		return userConnection;
	}

	// 引入smack包用于注册用户到Tigase（同步用户到Tigase）
	// <dependency>
	// <groupId>org.igniterealtime.smack</groupId>
	// <artifactId>smack</artifactId>
	// <version>3.2.1</version>
	// </dependency>
	// <dependency>
	// <groupId>org.igniterealtime.smack</groupId>
	// <artifactId>smackx</artifactId>
	// <version>3.2.1</version>
	// </dependency>
	public void register() {
		try {
			/*ConnectionConfiguration config = new ConnectionConfiguration(host, 5222);
			config.setSASLAuthenticationEnabled(false);
			config.setDebuggerEnabled(false);
			config.setSecurityMode(SecurityMode.disabled);

			XMPPConnection con = new XMPPConnection(config);
			//con.connect();
			// username帐号assword密码
			con.getAccountManager().createAccount(username, password);
			con.disconnect();*/
			
			String user_id = xmppConfig.getUsername() + "@"+xmppConfig.getHost();
			BasicDBObject jo = new BasicDBObject();
			jo.put("_id", generateId(user_id));
			jo.put("user_id", user_id);
			jo.put("domain",xmppConfig.getHost());
			jo.put("password", DigestUtils.md5Hex(xmppConfig.getPassword()));
			jo.put("type", "ole");
			dsForTigase.getDB().getCollection("tig_users").save(jo);
			System.out.println("注册到Tigase：" + xmppConfig.getHost() + "," + xmppConfig.getUsername() + "," + xmppConfig.getPassword());
		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void register(String userId, String password) throws Exception {
		
		/*XMPPConnection con = getConnection();
		//con.connect();
		con.getAccountManager().createAccount(username, password);
		con.disconnect();
		System.out.println("注册到Tigase：" + host + "," + username + "," + password);*/
		DBCollection collection=dsForTigase.getDB().getCollection("tig_users");
		String user_id = userId+"@"+xmppConfig.getHost();
		BasicDBObject query = new BasicDBObject("user_id",user_id);
		if(null!=collection.findOne(query)){
			System.out.println(userId + "  已经注册了!");
			return;
		}
		BasicDBObject jo = new BasicDBObject();
		jo.put("_id", generateId(user_id));
		jo.put("user_id", user_id);
		jo.put("domain",xmppConfig.getHost());
		jo.put("password",password);
		jo.put("type", "ole");
		jo.put("xmppVersion", 0);
		collection.save(jo);
		
		System.out.println("注册到  Tigase：" +xmppConfig.getHost() + "," + userId + "," + password);
	}
	
   public void registerAndXmppVersion(String userId, String password,int xmppVersion) throws Exception {
		
		/*XMPPConnection con = getConnection();
		//con.connect();
		con.getAccountManager().createAccount(username, password);
		con.disconnect();
		System.out.println("注册到  Tigase：" + host + "," + username + "," + password);*/
		DBCollection collection=dsForTigase.getDB().getCollection("tig_users");
		String user_id = userId+"@"+xmppConfig.getHost();
		BasicDBObject query = new BasicDBObject("user_id",user_id);
		if(null!=collection.findOne(query)){
			System.out.println(userId + "  已经注册了!");
			return;
		}
		BasicDBObject jo = new BasicDBObject();
		jo.put("_id", generateId(user_id));
		jo.put("user_id", user_id);
		jo.put("domain",xmppConfig.getHost());
		jo.put("password",password);
		jo.put("type", "ole");
		jo.put("xmppVersion", xmppVersion);
		collection.save(jo);
		
		System.out.println("注册到 Tigase" +xmppConfig.getHost() + "," + userId + "," + password);
	}

	public void registerByThread(String userId, String password,int xmppVersion) throws Exception {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					KXMPPServiceImpl.getInstance().registerAndXmppVersion(userId, password,xmppVersion);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void changePassword(String username, String password, String newPassword) throws Exception {
		XMPPConnection con = getConnection();
		con.login(username, password,"mobileClient");
		con.getAccountManager().changePassword(newPassword);
		con.disconnect();
		System.out.println("更新密码到Tigase： " + xmppConfig.getHost() + ", " + username + "  , " + password);
	}
	
	
	
	
	/**
	  * 加入群
	  * @param roomJid   群的 jid
	  * @param userName  用戶id
	  * @param password  用戶密碼
	  */
	 public void joinMucRoom(String roomJid,String userName,String password) {
		 XMPPConnection connection=null;
	        try {
	           connection = getConnection(userName,password);
	           
	           String jid = roomJid + getMucChatServiceName(connection);
	           
	           MultiUserChat muc = new MultiUserChat(connection, jid);
	           muc.join(userName, password);
	        } catch (XMPPException e) {
	            e.printStackTrace();
	        }catch (Exception e) {
	            e.printStackTrace();
	        }
	        closedConnection(connection);
	        
	 }

	

	public void joinMucRoom(String roomJid) {
	        try {
	        	// 创建聊天室
	           MultiUserChat muc = new MultiUserChat(getConnection(), roomJid);
	           muc.join("提醒");
	           //muc.invite(message, user, reason);
	        } catch (XMPPException e) {
	            e.printStackTrace();
	        }catch (Exception e) {
	            e.printStackTrace();
	        }
	 }
	 
	 
	/**
	 * 
     * 	房间名称 text-single muc#roomconfig_roomname
		描述 text-single muc#roomconfig_roomdesc
		允许占有者更改主题 boolean muc#roomconfig_changesubject
		最大房间占有者人数 list-single muc#roomconfig_maxusers
		其 Presence 是 Broadcast 的角色 list-multi muc#roomconfig_presencebroadcast
		列出目录中的房间 boolean muc#roomconfig_publicroom
		房间是持久的 boolean muc#roomconfig_persistentroom
		房间是适度的 boolean muc#roomconfig_moderatedroom
		房间仅对成员开放 boolean muc#roomconfig_membersonly
		允许占有者邀请其他人 boolean muc#roomconfig_allowinvites
		需要密码才能进入房间 boolean muc#roomconfig_passwordprotectedroom
		密码 text-private muc#roomconfig_roomsecret
		能够发现占有者真实 JID 的角色 list-single muc#roomconfig_whois
		登录房间对话 boolean muc#roomconfig_enablelogging
		仅允许注册的昵称登录 boolean x-muc#roomconfig_reservednick
		允许使用者修改昵称 boolean x-muc#roomconfig_canchangenick
		允许用户注册房间 boolean x-muc#roomconfig_registration
		房间管理员 jid-multi muc#roomconfig_roomadmins
		房间拥有者 jid-multi muc#roomconfig_roomowners
	 * @param user
	 * @param roomName
	 * @param subject
	 * @return
	 **/
    public String createMucRoom(String myNickName, String roomName, String roomSubject, String roomDesc) {
        try {
            String roomId = UUID.randomUUID().toString().replaceAll("-", "");
            String roomJid = roomId + getMucChatServiceName(getConnection());
            // 创建聊天室
            MultiUserChat muc = new MultiUserChat(getConnection(), roomJid);
            muc.create(myNickName);

            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复

            for (Iterator fields = form.getFields(); fields.hasNext();) {   
                FormField field = (FormField) fields.next();  
                if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                	// 设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }

             // 设置聊天室的新拥有者
            // List owners = new ArrayList();
           
            // submitForm.setAnswer("muc#roomconfig_roomowners", owners);

            // 设置聊天室的名字
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
            // 设置聊天室描述
            // if (!TextUtils.isEmpty(roomDesc)) {
            // submitForm.setAnswer("muc#roomconfig_roomdesc", roomDesc);
            // }
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 允许修改主题
            // submitForm.setAnswer("muc#roomconfig_changesubject", true);
            // 允许占有者邀请其他人
            // submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            //最大人数
            // List<String> maxusers = new ArrayList<String>();
            // maxusers.add("50");
            // submitForm.setAnswer("muc#roomconfig_maxusers", maxusers);
            // 公开的，允许被搜索到
            // submitForm.setAnswer("muc#roomconfig_publicroom", true);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);

            //是否主持腾出空间(加了这个默认游客进去不能发言)
            // submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
            // 房间仅对成员开放
            // submitForm.setAnswer("muc#roomconfig_membersonly", true);
            // 不需要密码
            // submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
            // false);
            // 房间密码
            // submitForm.setAnswer("muc#roomconfig_roomsecret", "111");
            // 允许主持 能够发现真实 JID
            // List<String> whois = new ArrayList<String>();
            // whois.add("anyone");
            // submitForm.setAnswer("muc#roomconfig_whois", whois);

            // 管理员
            // <field var='muc#roomconfig_roomadmins'>
            // <value>wiccarocks@shakespeare.lit<alue>
            // <value>hecate@shakespeare.lit<alue>
            // </field>

            // 仅允许注册的昵称登录
            // submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
            // 允许使用者修改昵称
            // submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
            // 允许用户注册房间
            // submitForm.setAnswer("x-muc#roomconfig_registration", false);
            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            muc.sendConfigurationForm(submitForm);

            // muc.changeSubject(roomSubject);
            // mMucChatMap.put(roomJid, muc);
            //mMucChatMap.put(roomJid, muc);
            return roomId;
        } catch (XMPPException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
	
	/**
	 * 
     * 	房间名称 text-single muc#roomconfig_roomname
		描述 text-single muc#roomconfig_roomdesc
		允许占有者更改主题 boolean muc#roomconfig_changesubject
		最大房间占有者人数 list-single muc#roomconfig_maxusers
		其 Presence 是 Broadcast 的角色 list-multi muc#roomconfig_presencebroadcast
		列出目录中的房间 boolean muc#roomconfig_publicroom
		房间是持久的 boolean muc#roomconfig_persistentroom
		房间是适度的 boolean muc#roomconfig_moderatedroom
		房间仅对成员开放 boolean muc#roomconfig_membersonly
		允许占有者邀请其他人 boolean muc#roomconfig_allowinvites
		需要密码才能进入房间 boolean muc#roomconfig_passwordprotectedroom
		密码 text-private muc#roomconfig_roomsecret
		能够发现占有者真实 JID 的角色 list-single muc#roomconfig_whois
		登录房间对话 boolean muc#roomconfig_enablelogging
		仅允许注册的昵称登录 boolean x-muc#roomconfig_reservednick
		允许使用者修改昵称 boolean x-muc#roomconfig_canchangenick
		允许用户注册房间 boolean x-muc#roomconfig_registration
		房间管理员 jid-multi muc#roomconfig_roomadmins
		房间拥有者 jid-multi muc#roomconfig_roomowners
	 * @param user
	 * @param roomName
	 * @param subject
	 * @param max
	 * @return
	 */
	public String createChatRoom(String nickName,String roomName,String subject,String max){  
		String jid = UUID.randomUUID().toString().replaceAll("-", "");  
        try{  
            MultiUserChat muc = new MultiUserChat(getConnection(), jid+"@muc."+getConnection().getServiceName());  
            // 创建聊天室  
            muc.create(nickName);   
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();   
            //向要提交的表单添加默认答复
            for (Iterator fields = form.getFields(); fields.hasNext();) {   
               FormField field = (FormField) fields.next();   
               if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) { 
            	   // 设置默认值作为答复  
                   submitForm.setDefaultAnswer(field.getVariable());   
               }   
           }   
       
          /* List list =  new ArrayList();  
           list.add(max);  
           submitForm.setAnswer("muc#roomconfig_maxusers", list); */
           
           //房间名称
          submitForm.setAnswer("muc#roomconfig_roomname", roomName);  
           //房间备注
           //         submitForm.setAnswer("muc#roomconfig_roomdesc", "wwh2222");  
           // 能够发现占有者真实 JID 的角色
           // submitForm.setAnswer("muc#roomconfig_whois", "anyone"); 
           
           // 设置聊天室是持久聊天室，即将要被保存下来  
           submitForm.setAnswer("muc#roomconfig_persistentroom", true);
           // 房间仅对成员开放  
           // submitForm.setAnswer("muc#roomconfig_membersonly", false);  
           // 允许占有者邀请其他人
           //submitForm.setAnswer("muc#roomconfig_allowinvites", true); 
           // 登录房间对话  
           submitForm.setAnswer("muc#roomconfig_enablelogging", true);  
           // 仅允许注册的昵称登录 
            //submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
           // 允许使用者修改昵称   
           	//submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);  
           // 允许用户注册房间  
           //submitForm.setAnswer("x-muc#roomconfig_registration", false);   
           muc.sendConfigurationForm(submitForm);   
           muc.changeSubject(subject);  
           System.out.println("roomJid  ===銆� "+muc.getRoom());
          // jid = muc.getRoom();  
        } catch (Exception e) {  
              e.printStackTrace();  
        }  
        return jid;  
    }  
	
	
	
	public String getMucChatServiceName(XMPPConnection connection){
		return "@muc."+connection.getServiceName();
	}
	public void send(int userId, String body) throws Exception {
		String sysUserId=sysUserList.get(0);
		if(StringUtil.isEmpty(sysUserId))
			sysUserId="10005";
		sysUserList.remove(0);
		Message message =null;
		XMPPConnection conn=null;
		try {
			conn = getConnection(sysUserId);
			 message = new Message();
			message.setFrom(sysUserId+"@"+conn.getServiceName());
		
			message.setTo(userId + "@" + conn.getServiceName());
			message.setBody(body);
			message.setType(Type.chat);
			String packetId=null;
			packetId = StringUtil.randomUUID();
			message.setPacketID(packetId);
			/*message.setPacketID(UUID.fromString(message.getPacketID()).toString());*/
			
			conn.sendPacket(message);
				System.out.println("发送推送消息" + message.toXML());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("发送推送失败!" + (null!=message?message.toXML():""));
			}
			closedConnection(conn);
			sysUserList.add(sysUserId);
		
	}

	public void send(List<Integer> userIdList, String body){
		
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				XMPPConnection conn=null;
				String sysUserId=sysUserList.get(0);

				if(StringUtil.isEmpty(sysUserId)){
					sysUserId="10005";
				}
				try {
					sysUserList.remove(0);
					conn = getConnection(sysUserId);
					Message message=null;
					String packetId=null;
					for (int userId : userIdList) {
						message = new Message();
						message.setFrom(sysUserId+"@"+conn.getServiceName());
						message.setTo(userId + "@" +conn.getServiceName());
						message.setBody(body);
						message.setType(Type.chat);
						
						packetId = StringUtil.randomUUID();
						message.setPacketID(packetId);
						conn.sendPacket(message);
						System.out.println("发送推送消息" + message.toXML());
						
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("发送推送失败!" );
				}
				closedConnection(conn);
				sysUserList.add(sysUserId);
			}
			
		});
		

	}
	public void send(List<Integer> userIdList, List<MessageBean> messageList) throws Exception {
		
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				String sysUserId=sysUserList.get(0);
				XMPPConnection conn =null;
				try {
					if(StringUtil.isEmpty(sysUserId)){
						sysUserId="10005";
					}
					sysUserList.remove(0);
					 conn = getConnection(sysUserId);
					Message message=null;
					String packetId=null;
					for (MessageBean messageBean : messageList) {
						for (int userId : userIdList) {
							 message = new Message();
							message.setFrom(sysUserId+"@"+conn.getServiceName());
							message.setTo(userId + "@" +conn.getServiceName());
							message.setBody(messageBean.toString());
							message.setType(Type.chat);
							packetId = StringUtil.randomUUID();
							message.setPacketID(packetId);
							conn.sendPacket(message);
							System.out.println("发送推送消息" + message.toXML());
							
						}
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("发送推送失败!" );
				}
				
				closedConnection(conn);
				sysUserList.add(sysUserId);
			}
			
		});
		

	}
	
	public void sendMsgToGroup(String jid, List<MessageBean> messageList) throws Exception {
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				String sysUserId=sysUserList.get(0);
				if(StringUtil.isEmpty(sysUserId)){
					sysUserId="10005";
				}
				sysUserList.remove(0);
				XMPPConnection conn=null;
				Message message=null;
				try {
					 conn = getConnection(sysUserId);
					 String packetId =null;
					for (MessageBean msg : messageList) {
						 message = new Message();
						message.setFrom(sysUserId+"@"+conn.getServiceName());
						message.setTo(jid + "@" +conn.getServiceName());
						message.setBody(msg.toString());
						message.setType(Type.groupchat);
						 packetId = StringUtil.randomUUID();
						message.setPacketID(packetId);
						conn.sendPacket(message);
					}
					System.out.println("推送成功：" + message.toXML());
					
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("发送推送失败!" + (null!=message?message.toXML():""));
				}
				
				closedConnection(conn);
				sysUserList.add(sysUserId);
				
			}
		});
		
			
			
		

	}
	//////////
	public void send(String username,String password,List<Integer> userIdList,String body) throws Exception{
		examineTigaseUser(username, password,0);
		XMPPConnection conn =null;
		Message message=null;
		conn=getConnection(username, password);
		try {
		for(int userId:userIdList){
			message = new Message();
			message.setFrom(userFrom);
			message.setTo(userId + "@" + conn.getServiceName());
			message.setBody(body);
			message.setType(Type.chat);
			String packetId = StringUtil.randomUUID();
			message.setPacketID(packetId);
			conn.sendPacket(message);
			System.out.println("系统推送成功：" + message.toXML());
			
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("发送推送失败!" + (null!=message?message.toXML():""));
		}
		closedConnection(conn);
	}
	
	public void send(String username, String password, int userId, String body) throws Exception {
		examineTigaseUser(username, password,0);
		XMPPConnection conn =null;
		Message message=null;
		try {
			conn =getConnection(username, password);
			 message = new Message();
			message.setFrom(userFrom);
			message.setTo(userId + "@" + conn.getServiceName());
			message.setBody(body);
			message.setType(Type.chat);
			String packetId = StringUtil.randomUUID();
			message.setPacketID(packetId);
		
			conn.sendPacket(message);
			System.out.println("系统推送成功：" + message.toXML());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("发送推送失败!" + (null!=message?message.toXML():""));
		}
		closedConnection(conn);
	}



	public void send(User user, int toUserId, String body) {
		
		XMPPConnection con=null;
		try {
			 con = getConnection();
			
			
			Message message = new Message();
			message.setFrom(user.getUserId() + "@" + con.getServiceName());
			message.setTo(toUserId + "@" + con.getServiceName());
			message.setBody(body);
			message.setType(Type.chat);
			String packetId = StringUtil.randomUUID();
			message.setPacketID(packetId);
			con.sendPacket(message);
			System.out.println("发送消息成功：" + message.toXML());
			
		} catch (XMPPException e) {
			System.out.println(user.getUserId()+" isAuthenticated ====> "+con.isAuthenticated());
		}catch (Exception e) {
			e.printStackTrace();
		}
		closedConnection(con);
	}

	public void send(User user, List<Integer> toUserIdList, String body) {
		XMPPConnection con =null;
		try {
			 con = getConnection(user.getUserId()+"",user.getPassword());

			for (int toUserId : toUserIdList) {
				Message message = new Message();
				message.setFrom(user.getUserId() + "@" + con.getServiceName());
				message.setTo(toUserId + "@" + con.getServiceName());
				message.setBody(body);
				message.setType(Type.chat);
				String packetId = StringUtil.randomUUID();
				message.setPacketID(packetId);
				con.sendPacket(message);
				System.out.println("公众号推送成功：" + message.toXML());
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		closedConnection(con);
	}

	
	public void examineTigaseUser(String userId,String password,int xmppVersion){
		
				try {
					DBObject q=new BasicDBObject("user_id",userId+"@"+xmppConfig.getHost());
					DBObject obj=dsForTigase.getDB().getCollection("tig_users").findOne(q);
					if((null!=obj))
						return;
					else{
						registerAndXmppVersion(userId, password, xmppVersion);
					}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
		
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
		sysUserList.add("10005");
		sysUserList.add("10006");
		sysUserList.add("10007");
		sysUserList.add("10008");
		sysUserList.add("10009");
		sysUserList.add("10010");
		sysUserList.add("10011");
		sysUserList.add("10012");
		sysUserList.add("10013");
		sysUserList.add("10014");
		sysUserList.add("10015");
	}

	
	

	

	private byte[] generateId(String username) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(username.getBytes());
	}

	public void updateToTig(long userId, String password) {
		try {
			String user_id = userId + "@" + xmppConfig.getHost();

			BasicDBObject q = new BasicDBObject();
			q.put("_id", generateId(user_id));

			// DBCollection dbCollection =
			// dsForTigase.getDB().getCollection("tig_users");
			BasicDBObject o = new BasicDBObject();
			o.put("$set", new BasicDBObject("password",password));
			dsForTigase.getDB().getCollection("tig_users").update(q, o);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private  void closedConnection(XMPPConnection conn){
		try {
			Thread.currentThread().sleep(2000);
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

public static class MyConnectionListener implements ConnectionListener{
		
		private XMPPConnection conn;
		
		public XMPPConnection getConn() {
			return conn;
		}

		public void setConn(XMPPConnection conn) {
			this.conn = conn;
		}
		
		public MyConnectionListener() {
			// TODO Auto-generated constructor stub
		}
		public MyConnectionListener(XMPPConnection conn){
			this.conn=conn;
		}

		@Override
		public void connectionClosed() {
			log.info((null!=conn?conn.getConnectionID():"")+" ====> connectionClosed");
			conn=null;
			
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			log.info((null!=conn?conn.getConnectionID():"")+" ====> connectionClosedOnError");
			
			if(null!=conn)
				conn.disconnect();
			conn=null;
			
		}

		@Override
		public void reconnectingIn(int seconds) {
			// TODO Auto-generated method stub
			log.info((null!=conn?conn.getConnectionID():"")+" ====> reconnectingIn");
			if(null!=conn)
				conn.disconnect();
			conn=null;
		}

		@Override
		public void reconnectionSuccessful() {
			// TODO Auto-generated method stub
			log.info((null!=conn?conn.getConnectionID():"")+" ====> reconnectionSuccessful");
		}

		@Override
		public void reconnectionFailed(Exception e) {
			
			log.info((null!=conn?conn.getConnectionID():"")+" ====> reconnectionFailed");
			if(null!=conn)
				conn.disconnect();
			conn=null;
		}

		
		
	}
public static class MessageBean {
	private Object content;
	private String fileName;
	private String fromUserId = "10005";
	private String fromUserName = "10005";
	private Object objectId;
	private long timeSend = System.currentTimeMillis() / 1000;
	private String toUserId;
	private String toUserName;
	private int fileSize;
	private int type;

	//新增头像
	private String portrait;
	
	
	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public Object getContent() {
		return content;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public Object getObjectId() {
		return objectId;
	}

	public long getTimeSend() {
		return timeSend;
	}

	public String getToUserId() {
		return toUserId;
	}

	public String getToUserName() {
		return toUserName;
	}
	
	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getType() {
		return type;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public void setObjectId(Object objectId) {
		this.objectId = objectId;
	}

	public void setTimeSend(long timeSend) {
		this.timeSend = timeSend;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

}

}
