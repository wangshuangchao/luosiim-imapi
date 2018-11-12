package cn.xyz.commons.constants;

/**
* @Description: TODO(消息类型常量)
* @author lidaye
* @date 2018年2月24日 
*/
public interface MsgType {

	 ////////////////////////////以下为在聊天界面显示的类型/////////////////////////////////
    public static final int TYPE_TEXT = 1; // 文字
    public static final int TYPE_IMAGE = 2;// 图片
    public static final int TYPE_VOICE = 3;// 语音
    public static final int TYPE_LOCATION = 4; // 位置
    public static final int TYPE_GIF = 5;  // gif
    public static final int TYPE_VIDEO = 6;// 视频
    public static final int TYPE_SIP_AUDIO = 7;// 音频
    public static final int TYPE_CARD = 8;// 名片
    public static final int TYPE_FILE = 9;// 文件
    public static final int TYPE_TIP = 10;// 自己添加的消息类型,代表系统的提示

    public static final int TYPE_READ = 26;    // 是否已读的回执类型

    public static final int TYPE_RED = 28;     // 红包消息
    public static final int TYPE_IMAGE_TEXT = 80;     // 单条图文消息
    public static final int TYPE_IMAGE_TEXT_MANY = 81;// 多条图文消息
    public static final int TYPE_LINK = 82; // 链接
    public static final int TYPE_83 = 83;   // 某个成员领取了红包
    public static final int TYPE_INPUT = 201; // 正在输入消息
    public static final int TYPE_BACK = 202;  // 撤回消息

    public static final int DIANZAN = 301; // 朋友圈点赞
    public static final int PINGLUN = 302; // 朋友圈评论

    public static final int TYPE_COMMENT = 27; // 通知评论消息

    ////////////////////////////音视频通话/////////////////////////////////
    public static final int TYPE_IS_CONNECT_VOICE = 100;    // 询问是否能连接 音频通话
    public static final int TYPE_CONNECT_VOICE = 102;       // 连接 音频通话
    public static final int TYPE_NO_CONNECT_VOICE = 103;    // 拒绝/取消连接 音频通话
    public static final int TYPE_END_CONNECT_VOICE = 104;   // 接通后结束 音频通话

    public static final int TYPE_IS_CONNECT_VIDEO = 110;    // 询问是否能连接 视频通话
    public static final int TYPE_CONNECT_VIDEO = 112;       // 连接 视频通话
    public static final int TYPE_NO_CONNECT_VIDEO = 113;    // 拒绝/取消连接 视频通话
    public static final int TYPE_END_CONNECT_VIDEO = 114;   // 接通后结束 视频通话
    public static final int TYPE_IS_MU_CONNECT_Video = 115; // 视频会议邀请
    public static final int TYPE_IS_MU_CONNECT_VOICE = 120; // 音频会议邀请
    ////////////////////////////音视频通话/////////////////////////////////
    public static final int TYPE_OK_CONNECT_VIDEO = 111;    // 确认可以连接 视频通话
    public static final int TYPE_OK_CONNECT_VOICE = 101;    // 确认可以连接 音频通话
    public static final int TYPE_OK_MU_CONNECT_VOICE = 121; // 音频会议进入了
    public static final int TYPE_EXIT_VOICE = 122;          // 音频会议退出了
    public static final int TYPE_VIDEO_IN = 116;            // 视频会议进入
    public static final int TYPE_VIDEO_OUT = 117;           // 视频会议退出

    ////////////////////////////群文件/////////////////////////////////
    public static final int TYPE_MUCFILE_ADD = 401; // 群文件上传
    public static final int TYPE_MUCFILE_DEL = 402; // 群文件删除
    public static final int TYPE_MUCFILE_DOWN = 403;// 群文件下载
    ////////////////////////////新朋友消息/////////////////////////////////
    public static final int TYPE_SAYHELLO = 500;// 打招呼
    public static final int TYPE_PASS = 501;    // 同意加好友
    public static final int TYPE_FEEDBACK = 502;// 回话
    public static final int TYPE_NEWSEE = 503;// 新关注
    public static final int TYPE_DELSEE = 504;// 删除关注
    public static final int TYPE_DELALL = 505;// 彻底删除
    public static final int TYPE_RECOMMEND = 506;// 新推荐好友
    public static final int TYPE_BLACK = 507; // 黑名单
    public static final int TYPE_FRIEND = 508;// 直接成为好友
    public static final int TYPE_REFUSED = 509;//取消黑名单

    // 群聊推送
    public static final int TYPE_CHANGE_NICK_NAME = 901;// 修改昵称
    public static final int TYPE_CHANGE_ROOM_NAME = 902;// 修改房间名
    public static final int TYPE_DELETE_ROOM = 903;     // 删除房间
    public static final int TYPE_DELETE_MEMBER = 904;   // 删除成员
    public static final int TYPE_NEW_NOTICE = 905;      // 新公告
    public static final int TYPE_GAG = 906;             // 禁言
    public static final int NEW_MEMBER = 907;           // 增加新成员
    public static final int TYPE_CHANGE_SHOW_READ = 915;    // 设置群已读消息 只有接收

    public static final int TYPE_SEND_DANMU = 910;          // 发送弹幕
    public static final int TYPE_SEND_GIFT = 911;           // 发送礼物
    public static final int TYPE_SEND_HEART = 912;          // 发送爱心
    public static final int TYPE_SEND_MANAGER = 913;        // 设置管理员
    public static final int TYPE_SEND_ENTER_LIVE_ROOM = 914;// 加入直播间
}

