package cn.xyz.repository;


import java.util.List;

import org.mongodb.morphia.query.UpdateResults;

import cn.xyz.mianshi.vo.PublicNum;

public interface PublicNumRepository {

	Integer addPublicNum(PublicNum publicNum);

	PublicNum getPublicNum(Integer publicId);

	List<Integer> getServiceIds(Integer publicId);

	List<PublicNum> getPublcNumListForCS(Integer csUserId);

	PublicNum removePublicNum(Integer publicId, Integer csUserId);

	UpdateResults deletePublicNum(Integer publicId);

}
