var loginDataObj=null;



$(function() {
	

	//$.cookie.raw = true;
	//$.cookie.json = true;
	
	//appInfo();

//var loginData = $.cookie("loginData");


	//if (myFn.isNil(loginData)) {
		//shikuLog("loginData  "+loginData);
		//loginData=DataMap.loginData;
		//if(myFn.isNil(loginData)){
			/*myData.userId =10007567;
			myData.jid = "10007567@im.shiku.co/youjob";
			myData.telephone ="5209d0451899763cce3d73c2abe5e863";
			myData.password = "e10adc3949ba59abbe56e057f20f883e";
			myData.access_token ="5adf3dda53e94591b8574f500daac52d";
			myData.nickname="5771";
			DataMap.loginData=myData;

			myData.loginResult = loginDataObj.loginResult;
			myData.user = loginDataObj.user;*/
			
			//ownAlert(2,"您尚未登录或登录已超时，请重新登录");
			//window.location.href = "index.html";
			/*window.wxc.xcConfirm('您尚未登录或登录已超时，请重新登录\"' + nickname + '\"？', window.wxc.xcConfirm.typeEnum.confirm,{
				onOk:function(){
					ownAlert(2,"您尚未登录或登录已超时，请重新登录");
					window.location.href = "login.html";
				}
			});*/
		
			
		//}else{
			/*myData.userId = loginDataObj.userId;
			myData.jid = loginDataObj.jid;
			myData.telephone = loginDataObj.telephone;
			myData.password = loginDataObj.password;
			myData.access_token = loginDataObj.access_token;
			myData.loginResult = loginDataObj.loginResult;
			myData.user = loginDataObj.user;
			myData.nickname=myData.user.nickname*/
	//	}
	//}else{
		//loginDataObj = eval("(" + loginData + ")");
	//}

   /* myData.userId =10007567;
	myData.jid = "10007567@im.shiku.co/youjob";
	myData.telephone ="5209d0451899763cce3d73c2abe5e863";
	myData.password = "e10adc3949ba59abbe56e057f20f883e";
	myData.access_token ="5adf3dda53e94591b8574f500daac52d";
	myData.nickname="5771";
	DataMap.loginData=myData;*/
			
	
	/*myData.userId = loginDataObj.userId;
	myData.jid = loginDataObj.jid;
	myData.telephone = loginDataObj.telephone;
	myData.password = loginDataObj.password;
	myData.access_token = loginDataObj.access_token;
	myData.loginResult = loginDataObj.loginResult;
	myData.user = loginDataObj.user;
	myData.nickname=myData.user.nickname;*/


	//进入主页面后加载左上角头像
	var h5="<img onclick="+'UI.showMe()'+" id='myAvatar' onerror='this.src=\"img/ic_avatar.png\"' src='"+myFn.getAvatarUrl(myData.userId)+"' class='myAvatar roundAvatar'>"
	$("#photo").append(h5);
	var sp="<p id='nickname' style='color:white;margin-top:10px'>"+myData.nickname+"</p>"
	$("#photo").append(sp);
	var online="<p id='myonline' style='color:white;margin-top:-100px;margin-left:110px'></p>"
	$("#photo").append(online);
	$("#uploadUserId").val(myData.userId);

	//loadUploadUrl();
	//init();
	//NetWork.networkListener(onNet,offNet);	
	// 百度定位
	//mySdk.locate();
	


	
});
function loadUploadUrl(){   //将上传的url赋到页面
	//我的资料-头像上传
	$("#prop #avatarForm").attr("action",""+AppConfig.uploadAvatarUrl); 
	$("#uploadFileModal #uploadFileFrom").attr("action",""+AppConfig.uploadUrl); 
}

function over(){
	var s=Document.getElementById("img1");
	s.src="img/378FMI5VN@}]1D{H5(NOP8D.png"
}
function out(){
	var s=Document.getElementById();
}

