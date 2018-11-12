//网络链接上了
function onNet(){
	//网络链接上了
	shikuLog("xmpp重连...");
	setTimeout(function(){
		mySdk.xmppLogin(function(){
			//重连成功后加入我自己的群聊
			GroupManager.joinMyRoom();
		});
	},2000);
	
    	
}
//断网了
function offNet(){
	//网络断开了
	$("#myonline").html("(离线)");
}

//检查网络状态 和xmpp 链接状态
function checkNetAndXmppStatus(){
	shikuLog("当前网络状态 "+NetWork.online);
	if(NetWork.online){
		shikuLog(" xmpp链接状态 "+myConnection.connected);
		if(!myConnection.connected){
			$("#myonline").html("(离线)");
			//xmpp 链接断开了
			mySdk.xmppLogin(function(){
				$("#myonline").html("(在线)");
				GroupManager.joinMyRoom();
			});
		}
	}else 
		ownAlert(3,"网络断开了,请检查网络!");
	
}

function shikuLog(obj){
	//log 打印
 	console.log("shikuLog "+obj);
}

function sleep(numberMillis) {
    var now = new Date();
    var exitTime = now.getTime() + numberMillis;
    while (true) {
        now = new Date();
        if (now.getTime() > exitTime)
            return;
    }
}

function welcome(from,userId,nickname,welcomeContent){ //打开系统聊天框，并显示欢迎信息
		// ownAlert(3,"欢迎测试");
		
		$("#userModal").modal('hide');
		ConversationManager.isOpen = true;
		ConversationManager.from = from;
		ConversationManager.fromUserId=myFn.getUserIdFromJid(from);
		var type = from.indexOf(myData.mucJID) == -1 ? 1 : 0;
		
	    UI.showDetails(from,type,nickname);//打开系统聊天框
	    $("#msgTab").addClass("msgMabayChange"); //让消息Tab 处于选中状态
		if (1 == type){

		} else {
			/*var to = from + "/" + myData.userId;
			GroupManager._XEP_0045_018(to);*/
		}
		var welcomeHtml = "<div id='msg_"+userId+"' class='chat_content_group buddy'>"
						+	"<div class='imgAvatar floatLeft'>"
						+		"<figure>"
						+	   		"<img src='img/im_10000.png' class='chat_content_avatar'>"
						+		"</figure>"
						+	"</div>"
						+  "<p class='chat_nick'>"+nickname+"</p>"
						+   "<div class='content'>"
						+   	 "<p class='chat_content'>"+welcomeContent+"</p>"
						+   "</div>"
						+"</div>";

		// 追加消息
		$("#messageContainer").append(welcomeHtml);

		// 滚动到底部
		UI.scrollToEnd();

};

   // 获取当前浏览器名 及 版本号
    function appInfo(){  
         var browser = {appname: 'unknown', version: 0,versionStr:0,},  
             userAgent = window.navigator.userAgent.toLowerCase();  // 使用navigator.userAgent来判断浏览器类型
        //msie,firefox,opera,chrome,netscape,ie 
         if ( /(msie|firefox|opera|chrome|netscape)\D+(\d[\d.]*)/.test(userAgent) ){  
            browser.appname = RegExp.$1;  
           browser.versionStr = RegExp.$2;  
        } else if ( /version\D+(\d[\d.]*).*safari/.test( userAgent ) ){ // safari  
             browser.appname = 'safari';  
             browser.versionStr = RegExp.$2;  
         }  
          shikuLog("appInfo name "+browser.appname+" version  "+browser.version);
         browser.version=browser.versionStr.split(".")[0];
         browser.version=parseInt(browser.version);
         shikuLog("appInfo  version  "+ browser.version);

         var errMsg="目前音视频只支持 Chrome 47 以下版本和Firefox 49及以下版本 请下载相应版本使用 ";
	         if("chrome"==browser.appname){
	         	if(47<browser.version){
	         		ownAlert(3,errMsg);
	         		return false;
	         	}

	         }else if("firefox"==browser.appname){
	         	if(49<browser.version){
	         		ownAlert(3,errMsg);
	         		return false;
	         	}
	         }else{
	           ownAlert(3,errMsg);
	           return false;
	       	}
       return true;  
         	
     } 