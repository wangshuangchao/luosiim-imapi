var UI = {
	initDatepicker:function(){
		$('.datepicker').datetimepicker({
	       minView: "month", //选择日期后，不会再跳转去选择时分秒 
	        format: "yyyy-mm-dd", //选择日期后，文本框显示的日期格式 
	        language: 'zh-CN', //汉化  
	        showMeridian : true,//是否加上网格
	        autoclose:true, //选择日期后自动关闭 
	        weekStart: 1,
	        todayBtn:  1, 
	       
	 });
	},
	choiceEmojl : function(key) {
		// var emojiHtml = "<img data-alias='hehe' src='" + _emojl[key] + "' width='25' height='25' title='"+key+"'/>";
		$("#messageBody").val($("#messageBody").val() + key);
		$("#emojl-panel #gifList").getNiceScroll().hide();//隐藏滚动条
		$("#emojl-panel #emojiList").getNiceScroll().hide();
		$("#emojl-panel").hide();
	},
	createItem : function(msg, fromUserId, direction,isSend) {   //消息Item

		//isSend  发送后创建消息
		var contentHtml = UI.createMsgContent(msg,direction);
		var html=null;
		
		if(myFn.isNil(contentHtml))
			return "";

		html= "<div id=msg_"+msg.id+" msgType="+msg.type+" class='chat_content_group " + (0 == direction ? "buddy" : "self")+ "'>"
			  +     "<div class='imgAvatar " + (0 == direction ? "floatLeft" : "floatRight")+ "'>"
			  +			"<figure>"
		      +			  "<img onerror='this.src=\"img/ic_avatar.png\"' src='" +myFn.getAvatarUrl('chat'==msg.chatType?fromUserId:msg.fromUserId)+ "' class='chat_content_avatar' />"
		      +			"</figure>"
		      +		"</div>"
		      +	    "<p class='chat_nick'>" + msg.fromUserName + "</p>"
			  +	    "<div class='content'>";
			if(1!=ConversationManager.isGroup) { //判断是否是群组消息
		    	 html=html + 	(0 == direction ? contentHtml : "<span id='msgStu_"+msg.id+"' class='msgStatus"+ (true == msg.isRead ? " msgStatusBG' style='background-color:#7BD286;'>已读" :1==isSend?" '><img src='img/loading.gif'>":" msgStatusBG'>送达")+ "</span>"+contentHtml);	
			}else{//群组消息

				if (myData.isShowGroupMsgReadNum || !myFn.isNil(msg.objectId)) {//开启群消息已读  若为@消息强制显示
					//获取群消息已读人数
					var readNum = 0
					if (!myFn.isNil(groupMsgReadList[msg.id])) { //判断已读列表人数是否为空
						readNum = groupMsgReadList[msg.id].length;
					}
					groupMsgReadNum[msg.id]=readNum; //将数据缓存到已读人数中
				 	html=html + contentHtml;
				}else{
				 	html=html + (0 == direction ? contentHtml+"<span id='groupMsgStu_"+msg.id+"' class=''></span>" : "<span id='groupMsgStu_"+msg.id+"' style='background-color:none;' ></span>"+contentHtml);

				}
		  			/*"+ (1==isSend?"'<img src='img/loading.gif'>":"")+ "*/
			} 
		 	
		  html=html +"</div>"
		  +"</div>";                                  

		  return html;

	},
	createMsgContent:function(msg,direction){
		var contentHtml = "";
		switch (msg.type){
			case 1:// 文字、表情
				 		contentHtml = "<p class='chat_content'>" + myFn.parseContent(msg.content) + "</p>";
				break;
			case 2:// 图片
			 	contentHtml = '<p id="chat_content" class="chat_content" >';
			 	if(msg.isReadDel=="true" || msg.isReadDel==1){ //判断是否为阅后即焚消息
			 		contentHtml += '<img id="readDelImg" class="shade" src="' + msg.content + '"  style="max-width:100%;"  onclick="UI.showImgZoom(\'' + msg.content + '\',\''+msg.id+'\')"/>';
			 	}else{ //不是
			 		contentHtml += '<img src="' + msg.content + '"  style="max-width:100%;" onclick="UI.showImgZoom(\'' + msg.content + '\')"/>';
			 	}
			 	contentHtml+='</p>';
			  break;
			case 3:// 语音 
				if("groupchat"==msg.chatType && myData.isShowGroupMsgReadNum){
					contentHtml = "<p id='voiceP_"+msg.id+"' class='chat_content' onclick='UI.showAudio(\"" + msg.content + "\",\"" + msg.id + "\",\""+msg.isReadDel+"\");GroupManager.sendRead(\"" + msg.id + "\")'>"
							+     "<img id='voiceImg' src='img/voice.png' style='width:25px; height:25px;'> <span style='display:inline; margin-left:15px; margin-right:10px;'>"+msg.timeLen+"\" </span>"
							+	  "<div id='voice_"+msg.id+"'></div>"
							+ "</p>";
				}else{
					contentHtml = "<p id='voiceP_"+msg.id+"' class='chat_content' onclick='UI.showAudio(\"" + msg.content + "\",\"" + msg.id + "\",\""+msg.isReadDel+"\")'>"
							+     "<img id='voiceImg' src='img/voice.png' style='width:25px; height:25px;'> <span style='display:inline; margin-left:15px; margin-right:10px;'>"+msg.timeLen+"\" </span>"
							+	  "<div id='voice_"+msg.id+"'></div>"
							+ "</p>";
				}
				
			  break;
			case 4:// 位置
				if(direction==1){//我发送的
					contentHtml ='<a class="mapMsg"  href="javascript:void(0)" onclick="showToMap(this)"'+
					' lng="'+msg.location_x+' "lat="'+msg.location_y+'"> '
					+'<div><p class="chat_content" style="width:100%;margin-bottom:0px;max-width:50%;margin-right:10%">我的位置 ：'+msg.objectId+'</p>'
					+'<img src="'+Map.imgApiUrl+msg.location_y+','+msg.location_x+' "  style="max-width:50%;margin-right:45px"/></div>'
					+'</a>';
					break;
				}else if(direction==0){
					contentHtml ='<a class="mapMsg"  href="javascript:void(0)" onclick="showToMap(this)"'+
					' lng="'+msg.location_x+' "lat="'+msg.location_y+'"> '
					+'<div style="display:inline"><p class="chat_content" style="display:inline;width:100%;max-width:50%;margin-bottom:0px;float:left;">我的位置 ：'+msg.objectId+'</p>'
					+'<img src="'+Map.imgApiUrl+msg.location_y+','+msg.location_x+' "  style="max-width:50%;margin-left:45px"/></div>'
					+'</a>';
					break;
				}
			 	
			  
			case 5:// 动画
			   contentHtml = '<p class="chat_contentGIF">'
			 				+    '<img src="gif/' + msg.content + '"  style="width:120px;height:120px;"/>'
			 				+'</p>';
			   break;
			case 6:// 视频
			 	// contentHtml = '<p class="chat_content">视频：<a href="javascript:;" onclick="UI.showVideo(\'' + msg.content + '\')">点击播放</a></p>';
				if("groupchat"==msg.chatType && myData.isShowGroupMsgReadNum){
					contentHtml = "<div id='vidoePlay_"+msg.id+"' class='m chat_content' style='max-width: 70%;'>"
							+		"<video id='video' poster='vs.png' controls onplay='GroupManager.sendRead(\"" + msg.id + "\")' onended='UI.videoPlayEnd(\"" + msg.id + "\",\""+msg.isReadDel+"\")'>"
						  	+			"<source src='"+msg.content+"' type='video/mp4'>"
							+		"</video>"
							+		"<script>plyr.setup();</script>"
							+ "</div>";
				}else{
					contentHtml = "<div id='vidoePlay_"+msg.id+"' class='m chat_content' style='max-width: 70%;'>"
							+		"<video id='video' poster='vs.png' controls  onended='UI.videoPlayEnd(\"" + msg.id + "\",\""+msg.isReadDel+"\")'>"
						  	+			"<source src='"+msg.content+"' type='video/mp4'>"
							+		"</video>"
							+		"<script>plyr.setup();</script>"
							+ "</div>";
				}			

			  break;
			case 7:// 音频
			 
			  break;
			case 8: // 名片
			 	contentHtml = '<p class="chat_content" style="cursor:pointer;background:white;border:1px solid #ccc;width:195px" onclick="UI.showUser(\''
					+ msg.objectId + '\')"><img  onerror=\'this.src=\"img/ic_avatar.png\"\'   width=40 height=40 src=\'' + myFn.getAvatarUrl(msg.objectId) + '\' />&nbsp;&nbsp;' + msg.content + '</br><span style="border-top:1px solid #ccc;display:block;margin-top:5px;margin-bottom:-5px;color:#9F9F9F">个人名片</span></p>';
			  break;
			case 9:// 文件
			 	contentHtml = '<p class="chat_content" style="width:195px;"><a href="' + msg.content + '" target="_blank"><img width=40 height=40 src="img/file.png"><text>'+msg.fileName.substring(msg.fileName.lastIndexOf("/")+1)+'</text></a></p>';
			  break;
			case 10://提醒
			 	GroupManager.showGroupLog(msg);
			 	return;
			  break;
			case 26:// 已读回执
				DataMap.msgStatus[msg.content] = 2; //将发送消息状态进行储存 2:已读
		 		changeRead(msg.content); //调用函数改变数据库中消息的状态
		 		$("#msgStu_"+msg.content+"").text("已读").show();
				return;
			  break;
			case 28://红包
			 	contentHtml='<p onclick="UI.openRedPacket(\''+msg.objectId+'\')" class="chat_content" style="background:#ff8a2a;width:195px;"><a style="color:white;text-decoration:none;"><img src="img/ic_chat_hongbao.png">'+msg.content+'</a></p>'
			  	if(direction==1){
			  		if(msg.fileName==1){
			  		contentHtml+="<p style='margin-right:42%;margin-top:-10px;color:gray'>普通红包</p>";
				  	}else if(msg.fileName==2){
				  		contentHtml+="<p style='margin-right:180px;margin-top:-10px;color:gray'>拼手气红包</p>";
				  	}else if(msg.fileName==3){
				  		contentHtml+="<p style='margin-right:180px;margin-top:-10px;color:gray'>口令红包</p>";
				  	}
				  	break;
				  }else if(direction==0){
				  	if(msg.fileName==1){
			  		contentHtml+="<p style='margin-left:45px;margin-top:-10px;color:gray'>普通红包</p>";
				  	}else if(msg.fileName==2){
				  		contentHtml+="<p style='margin-left:45px;margin-top:-10px;color:gray'>拼手气红包</p>";
				  	}else if(msg.fileName==3){
				  		contentHtml+="<p style='margin-left:45px;margin-top:-10px;color:gray'>口令红包</p>";
				  	}
				  	break;
				  }
			  	
			  break;
			case 501://同意加好友
				GroupManager.showGroupLog(msg);
			  break;
			case 201:// 正在输入
				// ownAlert(3,"正在输入");
				$("#chatHint").empty().text("对方正在输入......");
			 	$("#chatHint").show();
				setTimeout(function () {  //过6秒后隐藏
					$("#chatHint").empty();
	        		$("#chatHint").hide();
	    		}, 6000);
				return;
			  break;
			case 80://单条图文
			 	
			  break;
			case 81://多条图文
			 
			  break;
			case 202://消息撤回
				msg.messageId=msg.content;
				var recallHtml = "";
				if(msg.fromUserId==myData.userId){ //我自己
					recallHtml = "<div class='logContent'><span>你撤回了一条消息 ("+myFn.toDateTime(msg.timeSend)+")</span></div>";
				}else{
					recallHtml = "<div class='logContent'><span>"+msg.fromUserName+" 撤回了一条消息 ("+myFn.toDateTime(msg.timeSend)+")</span></div>";
				}
				
				$("#messageContainer").append(recallHtml);
				// $("#messageContainer").;
				$("#messageContainer #msg_"+msg.messageId).remove();
				msg.content=msg.fromUserName+" 撤回了一条消息 ("+myFn.toDateTime(msg.timeSend)+")";
				GroupManager.showGroupLog(msg);
			 	return;
			  break;

			default://默认 其他
				return null;
		}
		return contentHtml;
	},
	forwardingMsg:function(msg,userIdArr){
		//转发消息
		var msg=ConversationManager.refresh(msg);	
		var toUserId=null;
		for (var i = 0; i < userIdArr.length; i++) {
			toUserId=userIdArr[i];
			UI.sendMsg(msg,myFn.getJid(toUserId));
		}
	},
	//领取红包
	openRedPacket:function(packetId){
		$("#redpacket_details").empty();
		var html="";
		mySdk.getRedPacket(packetId,function(result){
			
			var redpacket=result.data["packet"];//红包实体
			var list="";
			list=result.data["list"];
			var num=0;
			var over;
			if(redpacket.type==1&&redpacket.userId==myData.userId){//普通红包，自己发的
				html="<p><img width=35 height=35 src=\'" + myFn.getAvatarUrl(redpacket.userId) + "\'><span>来自"+redpacket.userName+"的红包共"+redpacket.money+"元</span></p>"
					+"<p>红包已领取"+list.length+"/"+redpacket.count+"剩余"+redpacket.over.toFixed(2)+"元</p>";
				for(var i=0;i<list.length;i++){
					html+="<p style='border-bottom:1px solid #81999933;'><img width=35 height=35 src=\'" + myFn.getAvatarUrl(list[i].userId) + "\'><span>"+list[i].userName+"&nbsp;&nbsp;&nbsp;&nbsp;"+myFn.toDateTime(list[i].time)+"&nbsp;&nbsp;&nbsp;&nbsp;"+list[i].money+"元</span></p>";
				}
			}else if(redpacket.type!=3){//除了口令红包
				if(result.resultCode==1){
					mySdk.openRedPacket(packetId,function(result){//收到红包的实体
						list=result.data["list"];
						redpacket=result.data["packet"];
						if(result.resultCode==1){
							html="<p><img width=35 height=35 src=\'" + myFn.getAvatarUrl(redpacket.userId) + "\'><span>来自"+redpacket.userName+"的红包共"+redpacket.money+"元</span></p>"
							+"<p>红包已领取"+list.length+"/"+redpacket.count+"剩余"+redpacket.over.toFixed(2)+"元</p>";
							for(var i=0;i<list.length;i++){
								html+="<p style='border-bottom:1px solid #81999933;'><img width=35 height=35 src=\'" + myFn.getAvatarUrl(list[i].userId) + "\'><span>"+list[i].userName+"&nbsp;&nbsp;&nbsp;&nbsp;"+myFn.toDateTime(list[i].time)+"&nbsp;&nbsp;&nbsp;&nbsp;"+list[i].money+"元</span></p>";
							}
							$("#redpacket_details").append(html);
						}
					});
				}else if(result.resultCode==0){
					list=result.data["list"];
					html="<p><img width=35 height=35 src=\'" + myFn.getAvatarUrl(redpacket.userId) + "\'><span>来自"+redpacket.userName+"的红包共"+redpacket.money+"元</span></p>"
					+"<p>红包已领取"+list.length+"/"+redpacket.count+"剩余"+redpacket.over.toFixed(2)+"元</p>";
					for(var i=0;i<list.length;i++){
						html+="<p style='border-bottom:1px solid #81999933;width:495px;'><img width=35 height=35 src=\'" + myFn.getAvatarUrl(list[i].userId) + "\'><span>"+list[i].userName+"&nbsp;&nbsp;&nbsp;&nbsp;"+myFn.toDateTime(list[i].time)+"&nbsp;&nbsp;&nbsp;&nbsp;"+list[i].money+"元</span></p>";
					}
				}
			}else{//口令红包
				if(result.resultCode==1&&redpacket.userId!=myData.userId){
					$("#getredpacket").modal("hide");
					var command=prompt("请输入口令","");
					if(redpacket.greetings==command){
						mySdk.openRedPacket(packetId,function(result){//收到红包的实体
							list=result.data["list"];
							redpacket=result.data["packet"];
							if(result.resultCode==1){
								html="<p><img width=35 height=35 src=\'" + myFn.getAvatarUrl(redpacket.userId) + "\'><span>来自"+redpacket.userName+"的红包共"+redpacket.money+"元</span></p>"
								+"<p>红包已领取"+list.length+"/"+redpacket.count+"剩余"+redpacket.over.toFixed(2)+"元</p>";
								for(var i=0;i<list.length;i++){
									html+="<p style='border-bottom:1px solid #81999933;'><img width=35 height=35 src=\'" + myFn.getAvatarUrl(list[i].userId) + "\'><span>"+list[i].userName+"&nbsp;&nbsp;&nbsp;&nbsp;"+myFn.toDateTime(list[i].time)+"&nbsp;&nbsp;&nbsp;&nbsp;"+list[i].money+"元</span></p>";
								}
								$("#redpacket_details").append(html);
							}else{
								html="<p><img width=35 height=35 src=\'" + myFn.getAvatarUrl(redpacket.userId) + "\'><span>来自"+redpacket.userName+"的红包共"+redpacket.money+"元</span></p>"
								+"<p>红包已领取"+list.length+"/"+redpacket.count+"剩余"+redpacket.over.toFixed(2)+"元</p>";
								for(var i=0;i<list.length;i++){
									html+="<p style='border-bottom:1px solid #81999933;width:495px;'><img width=35 height=35 src=\'" + myFn.getAvatarUrl(list[i].userId) + "\'><span>"+list[i].userName+"&nbsp;&nbsp;&nbsp;&nbsp;"+myFn.toDateTime(list[i].time)+"&nbsp;&nbsp;&nbsp;&nbsp;"+list[i].money+"元</span></p>";
								}
							}
						});
						$("#getredpacket").modal("show");
					}
				}else{
					html="<p><img width=35 height=35 src=\'" + myFn.getAvatarUrl(redpacket.userId) + "\'><span>来自"+redpacket.userName+"的红包共"+redpacket.money+"元</span></p>"
						+"<p>红包已领取"+redpacket.receiveCount+"/"+redpacket.count+"剩余"+redpacket.over.toFixed(2)+"元</p>";
					for(var i=0;i<redpacket.receiveCount;i++){
						html+="<p style='border-bottom:1px solid #81999933;width:495px;'><img width=35 height=35 src=\'" + myFn.getAvatarUrl(list[i].userId) + "\'><span>"+list[i].userName+"&nbsp;&nbsp;&nbsp;&nbsp;"+myFn.toDateTime(list[i].time)+"&nbsp;&nbsp;&nbsp;&nbsp;"+list[i].money+"元</span></p>";
					}
				}
				
			}
			$("#redpacket_details").append(html);
		});
		$("#getredpacket").modal("show");
		/*if(redpacket.type==3){
			
		}*/
	},
	//selete事件
	commandRedPacket:function(){
		var vs = $('select  option:selected').val();
		
		if(vs==3){
			$("#spanGreetings").empty();
			$("#spanGreetings").append("口令");
		}else{
			$("#spanGreetings").empty();
			$("#spanGreetings").append("祝福语");
		}
		
	},
	//发送消息
	sendMsg : function(msg,toJid) {
		if (!myFn.isNil(DataMap.deleteRooms[ConversationManager.fromUserId])) { //判断用户是否被踢出该群
			ownAlert(3,"你已被踢出该群，无法发送消息");
			return;
		}
		msg.id=msg.messageId;
		var content=msg.content;
		ConversationManager.sendMsg(msg,function(){
			//接受方 为当前打开界面目标用户 才添加消息到界面
			if(ConversationManager.from==msg.toJid){
				msg.content=content;
				UI.showMsg(msg,myData.userId,1);
				 	//发送的消息显示到页面15s后进行重发检测
				 if(1!=ConversationManager.isGroup)
					ConversationManager.sendTimeoutCheck(msg.id);
				$("#messageBody").val("");
				var toUserId=myFn.getUserIdFromJid(ConversationManager.from);
				UI.moveFriendToTop(toUserId,null,0,ConversationManager.isGroup);
			}
			
		},toJid);

	},
	reSendMsg:function(msgId){
		//改变UI
		$("#msgStu_"+msgId+"").empty();
		$("#msgStu_"+msgId+"").append("<img src='img/loading.gif'>");
		//消息重发
		var msg=DataMap.msgMap[msgId];
		if(myFn.isNil(msg))
			return;
		ConversationManager.sendMsgAfter(msg);
		ConversationManager.sendTimeoutCheck(msgId); //调用方法进行重发检测
		
	},
	sendImg:function(){
		var imgUrl = $("#myFileUrl").val();
		var msg=ConversationManager.createMsg(2, imgUrl);
			UI.sendMsg(msg);
	},
	sendFile:function(){
		var content = $("#myFileUrl").val();
		var msg=ConversationManager.createMsg(9, content);
		msg.fileName=$("#filePath").val();
		msg.fileSize=$("#filePath").attr("size");

			UI.sendMsg(msg);
	},
	sendGif:function(gifName){
		$("#emojl-panel #gifList").getNiceScroll().hide(); //隐藏滚动条
		$("#emojl-panel").hide();//隐藏表情面板
		var msg = ConversationManager.createMsg(5, gifName);
		UI.sendMsg(msg);
	},
	showMsg : function(msg, fromUserId, direction) {
		//direction  1 自己发送的   0 别人发送的
		var itemHtml = this.createItem(msg, fromUserId, direction,1);
		if(myFn.isNil(itemHtml))
			return "";
		// 追加消息
		$("#messageContainer").append(itemHtml);
		// 滚动到底部
		setTimeout(function(){
			$(".nano").nanoScroller();//刷新滚动条
			UI.scrollToEnd();//滚动到底部
		},500);

		
	},
	showReSendImg:function(messageId){
		// $("#msgStu_"+id+"").attr("class","msgStatus msgStatusBG"); //改变背景
		if (ConversationManager.isGroup==1) { //群聊
			//消息框显示重发标志
			$("#groupMsgStu_"+messageId+"").empty();
			$("#groupMsgStu_"+messageId+"").append("<img id='resendMsg' src='img/resend.png' width='20px;' height='20px' onclick='UI.reSendMsg(\""+messageId+"\")'>");
		}else{
			//消息框显示重发标志
			$("#msgStu_"+messageId+"").empty();
			$("#msgStu_"+messageId+"").append("<img id='resendMsg' src='img/resend.png' width='20px;' height='20px' onclick='UI.reSendMsg(\""+messageId+"\")'>");
		}
	},
	showMsgNum :function(userId){
		//显示消息数量
		var msgNum = DataMap.msgNum[userId];
		if(myFn.isNil(msgNum)){
			// var i=$("#msgNum_"+userId+"  #span").html();
			// msgNum=parseInt(i);
			msgNum = 1;
		}
		else msgNum+=1;

		DataMap.msgNum[userId]=msgNum;
		messageNumber += 1; //将好友发送的未读消息汇总
		$("#myMessagesList #msgNum_"+userId+" #span").html(msgNum);
		$("#myMessagesList #msgNum_"+userId).show();
		// ConversationManager.changMessageNum(0,messageNumber);
		//显示到页面
		if($("#to #messageNum").css('display') == 'none'){
			$("#to #messageNum").show();
		}
		if(messageNumber > 99){  //数量大于99 则显示99+
			$("#to #messageNum").text("99+");
		}else{
			$("#to #messageNum").text(messageNumber);
		}
		if("NewFriend"==Temp.nowList&&10001==userId)
			UI.showNewFriends(0);
	},
	clearMsgNum:function(userId){
		var msgNum = DataMap.msgNum[userId];
		if(myFn.isNil(msgNum))
			msgNum = 0;
		
		messageNumber -= msgNum; //将已读的消息数从未读消息汇总数中减去
		DataMap.msgNum[userId]=0;
		$("#myMessagesList #msgNum_"+userId+" #span").html(1);
		$("#myMessagesList #msgNum_"+userId).hide();
		// ConversationManager.changMessageNum(1,messageNumber);
		//显示到页面
		if(!messageNumber>0){
			$("#to #messageNum").hide();
			return;
		}
		if(messageNumber > 99){  //数量大于99 则显示99+
			$("#to #messageNum").text("99+");
		}else{
			$("#to #messageNum").text(messageNumber);
		}
	},
	videoPlayEnd : function(messageId,readDel) { //视频播放结束后执行
		if(readDel=="true" || readDel==1){ //判断是否为阅后即焚消息
			//播放结束后显示阅后即焚图片
			$("#messageContainer #vidoePlay_"+messageId+"").attr("class","chat_content");
			$("#messageContainer #vidoePlay_"+messageId+"").empty().append("<img src='img/delete.gif' style='width:180px;'>");
			//将此条阅后即焚消息从缓存消息中删除
			myFn.deleteReadDelMsg(messageId);
		}
		
	},
	showAudio : function(videoUrl,messageId,readDel) {
		var type=videoUrl.substr(videoUrl.lastIndexOf('.')+1,videoUrl.length);
		if("amr"==type){
			var url=AppConfig.uploadUrl.substr(0,AppConfig.uploadUrl.lastIndexOf('/'))+"/amrToMp3";
			var params = {
					paths : videoUrl,
					
				};
				$.ajax({
					type:"POST",
					url:url,
					jsonp:"callback",
					data:params,
					success : function(result) {
						//if (1 == result.resultCode) {
							var res = eval("(" + result + ")");
							videoUrl=res.data[0].oUrl;
							UI.showAudioInUrl(videoUrl,messageId,readDel);
						//}
					},
					error : function(result) {
						
					}
				});
				
		}else{	
			videoUrl = videoUrl.substr(0, videoUrl.lastIndexOf('.')) + ".mp3";
			UI.showAudioInUrl(videoUrl,messageId,readDel);
			// videoUrl =
			// "http://96.f.1ting.com/56401610/e02941274d5babb817f2abcf5f6d0220/zzzzzmp3/2009fJun/10/10gongyue/02.mp3";
			
		}
	},
	//根据url 开始播放语音
	showAudioInUrl:function(videoUrl,msgId,readDel){
		//将页面上的语音消息都恢复成静态图片
		$("#messageContainer #voiceImg").attr("src","img/voice.png");

		//ownAlert(3,"开始播放语音");
		
		var voiceHtml = '<audio id="audio" autoplay="autoplay">'
					  +		'<source src="'+videoUrl+'" type="audio/mpeg"/>'
					  +'</audio>'
		$("#messageContainer #voice_"+msgId+"").empty().append(voiceHtml);
		//显示播放的gif图
		$("#messageContainer #voiceP_"+msgId+" #voiceImg").attr("src","img/voice.gif");

		//播放结束后恢复
		$("#messageContainer #voice_"+msgId+" #audio").bind('ended',function () {
			if(readDel=="true" || readDel==1 ){ //判断是否为阅后即焚消息
				$("#messageContainer #voiceP_"+msgId+"").attr("onclick","");
				$("#messageContainer #voiceP_"+msgId+"").empty().append("<img src='img/delete.gif' style='width:160px;'>");
				setTimeout(function(){
					UI.scrollToEnd();
				},400);

				//将此条阅后即焚消息从缓存消息中删除
				myFn.deleteReadDelMsg(msgId);

			}else{ // 不是阅后即焚 则恢复为静态图片
				$("#messageContainer #voiceP_"+msgId+" #voiceImg").attr("src","img/voice.png");
			} 
			$("#messageContainer #voice_"+msgId+"").empty(); //清除播放器      
			
		});
		// setTimeout(function(){
		// 		$("#messageContainer #voiceP_"+msgId+" #voiceImg").attr("src","img/voice.gif");
		// },timeLen*1000);

	},
	deleteMsg:function(type,del,msgId){

		$("#messageContainer #msg_"+msgId).remove();
		//删除消息
		mySdk.deleteMsg(type,del,msgId,function(){
			
		});       
	},
	scrollToEnd : function() {
		//document.getElementById("messageEnd").scrollIntoView();
		console.log("滚动到底部");
		$(".nano").nanoScroller({ scrollBottom: -1000000000});

	},
	createFriendsItem : function(imgUrl, userId, nickname, desc) {
		if(10000==userId)
			return"";

		var _item = "<div class='' id='friends_"+userId+"' onclick='UI.isChoose(\"" + userId + "\");'>" 
					+	"<div class='media-main'>"
					+      "<a href='javascript:UI.showUser(" + userId + ")' class='pull-left media-avatar'>"
					+           "<img onerror='this.src=\"img/ic_avatar.png\"' width='43' height='43' alt='' src="+ imgUrl + " class='roundAvatar'>"
				    +      "</a>"
				    +      "<div onclick='ConversationManager.open(\"" + (userId + "@" + AppConfig.boshDomain) + "\",\"" + nickname + "\");' style='cursor: pointer;' class='media-body'>"
				    +         "<h5 class='media-heading'>" + nickname + "</h5>"
				    +" <div id='msgNum_"+userId+"' class='news' style='float:right;display:none'><span id='span' style='background-color: #FA6A43;border-radius: 12px;padding:0 4px;color:white;'>"
				    +1+"</span></div>"
				    +         "<div style='color:#7E7979'>" +userId+ "</div>"
				    +      "</div>"
				    +   "</div>"
				    +"</div>";
		return _item;
	},
	createSysFriendsItem : function() {
		//系统消息
		var	_item = "<div class='' id='friends_"+10000+"' onclick='UI.isChoose(\"" + 10000 + "\");' style='border-bottom:1px solid #EEF0F5'>" 
					+	"<div class='media-main' onclick='UI.showUser(" + 10000+");'>"
					+      "<a href='javascript:UI.showUser(" + 10000 + ")' class='pull-left media-avatar'>"
					+                 "<img onerror='this.src=\"img/ic_avatar.png\"' width='40' height='40' alt='' src=\"img/im_10000.png\"' class='roundAvatar'>"
				    +      "</a>"
				    +      "<div onclick='ConversationManager.open(\"" + (10000 + "@" + AppConfig.boshDomain) + "\", \"系统客服\");' style='cursor: pointer;' class='media-body'>"
				    +         "<h5 class='media-heading'>" + "系统" + "</h5><div id='msgNum_"+10000+"' class='news' style='float:right;display:none'><span style='background-color: #FA6A43;border-radius: 12px;padding:0 4px;color:white'>"
				    +1+"</span></div>"
				    +         "<div class='media-desc'>" +  "系统消息" + "</div>"
				    +      "</div>"
				    +   "</div>"
				    +"</div>";

				 //新朋友
		 _item+= "<div class='' id='friends_"+10001+"' onclick='UI.isChoose(\"" + 10001 + "\");' style='border-bottom:1px solid #EEF0F5'>" 
					+	"<div class='media-main' onclick='UI.showNewFriends(" + 0+");'>"
					+      "<a href='javascript:(0)' class='pull-left media-avatar'>"
					+                 "<img onerror='this.src=\"img/ic_avatar.png\"' width='40' height='40' alt='' src=\"img/im_10001.png\"' class='roundAvatar'>"
				    +      "</a>"
				    +      "<div style='cursor: pointer;' class='media-body'>"
				    +         "<h5 class='media-heading'>" + "新的朋友" + "</h5><div id='msgNum_"+10001+"' class='news' style='float:right;display:none'><span style='background-color: #FA6A43;border-radius: 12px;padding:0 4px;color:white'>"
				    +1+"</span></div>"
				    +         "<div class='media-desc'>" +  "新朋友消息" + "</div>"
				    +      "</div>"
				    +   "</div>"
				    +"</div>";
		return _item;
	},
	createNewFriendsItem : function(obj) {
		var _item ="<div class='media-main'><a class='pull-left media-avatar' href='javascript:UI.showUser(" + obj.toUserId + ")'>"
								+		     "<img onerror='this.src=&quot;img/ic_avatar.png&quot;' width='40' height='40' alt='' src='"
								+			   myFn.getAvatarUrl(obj.toUserId) + "' class='media-object roundAvatar'>"
								+   "</a>"
								+   "<div style='cursor: pointer;' class='media-body'><div style='font-size:13px;'>" 
								+     obj.toNickname
								+    "</div>";

				if(0==obj.direction){
					
						/*我发送的*/
						if(500==obj.type||502==obj.type){
							_item=_item+"<div  colspan='3' style='color:#7E7979'>"
								+"等待验证</div>";
						}else if(503==obj.type){
							_item=_item+"<div width=100 style='color:#7E7979;display:inline'>等待验证</div>";
							
							/*+"<button class='btn btn-default' onclick='UI.showSeeHai(" + obj.toUserId + ")' width=100 style='float:right;margin-top:-15px;border:0;font-size:12px;background-color:#55CBC4;color:white'>"
							+"打招呼</div>";*/
							
						}else if(504==obj.type||505==obj.type){
							if(504==obj.type)
							_item=_item+"<div width=100 style='color:#7E7979'>已取消好友 </div>";
							if(505==obj.type)
							_item=_item+"<div style='color:#7E7979;display:inline'>已删除</div>";
							_item=_item+"<button class='btn btn-default' onclick='UI.addFriends(" + obj.toUserId + ")' width=100 style='border:0;float:right;margin-top:-15px;font-size:12px;background-color:#55CBC4;color:white;display:inline'>"
							+"加好友</button>";
							
						}else if(507==obj.type){
							_item=_item+"<div width=100 style='color:#7E7979;display:inline'>已拉黑</div>"
							 +"<button class='btn btn-default' onclick='UI.addFriends(" + obj.toUserId + ")' width=100 style='float:right;margin-top:-15px;border:0;font-size:12px;color:white;background-color:#55CBC4;display:inline'>"
							 +"移除黑名单</button>";
						}else {
							_item=_item+"<div style='color:#7E7979'>已互为好友 </div>";
						}
					
								
				}else {
					/*接受到的*/
					
						if(500==obj.type||502==obj.type){
						_item=_item+"<div style='color:#7E7979;display:inline'>"+obj.content+ "</div>"
								+"<button onclick='UI.addFriends(" + obj.toUserId + ")' class='btn btn-default' style='border:0;font-size:12px;display:inline;background-color:#55CBC4;float:right;margin-top:-15px;margin-left:10px;color:white'>"
								+ "同意</button>"
								+"<button onclick='UI.showReplySeeHai(" + obj.toUserId + ")' class='btn btn-default' style='border:0;font-size:12px;display:inline;background-color:#55CBC4;float:right;margin-top:-15px;color:white'>"
								+"回复</button>";
								
						}else if(503==obj.type){
							_item=_item+"<div width=100 style='display:inline;color:#7E7979'>申请加我为好友</div>"
							+"<button onclick='UI.addFriends(" + obj.toUserId + ")' class='btn btn-default' width=100 style='border:0;font-size:12px;display:inline;float:right;margin-top:-15px;background-color:#55CBC4;color:white'>"
							+"加好友</button>";
							
						}else if(504==obj.type||505==obj.type){
							_item=_item+"<div width=100 style='display:inline;color:#7E7979'>被取消了好友 </div>"
							+"<button onclick='UI.addFriends(" + obj.toUserId + ")' class='btn btn-default'  width=100 style='border:0;font-size:12px;display:inline;background-color:#55CBC4;float:right;margin-top:-15px;color:white'>"
							+"加好友</button>";
							
						}else if(507==obj.type){
							_item=_item+"<div width=100 style='display:inline;color:#7E7979'>被拉黑 </div>"
							+"<div  width=100 style='font-size:12px;display:inline'>"
							+"<a href='javascript:UI.addFriends(" + obj.toUserId + ");' >请求取消黑名单</a></div>";
							
						}else {
							_item=_item+"<div  style='color:#7E7979'>已互为好友</div>";
						}
						
				}

			_item+="</div></div>"	
				
		
		return _item;
	},
	msgWithFriend:function(msg){
		//处理 接受到的 好友验证类型消息
		if(501==msg.type)
			UI.showFriends(0);
		else if(504==msg.type||505==msg.type||507==msg.type){
			
			if(ConversationManager.fromUserId==msg.fromUserId){
					UI.hideChatBodyAndDetails();
			}
			UI.showFriends(0);
			$("#myMessagesList #friends_"+msg.fromUserId).remove();
		}
			

	},
	showMessages : function(){
		
		$("#o").show();
		/*$("#tabCon_0").show();*/
		$("#messagemyFriend").show();
		
		$("#back").hide();
		$("#prop").hide();
		/*$("#friends_10000").show();
		$("#friends_10001").show();*/
		$("#myMessagesList").show();
		$("#list").remove();

	   //UI.hideChatBodyAndDetails();
		$("#setPassword").hide();
		$("#privacy").hide();
		

	},
	showFriends : function(pageIndex) {

		$("#o").show();
		
		$("#prop").hide();
		
		UI.hideChatBodyAndDetails();
		$("#setPassword").hide();
		$("#privacy").hide();
		// $("#friend").addClass("back");

		if(myFn.isNil(pageIndex))
			pageIndex=0;
		$("#myFriendsList").hide();
		$("#myNearUserList").hide();
		mySdk.getFriendsList(myData.userId, null, 2, pageIndex, function(result) {

			//储存好友列表当前页码数，及当前页的好友数量
			myData.friendListPage = pageIndex;
			myData.friendListNum = result.pageData.length;
			//清空已记录的当前页中所有好友的userId
			myData.friendListUserIds=[];

			var html = "<table width='100%'>";
			for (var i = 0; i < result.pageData.length; i++) {
				var obj = result.pageData[i];
				//缓存好友
				DataMap.friends[obj.toUserId]=obj;
				//自己和黑名单好友不显示
				if(myData.userId==obj.toUserId||1==obj.blacklist)
					continue;
				//记录当前页中所有好友的userId
				myData.friendListUserIds.push(obj.toUserId);

				var imgUrl =myFn.getAvatarUrl(obj.toUserId);
				html += UI.createFriendsItem(imgUrl, obj.toUserId, obj.toNickname, '暂无签名');
				// tbFriendsListHtml += "<tr><td><img src='" + imgUrl + "' width=30 height=30 /></td><td width=100%>&nbsp;&nbsp;&nbsp;&nbsp;" + obj.toNickname
				// 		+ "</td><td><input id='userId' name='userId' type='checkbox' value='" + obj.toUserId + "' /></td></tr>";
			}
			html += "</table>"

			html += GroupManager.createPager(pageIndex, result.pageData.length, 'UI.showFriends');
			$("#myFriendsList").empty();
			$("#myFriendsList").append(html);
			$("#myFriendsList").show();
			
			$("#btnAttentionList").removeClass("border");
			$("#btnMyFriends").addClass("border");

		});
	},
	handoverImg : function(that){ //切换图标 和背景

		var otherPId = ["friend","messages","room","company","mydata","privacySet","pswmanage"];

		//切换背景操作
		var thisId = $(that).attr('id');
		//更改自己的背景
		$("#"+thisId+"").addClass("back");
		//将其它兄弟的背景还原
		for (var j = 0; j < otherPId.length; j++) {
			if (thisId == otherPId[j] ) {
				continue;
			}
			$("#"+otherPId[j]+"").removeClass("back");
		}
		Temp.leftTitle=thisId;
		//切换图标操作
		var imgId =$(that.children[0]).attr('id');
		//更改自己的图标
		$("#"+imgId+"").attr('src',"img/"+imgId+"1.png");
		//将其它兄弟的图标还原
		//所有 img 的Id
		var otherImgId = ["friendImg","messageImg","roomImg","compImg","myProfileImg","privacyImg","passwordImg"];
		for (var i = 0; i < otherImgId.length; i++) {
			var oImgId = otherImgId[i];
			if(imgId == oImgId){
				continue;
			}
			$("#"+oImgId+"").attr('src',"img/"+oImgId+".png");
		}

	},
	showAddFriendList : function(pageIndex){
		mySdk.getFriendsList(myData.userId, null, 2, pageIndex, function(result) {
			var userIds = Checkbox.parseData();//调用方法解析数据
			var tbFriendsListHtml = "<tbody>";
			var obj=null;
			var imgUrl=null;
			for (var i = 0; i < result.pageData.length; i++) {
				obj = result.pageData[i];
				imgUrl = myFn.getAvatarUrl(obj.toUserId);
				//这里的Id createGroupShow 表示已选展示区的id
				var inputHtml = "<input id='createGroupShow' name='userId' type='checkbox' value='" + obj.toUserId + "' onclick='Checkbox.checkedAndCancel(this)'/>";
				
				if(0 != userIds.length){
					for (var j = 0; j < userIds.length; j++) {
						var userId = userIds[j];
						if(obj.toUserId == userId){
							//$("#input_"+obj.toUserId+"").attr("checked",'checked'); 
							inputHtml = "<input id='createGroupShow' name='userId' type='checkbox' value='" + obj.toUserId + "' checked='checked' onclick='Checkbox.checkedAndCancel(this)'/>" 
						}
					}
				}
				tbFriendsListHtml += "<tr><td style='padding: 5px;'>"
				                  +     "<img onerror='this.src=\"img/ic_avatar.png\"'  src='" + imgUrl + "' width=30 height=30 class='roundAvatar' /></td>"
				                  +     "<td width=100%>&nbsp;&nbsp;&nbsp;&nbsp;" + obj.toNickname + "</td>"
				                  +     "<td>"
				                  +     inputHtml  
				                  +     "</td>"
				                  +  "</tr>";
			}
			tbFriendsListHtml += "</tbody>";
			var pageHtml = GroupManager.createPager(pageIndex, result.pageData.length, 'UI.showAddFriendList');
			$("#tbFriendsList").empty();
			$("#friends_page").empty();
			$("#tbFriendsList").append(tbFriendsListHtml);
			$("#friends_page").append(pageHtml);

		});
	},
	showNearbyUser : function(pageIndex) {
		/*$("#myFriendsList").hide();
		$("#myNearUserList").hide();*/
		myFn.invoke({
			url : '/nearby/user',
			data : {
				pageIndex : pageIndex,
				pageSize : 10,
				nickname : $("#key").val()
				/*longitude : myData.locateParams.longitude,
				latitude : myData.locateParams.latitude*/
			},
			success : function(result) {
				if (1 == result.resultCode) {
					var html = "<div id='nearbyUserList' width='96%'>";
					var obj=null;
					for (var i = 0; i < result.data.length; i++) {
						obj = result.data[i];
						html += "<table id='nearUser_"+obj.userId+"' onclick='UI.nearbyUserClick(" + obj.userId + ")' style='border-radius:6px;'><tr><td rowspan='2' width=54 height=54>"
								+    "<a href='#' style='margin-left:5px;'>"
								+       "<img onerror='this.src=&quot;img/ic_avatar.png&quot;' width='40' height='40' alt='' src='"+ myFn.getAvatarUrl(obj.userId) + "' class='roundAvatar'>"
								+   "</a></td>"
								+   "<td style='font-size:13px;'>" 
								+     obj.nickname
								+    "</td>"
								+    "<td rowspan='2' width=50 style='font-size:12px;'>";
						if(obj.userId != DataMap.allFriendsUIds[obj.userId])
							html += "<img width='18' height='18' alt='' src='img/addFriend.png'  onclick='UI.addFriends(" + obj.userId + ",event)' >";

							html += "</td><tr><td class='media-desc'>"
								+        (myFn.isNil(obj.desc) ? "暂无签名" : obj.desc) 
								+"</td></tr></tr></table>";
					}

					html += "</div>";
					html += GroupManager.createPager(pageIndex, result.data.length, 'UI.showNearbyUser');
					
					$("#_myNearUserList").empty();
					$("#_myNearUserList").append(html);
					$("#addfriend").modal("show");
					/*$("#myNearUserList").show();*/
				} else {
				}
			},
			error : function(result) {
			}
		});
	},
	nearbyUserClick : function(userId){
		//点击后改变背景
		$("#nearbyUserList #nearUser_"+userId+"").addClass("fActive");
     	$("#nearbyUserList #nearUser_"+userId+"").siblings().removeClass("fActive");
     	UI.showUser(userId);
	},
	/*showAttentionList:function(pageIndex){		
		$("#myFriendsList").hide();
		$("#myNearUserList").hide();
		
		mySdk.getFriendsList(null,null,1,0,function(result){
			var html = "<table width='100%'>";
			//var tbFriendsListHtml = "";
			var obj=null;
			var imgUrl =null;
			for (var i = 0; i < result.pageData.length; i++) {
				 obj = result.pageData[i];
				 imgUrl = myFn.getAvatarUrl(obj.toUserId);
				html += UI.createFriendsItem(imgUrl, obj.toUserId, obj.toNickname, '暂无签名');
				// tbFriendsListHtml += "<tr><td><img src='" + imgUrl + "' width=30 height=30 /></td><td width=100%>&nbsp;&nbsp;&nbsp;&nbsp;" + obj.toNickname
				// 		+ "</td><td><input id='userId' name='userId' type='checkbox' value='" + obj.toUserId + "' /></td></tr>";
			}
			html += "</table>"
			html += GroupManager.createPager(pageIndex, result.pageData.length, 'UI.showFriends');

			$("#myFriendsList").empty();
			$("#myFriendsList").append(html);
			$("#myFriendsList").show();
			$("#btnMyFriends").removeClass("border");
			$("#btnAttentionList").addClass("border");
			//$("#btnNearbyUser").removeClass("border");
			
		})
		
	},*/
	addFriends : function(toUserId,ev) {
		if(!myFn.isNil(ev)){
			ev.stopPropagation(); //阻止事件向父元素冒泡
		}
		mySdk.addAttention(toUserId,function(result){
				//关注成功 已互为好友
					$("#addfriend").modal('hide');
					$("#divNewFriendList").hide();
					if(2==result.type||4==result.type){
						UI.showFriends(0);
						friendRelation[toUserId] = true;

					}else {
						//打招呼
						UI.showAttentionList(0);

					}
					//关注成功后将数据储存到好友和单向关注userId 列表中
					DataMap.allFriendsUIds[toUserId] = toUserId;
					
					UI.showMessages();
					
					mySdk.getUser(toUserId,function(user){
						
						var imgUrl =myFn.getAvatarUrl(user.userId);
						friendHtml = UI.createFriendsItem(imgUrl, user.userId, user.nickname, user.description);
						$(friendHtml).insertAfter("#myMessagesList #friends_10001"); //加入到新朋友下方
						UI.isChoose(user.userId);
						UI.showMsgNum(toUserId);

						ConversationManager.open(user.userId + "@" + AppConfig.boshDomain,user.nickname);

						var msg=ConversationManager.createMsg(1,"你们已互为好友，请开始聊天吧");
						msg.fromUserId=toUserId;
						ConversationManager.showLog(msg);	
					});
					


					/*UI.showNewFriends(0);*/	
		});
		

	},
	refuseFriends:function(toUserId){
		//拒绝加好友
		var msg=ConversationManager.createMsg(XmppMessage.Type.REFUSED,"");
		UI.sendMsg(msg,myFn.getJid(toUserId));
	},
	newCreateRoome : function(){ //新建房间
		$("#createGroupShow").empty();  //清空已选好友列表
		Checkbox.cheackedFriends = {};  //清空储存的数据

		$('#newRoomModal').modal('show'); //显示弹框
		UI.showAddFriendList(0); //展示第一页的好友
		
	},
	showSetting:function() {
		$("#o").hide();
		UI.hideChatBodyAndDetails();
		$("#setPassword").hide();

		mySdk.getUser(myData.userId, function(result) {
			myData.user = result;
			var setting =myData.user.settings;
				if(1==setting.allowAtt)
					$("#allowAtt").attr('checked',true);
				if(1==setting.allowGreet)
					$("#allowGreet").attr('checked',true);
				if(1==setting.friendsVerify)
					$("#friendsVerify").attr('checked',true);


				$("#privacy").show();
				$("#prop").hide();

		});
		myFn.switchEncrypt();
		
	},
	showPwd:function() {
		/*$("#edit_pwd").modal('show');*/
		$("#o").hide();
		UI.hideChatBodyAndDetails();
		$("#privacy").hide();
		$("#prop").hide();
		$("#setPassword").show();

	},
	/*showLog:function(msg){
		if(msg.objectId==ConversationManager.fromUserId){
			var logHtml ="<div class='logContent' >"
						+"	<span>"+msg.content+"</span> "
						+"</div>";
			$("#messageContainer").append(logHtml);
			UI.scrollToEnd();
		}
		switch(msg.contentType){
			case:501
				alert("log");

		}
	},*/
	showMe:function() {

		$("#o").hide();
		UI.hideChatBodyAndDetails();
		$("#privacy").hide();
		$("#setPassword").hide();

		mySdk.getUser(myData.userId, function(result) {
			myData.user = result;

			$("#avatar_preview").attr("src", myFn.getAvatarUrl(myData.userId)+"?x="+Math.random()*10);
			$("#avatarUserId").val(myData.user.userId);
			$("#mynickname").val(myData.user.nickname);
			$("#mydescription").val(myData.user.description);
			$('input[id=sex][value=' + myData.user.sex + ']').attr('checked', true);
			$("#mybirthday").val(0 == myData.user.birthday ? "" : myFn.toDate(myData.user.birthday));
			$("#provinceId").val(myData.user.provinceId);
			$("#cityId").val(myData.user.cityId);
			$("#prop").show();
			UI.initDatepicker();
		});
	},
	showUser : function(userId) {

		mySdk.getUser(userId,function(result){
			var obj = result;
			Temp.toNickname=obj.nickname;
			Temp.toJid=myFn.getJid(obj.userId);
			var imgUrl=10000==userId?"img/im_10001.png":myFn.getAvatarUrl(obj.userId);
					var html = "<ul class='media-list'>"
							+     "<li style='' class='media'>"
							+         "<a href='javascript:;' style='cursor: pointer;' class='pull-left'>"
							+             "<img width='50' height='50' alt='' src='" + imgUrl + "' class='roundAvatar'>"
							+		  "</a>"
							+         "<div style='cursor: pointer;' class='media-body'>"
							+         	 "<h5 class='media-heading'>" + obj.nickname+ "</h5>"
							+         	 "<div class='media-desc'>" + obj.userId + "</div>"
							+         "</div>"
							+     "</li>"
							+  "</ul>"
							+ "<table class='table'><tr><td width='15%'>签名</td> <td width='80%'>"
							+ (myFn.isNil(obj.description) ? "无" : obj.description) + "</td></tr><tr><td>性别</td><td>" + (1 == obj.sex ? "男" : "女")
							+ "</td></tr><tr><td>生日</td><td>" + (myFn.toDate(obj.birthday)) + "</td></tr><tr><td>国家</td><td>中国</td></tr><tr><td>省份</td><td>"
							+ (0 == obj.provinceId||myFn.isNil(obj.provinceId) ? "" : TB_AREAS_C[obj.provinceId]) + "</td></tr><tr><td>城市</td><td>"
							+ (0 == obj.cityId||myFn.notNull(obj.cityId)  ? "" : TB_AREAS_C[obj.cityId]) + "</td></tr><tr><td>区县</td><td>"
							+ (0 == obj.areaId ? "" : TB_AREAS_C[obj.areaId]) + "</td></tr>"
							+"<tr class='addAttention'><td colspan='2'><button onclick='UI.addFriends(\"" + obj.userId+"\");' class='btn btn-default' style='background-color: #3A3F45;width: 60%;height:40px;margin-left:45px;font-size:16px;color:white'>加好友</button></td></tr>"
							+"<tr class='seeHai'><td colspan='2'><buttton onclick='UI.showSeeHai(\"" + obj.userId +"\");'  class='btn btn-default ' style='background-color: #55CBC4;width: 60%;height:40px;margin-left:45px;font-size:16px;color:white'>打招呼</button></td></tr>"
							+"<tr class='sendMsg'><td colspan='2'><button onclick='ConversationManager.open(\"" + (userId + "@" + AppConfig.boshDomain) + "\",\"" + obj.nickname + "\");' class='btn btn-default ' style='background-color: #3A3F45;width: 60%;height:40px;margin-left:45px;font-size:16px;color:white'>发消息</button></td></tr></table>";

					$("#userModalBody").empty();
					$("#userModalBody").append(html);
					$("#userModal").modal('show');

					mySdk.getFriends(obj.userId,function(data){
						if(myFn.isNil(data)){
							$("#userModalBody .seeHai").hide();
							$("#userModalBody .sendMsg").hide();
							$("#userModalBody .addAttention").show();
						}else if(1==data.blacklist){
							$("#userModalBody .seeHai").hide();
							$("#userModalBody .sendMsg").hide();
							$("#userModalBody .addAttention").hide();
						}else if(0==data.status){
							$("#userModalBody .seeHai").hide();
							$("#userModalBody .sendMsg").hide();
							$("#userModalBody .addAttention").show();

							/*$("#userModal .addAttention").click(function(){
								UI.addFriends(data.toUserId);
							});*/
						}else if(1==data.status){
							//已经关注 打招呼
							$("#userModalBody .seeHai").show();
							$("#userModalBody .addAttention").hide();
							$("#userModalBody .sendMsg").hide();
						}else{
							//已经是好友了
							$("#userModalBody .addAttention").hide();
							$("#userModalBody .seeHai").hide();
							$("#userModalBody .sendMsg").show();
						}
					});
		});
	},
	//文件上传
	upload:function(){
		//$("#myfile")[0].files[0]=e.files[0];
		var file= $("#myfile")[0].files[0];
		/*if(file==undefined){
			file=e.files[0];
		}
		alert(file);
		alert(e);*/
		//$("#myfile").val($("#box").val());
		//alert($("#myfile")[0].files[0]);
		//alert($("#myfile").val());
		Temp.file=file;
		var filesize =file.size/1024/1024;
			if(filesize > 20){
                ownAlert(3,"文件大小超过限制，最多20M");
                return false;
            }
            $("#filePath").val(file.name);
            $("#filePath").attr("size",file.size);
            $("#filePath").after("<img id='loadingimg' src='img/loading.gif'>");
			$("#uploadFileFrom").ajaxSubmit(function(data) {//成功
				$("#loadingimg").hide();
		          var obj = eval("(" + data + ")");
				if (!myFn.isNil(obj.url)) {
					Temp.file.url=obj.url;
					if(Temp.uploadType=="sendImg")
						$("#myImgPreview").attr("src",obj.url);
					else ownAlert(3,"上传成功!");
					$("#myFileUrl").val(obj.url);
					
				}    
		  });
	},
	//头像上传
	uploadPhoto:function(docObj){
		//判断图片后缀是否为png或jpg
		var f = $(docObj).val();
	    f = f.toLowerCase();
	    var strRegex = ".png|jpg|jpeg$";
	    var re=new RegExp(strRegex);
	    if (!re.test(f.toLowerCase())){
	    	ownAlert(2,"请选择正确格式的图片");
	    	return;
	    }

		$("#avatarForm").ajaxSubmit(function(data) {//成功
		        var obj = eval("(" + data + ")");
				var obj = eval("(" + data + ")");
			if (1 == obj.resultCode) {
				ownAlert(1,"头像上传成功！");
				$("#avatar_preview").attr("src", obj.data.oUrl+"?x="+Math.random()*10);
				$("#photo #myAvatar").attr("src", obj.data.oUrl+"?x="+Math.random()*10);
				$("#avatar_preview").show();
			}else 
			 	ownAlert(3,"上传头像失败!");  
		  });
	},
	showSeeHai : function(userId) {
		//$("#userModal").modal('hide');
		$("#divSeeHai #seeText").val("你好,我是"+myData.nickname);
		$("#divSeeHai").modal('show');
	},
	showReplySeeHai : function(userId) {
		Temp.toJid=myFn.getJid(userId);
		$("#divReplySeeHai #replyText").val("你是？");
		$("#divReplySeeHai").modal('show');
	},
	quit : function() {
		window.wxc.xcConfirm("是否确认退出登录？", window.wxc.xcConfirm.typeEnum.confirm,{
			onOk:function(){
				myConnection.disconnect("");
				$.cookie("telephone", "");
				$.cookie("password", "");
				$.cookie("loginData", "");
				window.location.href = "login.html";
			}
		});

	},
	help:function(){
		ownAlert(3,"尊敬的用户你好，web版已升级到2.0版，欢迎您的使用!<br/>深圳市视酷信息技术有限公司出品");
	},
	showBlackList : function(){ //黑名单
		/*$("#myFriendsList").hide();
		$("#myNearUserList").hide();*/
		$("#blackListManager #blackList").empty();//情况数据
		var isEmpty = true; //是否为空
		var blackListHtml="";
		$("#myFriendsList").empty();
		for(var key in DataMap.blackListUIds){ //遍历黑名单map
			var blackUserId = DataMap.blackListUIds[key];
			isEmpty = false;
			mySdk.getUser(blackUserId, function(data){

			 blackListHtml+= "<table id='blackList'><tr id='blacklist_"+data.userId+"' style='border-bottom:1px solid #eeeeee;margin-left:10px'>"
					  		+	"<td  width=54 height=54>"
					  		+		"<a href='javascript:UI.showUser(" + data.userId + ")'>"
							+			"<img onerror='this.src=&quot;img/ic_avatar.png&quot;' width='40' height='40' src='"+myFn.getAvatarUrl(data.userId) + "' class='media-object roundAvatar'>"
							+   	"</a>"
							+   "</td>"
							+   "<td width='150'><p style='font-size:13px; width:10em;' class='textFlow'>" 
							+     data.nickname
							+   "</p></td>"
							+   "<td width='100'>被拉黑 </td>"
							+	"<td  width='100' style='font-size:12px;'>"
							+		"<a href='javascript:mySdk.deleteBlacklist(" + data.userId + ");'>移除黑名单"
							+   "</a></td></tr></table>";

					/*$("#blackListManager #blackList").append(blackListHtml);*/
					$("#blackList").empty();
					if(blackListHtml==null){
						return;
					}else{
						$("#myFriendsList").append(blackListHtml);
					}
					


			});
		}
		
		
		if(isEmpty){ //显示暂无数据
			$("#myFriendsList").empty();
			$("#myFriendsList").append("<div style='text-align:center;margin-top:60px;'><img src='img/noData.png' style='width:40px;margin-left:10px'><span style='margin-left:20px; color:#716C6C;; font-size:16px;'>暂无数据</span></div>");
		}
		$("#btnMyFriends").removeClass("border");
		$("#btnAttentionList").addClass("border");	
		/*$("#blackListManager").modal('show');*/
		


	},
	isChoose : function(userId){ //好友列表选中状态切换
     $("#friends_"+userId+"").addClass("fActive");
     $("#friends_"+userId+"").siblings().removeClass("fActive");
     changeTab('0','msgTab'); //恢复顶部切换按钮的状态

	},
	showImgZoom : function(src,messageId) {
		$("#imgZoomBody").empty();
		var imgHtml = "<img src='"+src+"' style='max-width: 100%; max-height: 100%;'>";
     	$("#imgZoomModal").modal('show');
     	$("#imgZoomBody").append(imgHtml);
     	if(!myFn.isNil(messageId)){ //阅后即焚消息
			$("#messageContainer #msg_"+messageId+" #chat_content").empty();
			$("#messageContainer #msg_"+messageId+" #chat_content").append("<img src='img/delete.gif'  style='max-width:100%;'/>");

			//将此条阅后即焚消息从缓存消息中删除
			myFn.deleteReadDelMsg(messageId);
		}

	},
	showNewFriends:function(pageIndex){
		Temp.nowList="NewFriend";

			mySdk.getNewFriendsList(myData.userId, pageIndex, function(result) {
				var html = "<div id='list'>";
				//var tbFriendsListHtml = "";
				var toUserId=null;
				for (var i = 0; i < result.length; i++) {
					var obj = result[i];
					toUserId=obj.toUserId;
					if(obj.toUserId==myData.userId)
					toUserId=obj.userId;
					if(507==obj.type){ //排除黑名单用户
						continue;
					}
					html += UI.createNewFriendsItem(obj);
					
				}
				html += GroupManager.createPager(pageIndex, result.length, 'UI.showFriends');
				html +="<div>"
				
				$("#myMessagesList").hide();
				$("#messagemyFriend").hide();
				
				$("#back").show();
				
				$("#divNewFriendList").html(html);
				$("#divNewFriendList").show();
				
				UI.clearMsgNum(10001);

		});
		
	},
	changeDetailsBtn : function(userId,type,friend){ //改变详情页面的按钮 type:0表示不是好友  type:1  表示已经互为好友  type:-1 //加入黑名单
		var detailsHtml =null;
		var blacklist=0;
		if(myFn.isNil(type)){
			if(myFn.isNil(friend))
				type=0;
			else if(2==friend.status){
				type=1;
			}else{
				blacklist=friend.blacklist;
				type=-1;
			}

		}
		if(10000==userId) 
			return;
		else if(type==-1){ //黑名单
			$("#tabCon_1 #friendDetailsBtn").empty();
			detailsHtml = '<div>'
							 + '</div>'
							 + '<div style="text-align:center">'
							 +	"<button  onclick='mySdk.deleteFriends(\"" + userId+"\");' class='btn btn-danger' style='height: 50px;width: 60%;margin-left: 20%;margin-top:30px'>删除好友</button>"
							 + '</div>';
			detailsHtml =detailsHtml+'<div>'
											 +	"<button  onclick='mySdk.deleteBlacklist(\"" + userId+"\");' class='btn btn-danger' style='height: 50px;width: 60%;margin-left: 20%;margin-top:30px'>取消黑名单</button>"
											 +'</div>';
							
			$("#tabCon_1 #friendDetailsBtn").append(detailsHtml);
			return;
		}else if(type==1){ //好友
			$("#tabCon_1 #friendDetailsBtn").empty();
			detailsHtml = '<div>'
								/**
							 +	"<button onclick='mySdk.deleteAttention(\"" + userId+"\");' class='btn btn-danger' style='height: 50px;width: 60%;margin-left: 20%'>取消关注</button>"
							 */
							 + '</div>'
							 + '<div style="text-align:center">'
							 +	"<button  onclick='mySdk.deleteFriends(\"" + userId+"\");' class='btn btn-danger' style='height: 40px;width: 30%;margin-top:30px'>删除好友</button>"
							 + '</div>';

			if(1==blacklist)
				detailsHtml =detailsHtml+'<div style="text-align:center">'
								 +	"<button  onclick='mySdk.deleteBlacklist(\"" + userId+"\");' class='btn btn-danger' style='height: 40px;width: 30%;margin-top:30px'>取消黑名单</button>"
								 +'</div>';
			else 
				detailsHtml =detailsHtml+'<div style="text-align:center">'
								 +	"<button  onclick='mySdk.addBlacklist(\"" + userId+"\");' class='btn btn-danger' style='height: 40px;width: 30%;margin-top:30px'>加入黑名单</button>"
								 +'</div>';
		}else if(type==0){ //非好友
			$("#tabCon_1 #friendDetailsBtn").empty();
			detailsHtml = '<div style="text-align:center">'
							+	"<button  onclick='UI.addFriends(\"" + userId+"\");' class='btn btn-default detailsBtn' style='width:30%;color:white'>加好友</button>"
							 + '</div>'
							 +'<div style="text-align:center">'
							 +  "<button onclick='UI.showSeeHai(\"" + userId +"\");'  class='btn btn-default detailsBtn' style='width:30%;color:white'>打招呼</button>"
							 +'</div>';
		}
		
		$("#tabCon_1 #friendDetailsBtn").append(detailsHtml);
	

	},
	hideChatBodyAndDetails:function(){
		//隐藏右侧界面  聊天界面 和好友详情界面
		//
		ConversationManager.isOpen=false;
		ConversationManager.isGroup=0;
		ConversationManager.from="";
		$("#tabCon_2").hide();
		$("#tabCon_1").hide();
		$("#tabCon_0").hide();
		$("#tab").hide();

	},
	showDetails:function(from,type,name){
				$("#messageContainer").empty();
				$("#messageBody").empty();
				$("#chatTitle").empty();
				$("#desname").empty();
				$("#dessign").empty();
				$("#dessex").empty();
				$("#gname").empty();
				$("#gnickname").empty();
				$("#gnotice").empty();
				$("#creator").empty();
				$("#desbirthday").empty();
				$("#descountry").empty();
				$("#gdesc").empty();
				$("#gcreateTime").empty();
				
				
				$("#desname").append(name);
				$("#tab").show();
				$("#tabCon_1").hide();
				$("#tabCon_2").hide();
				$("#tabCon_0").show();
				$("#word").addClass('word');
				$("#details").removeClass('word');
				$("#chatPanel").show();
				var fromUserId = myFn.getUserIdFromJid(from);
				
				if (1 == type) { //判断是单聊还是群聊
					ConversationManager.showAvatar(fromUserId);//显示聊天窗口顶部头像(单聊)
					$("#chatTitle").append("<span>"+name+"</span><span id='state'>(离线)</span>");
					mySdk.getUserOnLine(fromUserId,function(status){
						if(status==1){
							$("#state").empty();
							$("#state").append("(在线)");
						}
					});
					mySdk.getUser(fromUserId,function(user){

						/*$("#chatTitle").append((1==user.onlinestate?"(在线)":"(离线)"));*/
						$("#desprovince").empty();
						$("#dessex").append((1 == user.sex ? "男" : "女"));
						if(user.description==""){
							$("#dessign").append("暂无签名");
						}else{
							$("#dessign").append(user.description);
						}
						if(user.countryId==1){
							$("#descountry").append("中国");
						}
						$("#desbirthday").append(myFn.toDate(user.birthday));
						$("#desprovince").append(TB_AREAS_C[user.provinceId]);
						delete DataMap.userMap[user.userId]; //清除缓存数据
						// UI.changeDetailsBtn(user.userId,null,user.friends);
						UI.changeDetailsBtn(user.userId,1);//出现在好友列表中的数据均为好友关系
					});
					$("#g_toolbar").hide();
					$("#details").click(function(){
							changeTab(1,"detailsTab");
					});
				} else { //群聊
					$("#chatTitle").append(name);
					var objectId=fromUserId;
					mySdk.getRoom(GroupManager.roomData.id,function(result){
							//给群组详情界面添加群主头像
							var imgUrl = myFn.getAvatarUrl(result.userId);
							$("#tabCon_2 #desphoto").empty();
							$("#tabCon_2 #desphoto").append("<img onerror='this.src=\"img/ic_avatar.png\"' src="+imgUrl+" style='margin-left: 50px;width:40px; height:40px;'  class='roundAvatar'>");
							$("#gdesc").append(result.desc);
							$("#creator").empty();
							$("#creator").append(result.nickname);
							$("#gcreateTime").append(myFn.toDate(result.createTime));
							$("#gname").append(result.name);
							if(myFn.notNull(result.notice)&&myFn.notNull(result.notice.text)){
								$("#gnotice").append(result.notice.text);
							}else{
								$("#gnotice").append("暂无公告");
							}
							
						});
					mySdk.getMember(GroupManager.roomData.id,myData.userId,function(result){
							$("#gnickname").append(result.nickname);
					});
					$("#g_toolbar").show();
					$("#details").click(function(){ //点击详情按钮
							changeTab(2,"detailsTab");	
					})
				}

	},
	moveFriendToTop : function(fromUserId,fromUserName,showNum,isGroup) { 
			//将发送消息的好友移动到消息列表顶部
			//然后显示消息提醒
		var friendHtml=null;
		//No1 将发送消息的好友移动到新朋友的下方
		
		var i=0;
		if(1==isGroup)
			 friendHtml = $("#myMessagesList #groups_"+fromUserId).prop("outerHTML");
		else
			 friendHtml = $("#myMessagesList #friends_"+fromUserId).prop("outerHTML");
		//判断发送消息的好友是否在当前页中
		if(myFn.isNil(friendHtml)){ //不存在
			if(i==0){
				i=1;
				//创建好友的html
				if(1==isGroup){//群组的
					/*var roomId=DataMap.myRooms[fromUserId].id;
					var room=DataMap.rooms[roomId];
					friendHtml= GroupManager.createMyItem(room);
						$(friendHtml).insertAfter("#myMessagesList #friends_10001"); //加入到新朋友下方
						if(1==showNum)
							UI.showMsgNum(fromUserId);	*/
					
				}else{//好友的
					var imgUrl =myFn.getAvatarUrl(fromUserId);
					if(myFn.isNil(fromUserName))
						fromUserName=DataMap.friends[fromUserId].toNickname;
					friendHtml = UI.createFriendsItem(imgUrl, fromUserId, fromUserName, "");

					$(friendHtml).insertAfter("#myMessagesList #friends_10001"); //加入到新朋友下方
					if(1==showNum)
						UI.showMsgNum(fromUserId);
					
			   }
		   }
		   i=0;
		}else{ //存在 则直接加入到新朋友下方
			if(1==isGroup)
				$("#myMessagesList #groups_"+fromUserId).remove();
			else
			    $("#myMessagesList #friends_"+fromUserId).remove();
			$(friendHtml).insertAfter("#myMessagesList #friends_10001");
			if(1==showNum)
				UI.showMsgNum(fromUserId);	
		}
		
	},
	sendCard : function(){ //发送名片
		Checkbox.cheackedFriends = {};  //清空储存的数据
		Temp.friendListType="sendCard";
		UI.loadFriendList(0);
	},
	loadFriendList : function(pageIndex,cb){ //加载好友列表，带翻页和记忆上一页的数据功能
	    var tbInviteListHtml = "";
		mySdk.getFriendsList(myData.userId,null,2, pageIndex, function(result) {
			var friendsList = result.pageData;
			var obj=null;
			var choosedUIds = Checkbox.parseData(); //调用方法获取已勾选的好友
			for(var i = 0; i < friendsList.length; i++){
				 obj = friendsList[i];
				 var inputItem = "<input id='false' name='invite_userId' type='checkbox' value='" + obj.toUserId + "' onclick='Checkbox.checkedAndCancel(this)'/>";
				 if(0 != choosedUIds.length){
					 for (var j = 0; j < choosedUIds.length; j++) {
					 	 cUId = choosedUIds[j]
					 	 if(obj.toUserId == cUId){
					 	 	inputItem = "<input id='false' name='invite_userId' type='checkbox' checked='checked' value='" + obj.toUserId + "'  onclick='Checkbox.checkedAndCancel(this)'/>";
					 	 }
				 	}	 
				 }
				tbInviteListHtml += "<tr><td><img onerror='this.src=\"img/ic_avatar.png\"' src='" + myFn.getAvatarUrl(obj.toUserId)
				+ "' width=30 height=30 class='roundAvatar'/></td><td width=100%>&nbsp;&nbsp;&nbsp;&nbsp;" + obj.toNickname
				+ "</td><td>"+inputItem+"</td></tr>";
			}
			var pageHtml = GroupManager.createPager(pageIndex, result.pageData.length,'UI.loadFriendList');
			$("#card #cardPage").empty().append(pageHtml);
			$("#friends").empty();
			$("#friends").append(tbInviteListHtml);
			$("#card").modal('show');
		});
	},
	searchFriendAndMember:function(){
		//搜索好友 和搜索群成员
		var keyword=$("#divFriendList #keyword").val();
		if(myFn.isNil(keyword)){
			ownAlert(3,"请输入搜索关键字");
			return;
		}
		if("@Member"==Temp.friendListType){
			mySdk.getMembersList(GroupManager.roomData.id,keyword,function(result){
	     		Temp.MemberHttping=null;
	     		var tbInviteListHtml = "";
	            var obj=null;
	            for(var i = 0; i < result.length; i++){
	                 obj = result[i];
	                tbInviteListHtml += "<tr><td><img onerror='this.src=\"img/ic_avatar.png\"' src='" + myFn.getAvatarUrl(obj.userId)
	                + "' width=30 height=30 /></td><td width=100%>&nbsp;&nbsp;&nbsp;&nbsp;" + obj.nickname	
	                + "</td><td><input id='divFriendListSelect'  type='checkbox' value='" + obj.userId +"' nickname='"+obj.nickname+"' "
	                + " onclick='Checkbox.checkedAndCancel(this)'  />"
	                + "</td></tr>";
	            }
	            $("#friendlist").empty();
	            $("#friendlist").append(tbInviteListHtml);
	            $("#divFriendList").modal('show');
       		});
		}
	}

};