package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Entity(value = "emoji", noClassnameStored = true)
@Indexes({ @Index("userId"), @Index("emojiId") })
public class Emoji{
	@Id
	private ObjectId emojiId;//收藏id
	private Integer userId;//用户id
	private Integer type;//收藏类型    1.图片   2.视频    3.文件  4.语音  5.文本   6.表情
	private String url;
	private String msgId;
	private String msg;
	private String roomJid;//房间JId
	private long createTime;//收藏时间
	
	public Emoji() {}
	
	public Emoji(ObjectId emojiId, Integer userId, int type, String url, String msgId, String msg, String roomJid,
			long createTime) {
		this.emojiId = emojiId;
		this.userId = userId;
		this.type = type;
		this.url = url;
		this.msgId = msgId;
		this.msg = msg;
		this.roomJid = roomJid;
		this.createTime = createTime;
	}

	public ObjectId getEmojiId() {
		return emojiId;
	}

	public void setEmojiId(ObjectId emojiId) {
		this.emojiId = emojiId;
	}

	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getRoomJid() {
		return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}

	@Override
	public String toString() {
		return "Emoji [emojiId=" + emojiId + ", userId=" + userId + ", type=" + type + ", url=" + url + ", msgId="
				+ msgId + ", msg=" + msg + ", roomJid=" + roomJid + ", createTime=" + createTime + "]";
	}

	
	
	
}
