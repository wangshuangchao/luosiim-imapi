package cn.xyz.mianshi.service;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.example.UserExample;
import cn.xyz.mianshi.example.UserQueryExample;
import cn.xyz.mianshi.vo.Course;
import cn.xyz.mianshi.vo.CourseMessage;
import cn.xyz.mianshi.vo.Emoji;
import cn.xyz.mianshi.vo.InviteListVo;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.WxUser;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;

public interface UserManager {

	User createUser(String telephone, String password);

	void createUser(User user);

	User.UserSettings getSettings(int userId);

	User getUser(int userId);
	
	/*User getfUser(int userId);*/

	User getUser(int userId, int toUserId);

	User getUser(String telephone);
	
	String getNickName(int userId);
	
	String getPortrait(int userId);

	int getUserId(String accessToken);

	boolean isRegister(String telephone);

	User login(String telephone, String password);
	

	Map<String, Object> login(UserExample example);
	Map<String, Object> loginv1(UserExample example);
	

	Map<String, Object> loginAuto(String access_token, int userId, String serial,String appId,double latitude,double longitude);

	void logout(String access_token,String areaCode,String userKey);
	
	void outtime(String access_token,int userId);

	List<DBObject> query(UserQueryExample param);

	Map<String, Object> register(UserExample example);

	Map<String, Object> registerIMUser(UserExample example);
	
	void addUser(int userId,String password);

	void resetPassword(String telephone, String password);

	void updatePassword(int userId, String oldPassword, String newPassword);

	User updateSettings(int userId,User.UserSettings userSettings);

	User updateUser(int userId, UserExample example);

	List<DBObject> findUser(int pageIndex, int pageSize);

	List<Integer> getAllUserId();
	//消息免打扰
	User updataOfflineNoPushMsg(int userId,int OfflineNoPushMsg);
	//添加收藏
	Object addEmoji(int userId,String url,String roomJid,String msgId,int type);
	//收藏列表
	List<Emoji> emojiList(int userId,int type,int pageSize,int pageIndex);
	
	List<Emoji> emojiList(int userId);
	//取消收藏
	void deleteEmoji(ObjectId emojiId);
	//添加消息课程
	void addMessageCourse(int userId,List<String> messageIds,long createTime,String courseName,String roomJid);
	//通过userId获取用户课程
	List<Course> getCourseList(int userId);
	//修改课程
	void updateCourse(Course course,String courseMessageId);
	//删除课程
	void deleteCourse(ObjectId courseId);
	//发送课程
	List<CourseMessage> getCourse(String courseId);
	
	//void updateContent(ObjectId courseMessageId);
	//添加微信公众号用户
	WxUser addwxUser(JSONObject jsonObject);

	ResultInfo<InviteListVo> getUserInfo(Integer userId, List<Integer> idList);
	
	ResultInfo<String> changePhone(Integer userId, String phone, String areaCode);
	
	ResultInfo<String> rename(Integer userId, String lsId);
}
