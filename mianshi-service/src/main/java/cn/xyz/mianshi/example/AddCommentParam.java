package cn.xyz.mianshi.example;

public class AddCommentParam {
	private String messageId;
	private int toUserId;
	private String toNickname;
	private String toBody;
	private String body;

	//新增头像
	private String portrait;//评论者头像
	
	
	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public int getToUserId() {
		return toUserId;
	}

	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
	}

	public String getToNickname() {
		return toNickname;
	}

	public void setToNickname(String toNickname) {
		this.toNickname = toNickname;
	}

	public String getToBody() {
		return toBody;
	}

	public void setToBody(String toBody) {
		this.toBody = toBody;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
