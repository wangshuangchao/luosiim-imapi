package cn.xyz.mianshi.vo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 通知
 * 
 * @author Administrator
 *
 */
//@Entity(value = "Notice", noClassnameStored = true)
public class MsgNotice {

	private int from;
	private int to;
	private String roomJid;//群组的jid
	private String name;// 发起通知用户
	private String toName;
	private String groupName;
	private String title;//通知标题
	private int type;//消息类型
	private String text;//显示的通知内容
	private String fileName;//附加内容
	private String objectId;
	
	private int status;// 状态：0=未读；1=已读
	private long time;// 通知时间
	
	private int msgNum;
	
	private int isGroup=0;//是否群组消息
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public String getToName() {
		return toName;
	}
	public void setToName(String toName) {
		this.toName = toName;
	}
	public int getMsgNum() {
		return msgNum;
	}
	public void setMsgNum(int msgNum) {
		this.msgNum = msgNum;
	}
	public String getRoomJid() {
		return roomJid;
	}
	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getIsGroup() {
		return isGroup;
	}
	public void setIsGroup(int isGroup) {
		this.isGroup = isGroup;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return JSON.toJSONString(this);
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	

	

}
