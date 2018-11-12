package cn.xyz.mianshi.service;

import org.bson.types.ObjectId;

import cn.xyz.commons.vo.PublicNumVo;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.commons.vo.UpublicDto;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.PublicNumDvo;

public interface PublicNumManager {

	PublicNum createPublicNum();

	ResultInfo<Integer> addPublicNum(PublicNum publicNum);

	ResultInfo<UpublicDto> getAttentionList(Integer userId);

	ResultInfo<String> addAttention(Integer userId, Integer publicId);

	ResultInfo<PublicNum> getDetail(Integer publicId);

	ResultInfo<PublicNumDvo> getByName(Integer userId, String nickname);

	ResultInfo<Integer> getServiceId(Integer publicId);

	ResultInfo<PublicNumVo> getPublicNumListForCS(Integer csUserId);

	ResultInfo<String> cancelAttention(Integer userId, Integer publicId);

	ResultInfo<String> removePublicNum(Integer publicId, Integer csUserId);

	ResultInfo<String> deletePublicNum(Integer publicId);

	boolean isAtt(Integer userId,String nickName);
	
	void deleteByObjectId(ObjectId id);
}
