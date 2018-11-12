package cn.xyz.mianshi.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class InviteVo {

	private List<Integer> addUserIds;//被邀请者id
	private String push;//推送给群主
}
