package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 

* <p>Title: Complint</p>  
* <p>Description:投诉建议</p>  
* @author xiaobai  
* @date 2018年5月15日
 */
@Getter
@Setter
@ToString
@Entity(value = "complint", noClassnameStored = true)
public class Complint {

	private @JSONField(serialize = false) @Id ObjectId id;
	private Integer userId;//留言用户id
	private String nickname;//用户昵称
	private String lsId;//留言用户罗斯号
	private String title;//留言标题
	private String content;//留言内容
	private long createTime;//留言时间
	private Integer isHandle;//是否已处理
}
