package cn.xyz.commons.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PublicNumDto {

	private Integer publicId;// 公众号Id
	private String nickname;// 昵称
	private String portraitUrl;//公众号头像
}
