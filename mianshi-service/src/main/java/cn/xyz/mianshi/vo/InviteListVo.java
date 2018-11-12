package cn.xyz.mianshi.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class InviteListVo {

	private Integer InviteId;//邀请者ID
	private String InviteName;//邀请者昵称
	private List<UserVo> userVo;//被邀请者的id和昵称集合
	private String push;//推送信息
	private String msg;//验证消息
}
