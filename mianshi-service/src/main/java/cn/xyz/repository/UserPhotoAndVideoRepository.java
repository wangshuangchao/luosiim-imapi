package cn.xyz.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import cn.xyz.mianshi.vo.UserPhotoAndVideo;

/**
 * 
* <p>Title: MsgRepositoryImpl</p>  
* <p>Description: </p>  
* @author xiaobai  
* @date 2018年8月1日
 */
@Service
public interface UserPhotoAndVideoRepository  {
	public List<String> getUserPohots(Integer userId);
	public void addUserPhotos(UserPhotoAndVideo upav);
}