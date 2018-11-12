var msgHistory = {};  //储存用于获取聊天历史记录的数据
var messageNumber = 0;  //记录用户接收到的(未读)消息数量
var friendRelation = {}; //记录好友关系  key：userId  value： true/false  true:是好友 false:不是好友
var ConversationManager = {
	isOpen : true,// 聊天窗口是否打开
	from : "1ebab36134ff4e5e893387b4bb1dce7a@muc.im.shiku.co",// 目标用户
	fromUserId:"1ebab36134ff4e5e893387b4bb1dce7a",
	msgsList : {},
	roomData : null,
	user:null,
	friend:null,
	isGroup:1,//1是群聊 0是单聊
	/**
	 * 打开会话
	 */
	open : function(from, name) {
		//判断目标会话界面是否已经打开
		if (ConversationManager.isOpen && this.from==from && this.fromUserId==myFn.getUserIdFromJid(from)){
			ownAlert(3,"目标会话界面已经打开，请勿重复操作");
			return;
		}

		$("#userModal").modal('hide');
		this.isOpen = true;
		this.from = from;
		this.fromUserId=myFn.getUserIdFromJid(from);
		var type = from.indexOf(AppConfig.mucJID) == -1 ? 1 : 0;
		//type 1 单聊  0 群组
		if(0==type)
			this.isGroup=1;	

		//将获取消息历史记录的数据进行临时存储
		msgHistory["type"] = type;
		msgHistory["from"] = from;
		msgHistory["index"] = 0; //将聊天记录的页码数进行初始化
		var chatType=1==type?"chat":"groupchat";
		//判断是否存在本地未读消息
		if(!myFn.isNil(DataMap.unReadMsg[ConversationManager.fromUserId]) ){ 
			var unReadMsg = DataMap.unReadMsg[ConversationManager.fromUserId];

			UI.showDetails(from,type,name); //显示详情数据
			if (1 == type){
				$("#Snapchat").show();
			} else {
				GroupManager._XEP_0045_037(ConversationManager.fromUserId,myData.userId);
				$("#Snapchat").hide();
			}
			for (var i = 0; i < unReadMsg.length; i++) {
				var msg = unReadMsg[i];
				var itemHtml = UI.createItem(msg, ConversationManager.fromUserId, 0);
				if(myFn.isNil(itemHtml))
					return "";
				$("#messageContainer").append(itemHtml);
				if(msg.chatType == "chat"){//单聊
					ConversationManager.sendReadReceipt(ConversationManager.from, myData.jid, msg.id);  //发送已读回执
				}else if("groupchat"==msg.chatType){ //群聊
					//发送已读回执到群内
					if(myData.isShowGroupMsgReadNum || !myFn.isNil(msg.objectId)){ //判断是否开启了显示群组消息已读人数  若为@消息强制发送
						GroupManager.sendRead(msg.id); //调用方法发送已读回执
					}
				}
			}
			

			//存储消息记录的结束时间
			DataMap.msgEndTime[ConversationManager.fromUserId] = unReadMsg[0].timeSend;

			//清除未读消息数量提示
			UI.clearMsgNum(ConversationManager.fromUserId);
			mySdk.showLoadHistoryIcon(1); //加载消息历史结束后，显示消息历史的相关Icon	
			return;
		}
		//检查用户是否被踢出该群，若被踢出则将详情界面隐藏
		if (!myFn.isNil(DataMap.deleteRooms[ConversationManager.fromUserId])) { 
			$("#tab #details").hide();
			changeTab(0,"msgTab"); //默认选中消息面板
		}else{
			$("#tab #details").show();
			changeTab(0,"msgTab");
		}

		ConversationManager.showHistory(false, type, from, 0, function(status, result) {
			if (0 == status) {
				UI.showDetails(from,type,name); //显示顶部数据
				if (1 == type){
					$("#Snapchat").show();
				} else {
					GroupManager._XEP_0045_037(ConversationManager.fromUserId,myData.userId);
					$("#Snapchat").hide();
					
				}

				var length = result.length - 1;
				for (var i = length; i >= 0; i--) {
					var o = result[i];
					var msg=eval("(" + o.body.replace(/&quot;/gm, '"') + ")");
					if(msg.type>100)
						return;
					msg.chatType=chatType;
					msg.id=o.messageId;

				ConversationManager.decryptMsg(msg,function(cbMsg){
						//消息显示到Html中
						ConversationManager.showHistoryToHtml(type,o,cbMsg);
				});
				
				}
				//清除未读消息数量提示
				UI.clearMsgNum(ConversationManager.fromUserId);
				mySdk.showLoadHistoryIcon(1); //加载消息历史结束后，显示消息历史的相关Icon	

			} else{
				ownAlert(2,result);
			}

			setTimeout(function(){ //将滚动条移动到最下方
				$(".nano").nanoScroller();//刷新滚动条
				UI.scrollToEnd(); //滚动到底部
			},400);
		});
	},
	showAvatar : function(userId){ //显示聊天窗口顶部头像和昵称
		$("#chatAvator").empty();
		$("#desphoto").empty();
		$("#gphoto").empty();
		var imgUrl=10000!=userId?myFn.getAvatarUrl(userId):"img/im_10000.png";
		var avatarHtml ="<div class='imgAvatar'>"
			           +	"<figure style='height:40px;width:40px;'>"
		               +	  "<img onerror='this.src=\"img/ic_avatar.png\"' src='" + imgUrl+ "' class='chat_content_avatar'>"
		               +	"</figure>"
		               +"</div>";
		$("#chatAvator").append(avatarHtml);
		$("#desphoto").append(avatarHtml);
		$("#gphoto").append(avatarHtml);
		$("#"+userId+"").hide();
	},
	showHistory : function(isLocal, type, id, pageIndex, cb,endTime) {
		console.log("历史记录当前页码数:"+pageIndex);
		if (isLocal) {
			var msgs = this.msgsList[from];
			var length = msgs.length - 1;
			for (var i = length; i == 0; i--) {
				var msg = msgs[i];
			}
		} else {
			var eTime = endTime*1000;
			if(myFn.isNil(endTime)){
				eTime = 0;
			}
			var url = 1 == type ? '/tigase/shiku_msgs' : '/tigase/shiku_muc_msgs';
			var params = {
				pageIndex : pageIndex,
				pageSize : 10,
				endTime : eTime
			};
			params[1 == type ? "receiver" : "roomId"] = myFn.getUserIdFromJid(id);
			myFn.invoke({
				url : url,
				data : params,
				success : function(result) {
					if (1 == result.resultCode) {
						cb(0, result.data);
						var msg=null;
						for (var i = 0; i < result.data.length; i++) {
							msg=eval("(" + result.data[i].body.replace(/&quot;/gm, '"') + ")");
							DataMap.msgMap[msg.messageId]=msg;
						}
						msgHistory["index"] = msgHistory["index"] + 1; //页码数加1
					} else {
						cb(1, result.resultMsg);
					}
				},
				error : function(result) {
					cb(1, null);
				}
			})
		}
	},
	showHistoryToHtml:function(type,o,msg){ //显示历史消息记录

		var itemHtml = "";
			if (1 == type) {
				if (o.direction == 0) {
					msg.fromUserName=myData.nickname;
					itemHtml += UI.createItem(msg, o.sender, 1);
				} else {
					msg.fromUserName=DataMap.friends[o.receiver].toNickname;
					itemHtml += UI.createItem(msg, o.receiver, 0);
				}
			} else {
				if (myData.userId == o.sender) {
					//群组
					//发送者是自己
					msg.fromUserName=GroupManager.roomCard;
					itemHtml += UI.createItem(msg, o.sender, 1);
				} else {
					itemHtml += UI.createItem(msg, o.sender, 0);
				}
			}
			if(myFn.isNil(itemHtml))
				return "";
			$("#messageContainer").append(itemHtml);
			
			//检查记录中是否存在未读消息
			/*if(1!=ConversationManager.isGroup&&(true!=msg.isRead ||1!=msg.isRead)){
				ConversationManager.sendReadReceipt(ConversationManager.from, myData.jid, o.messageId); //发送已读回执
				
			}*/
			setTimeout(function(){
				UI.scrollToEnd();
			},500);
		
	},
	//消息存储
	storeMsg : function(from, to, msg) {
		var msgs = ConversationManager.msgsList[from];
		if (undefined == msgs || null == msgs) {
			msgs = new Array();
			this.msgsList[from] = msgs;
		}
		msgs.push(msg);
	},
	//消息加密
	encrypt:function(msg,cb) {
		var url ='/tigase/encrypt';
		var key="12345678";
		var params = {
				text : msg.content,
				key : key
			};
		myFn.invoke({
				url : url,
				data : params,
				success : function(result) {
					if (1 == result.resultCode) {
						cb(msg.type,result.data.text);	
					}
				},
				error : function(result) {
					cb(1, null);
				}
			});
	},
	//消息解密
	decrypt:function(msg,cb) {
			var url ='/tigase/decrypt';
			var key="12345678";
			var params = {
					text : msg.content,
					key : key
				};
		myFn.invoke({
				url : url,
				data : params,
				success : function(result) {
					if (1 == result.resultCode) {
						cb(msg.type,result.data.text);	
					}
				},
				error : function(result) {
				}
			});
		
	},
	decryptMsg:function(msg,cb) {
		//检查消息是否加密 并解密
		if(msg.type==26){
			return cb(msg);
		}
		if(myFn.isEncrypt(msg)){
			ConversationManager.decrypt(msg,function(type,text){
				msg.type=type;
				msg.content=text;
				cb(msg);
			});
			
		}else{
			cb(msg);
		}
		
	},
	//收到消息
	receiver : function(elem) {
		//收到的消息可能是多条的
		var msgArr=ConversationManager.getMessages(elem);
		if(null==msgArr)
			return;	
		var message=null;
		for (var i = 0; i < msgArr.length; i++) {
			 message=ConversationManager.checkMessage(msgArr[i]);
			 if(null==message)
			 	continue;
			 //处理单条消息
			 ConversationManager.processMsg(message);
		}
	},
	//获取 xml message
	getMessages:function(elem){
		var msgArr=null;
		//检查是否被挤下线
		if(ConversationManager.checkConflict(elem))
			return null;
		if (elem.childNodes.length == 0)
			return msgArr;
		/*for (i = 0; i < elem.childNodes.length; i++) {
                child = elem.childNodes[i];
               
        }*/
		/*if (elem.firstChild.nodeName != "message") 
			return msgArr;*/
		msgArr=new Array();
		var child=null;
		for (i = 0; i < elem.childNodes.length; i++) {
                child = elem.childNodes[i];
               	msgArr.push(child);
        }
		
		shikuLog("收到 message "+Strophe.serialize(elem).replace(/&quot;/gm, '"'));
		return msgArr;
	},
	//处理收到的单条消息
	processMsg:function(message){
		if(myFn.isNil(message))
				return;
			var from = message.getAttribute('from');
			var fromUserId = myFn.getUserIdFromJid(from);
			//判断消息是否来自于黑名单用户，是则不接收
			if(!myFn.isNil(DataMap.blackListUIds[fromUserId])){
				return;
			}
			var type = message.getAttribute('type');
			var bodyElem = message.getElementsByTagName('body')[0];
				//shikuLog("body "+Strophe.serialize(bodyElem).replace(/&quot;/gm, '"'));
			// 非单聊或群聊消息或消息内容为空
			 if ((type != "chat" && type != "groupchat") || bodyElem == undefined || bodyElem.length <= 0) {
				//shikuLog("跳过： type "+type+"  "+ bodyElem);
				return;
			}
			
			var bodyText = Strophe.getText(bodyElem);
			if ("{" != bodyText.charAt(0) || "}" != bodyText.charAt(bodyText.length - 1)) {
				//shikuLog("跳过：" + bodyText);
				return;
			}
			
			var msg = eval("(" + bodyText.replace(/&quot;/gm, '"') + ")");
			var contextType=msg.type;
			
			
			var id = message.getAttribute('id');
			var to = message.getAttribute('to');
			
			var toUserId = myFn.getUserIdFromJid(to);
			msg.id=id;
			msg.chatType=type;
			//消息的发送者userID  群组的Jid
			msg.fromId=fromUserId;
			DataMap.msgMap[msg.id]=msg;
			if(type == "chat"){//单聊 
				// 收到消息立即发送回执给发送者
				if(msg.fromUserId!=myData.userId){
				  var delay=message.getAttribute("delay");//有这个字段就代表是离线消息
				  if(myFn.isNil(delay)) //离线消息 不发送达回执
				    ConversationManager.receipt(from, to, id);
				}

			}else if(type == "groupchat"){//群聊 


			}
			//消息来源的JID  其他地方要用
			msg.fromJid=from;
			//过滤消息类型  接受到true 就 返回不继续执行
			if(ConversationManager.filterMsgType(msg,fromUserId))
				return;
			
			ConversationManager.decryptMsg(msg,function(cbMsg){
				ConversationManager.receiverShowMsg(from,fromUserId,to,cbMsg);
			});
				
			
	},
	//接受的消息显示到页面
	receiverShowMsg:function(from,fromUserId,to,msg){
			//if(msg.chatType == "groupchat")//群聊
			
				// 存储消息
				ConversationManager.storeMsg(from, to, msg);
				var fromJid=ConversationManager.from;
				var fromUserName=msg.fromUserName;//发送方的用户昵称
				var isGroup=myFn.isNil(msg.objectId)?false:myFn.getUserIdFromJid(fromJid)==msg.objectId;
					// 显示
					if (ConversationManager.isOpen && (fromJid.toLowerCase()
							== from.substr(0, from.indexOf("/")).toLowerCase()||isGroup)) { //判断聊天面板是否打开
						//shikuLog("聊天面板已打开，显示消息。")
						shikuLog("showMsg  Type  " + msg.type+"   "+msg.content);
						if (from.indexOf(AppConfig.mucJID) != -1) {
							var jid = from.substr(0, from.indexOf("@"));
							if (GroupManager.filters[jid]) {
								return;
							}
						}
						if(msg.fromUserId==myData.userId)
							return;
						else if (fromUserId == myData.userId) {
							// shikuLog("显示自己发送的")
							// UI.showMsg(msg, fromUserId, 1);
						} else
							UI.showMsg(msg, fromUserId, 0);

						if (XmppMessage.Type.READ==msg.type) {//已读回执
							if("groupchat"==msg.chatType ){ //群聊 
								GroupManager.disposeReadReceipt(msg); //调用方法处理已读回执 ,这里的fromUserId为群组jid
							}else{ //单聊的已读回执则终止
								return;
							}

						}
						if(XmppMessage.Type.INPUT==msg.type)// 正在输入状态 终止执行
							return;
						if(msg.chatType == "chat"){//单聊
							ConversationManager.sendReadReceipt(from, to, msg.id); //发送已读回执
							UI.moveFriendToTop(fromUserId,fromUserName,0,0);
						}else  if("groupchat"==msg.chatType){ //群聊
							//发送已读回执到群内
							if(myData.isShowGroupMsgReadNum || !myFn.isNil(msg.objectId)){ //判断是否开启了显示群组消息已读人数  若为@消息强制发送
								if (6==msg.type||3==msg.type) { //语音视频暂时不发
								}else{
									GroupManager.sendRead(msg.id); //调用方法发送已读回执
								}
							}
							UI.moveFriendToTop(fromUserId,fromUserName,0,1);
						}

						
					}else {
						if (XmppMessage.Type.READ==msg.type) {//已读回执
							if("groupchat"==msg.chatType ){ //群聊 
								GroupManager.disposeReadReceipt(msg); //调用方法处理已读回执 ,这里的fromUserId为群组jid
								return;
							}else{ //单聊的已读回执则终止
								return;
							}
						}
						//正在输入状态 终止执行
						if(XmppMessage.Type.INPUT==msg.type)
							return;
						if("chat"==msg.chatType){
							//接受到消息好友移动到新朋友的下方  //显示未读消息数量提示
							// $("#myFriendsList #friends_"+fromUserId).insertAfter("#friends_10001");	
							UI.moveFriendToTop(fromUserId,fromUserName,1,0);
						}else if("groupchat"==msg.chatType){
							UI.moveFriendToTop(fromUserId,fromUserName,1,1);
							$("#myRoomList #groups_"+fromUserId).prependTo($("#myRoomList"));
						}

						//存储没有显示到页面的消息(未读消息)
						if(myFn.isNil(DataMap.unReadMsg[fromUserId]) ){ //判断是否存在记录，没有则创建
							/*ownAlert(3,"没有数据哦");*/
							var unReadMsgMap = new Array();
							unReadMsgMap.push(msg);
							DataMap.unReadMsg[fromUserId] = unReadMsgMap;
						}else{ //存在记录，则添加
							DataMap.unReadMsg[fromUserId].push(msg);
						}
							


					}

	},
	//消息回执
	receipt : function(from, to, id) {
		var receipt = $msg({
			to : from,
			from : to,
			type : 'chat'
		}).c("received", {
			xmlns : "urn:xmpp:receipts",
			id : id
		}, null);
		myConnection.send(receipt.tree());

		shikuLog("发送回执： messageId " + id);
	},
	//发送已读回执
	sendReadReceipt : function(from, to,messageId) {
		var type = '';
		if (from.indexOf("@"+AppConfig.mucJID) != -1) {
			type = 'groupchat';
		} else {
			type = 'chat';
		}

		var msg=ConversationManager.createMsg(26,messageId);
		var msgObj=msg;
		//msgObj.messageId=null;//
		var receipt = $msg({
			to : from,
			from : to,
			type : type,
			id:msg.messageId
		}).c("body", null, JSON.stringify(msgObj)).c
		("request", {
			xmlns : "urn:xmpp:receipts"
		}, null);
		
		msg.toJid=from;
		msg.elem=receipt;
		//设置发送消息重发次数
		msg.reSendCount=3;

		// 存储消息
		DataMap.msgMap[messageId]=msg;
		ConversationManager.connSend(receipt,msg.messageId);

		//调用方法将该消息在数据库中的状态改为已读
		changeRead(messageId);
		shikuLog("发送已读回执："+msg.messageId+"       类型:"+type);
		
	},
	//检测消息内容
	checkMessage:function(message){
		 if(ConversationManager.checkReceived(message))
			return null;
		else return message;

	},
	//检测是否为下线消息
	checkConflict:function(elem){
		var type = elem.getAttribute('type');
		var condition=elem.getAttribute('condition');
		if(myFn.isNil(type)||myFn.isNil(condition))
			return false;
		if("remote-stream-error"==condition&&"terminate"==type){
			ownAlert(3,"您已经被挤下线");
			window.location.href = "login.html";
			return true;
		}
		return false;
	},
	//检测是否为消息回执
	checkReceived:function(message){
		var received=message.getElementsByTagName('received')[0];
		if(myFn.isNil(received))
			return false;
		else {
			var id = received.getAttribute('id');
			var xmlns=received.getAttribute('xmlns');
			if(myFn.notNull(xmlns)&&myFn.notNull(id)){
				shikuLog("收到送达回执 ："+id);
				DataMap.msgStatus[id] = 1; //将发送消息状态进行储存 1:送达
				//将对应消息的状态显示为送达
				$("#msgStu_"+id+"").attr("class","msgStatus msgStatusBG"); //改变背景
				$("#msgStu_"+id+"").empty();
				if(1==ConversationManager.isGroup){//群聊
					// $("#groupMsgStu_"+id+"").attr("class","msgStatus"); 
					if(myData.isShowGroupMsgReadNum || !myFn.isNil(message.objectId)) {//开启群消息已读才显示  若为@消息强制显示
						$("#groupMsgStu_"+id+"").text("0人").show();
						GroupManager.changeReadNum(id); //改变数量
					}  
				}
				else
					$("#msgStu_"+id+"").text("送达").show();
				return true;
			}
		}
		return false;
	},
	filterMsgType:function(msg,fromUserId){
		//过滤消息类型 如果被过滤了 就返回 true 则不继续执行

		//群聊会议消息
		/*if(parseInt(msg.type/100)==1)
			return true;*/
		 if(parseInt(msg.type/100)==9||401==msg.type||402==msg.type){
			//调整控制信息  401 群文件上传  402 群文件删除
			msg=GroupManager.converGroupMsg(msg);
			UI.showMsg(msg, fromUserId, 0);
			return true;
		}else if (parseInt(msg.type/100)==5){
			//好友验证 消息
			UI.showMsgNum(10001);
			msg.fromUserId=fromUserId;
			UI.msgWithFriend(msg);
			return true;
		}else if (msg.type>99&&msg.type<130){
			//音视频通话相关消息
			SIPWin.processMsg(msg);
			return true;
		}
		return false;

	},
	refresh:function(msg){
		var timeSend = Math.round(new Date().getTime() / 1000);
		var messageId=myFn.randomUUID();

			msg.messageId=messageId;
			msg.fromUserId = myData.userId + "";
			msg.fromUserName = myData.nickname;
			msg.timeSend = timeSend;
			if(1==myData.isEncrypt)
				msg.isEncrypt=myData.isEncrypt;
			if(1==myData.isReadDel)
				msg.isReadDel=myData.isReadDel;
		return msg;
	},
	createMsg :function(type,content){
		var timeSend = Math.round(new Date().getTime() / 1000);
		var messageId=myFn.randomUUID();
			var msg = {
				messageId:messageId,
				fromUserId : myData.userId + "",
				fromUserName : myData.nickname,
				content : content,
				timeSend : timeSend,
				type : type
			};
			if(1==myData.isEncrypt)
				msg.isEncrypt=myData.isEncrypt;
			if(1==myData.isReadDel)
				msg.isReadDel=myData.isReadDel;
			return msg;
	},
	showLog :function(msg){//日志
		if(msg.fromUserId==ConversationManager.fromUserId){
			var logHtml ="<div class='logContent' >"
						+"	<span>"+msg.content+"</span> "
						+"</div>";
			$("#messageContainer").append(logHtml);
			UI.scrollToEnd();
		}

	},
	sendMsg : function(msg,callback,toJid) {
		//发送消息开始
		//检查Xmpp 是否在线
		if(myConnection.connected){
			//检查消息是否需要加密
			ConversationManager.checkEncrypt(msg,function(result){
				ConversationManager.sendMsgAfter(result,toJid);
			  if(callback)	
				callback();
			});
		}else{
			window.wxc.xcConfirm("你已掉线是否重新登录？", window.wxc.xcConfirm.typeEnum.confirm,{
				onOk:function(){
					mySdk.xmppLogin(function(){
						GroupManager.joinMyRoom();
						ConversationManager.checkEncrypt(msg,function(result){
							ConversationManager.sendMsgAfter(result,toJid);
						  if(callback)	
						 		callback();
						});
					});
				}
			});
		}
		

	},
	sendMsgAfter:function(msg,toJid){
		//组装xmpp 消息体 继续发送
		var type=msg.type;
		var from = myData.jid;
		// toJid指定的消息接受者
		// Temp.toJid 临时的消息接受者
		// ConversationManager.from  聊天框的消息接受者
		
		/*if(myFn.isNil(toJid))
			toJid=Temp.toJid;*/
		if(myFn.isNil(toJid))
		 	toJid = ConversationManager.from;
		// 发送消息
		var elem = null;
		var msgId=msg.messageId;
		var msgObj=msg;
		if (toJid.indexOf("@"+AppConfig.mucJID) != -1) {
			/*msgObj.messageId=null;*/
			//单聊body 不需要messageId
			elem = $msg({
				to : toJid,
				type : 'groupchat',
				id : msgId
			}).c("body", null, JSON.stringify(msgObj)).c("request", {
				xmlns : "urn:xmpp:receipts"
			}, null);
		} else {
			elem = $msg({
				to : toJid,
				type : 'chat',
				id : msgId
			}).c("body", null, JSON.stringify(msgObj)).c("request", {
				xmlns : "urn:xmpp:receipts"
			}, null);
		}
		// 存储消息
		ConversationManager.storeMsg(toJid, from, msg);

		msg.id=elem.nodeTree.id;
		msg.toJid=toJid;
		msg.elem=elem;
		//设置发送消息重发次数
		if(msg.type>100)
			msg.reSendCount=3;
		else 
			msg.reSendCount=1;
		DataMap.msgMap[msgId]=msg;
		ConversationManager.connSend(elem,msgId); 
		return msg;
	},
	connSend:function(elem,msgId){
		//调用当前链接发送消息
		//myConnection.send(elem.tree());
		shikuLog("sendMsg  "+myFn.parseMessage(elem));
		myConnection.sendIQ(elem.tree(),function(stanza){
				 shikuLog("sendIQ result "+Strophe.serialize(stanza));
			},function(stanza){
				 shikuLog("sendIQ error "+Strophe.serialize(stanza));
			},null
		);
		ConversationManager.sendTimeoutCheck(msgId);
	},
	sendTimeout:function(msgId){
		var msg=DataMap.msgMap[msgId];
		if(myFn.isNil(msg)){
			UI.showReSendImg(msgId);
			shikuLog("sendTimeout  消息找不到了");
		}
		//检查网络状态
		checkNetAndXmppStatus();
		if(msg.reSendCount>0){
			shikuLog(" 消息自动重发 "+msgId+"  type "+msg.type+" content ==  "+msg.content+"  reSendCount "+msg.reSendCount);
			msg.reSendCount=msg.reSendCount-1;
			DataMap.msgMap[msgId]=msg;
			ConversationManager.connSend(msg.elem,msgId);
		}else{
			shikuLog(" showReSendImg "+msgId+"  type "+msg.type+" content ==  "+msg.content+"  reSendCount "+msg.reSendCount);
			UI.showReSendImg(msgId);
		}
	},
	sendTimeoutCheck : function(messageId){
		//发送的消息显示到页面6s后进行重发检测
		setTimeout(function(){
			//根据messageId 到存放消息状态的map中查找是否有记录
			if (myFn.isNil(DataMap.msgStatus[messageId])) { //没有记录，显示重发标志
				ConversationManager.sendTimeout(messageId);
			}
			
		},6000);
	},
	checkEncrypt:function(msg,callback){
		//检测消息加密  如果加密 调用接口加密
		var content=msg.content;
		if(myData.isEncrypt==1 || myData.isEncrypt=='1'){
				ConversationManager.encrypt(msg,function(type,text){
					msg.type=type;
					msg.content=text;
					//ConversationManager.sendMsgAfter(content,msg);
					callback(msg);
				});
		}else{
			//ConversationManager.sendMsgAfter(content,msg);
			callback(msg);
		}
	}
	
};

 function changeTab(tabCon_num,id){
 	
    for(i=0;i<=2;i++) {
        document.getElementById("tabCon_"+i).style.display="none"; //将所有的层都隐藏
    }
    document.getElementById("tabCon_"+tabCon_num).style.display="block";//显示当前层
       
    //切换图片
	//更改自己的图标
	$("#"+id+"").addClass("msgMabayChange");
	//将其它兄弟的图标还原
	if("msgTab"==id){//消息面板
		$("#detailsTab").removeClass("msgMabayChange");
	}else if("detailsTab"==id){//详情面板

		$("#msgTab").removeClass("msgMabayChange");
	}

};

