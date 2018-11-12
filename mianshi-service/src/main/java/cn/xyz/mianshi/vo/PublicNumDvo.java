package cn.xyz.mianshi.vo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PublicNumDvo {

	private Integer publicId;// 公众号Id
	private String nickname;// 昵称
	private String message;//首页发送的欢迎语
	private String messageUrl;//图片链接
	private String introduce;//公众号介绍
	private String indexUrlTital;//主页链接标题
	private String indexUrl;//要跳转的主页链接
	private String portraitUrl;//公众号头像
	private int type;//公众号类型
	private String phone;//公众号客服电话
	private Integer isAtt;//是否已经关注
}