function init() { //进入主页面后执行
	// 加载好友
	UI.showFriends(0);
	// 加载所有房间
	/*GroupManager.showAllRoom(0);*/

	//进入页面，打开10000号系统聊天框，并显示欢迎语。
	var welcomeContent ="欢迎使用视酷IM_WEB版。";
	var from = "10000@www.shiku.co";
	var userId = 10000;
	var nickname = "系统";
	welcome(from,10000,nickname,welcomeContent);

	//setInterval
	mySdk.xmppLogin(function(){
		//xmpp 登陆成功
		//加入我的房间
		GroupManager.joinMyRoom();
		mySdk.sendOnline();
	});

	
	mySdk.loadFriendsOrBlackList("friendList"); //调用方法获取好友和单向关注列表的userId;
	mySdk.loadFriendsOrBlackList("blackList"); //调用方法获取黑名单列表的userId;
	// ********************************************************************************
	// 我的房间
	$("#btnMyRoom").click(function() {
		$("#btnAllRoom").removeClass("border");
		$("#btnMyRoom").addClass("border");
		
		GroupManager.showMyRoom(0);	
	});
	// 所有房间
	$("#btnAllRoom").click(function() {
		$("#btnMyRoom").removeClass("border");
		$("#btnAllRoom").addClass("border");
		
		GroupManager.showAllRoom(0);
	});
	// ********************************************************************************
	// ********************************************************************************
	// Ctrl+Enter发送
	/*$(document).keypress(function(e) {
		if (e.ctrlKey && e.which == 13) {
			var content = $("#messageBody").val();
			if (myFn.isNil(content)) {
				ownAlert(3,"请输入要发送的内容");
				return;
			}
			var msg=ConversationManager.createMsg(1, content);
			UI.sendMsg(msg);
		}
	});*/

	//发送名片
	$("#sendOK").click(function(){
		var myArray = Checkbox.parseData();//调用方法解析数据
		if (0 == myArray.length) {
			ownAlert(3,"请选择要发送的好友名片");
			return;
		} else {
			for(var i=0;i<myArray.length;i++){
				mySdk.getUser(myArray[i],function(result){
							var  msg=ConversationManager.createMsg(8, result.nickname);
							msg.objectId=result.userId;
							UI.sendMsg(msg);
							sleep(100);
					})
				
				
			}
			$("#card").modal('hide');
		}
		
	});

	//取消发送名片
	$("#sendOff").click(function(){
		$("#card").modal('hide');
	});
	// 发送
	$("#btnSend").click(function() {
		var content = $("#messageBody").val();
		if (myFn.isNil(content)) {
			ownAlert(3,"请输入要发送的内容");
			return;
		}
		var msg=ConversationManager.createMsg(1, content);
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
		UI.sendMsg(msg);
		
	});
	// 图片
	$("#btnImg").click(function() {
		//$("#choiceImgModal").modal('show');
		$("#uploadFileModal #icon").attr("src","img/uploadImg.png");
		Temp.uploadType="sendImg";
		$("#myImgPreview").show();
		$("#myfile").attr("accept","image/*");
		$("#uploadModalLabel").html("发送图片");
		$("#myImgPreview").attr("src","");
		$("#myFileUrl").val("");
		$("#myfile").val("");
		$("#uploadFileModal").modal('show');
	});
	//文件
	$("#btnFile").click(function() {
		//$("#choiceFileModal").modal('show');
		$("#uploadFileModal #icon").attr("src","img/uploadFile.png");
		$("#myImgPreview").hide();
		Temp.uploadType="sendFile";
		$("#myfile").attr("accept","");
		$("#uploadModalLabel").html("发送文件");
		$("#myImgPreview").attr("src","");
		$("#myFileUrl").val("");
		$("#myfile").val("");
		$("#uploadFileModal").modal('show');
	});
	// 表情
	$("#btnEmojl").click(function(event) {
		$("#gif-panel").hide();
		$("#gif-panel #gifList").getNiceScroll().hide();
		var e = window.event || event;
		/*alert($("#btnEmojl").html());*/
		/*$("#btnEmojl").html("<img src='img/2.png'>");*/
		if (e.stopPropagation) {
			e.stopPropagation();
			
		} else {
			e.cancelBubble = true;
		}
		$('#emojl-panel').toggle();
		$("#emojl-panel #emojiList").getNiceScroll().show();
		$("#emojl-panel #emojiList").getNiceScroll(0).doScrollTop(0, 0);

	});
	// GIF 动画
	$("#btnGif").click(function(event) {
		$('#emojl-panel').hide();
		var e = window.event || event;
		if (e.stopPropagation) {
			e.stopPropagation();
		} else {
			e.cancelBubble = true;
		}
		$('#gif-panel').toggle();
		$("#gif-panel #gifList").getNiceScroll().show(); //显示滚动条
		$("#gif-panel #gifList").getNiceScroll(0).doScrollTop(0, 0); // 滚动到顶部
	});
	//@群成员
	$("#messageBody").keyup(function(){
       if(1!=ConversationManager.isGroup||Temp.MemberHttping)
       	return;
 		var text=$("#messageBody").val();
 		if(1>text.length){
 			Checkbox.cheackedFriends={};//清空选中的数据
 			return;
 		}
       var str =text.charAt(text.length - 1);
       if("@"!=str)
       	return;
       Temp.friendListType="@Member";
       $("#divFriendListTitle").html("选择成员");
       $("#divFriendListBtnOk").html("确认");
       Checkbox.checkedNames=[];
        Temp.nickname=null;
      	Temp.MemberHttping=true;//网络请求中 再次触发事件不继续
     	mySdk.getMembersList(GroupManager.roomData.id,null,function(result){
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
	});
	//发送红包
	$("#redPacketSend").click(function(){
		 //var reg = new RegExp("^[0-9]+(.[0-9]{2})?$/");
		var reg = /(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)/;
		 var money=$("#divRedPacket #money").val();
		if(!reg.test(money)){
        	ownAlert(3,"请输入正确的金额!");
        	return;
    	}else if(money>500||money<0.01){
    		ownAlert(3,"红包总金额在0.01~500之间哦!");
    		return;
    	}
    	var redType=$("#divRedPacket #redType").val();
    
		var count=$("#divRedPacket #count").val();

		var greetings=$("#divRedPacket #greetings").val();

		if(1==ConversationManager.isGroup){//群聊红包
			var room=GroupManager.roomData;
			if(count>room.userSize){
				ownAlert(3,"数量不能大于群组人数");
				return;
			}

		}else 
			count=1;

		if(myFn.isNil(greetings))
			greetings="恭喜发财，万事如意";
		if(greetings.length>9){
			greetings=greetings.substring(0,9);
		}
		mySdk.sendRedPacket(redType,money,count,greetings,function(data){
			var msg=ConversationManager.createMsg(28,greetings);
			//红包类型
			msg.fileName=data.type;
			msg.fileSize=data.status;
			//红包ID
			msg.objectId=data.id;
			UI.sendMsg(msg);
			$("#divRedPacket").modal("hide");
		});
			
		
	});
	// 表情框
	$("#emojl-panel").click(function(event) {
		var e = window.event || event;
		if (e.stopPropagation) {
			e.stopPropagation();
		} else {
			e.cancelBubble = true;
		}
	});
	// 空白点击事件
	document.onclick = function() {
		$("#gif-panel #gifList").getNiceScroll().hide();//隐藏滚动条
		$("#emojl-panel #emojiList").getNiceScroll().hide();
		$("#emojl-panel").hide();
		$("#gif-panel").hide();
	};
	// ********************************************************************************
	
	//确认转发  @群成员
	$("#divFriendListBtnOk").click(function(){
		var userIdArr =Checkbox.parseData();
		if("@Member"==Temp.friendListType){
			//@群成员
			if(1>userIdArr.length)
				return;
			var text=$("#messageBody").val();
			var i=0;
			for (var key in Checkbox.checkedNames) {
				if(i==Checkbox.checkedNames.length-1)
				   	text+=Checkbox.checkedNames[key]+"  ";
				else
					text+=Checkbox.checkedNames[key]+"  @";
				i++;
			}
			$("#messageBody").val(text);
			Checkbox.checkedNames=[];
		}

		$("#divFriendList").modal('hide');
		
		
	});
	


	//消息
	// $("#word").click(function(){
	// 	$("#word").addClass('word');
	// 	$("#details").removeClass('word');
	// 	$("#chatPanel").show();
		
	// });
	// //详情
	// $("#details").click(function(){
	// 	$("#details").addClass('word');
	// 	$("#word").removeClass('word');
	// 	$("#chatPanel").hide();
	// 	$("#des").show();
	// });
	

	//发红包
	$("#redback").click(function(){
		
       if(ConversationManager.isGroup==1){
       		$("#trRedCount").show();
       		$("#luck").show();
       		/*$("#redType").children('option[value="2"]').wrap('<span>').show();*/
       		/*$("#redType").eq(1).show();*/
       		/*$("#redType").remove();
			var html="<option  value='1' selected='true'>普通红包</option>"+"<option value='2'>口令红包</option>";
			$("#redType").append(html);*/
		}else{
			$("#trRedCount").hide();
			$("#luck").hide();
			/*$("#redType").children('option[value="2"]').wrap('<span>').hide();*/
			/*$("#redType").eq(1).hide();*/
			/*$("#redType").remove();
			var html1="<option  value='1' selected='true'>普通红包</option>"+"<option value='2'>拼手气红包</option>"+"<option value='3'>口令红包</option>";
			$("#redType").append(html1);*/
		}
		$("#divRedPacket").modal('show');
	});
	
	$("#btnEditOK").click(function() {
		var obj ={};
		obj["nickname"]=$("#from #mynickname").val();
		obj["description"]=$("#from #mydescription").val();
		// obj["nickname"]=$("#from #nickname").val();
		obj["sex"]=$("#from #sex:checked").val();
		obj["birthday"]=$("#from #mybirthday").val();
	
		//obj["cityId"]=$("#from #cityId").val();
	
		var birthday = obj["birthday"];
		var timestamp = Math.round(new Date(birthday).getTime() / 1000);
		if(timestamp>new Date().getTime()/1000){
			ownAlert(3,"时间不能超过当前时间!");
			return;
		}
		obj["birthday"] = timestamp;

		mySdk.updateUser(obj,function(user){
			$("#edit_modal").modal('hide');
			loginDataObj.user=user;
			myFn.setCookie("loginData",loginDataObj);
			$("#photo #nickname").html(user.nickname);
			//更新数据
			DataMap.userMap[user.userId].nickname = obj["nickname"];
			DataMap.userMap[user.userId].description = obj["description"];
			DataMap.userMap[user.userId].sex = obj["sex"];
			DataMap.userMap[user.userId].birthday = obj["birthday"];
			// $("#mybirthday").val(0 == myData.user.birthday ? "" : myFn.toDate(myData.user.birthday));
		});
	});

	$("#btnEditCancel").click(function() {
		$("#edit_modal").modal('hide');
	});
	$("#btnSettingCancel").click(function() {
		$("#edit_setting").modal('hide');
	});
	$("#btnPwdCancel").click(function() {
		$("#edit_pwd").modal('hide');
	});
	$("#seeHaiSendCancel").click(function() {
		$("#divSeeHai").modal('hide');
		Temp.toJid=null;
	});
	//发送打招呼信息
	$("#seeHaiSend").click(function() {
			console.log("发送打招呼信息");
			var msg=ConversationManager.createMsg(XmppMessage.Type.SAYHELLO,$("#seeText").val());
			UI.sendMsg(msg,Temp.toJid);
			ownAlert(1,"发送成功!");
			$("#divSeeHai").modal('hide');
			
	});
	$("#replySeeSendCancel").click(function() {
		$("#divReplySeeHai").modal('hide');
		Temp.toJid=null;
	});
	//发送打招呼信息
	$("#replySeeSend").click(function() {

			var msg=ConversationManager.createMsg(XmppMessage.Type.FEEDBACK,$("#replyText").val());
			UI.sendMsg(msg,Temp.toJid);
			ownAlert(1,"发送成功!");
			$("#divReplySeeHai").modal('hide');
			Temp.toJid=null;
	});
	$("#btnSettingOK").click(function() {
			var obj = $("#form4").serializeObject();
			obj["allowAtt"]=0;
			obj["allowGreet"]=0;
			obj["friendsVerify"]=0;
			if($("#allowAtt").is(':checked')==true)
					obj["allowAtt"]=1;
			if($("#allowGreet").is(':checked')==true)
					obj["allowGreet"]=1;
			if($("#friendsVerify").is(':checked')==true)
					obj["friendsVerify"]=1;
			if($("#infoEncrypt").is(':checked')==true){
				myFn.switchEncrypt(1);
			}else{
				myFn.switchEncrypt(0);
			}

		myFn.invoke({
			url : '/user/settings/update',
			data : obj,
			success : function(result) {
				if (1 == result.resultCode) {
					
					/*$("#edit_setting").modal('hide');*/
					ownAlert(1,"保存成功")
				} else {
					ownAlert(2,result.resultMsg);
				}
			},
			error : function(result) {
				ownAlert(2,"资料更新失败，请稍后再试！");
			}
		});

		
	});
	$("#close").click(function(){
		$("#getredpacket").modal("hide");
	});
	$("#btnPwdOK").click(function() {
			var obj = $("#form5").serializeObject();
			obj["oldPassword"]=$("#oldPwd").val();
			obj["newPassword"]=$("#newPassword").val();
			obj["password1"]=$("#password1").val();
			if(myFn.isNil(obj["oldPassword"])||
				myFn.isNil(obj["newPassword"])||
				myFn.isNil(obj["password1"])){
				ownAlert(3,"请输入值!");
				return;
			}else if(obj["newPassword"]!=obj["password1"]){
				ownAlert(3,"两次密码输入不一致!");
				return;
			}

		myFn.invoke({
			url : '/user/password/update',
			data : obj,
			success : function(result) {
				if (1 == result.resultCode) {
					ownAlert(3,"修改密码成功!");
					$("#edit_setting").modal('hide');
				} else {
					ownAlert(2,result.resultMsg);
				}
			},
			error : function(result) {
				ownAlert(2,"资料更新失败，请稍后再试！");
			}
		});
	});
	// ********************************************************************************

	// 确认发送文件
	$("#btnSendFileOK").click(function() {
		var fielUrl=$("#myFileUrl").val();
		if(myFn.isNil(fielUrl)){
			ownAlert(3,"请先上传文件!");
			return;
		}
		
		if("sendImg"==Temp.uploadType)
			UI.sendImg();
		else if("sendFile"==Temp.uploadType)
			UI.sendFile();
		else if("uploadFile"==Temp.uploadType)//群文件上传
			GroupManager.addGroupFile(Temp.file);
			
		$("#uploadFileModal").modal('hide');
		 
		
		//$("#choiceFileModal").modal('hide');
	});
	// 取消发送文件
	$("#btnSendFileCancel").click(function() {
		//$("#choiceFileModal").modal('hide');
		$("#uploadFileModal").modal('hide');
		// 取消发送应删除已上传到服务的图片
		
		//取消发送 删除已上传的文件
		if(!myFn.isNil(Temp.file)&&!myFn.isNil(Temp.file.url)){
			mySdk.deleteFile(Temp.file.url,function(){
				//删除文件成功
			});
		}
		Temp.file=null;
	});

	
	/*
      *	这一块先注释掉不要删除
	 $("#avatarUpload").click(function(){
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
	});*/

	/*$("#uploadStart").click(function(){
		var file= $("#myfile")[0].files[0];
		Temp.file=file;
		var filesize =file.size/1024/1024;
		
            if(filesize > 3){
                ownAlert(3,"文件大小超过限制，最多20M");
                return false;
            }
            $("#filePath").val(file.name);
            $("#filePath").attr("size",file.size);

			$("#uploadFileFrom").ajaxSubmit(function(data) {//成功
		          var obj = eval("(" + data + ")");
				if (!myFn.isNil(obj.url)) {
					Temp.file.url=obj.url;
					$("#myImgPreview").attr("src",obj.url);
					$("#myImgPreview").show();
					$("#myFileUrl").val(obj.url);
					
				}    
		  });
	});*/

	// 初始化bootstrap日期选择器
	$('#birthday').datepicker({
		format : 'yyyy/mm/dd',
		language : 'zh-CN',
		pickDate : true,
		pickTime : true,
		hourStep : 1,
		minuteStep : 15,
		secondStep : 30,
		inputMask : true
	});
	$("#btnQueryNearbyUser").click(function() {
		UI.showNearbyUser(0);
	});
	$("#btnQueryAllRoom").click(function() {
		GroupManager.showAllRoom(0);
	});
	


	$("#newGroup").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'新建群组',
         placement:'bottom'
    });
   $("#myFriend").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'加好友',
         placement:'bottom'
    });


