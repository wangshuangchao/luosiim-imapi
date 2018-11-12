<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
<meta http-equiv="Content-Type" content="audio/amr" />
<meta http-equiv ="proma" content = "no-cache"/>
<meta http-equiv="cache-control" content="no cache" />
<meta http-equiv="expires" content="0" />

<title>视酷即时通讯群聊</title>
<link href="assets/css/bootstrap.css" rel="stylesheet" />
<link href="assets/css/datetimepicker.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" media="screen and (max-device-width:400px)" href="tinyScreen.css"/>
<link rel="stylesheet" type="text/css" media="screen and (min-width: 400px)and (max-device-width: 600px)" href="smallScreen.css" />
<link href="http://cdn.bootcss.com/bootstrap/3.3.4/css/bootstrap.min.css" rel="stylesheet">
<link href="http://cdn.bootcss.com/bootstrap-select/1.6.3/css/bootstrap-select.min.css" rel="stylesheet">
<link href="http://cdn.bootcss.com/bootstrap-datepicker/1.4.0/css/bootstrap-datepicker.min.css" rel="stylesheet">

<link href="css/_coolchat.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="css/xcConfirm.css"/> <!-- 自定义弹框    -->
<link rel="stylesheet" href="assets/js/contextMenu/dist/jquery.contextMenu.css"  type="text/css" />
<link rel="icon"  href="img/shiku.ico"/>

<!--聊天界面滚动条相关 -->
<link rel="stylesheet" href="scrollbar/nanoscroller.css">

<!-- 聊天界面滚动条相关 -->
<script type="text/javascript" src="scrollbar/jquery.nanoscroller.js"></script>
<script src="assets/js/jquery-3.2.1.min.js" type="text/javascript"></script>


<script src="assets/js/jquery.cookie.js" type="text/javascript"></script>
<script src="assets/js/bootstrap.min.js" type="text/javascript"></script>

<script src="assets/js/echarts.js" type="text/javascript"></script>
<script src="assets/js/bootstrap-datetimepicker.min.js" type="text/javascript"></script>
<!-- 文件上传 -->
<script src="assets/js/jquery.form.js" type="text/javascript"></script>

<script src="http://cdn.bootcss.com/bootstrap-select/1.6.3/js/bootstrap-select.min.js" type="text/javascript"></script>
<script src="http://cdn.bootcss.com/bootstrap-datepicker/1.4.0/js/bootstrap-datepicker.min.js"></script>
<script src="http://cdn.bootcss.com/bootstrap-datepicker/1.4.0/locales/bootstrap-datepicker.zh-CN.min.js"></script>

<script src="assets/js/artdialog/artDialog.source.js?skin=simple" type="text/javascript"></script>
<script src="js/strophe.js" type="text/javascript"></script>
<script src="js/tb_areas.js" type="text/javascript"></script>
<script src="js/xmpp-message.js" type="text/javascript"></script>


<!-- 系统相关的一下公用 方法 -->
<script src="js/commons.js" type="text/javascript"></script>
<!-- 群聊相关的  -->
<script src="js/_coolchat-group.js" type="text/javascript"></script>
<!-- 配置相关  数据储存相关  -->
<script src="js/_coolchat-commons.js" type="text/javascript"></script>
<!-- 里面放的都是一些页面要调用的方法  js-->
<script src="js/chat-function.js" type="text/javascript"></script>
<!--单聊 聊天有关的  js-->
<script src="js/_coolchat.js" type="text/javascript"></script>
<!--聊天界面 UI 界面处理 相关的 -->
<script src="js/chat-ui.js" type="text/javascript"></script>
<!--聊天界面 index 页面 初始化相关的 -->
<script src="js/chat-init.js" type="text/javascript"></script>
<!--调用接口相关的 Js -->
<script src="js/chat-sdk.js" type="text/javascript"></script>


<!--组织架构相关 Js -->
<!-- <script src="assets/js/jquery.jmp3.js" type="text/javascript"></script>
<script src="js/xcConfirm.js" type="text/javascript"></script>  --><!-- 自定义弹框    -->

