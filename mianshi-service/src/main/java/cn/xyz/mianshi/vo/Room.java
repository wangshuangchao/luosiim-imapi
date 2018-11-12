package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

import com.alibaba.fastjson.annotation.JSONField;

@Entity(value = "shiku_room", noClassnameStored = true)
@Indexes({ @Index("userId"), @Index("jid"), @Index("userId,jid") })
public class Room {

	// 房间编号
	@Id
	private ObjectId id;

	private String jid; //群的id
	// 房间名称
	private String name;
	// 房间描述
	private String desc;
	// 房间主题
	private String subject;
	// 房间分类
	private Integer category;
	//是否需要群主验证才能加入
	private Integer isNeedVerification;
	
	//2018年8月16日13:57:28添加,用于判断是否支持群内加好友
	private int isAllowAddFriend;
	
	// 房间标签
	private List<String> tags;
	//语音通话标识符
	private String call;
	//视频会议标识符
	private String videoMeetingNo;
	
	//群主设置 群内消息是否发送已读 回执 显示数量
	private int showRead;
	
	// 房间公告
	private Notice notice;
	// 公告列表
	private List<Notice> notices;

	// 当前成员数
	private Integer userSize;
	// 最大成员数
	private Integer maxUserSize = 1000;
	// 自己
	private Member member;
	// 成员列表
	private List<Member> members;

	private Integer countryId;// 国家Id
	private Integer provinceId;// 省份Id
	private Integer cityId;// 城市Id
	private Integer areaId;// 地区Id

	private Double longitude;// 经度
	private Double latitude;// 纬度

	// 创建者Id
	private Integer userId;
	// 创建者昵称
	private String nickname;

	// 创建时间
	private Long createTime;
	// 修改人
	private Integer modifier;
	// 修改时间
	private Long modifyTime;

	// 状态
	private Integer s;
	
	private Integer isLook;//是否可见   0为可见   1为不可见

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	
	public Integer getIsNeedVerification() {
		return isNeedVerification;
	}

	public void setIsNeedVerification(Integer isNeedVerification) {
		this.isNeedVerification = isNeedVerification;
	}

	
	

	public int getIsAllowAddFriend() {
		return isAllowAddFriend;
	}

