package cn.xyz.repository;

import java.util.List;

import cn.xyz.mianshi.vo.UPublicNum;

public interface UPublicNumRepository {

	void addUpublicNum(UPublicNum upublicNum);

	List<UPublicNum> getPublicNumByUserId(Integer userId);

	Object addAttention(UPublicNum uPub);

	Object cancelAttention(Integer userId, Integer publicId);

	UPublicNum getUpublicNum(Integer userId, Integer publicId);

	Object removeAtt(Integer publicId);

	boolean isAtt(Integer userId, String nickName);

}
