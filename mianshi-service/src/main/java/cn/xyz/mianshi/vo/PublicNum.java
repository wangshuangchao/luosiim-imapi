package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.utils.IndexDirection;

import com.alibaba.fastjson.annotation.JSONField;

import cn.xyz.commons.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
@Entity(value = "publicNum", noClassnameStored = true)
@Indexes({ @Index("publicId,nickname,csUserId") })
public class PublicNum {
	
	private @JSONField(serialize = false) @Id ObjectId id;// 主键Id
	private Integer publicId;// 公众号Id
	@Indexed(value = IndexDirection.ASC)
	private String nickname;// 昵称
	private Long createTime;// 注册时间
	private Integer csUserId;//绑定的客服id
	private String message;//首页发送的欢迎语
	private String messageUrl;//图片链接
	private String introduce;//公众号介绍
	private String indexUrlTital;//主页链接标题
	private String indexUrl;//要跳转的主页链接
	private String portraitUrl;//公众号头像
	private Integer isDel;//是否注销公众号,默认为0未注销,1为已经注销
	private Long updateTime;//最后修改时间
	
	
	//新增
	private int type;//公众号类型1为系统类型,0为普通类型
	private String phone;//公众号客服电话
	public PublicNum() {
		super();
		this.createTime = DateUtil.currentTimeSeconds();
	}
	public PublicNum(Integer publicId, String nickname, Integer userId) {
		super();
		this.publicId = publicId;
		this.nickname = nickname;
		this.csUserId = userId;
		this.createTime = DateUtil.currentTimeSeconds();
	}

	
	

	
}
