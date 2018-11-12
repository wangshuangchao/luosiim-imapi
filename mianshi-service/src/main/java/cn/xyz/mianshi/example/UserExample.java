package cn.xyz.mianshi.example;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;

public class UserExample extends BaseExample {

	private Long birthday;
	//private String companyId;
	private String description;
	private String idcard;
	private String idcardUrl;
	private String name;
	private @NotNull String nickname;
	private @NotNull String password;
	private Integer sex;
	//新加用户头像
	private String portrait;
	
	private @NotNull String telephone;
	private String areaCode="86";
	private String randcode;
	private String phone;
	private Integer userType;
	private String appId;
	private int xmppVersion;//xmpp 心跳包的时候用到
	private Integer d = 0;
	private Integer w = 0;
	private String email;

	public Long getBirthday() {
		return birthday;
	}

	public void setBirthday(Long birthday) {
		this.birthday = birthday;
	}

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getIdcardUrl() {
		return idcardUrl;
	}

	public void setIdcardUrl(String idcardUrl) {
		this.idcardUrl = idcardUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public Integer getD() {
		return d;
	}

	public void setD(Integer d) {
		this.d = d;
	}

	public Integer getW() {
		return w;
	}

	public void setW(Integer w) {
		this.w = w;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRandcode() {
		return randcode;
	}

	public void setRandcode(String randcode) {
		this.randcode = randcode;
	}

	public int getXmppVersion() {
		return xmppVersion;
	}

	public void setXmppVersion(int xmppVersion) {
		this.xmppVersion = xmppVersion;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	@Override
	public String toString() {
		return "UserExample [birthday=" + birthday + ", description=" + description + ", idcard=" + idcard
				+ ", idcardUrl=" + idcardUrl + ", name=" + name + ", nickname=" + nickname + ", password=" + password
				+ ", sex=" + sex + ", portrait=" + portrait + ", telephone=" + telephone + ", areaCode=" + areaCode
				+ ", randcode=" + randcode + ", phone=" + phone + ", userType=" + userType + ", appId=" + appId
				+ ", xmppVersion=" + xmppVersion + ", d=" + d + ", w=" + w + ", email=" + email + "]";
	}
	
}