<!-- &callback=mapInit -->

 <!-- <script type="text/javascript" src="https://api.map.baidu.com/api?v=2.0&ak=tVZRqzVYKwbwX7NytFnDWAUh4RbMnPDL"></script> -->
 <!-- 
 <script  type="text/javascript" src="js/baidu-map.js"></script>  -->

<!-- <script src="assets/js/contextMenu/src/jquery.contextMenu.js" type="text/javascript"></script> -->
<script src="assets/js/contextMenu/dist/jquery.ui.position.min.js" type="text/javascript"></script>
 
<!-- <script src="js/contextMenu.js" type="text/javascript"></script> -->
<!-- 视频播放器 -->
<link rel="stylesheet" href="assets/dist/plyr.css">
<script src="assets/dist/plyr.js"></script>


<!-- [if lt IE 9] -->
<script src="http://cdn.bootcss.com/html5shiv/3.7.2/html5shiv.min.js"></script>
<script src="http://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>
<!--[endif]-->
<style type="text/css">
	body{
		background-color: #EEF0F5;
	}
	*{
		transition: 0.4s;
		-webkit-transition: 0.4s;
	}
	
</style>

</head>

<body>
	<div id="userId" style="display: none">${wxUser.wxuserId}</div>
	<div id="openId" style="display: none">${wxUser.openId}</div>
	<div id="nickname" style="display: none">${wxUser.nickname}</div>
	<div id="imgurl" style="display: none">${wxUser.imgurl}</div>
	<div class="container" style="width:100%;height: 100%">
		<div id="tabCon_0" class="col-sm-12 " style="padding: 0;">
			<div id="chatPanel" class="" style="background-color: #EEEEEE; border-radius: 0;">
				<div class="panel-heading" style="background-color: #EEEEEE; vertical-align:middle;border-bottom: 1px solid #d6d6d6">
					<div style="height:40px">
						<div  class="headContent"></div>
						<div class="headContent"><h3 class="panel-title" style="margin-top: 15px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;max-width: 150px;">视酷IM公众号群聊</h3></div>
						<div class="headContent hint" style="display: none;" id="chatHint"></div>
					</div>
				</div>
				<div class="row panel-body" style="padding: 0px;height: 100%">
					<div id='messagePanel' class="nano" style="border-bottom: 1px solid #d6d6d6;background-color: #F2F4F7; height: 500px; overflow: hidden;">
						<div class="nano-content">
							<div id="messageContainer" style="overflow: hidden;"></div>
							<div id="messageEnd" style="height: 0px; overflow: hidden"></div>
						</div>
						

					</div>
					
					<div id="emojl-panel" class="emojl-panel" style="display: none;">
						<div id="emojiList" style="height: 160px;overflow-y:auto;"></div>
						<!-- <div id="gifList" style="display: none;height: 160px; overflow-y:auto;"></div> -->
					</div>

					<div id="gif-panel" class="emojl-panel" style="display: none;">
						<div id="gifList" style="height: 160px; overflow-y:auto;"></div>
					</div>
					<!-- <div style="border:1px solid black;display: inline;"></div> -->
					
					<div style="margin: 8px; cursor: pointer; color: blue; font-size: 12px;">
						<span id="btnEmojl">
							<img id="img1" alt="" src="img/emoji.png">
						</span> &nbsp;
						<!-- <span id="btnGif">
							<img id="gif" alt="" src="img/gif.png">
						</span> &nbsp; -->
						<span id="btnImg">
								<img alt="" src="img/p.png">
						</span>&nbsp;
						<!-- <span id="btnSipCall">
							<img id="sipCall" alt="" src="img/call.png">
						</span> &nbsp; -->
						<span id="btnFile">
							<img alt="" src="img/wj.png">
						</span>&nbsp;
						<!-- <span id="btnmin" onclick="UI.sendCard(0)">
							<img alt="" src="img/mingping.png">
						</span>&nbsp; -->
						<!-- <span id="place">
							<img src="img/wz.png">
						</span>&nbsp; -->
						<!-- <span id="redback">
							<img style="margin-top: 5px" src="img/red.png">
						</span> -->
						
					</div>
					<textarea class="form-control Input_text" id="messageBody" style="padding: 0"></textarea>

					<div style="margin-top: 15px; float: right;">
						<button id="btnSend" type="button" class="btn sou" style="color: white; margin:10px 10px;">发送（Ctrl+Enter）</button>
					</div>
				</div>
			</div>
		</div>

		
	

	

	<!--消息转发-->
	<div id="msgTranspond" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog" style="width: 400px;">
			<div class="modal-content">
				<div class="modal-header" style="padding: 5px 5px 0 0; border:0;">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					<div style="width: 100%;margin-left:10px;">
						<div class="modal-title transpond" id="friendTitle" style="color: #FFFFFF;" onclick="Transpond.toggleFriendOrGroup('friend')">好友</div>
						<div class="modal-title transpond" id="groupTitle" style="margin-left: -10px;color: #FFFFFF;" onclick="Transpond.toggleFriendOrGroup('group')">群组</div>
					</div>
				</div>
				<div class="modal-body" style="margin-top:22px;padding-top:0;">
					<table width="100%" id="search">  
						<tr>
							<td width="100%">
								<input id="keyword" type="text" placeholder="" class="form-control" style="border-radius: 5px 0 0 5px;height: 33.5px;margin-top: 0.6px;">
							</td>
							<td style="margin-bottom: 5px;">
								<button id="keywordSearchFrend" type="button" class="searchButten sou" onclick="Transpond.search('friend')">
									<img width="18" height="18" src="img/search.png" >搜索
								</button>
							</td>
						</tr>
					</table>

					<div style="border-left: 1px solid #ddd; border-right: 1px solid #eeeeee; border-bottom:1px solid #eeeeee; height: 480px; overflow-y: auto;">
						<table class="table"> <tbody id="friendList"> </tbody> </table>
						<table class="table"> <tbody id="groupList" style="display: none;"></tbody> </table>
					</div>
					<div id="pageFriend"></div>
					<div id="pageGroup" style='display: none;'></div>
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn sou" onclick="Transpond.confirmTranspond()">确认发送</button>
				</div>
			</div>
		</div>
	</div>

	

