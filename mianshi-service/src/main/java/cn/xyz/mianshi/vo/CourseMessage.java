package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value="courseMessage",noClassnameStored=true)
public class CourseMessage {
	private @Id ObjectId courseMessageId;
	private int userId;
	private String courseId;
	private long createTime;
	private String message;
	
	public CourseMessage() {}

	public CourseMessage(ObjectId courseMessageId, int userId, String courseId, long createTime, String message) {
		this.courseMessageId = courseMessageId;
		this.userId = userId;
		this.courseId = courseId;
		this.createTime = createTime;
		this.message = message;
	}

	public ObjectId getCourseMessageId() {
		return courseMessageId;
	}

	public void setCourseMessageId(ObjectId courseMessageId) {
		this.courseMessageId = courseMessageId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	
}
