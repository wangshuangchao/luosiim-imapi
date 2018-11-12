package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import com.alibaba.fastjson.annotation.JSONField;

import cn.xyz.commons.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
@Entity(value = "u_publicNum", noClassnameStored = true)
@Indexes(@Index("userId,publicId"))
public class UPublicNum {

	private @JSONField(serialize = false) @Id ObjectId id;//主键Id
	private long time;// 关注时间
	private String publicName;// 公众号名称
	private String portraitUrl;//公众号头像
	private Integer publicId;// 公众号Id
	private Integer userId;// 用户Id
	private Integer isAtt = 0;//是否关注
	private int type;//公众号类型
	public UPublicNum() {
		super();
	}

	public UPublicNum(int userId, int publicId) {
		super();
		this.userId = userId;
		this.publicId = publicId;
		this.time = DateUtil.currentTimeSeconds();
	}

	
}