<!-- 
	<div id="userModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog" style="width: 100%;max-width: 400px;margin-top: 60px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					<h4 class="modal-title" id="myModalLabel">详细信息</h4>
				</div>
				<div class="modal-body" id="userModalBody"></div>
				<div class="modal-footer"></div>
			</div>
		</div>
	</div> -->
	
	
	<div id="uploadFileModal" class="modal fade" tabindex="-1" role="dialog"  aria-hidden="true">
			
			<!-- 上传文件操作 标识  sendImg 发送图片  sendFile 发送文件  uploadFile 群文件上传 -->
			 <!-- <input id="uploadType" name="uploadType" type="hidden" value="sendImg" /> -->

			<div class="modal-dialog" style="width: 100%;max-width: 400px;margin-top: 60px;">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
						<h4 class="modal-title" id="uploadModalLabel">文件上传</h4>
					</div>
					<div class="modal-body">
					<form id="uploadFileFrom" action="" method="post" enctype="multipart/form-data">
							<input id="uploadUserId"  type="hidden" name="userId" value="" /><br>
							<input id="myfile" name="file" type="file" accept="image/*" style="width: 300px;display: none;" onchange="UI.upload()"> 
							<img id="icon" src="img/uploadImg.png" style="/*width: 80px;max-height: 150px;*/margin: 0 auto;display: inline;" onclick="myFn.getPicture()">
							<!-- <div id="box" style="width:200px;height:200px;border:1px dashed black;">
								拖拽到这里上传
							</div> -->
					</form>

						<img id="myImgPreview" src="https://plus.yixin.im/res/default/card.png?8fa89eb5c07677f613cb2552cffe8280"
							 style="width: 120px;" /> 
						<input id="myFileUrl" name="myFileUrl" type="hidden" />
						<input id="filePath" name="filePath" type="hidden" />
					</div>
					<div class="modal-footer">
						<button type="button" class="btn sou" id="btnSendFileCancel">取消发送</button>
						<button type="button" class="btn sou" id="btnSendFileOK">确认发送</button>
					</div>
				</div>
			</div>
	</div>
	
	

	<!-- 图片缩放 -->
	<div id='imgZoomModal' class='modal fade' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true' >
		<div class='modal-dialog' style="max-width: 1500px; max-height: 1150px;margin-top: 60px">
			<div class='modal-content' style="background: #ffffff;">
				<div  class='modal-header' style="border:0;">   <!-- class='modal-header' -->
					<button type='button' class='close' data-dismiss='modal' aria-hidden='true' style="height: 20px; width: 20px;">&times;</button>
				</div>
				<div id ='imgZoomBody' class='modal-body' style="text-align: center;">
					
				</div>
			</div>
		</div>
	</div>

	
	
	<!--音视频-->
	<!-- data-backdrop="static" -->
	<div id="divSipml5" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" 
			aria-hidden="true" >
		
		<div class="modal-dialog" style="width:100%;height:100%;align:center;"> 
			<iframe  id="sipml5" class="modal-content" src="sipml5/call.html" scrolling="no"
					style="width:40%;height:90%;padding:0.2%;margin-left:30%;">
			</iframe>
		</div>
	</div>
	

	
