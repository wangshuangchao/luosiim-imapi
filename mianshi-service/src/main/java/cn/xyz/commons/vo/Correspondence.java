package cn.xyz.commons.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Correspondence {

	private String num;//客户端获取的号码
	private int isRegister;//是否已注册0为未注册,1为已经注册
	private Integer userId;//用户Id
	private String lsId;//罗斯号Id
	private String portrait;//用户头像
	private String nickname;// 昵称
	private String telephone;
	private String phone;
}
