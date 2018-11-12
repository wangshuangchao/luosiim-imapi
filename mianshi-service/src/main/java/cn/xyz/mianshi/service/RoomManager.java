package cn.xyz.mianshi.service;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.Room.Share;
import cn.xyz.mianshi.vo.User;

public interface RoomManager {
	public static final String BEAN_ID = "RoomManagerImpl";

	Room add(User user, Room room, List<Integer> idList);

	void delete(User user, ObjectId roomId);
	void delete( ObjectId roomId);

	boolean update(User user, ObjectId roomId, String roomName, String notice, String desc,int showRead);

	Room get(ObjectId roomId);
	 
	Room exisname(Object roomname);

	List<Room> selectList(int pageIndex, int pageSize, String roomName);

	Object selectHistoryList(int userId, int type);

	Object selectHistoryList(int userId, int type, int pageIndex, int pageSize);

	void deleteMember(User user, ObjectId roomId, int userId);

	void updateMember(User user, ObjectId roomId, Room.Member member);

	void updateMember(User user, ObjectId roomId, List<Integer> idList);
	
	void Memberset(Integer offlineNoPushMsg,ObjectId roomId,int userId);

	Member getMember(ObjectId roomId, int userId);

	List<Room.Member> getMemberList(ObjectId roomId,String keyword);

	void join(int userId, ObjectId roomId, int type);

	void leave(int userId, ObjectId roomId);
	
	void setAdmin(ObjectId roomId,int touserId,int type,int userId);

	Share Addshare(ObjectId roomId,long size,int type ,int userId, String url,String name);
	
	List<Room.Share> findShare(ObjectId roomId,long time,int userId,int pageIndex,int pageSize);
	
	Object getShare(ObjectId roomId,ObjectId shareId);
	
	void deleteShare(ObjectId roomId,ObjectId shareId,int userId);
	
	String getCall(ObjectId roomId);
	
	String getVideoMeetingNo(ObjectId roomId);

	void transfer(ObjectId roomId, int userId, int toUserId);

	void askOwner(User user, ObjectId roomId, List<Integer> idList, String msg);

	void turnIsNeedVerification(ObjectId roomId, int isNeedVerification);

	void addMember(User user, ObjectId roomId, Integer addUserId, Integer inviter);

	void addMember(User user, ObjectId roomId, List<Integer> idList, Integer inviter);

	void turnIsAllowAddFriend(ObjectId roomId, Integer isAllowAddFriend);
}