</div>



	
<script type="text/javascript">
	
	document.getElementById("messageBody").onkeydown=function(e){
		e=e||window.event;
		 if(e.ctrlKey && e.keyCode == 13){
		 		var content = $("#messageBody").val();
				if (myFn.isNil(content)) {
					ownAlert(3,"请输入要发送的内容");
					return;
				}
				var msg=ConversationManager.createMsg(1, content);
				UI.sendMsg(msg);
    			
		 }
	}
	var user=null;
	var userId=$("#userId").html()+"@im.shiku.co";
	var openId=$("#openId").html();
	/*openId=$("#openId").val();*/
	/* alert(openId);
	alert(userId); */
	$(function(){
		alert(openId);
		/* myData.userId="10010323";
		myData.nickname="风度"; */
		myConnection = new Strophe.Connection(AppConfig.boshUrl);
		console.log("xmpp开始链接-----");
		myConnection.connect(userId, openId, function(status) {
			if (status == Strophe.Status.CONNECTED) {
				myConnection.send($pres().tree());
				myConnection.xmlInput = ConversationManager.receiver;
				//callback();
				console.log("xmpp连接成功-----");
				GroupManager._XEP_0045_037("1ebab36134ff4e5e893387b4bb1dce7a","10010323");
				//$("#myonline").html("(在线)");
			} else if (status = Strophe.Status.CONNECTING) {
				console.log("xmpp连接中 。。。");
				$("#myonline").html("(连接中)");
				//15s 后链接成功显示离线
				setTimeout(function(){
					if(!myConnection.connected)
						$("#myonline").html("(离线)");
				},15000);
			} else if(status = Strophe.Status.CONNFAIL){
				console.log("xmpp连接被断开或失败 。。。 CONNFAIL");
				$("#myonline").html("(离线)");
			} else if(status = Strophe.Status.DISCONNECTED){
				console.log("xmpp连接已断开 。。。 DISCONNECTED");
				$("#myonline").html("(离线)");
			}else {
				console.log("xmpp连接失败 ！！！"+status);
				// Strophe.Status.CONNFAIL 连接失败
				// Strophe.Status.AUTHFAIL 帐号或密码错误
				// Strophe.Status.DISCONNECTED 连接断开
				ownAlert(2,"登录失败，请重新登录");
				//window.location.href = "login.html";
			}
		});
		/*ConversationManager.open();*/

		//Demo.joinRoom("1ebab36134ff4e5e893387b4bb1dce7a","10010318");
		//GroupManager._XEP_0045_037("1ebab36134ff4e5e893387b4bb1dce7a","10010318");
	
	});
	function getId() {
		return Math.round(new Date().getTime() / 1000) + Math.floor(Math.random() * 1000);
	};
	function getGroupAddr(groupId) {
		return groupId + "@" + AppConfig.mucJID;
	};
	var Demo = {
			joinRoom:function(groupId,userId){
				console.log("加入群聊成功");
				var id =getId();
				var to =getGroupAddr(groupId)+ "/" + userId;
				var pres = $pres({
					id : id,
					to : to
				}).c("x", {
					xmlns : "http://jabber.org/protocol/muc"
				}).c("history",{maxchars:'0',seconds:'0'});
				// 发送报文
				console.log("加入群聊成功");
				GroupManager.getCon().sendIQ(pres.tree(),function(stanza){
						 console.log("sendIQ result "+Strophe.serialize(stanza));
				},function(stanza){
					 console.log("sendIQ error "+Strophe.serialize(stanza));
				},null);
				console.log("加入群聊成功");
			}

	};


	 function createMsg(type,content){
		var timeSend = Math.round(new Date().getTime() / 1000);
		var messageId=myFn.randomUUID();
			var msg = {
				messageId:messageId,
				fromUserId :"10010323",
				fromUserName : "风度",
				content : content,
				timeSend : timeSend,
				type : type
			};
			if(1==myData.isEncrypt)
				msg.isEncrypt=myData.isEncrypt;
			if(1==myData.isReadDel)
				msg.isReadDel=myData.isReadDel;
			return msg;
	};

	$("#btnSend").click(function(){
		alert("asdasd");
		var content = $("#messageBody").val();
		if (myFn.isNil(content)) {
			ownAlert(3,"请输入要发送的内容");
			return;
		}
		var msg=createMsg(1, content);
		if(1!=ConversationManager.isGroup){ //isGroup//1是群聊 0是单聊
			UI.sendMsg(msg);
			return;
		}
		//群消息
		msg.fromUserName=GroupManager.roomCard;
		msg.objectId=ConversationManager.fromUserId;

		var userIdArr=Checkbox.parseData();
		//@群成员了
		if(null!=userIdArr&&0<userIdArr.length){
			msg.objectId="";
			for (var i = 0; i < userIdArr.length; i++) {
				if(i==userIdArr.length-1)
					msg.objectId+=userIdArr[i]+"";
				else 
					msg.objectId+=userIdArr[i]+",";
			}

		}
		sendMsg(msg);
	});

	function sendMsg(msg,toJid){
		if (!myFn.isNil(DataMap.deleteRooms[ConversationManager.fromUserId])) { //判断用户是否被踢出该群
			ownAlert(3,"你已被踢出该群，无法发送消息");
			return;
		}
		msg.id=msg.messageId;
		var content=msg.content;
		sendMsg1(msg,function(){
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

	};

	 function sendMsg1(msg,callback,toJid) {
		//发送消息开始
		//检查Xmpp 是否在线
		if(myConnection.connected){
			//检查消息是否需要加密
			ConversationManager.checkEncrypt(msg,function(result){
				sendMsgAfter(result,toJid);
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
		

	};

	function sendMsgAfter(msg,toJid){
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
	};


	/*function joinRoom(groupId,userId){

		var id = GroupManager.getId();
		var to = GroupManager.getGroupAddr(groupId)+ "/" + userId;
		var pres = $pres({
			id : id,
			to : to
		}).c("x", {
			xmlns : "http://jabber.org/protocol/muc"
		}).c("history",{maxchars:'0',seconds:'0'});
		// 发送报文
		GroupManager.getCon().sendIQ(pres.tree(),function(stanza){
				 console.log("sendIQ result "+Strophe.serialize(stanza));
		},function(stanza){
			 console.log("sendIQ error "+Strophe.serialize(stanza));
		},null);
		//console.log(" 037 "+pres.tree());
	};*/
	
</script>
</body>
</html>