//init初始化结束
}



// $(document).ready(function(){   
// 	$("#chatPanel #messagePanel").mCustomScrollbar();

// });



$(document).ready(function(){   

    //加载表情
    $.getJSON("json/emoji.json",function(data){  
            var emojiHtml = "";  //emoji 的Html
            //var emojiNum = 0;
            $.each(data,function(infoIndex,info){  
                  emojiHtml +="<img src='emojl/"+info['filename']+".png' alt='"+info['chinese']+"' title='"+info['chinese']+"' onclick='UI.choiceEmojl(\"" +"["+info['english']+"]"+ "\")' />"
                  //emoji[emojiNum] = emojiHtml;
            }) 
            $("#emojl-panel #emojiList").append(emojiHtml);
            //初始化GIF动画的滚动条
			/*$("#emojl-panel #emojiList").niceScroll({
				  cursorcolor: "rgb(113, 126, 125)",
		          cursorwidth: "5px", // 滚动条的宽度，单位：便素
		          autohidemode: true, // 隐藏滚动条的方式
		          railoffset: 'top', // 可以使用top/left来修正位置
		          enablemousewheel: true, // nicescroll可以管理鼠标滚轮事件
		          smoothscroll: true, // ease动画滚动  
		          cursorminheight: 32, // 设置滚动条的最小高度 (像素)
		          iframeautoresize: true,//iframeautoresize: true
		          bouncescroll: false,
		          railalign: 'right',
			});*/
			//$("#emojl-panel #emojiList").getNiceScroll().show(); //显示滚动条
    });

    //加载gif 动画
    $.getJSON("json/gif.json",function(data){  
            var gifHtml = "";  //gif 的Html
            $.each(data,function(infoIndex,info){  
                  gifHtml +="<img src='gif/"+info['filename']+"' style='width:72px; height:72px;margin-left:15px;' onclick='UI.sendGif(\"" +info['filename']+ "\")' />"
            }) 
            $("#gif-panel #gifList").append(gifHtml);
            //初始化GIF动画的滚动条
			/*$("#gif-panel #gifList").niceScroll({
				  cursorcolor: "rgb(113, 126, 125)",
		          cursorwidth: "5px", // 滚动条的宽度，单位：便素
		          autohidemode: false, // 隐藏滚动条的方式
		          railoffset: 'top',// 可以使用top/left来修正位置
		          enablemousewheel: true, // nicescroll可以管理鼠标滚轮事件
		          smoothscroll: true, // ease动画滚动  
		          cursorminheight: 32, // 设置滚动条的最小高度 (像素)
		          iframeautoresize: true,//iframeautoresize: true
		          bouncescroll: false,
		          railalign: 'right',
			});*/
			//$("#gif-panel #gifList").getNiceScroll().show(); //显示滚动条
    });

  	//初始化滚动条
	// $("#messagePanel").niceScroll({
	// 	  cursorcolor: "#838588",
 //          cursorwidth: "8px", // 滚动条的宽度，单位：便素
 //          zindex: "auto", // 改变滚动条的DIV的z-index值
 //          hwacceleration: true, // 激活硬件加速
 //          autohidemode: true, // 隐藏滚动条的方式
 //          railoffset: false,
 //          enablemousewheel: true, // nicescroll可以管理鼠标滚轮事件
 //          smoothscroll: true, // ease动画滚动
 //          cursorminheight: 32, // 设置滚动条的最小高度 (像素)
	// });

	//$(".nano").nanoScroller();

    //初始化滚动条
	/*$("#messageDisplayArea").niceScroll({
		  cursorcolor: "#838588",
          cursorwidth: "8px", // 滚动条的宽度，单位：便素
          autohidemode: true, // 隐藏滚动条的方式
          railoffset: false,
          enablemousewheel: true, // nicescroll可以管理鼠标滚轮事件
          smoothscroll: true, // ease动画滚动  
          cursorminheight: 32, // 设置滚动条的最小高度 (像素)
          iframeautoresize: true //iframeautoresize: true
	});
	*/






	//消息输入区按钮提示
	//
 $("#btnSipCall").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'音视频',
         placement:'top'
    });	
   $("#btnEmojl").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'表情',
         placement:'top'
    });
   $("#btnGif").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'动图',
         placement:'top'
    });
   $("#btnImg").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'图片',
         placement:'top'
    });

   $("#btnFile").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'文件',
         placement:'top'
    });

   $("#btnmin").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'名片',
         placement:'top'
    });

   $("#place").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'位置',
         placement:'top'
    });
   
   $("#redback").tooltip({ //鼠标移动弹出提示
         trigger:'hover',
         html:true,
         title:'红包',
         placement:'top'
    });

   $("#rightTopMnue #blackListImg").tooltip({ //右上角菜单提示
         trigger:'hover',
         html:true,
         title:'黑名单',
         placement:'bottom'
    });

   $("#rightTopMnue #helpImg").tooltip({ //右上角菜单提示
         trigger:'hover',
         html:true,
         title:'帮助',
         placement:'bottom'
    });

   $("#rightTopMnue #quitImg").tooltip({ //右上角菜单提示
         trigger:'hover',
         html:true,
         title:'退出',
         placement:'bottom'
    });

   $("#rightTopMnue #privacyImg").tooltip({ //右上角菜单提示
   		trigger:'hover',
   		html:true,
   		title:'隐私',
   		placement:'bottom'
   });

   $("#rightTopMnue #passwordImg").tooltip({ //右上角菜单提示
   		trigger:'hover',
   		html:true,
   		title:'密码',
   		placement:'bottom'
   });

   $("#photo #myAvatar").tooltip({ //右上角菜单提示
   		trigger:'hover',
   		html:true,
   		title:'个人资料',
   		placement:'bottom'
   });


   $("#avatarForm #avatar_preview").tooltip({ //右上角菜单提示
   		trigger:'hover',
   		html:true,
   		title:'点击更换头像',
   		placement:'bottom'
   });

});