	public void setIsAllowAddFriend(int isAllowAddFriend) {
		this.isAllowAddFriend = isAllowAddFriend;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public String getVideoMeetingNo() {
		return videoMeetingNo;
	}

	public void setVideoMeetingNo(String videoMeetingNo) {
		this.videoMeetingNo = videoMeetingNo;
	}

	public Notice getNotice() {
		return notice;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

	public List<Notice> getNotices() {
		return notices;
	}

	public void setNotices(List<Notice> notices) {
		this.notices = notices;
	}

	public Integer getUserSize() {
		return userSize;
	}

	public void setUserSize(Integer userSize) {
		this.userSize = userSize;
	}

	public Integer getMaxUserSize() {
		return maxUserSize;
	}

	public void setMaxUserSize(Integer maxUserSize) {
		this.maxUserSize = maxUserSize;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}

	public Integer getCountryId() {
		return countryId;
	}

	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public Integer getAreaId() {
		return areaId;
	}

	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Integer getModifier() {
		return modifier;
	}

	public void setModifier(Integer modifier) {
		this.modifier = modifier;
	}

	public Long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Integer getS() {
		return s;
	}

	public void setS(Integer s) {
		this.s = s;
	}

	public int getShowRead() {
		return showRead;
	}

	public void setShowRead(int showRead) {
		this.showRead = showRead;
	}

	public Integer getIsLook() {
		return isLook;
	}

	public void setIsLook(Integer isLook) {
		this.isLook = isLook;
	}

	@Entity(value = "shiku_room_notice")
	@Indexes({ @Index("roomId"), @Index("userId") })
	public static class Notice {
		@Id
		@JSONField(serialize = false)
		private ObjectId id;// 通知Id
		@JSONField(serialize = false)
		private ObjectId roomId;// 房间Id
		private String text;// 通知文本
		private String userId;// 用户Id
		private String nickname;// 用户昵称
		private String time;// 时间

		public ObjectId getId() {
			return id;
		}

		public void setId(ObjectId id) {
			this.id = id;
		}

		public ObjectId getRoomId() {
			return roomId;
		}

		public void setRoomId(ObjectId roomId) {
			this.roomId = roomId;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

	}

	@Entity(value = "shiku_room_member")
	@Indexes({ @Index("roomId"), @Index("userId"), @Index("roomId,userId"), @Index("userId,role") })
	public static class Member {
		@Id
		@JSONField(serialize = false)
		private ObjectId id;

		// 房间Id
		@JSONField(serialize = false)
		private ObjectId roomId;

		// 成员Id
		private Integer userId;

		// 成员昵称
		private String nickname;
		//新加用户头像
		private String portrait;
		
		// 成员角色：1=创建者、2=管理员、3=成员
		private Integer role;

		// 订阅群信息：0=否、1=是
		private Integer sub;
		
		//语音通话标识符
		private String call;
		
		//视频会议标识符
		private String videoMeetingNo;
		
		//消息免打扰（1=是；0=否）
		private Integer offlineNoPushMsg=0;
		
		// 大于当前时间时禁止发言
		private Long talkTime;

		// 最后一次互动时间
		private Long active;

		// 创建时间
		private Long createTime;

		// 修改时间
		private Long modifyTime;

		public ObjectId getId() {
			return id;
		}
		
		public String getCall() {
			return call;
		}

		public void setCall(String call) {
			this.call = call;
		}
		
		public String getVideoMeetingNo() {
			return videoMeetingNo;
		}

		public void setVideoMeetingNo(String videoMeetingNo) {
			this.videoMeetingNo = videoMeetingNo;
		}

		public void setId(ObjectId id) {
			this.id = id;
		}

		public ObjectId getRoomId() {
			return roomId;
		}

		public void setRoomId(ObjectId roomId) {
			this.roomId = roomId;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		
		public String getPortrait() {
			return portrait;
		}

		public void setPortrait(String portrait) {
			this.portrait = portrait;
		}

		public Integer getRole() {
			return role;
		}

		public void setRole(Integer role) {
			this.role = role;
		}

		public Integer getOfflineNoPushMsg() {
			return offlineNoPushMsg;
		}

		public void setOfflineNoPushMsg(Integer offlineNoPushMsg) {
			this.offlineNoPushMsg = offlineNoPushMsg;
		}

		public Integer getSub() {
			return sub;
		}

		public void setSub(Integer sub) {
			this.sub = sub;
		}

		public Long getTalkTime() {
			return talkTime;
		}

		public void setTalkTime(Long talkTime) {
			this.talkTime = talkTime;
		}

		public Long getActive() {
			return active;
		}

		public void setActive(Long active) {
			this.active = active;
		}

		public Long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Long createTime) {
			this.createTime = createTime;
		}

		public Long getModifyTime() {
			return modifyTime;
		}

		public void setModifyTime(Long modifyTime) {
			this.modifyTime = modifyTime;
		}

	}
	
	@Entity(value="shiku_room_share",noClassnameStored=true)
	public static class Share {
		
		
		private @Id ObjectId shareId;//id
		private @Indexed ObjectId roomId;
		private String name;//文件名称
		private String url;//文件路径
		private long time;//发送时间
		private @Indexed Integer userId;//发消息的用户id
		private String nickname;//昵称
		private int type;//文件类型()
		private float size;//文件大小
		
		public Share() {}
		
		public Share(ObjectId shareId, ObjectId roomId, String name, String url, long time, Integer userId,
				String nickname, int type, float size) {
			this.shareId = shareId;
			this.roomId = roomId;
			this.name = name;
			this.url = url;
			this.time = time;
			this.userId = userId;
			this.nickname = nickname;
			this.type = type;
			this.size = size;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public ObjectId getShareId() {
			return shareId;
		}

		public void setShareId(ObjectId shareId) {
			this.shareId = shareId;
		}

		public ObjectId getRoomId() {
			return roomId;
		}

		public void setRoomId(ObjectId roomId) {
			this.roomId = roomId;
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

		public Integer getUserId() {
			return userId;
		}


		public void setUserId(Integer userId) {
			this.userId = userId;
		}


		public String getNickname() {
			return nickname;
		}


		public void setNickname(String nickname) {
			this.nickname = nickname;
		}


		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public float getSize() {
			return size;
		}

		public void setSize(float size) {
			this.size = size;
		}
		
	}

	@Override
	public String toString() {
		return "Room [id=" + id + ", jid=" + jid + ", name=" + name + ", desc=" + desc + ", subject=" + subject
				+ ", category=" + category + ", isNeedVerification=" + isNeedVerification + ", isAllowAddFriend="
				+ isAllowAddFriend + ", tags=" + tags + ", call=" + call + ", videoMeetingNo=" + videoMeetingNo
				+ ", showRead=" + showRead + ", notice=" + notice + ", notices=" + notices + ", userSize=" + userSize
				+ ", maxUserSize=" + maxUserSize + ", member=" + member + ", members=" + members + ", countryId="
				+ countryId + ", provinceId=" + provinceId + ", cityId=" + cityId + ", areaId=" + areaId
				+ ", longitude=" + longitude + ", latitude=" + latitude + ", userId=" + userId + ", nickname="
				+ nickname + ", createTime=" + createTime + ", modifier=" + modifier + ", modifyTime=" + modifyTime
				+ ", s=" + s + ", isLook=" + isLook + "]";
	}


	
}