function creatMsgHistory(type,o,msg){ //生成一条聊天消息的html
		
		var itemHtml = "";
			if (1 == type) {
				if (o.direction == 0) {
					msg.fromUserName=myData.nickname;
					itemHtml += UI.createItem(msg, o.sender, 1);
				} else {
					msg.fromUserName=DataMap.friends[o.receiver].toNickname;
					itemHtml += UI.createItem(msg, o.receiver, 0);
				}
			} else {
				if (myData.userId == o.sender) {
					//群组
					//发送者是自己
					msg.fromUserName=GroupManager.roomCard;
					itemHtml += UI.createItem(msg, o.sender, 1);
				} else {
					itemHtml += UI.createItem(msg, o.sender, 0);
				}
			}
			if(myFn.isNil(itemHtml)){
				return "";
			}
		//检查记录中是否存在未读消息
		if(1!=ConversationManager.isGroup&&(true!=msg.isRead ||1!=msg.isRead)){
			ConversationManager.sendReadReceipt(ConversationManager.from, myData.jid, o.messageId); //发送已读回执
			
		}

		return itemHtml;		
	}

	function changeRead(messageId){  //修改消息状态为已读
		if(myFn.isNil(messageId)){
			// ownAlert(3,"messageId 为空");
			return;
		}
		myFn.invoke({
				url:"/tigase/changeRead",
				data:{
					messageId:messageId
				},
				success:function(result){
					// ownAlert(3,"修改已读完成");
				}
		})
	};

	
