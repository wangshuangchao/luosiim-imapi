
var XmppMessage={
  	Type:{
  		/**
     * 消息类型：系统广播消息
     */
     _800:800,// 系统广播
     _801:801,// 活动报名
     _802:802,// 奖励促销
    /**
     * 消息类型：群聊提示消息
     */
     _900:900,// 已进群
     _901:901,// 已退群
   
    /**
     * 消息类型：商务圈消息
     */
     NEW_COMMENT:600,// 新评论
     _601:601,// 新礼物
     _602:602,// 新赞
     _603:603,// 新公共消息


    /**
     * 消息类型：音视频通话 会议消息
     */
    //单聊 语音
    VOICE_ASK:100,//询问能否接听语音通话 
    VOICE_ANSWER:101,//确定可以接听语音通话
    VOICE_CONNECT:102,//接听语音通话  无用
    VOICE_CANCEL:103,//拒绝取消语音拨号 
    VOICE_STOP:104,//接通后结束语音通话
    //单聊视频
    VIDEO_ASK:110,//询问能否接听通话 
    VIDEO_ANSWER:111,//确定可以接听通话
    VIDEO_CONNECT:112,//接听通话  无用
    VIDEO_CANCEL:113,//拒绝取消拨号 
    VIDEO_STOP:114,//接通后结束通话
    //视频会议
    Conference_VIDEO_INVITE:115,//邀请进行视频会议
    Conference_VIDEO_JOIN:116,//加入视频会议
    Conference_VIDEO_EXIT:117,//退出视频会议
    Conference_VIDEO_OUT:118,//踢出视频会议
     //语音会议
    Conference_VOICE_INVITE:120,//邀请进行视频会议
    Conference_VOICE_JOIN:121,//加入视频会议
    Conference_VOICE_EXIT:122,//退出视频会议
    Conference_VOICE_OUT:123,//踢出视频会议
    



    /**
     * 消息类型：新朋友消息
     */
     SAYHELLO:500,// 打招呼
     PASS:501,// 同意加好友
     FEEDBACK:502,// 回话
     NEWSEE:503,// 新关注
     DELSEE:504,// 删除关注
     DELALL:505,// 彻底删除
     RECOMMEND:506,// 新推荐好友
     BLACK:507,// 黑名单
     FRIEND:508,// 直接成为好友
     REFUSED:509,//拒绝成为好友

     READ:26, // 是否已读的回执类型
     COMMENT:27, // 通知评论消息
     RED:28, // 红包消息

    // //////////////////////////////以上均为广播消息的类型///////////////////////////////////

    // ////////////////////////////以下为在聊天界面显示的类型/////////////////////////////////
     TEXT:1,// 文字
     IMAGE:2,// 图片
     VOICE:3,// 语音
     LOCATION:4,// 位置
     GIF:5,// gif
     VIDEO:6,// 视频
     SIP_AUDIO:7,// 音频
     CARD:8,// 名片
     FILE:9,//文件
     TIP:10,// 自己添加的消息类型,代表系统的提示

     INPUT:201, // 正在输入消息
     IMAGE_TEXT:80, // 单条
     IMAGE_TEXT_MANY:81, //多条

     //PINGLUN:42, // 正在输入消息
   
    // 群聊推送
     CHANGE_NICK_NAME:901,// 修改昵称
     CHANGE_ROOM_NAME:902,// 修改房间名
     DELETE_ROOM:903,// 删除房间
     DELETE_MEMBER:904,// 删除成员
     NEW_NOTICE:905,// 新公告
     GAG:906,// 禁言
     NEW_MEMBER:907// 增加新成员
  	},

    packetId:null,// 消息包的Id
	type:null,// 消息的类型
	timeSend:null,// 发送时间,秒级别的,为点击发送按钮，开始发送的时间
   	isMySend:true// 是否是由我自己发送，代替toUserId，toUserId废弃不用,默认值true，代表是我发送的
}