package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
@Entity(value = "s_pv", noClassnameStored = true)
@Indexes({ @Index("uesrId,msgId") })
public class UserPhotoAndVideo {

	@Id
	private ObjectId id;
	private String url;//图片集合
	private long time;//当前时间
	private Integer uesrId;//用户id
	private ObjectId msgId;//消息id
	public UserPhotoAndVideo() {
		super();
	}
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public Integer getUesrId() {
		return uesrId;
	}
	public void setUesrId(Integer uesrId) {
		this.uesrId = uesrId;
	}
	public ObjectId getMsgId() {
		return msgId;
	}
	public void setMsgId(ObjectId msgId) {
		this.msgId = msgId;
	}
	@Override
	public String toString() {
		return "UserPhotoAndVideo [url=" + url + ", time=" + time + ", uesrId=" + uesrId + ", msgId=" + msgId + "]";
	}
	
}
