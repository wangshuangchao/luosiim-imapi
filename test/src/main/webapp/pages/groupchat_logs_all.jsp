<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/taglibs/samples-uitls" prefix="utils"%>
<jsp:include page="top.jsp"></jsp:include>
<script type="text/javascript" src="/pages/js/echarts.min.js"></script>
<script type="text/javascript" src="/pages/plugins/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script type="text/javascript">
	$(document).ready(function(){  
		initDatepicker();   
	}); 
	function getPage(pageIndex) {
		window.location.href = "/console/groupchat_logs_all?pageIndex=" + pageIndex + "&pageSize=25&room_jid_id=" + $("#room_jid_id").val();
	}
	function deleteQueryResult() {
		if (confirm("是否确认清空查询到的${page.total}条消息？")) {
			window.location.href = "/console/groupchat_logs_all/del?room_jid_id=" + $("#room_jid_id").val();
		}
	}
	//删除
	function deletemucMsg(num){
		
		var myDate = new Date();
		var roomJid=$("#room_jid_id").val();
		var startTime=0;
		var endTime=0;
		if(num==1){//删除最近1小时
			startTime=myDate.getTime();
			endTime= ((myDate.getTime()/1000)-60*60)*1000;
			alert("start"+startTime);
			alert("endTime"+endTime);
		}else if(num==2){//删除最近12小时
			startTime=myDate.getTime();
			endTime=((myDate.getTime()/1000)-60*60*12)*1000;
		}else if(num==3){//删除最近24小时
			startTime=myDate.getTime();
			endTime=((myDate.getTime()/1000)-60*60*24)*1000;
		}else if(num==4){//删除一个月以前
			startTime=myDate.getTime();
			endTime=((myDate.getTime()/1000)-60*60*24*30)*1000;
		}else if(num=6){
			var startdate=new Date($("input[name='startTime']").val());
			endTime=startdate.getTime();
			var enddate=new Date($("input[name='endTime']").val());
			startTime=enddate.getTime();
		}
		if(confirm("是否确认删除")){
			$.ajax({
				type:"POST",
				url:"/console/deleteMsgGroup",
				data:"startTime="+startTime+"&endTime="+endTime+"&room_jid_id="+roomJid,
				success:function(data){
					alert("删除成功");
				}
			});	
		}
	}
	//初始化时间插件 Start
	 function initDatepicker(){
		 $('.datepicker').datetimepicker({
		        minView: "month", //选择日期后，不会再跳转去选择时分秒 
		        format: "yyyy-mm-dd HH:mm:ss", //选择日期后，文本框显示的日期格式 
		        language: 'zh-CN', //汉化  
		        showMeridian : true,//是否加上网格
		        autoclose:true, //选择日期后自动关闭 
		        weekStart: 1,
		        todayBtn:  1,
		        // todayHighlight: 0,
		 });
		  
	}//初始化时间插件 End

</script>
<div class="container" style="margin-top: 10px; margin-bottom: 10px; padding-left: 0px; padding-right: 0px;">
	<table style="margin-top: 10px; margin-bottom: 10px; margin-left: 10px;">
		<tr style="display: none">
			<td>
				<form class="form-inline">
					<input id="pageIndex" name="pageIndex" type="hidden" value="0" /> <input id="pageSize" name="pageSize" type="hidden" value="25" /> <input id="room_jid_id"
						name="room_jid_id" type="text" class="form-control input-sm" placeholder="房间Id" value="${room_jid_id}" />
					<button type="submit" class="btn btn-default btn-sm">搜索消息</button>
					<c:if test="${sessionScope.IS_ADMIN == 1}">
						<button type="button" class="btn btn-default btn-sm" onclick="deleteQueryResult();">清空消息</button>
					</c:if>
				</form>
			</td>
		</tr>
		<tr>
			<td style="color: red">删除聊天记录</td>
		</tr>
		<tr>
			<td>
				<button class="btn btn-default btn-sm" onclick="deletemucMsg(1);">最近一小时</button>
				<button class="btn btn-default btn-sm" onclick="deletemucMsg(2);">最近24小时</button>
				<button class="btn btn-default btn-sm" onclick="deletemucMsg(3);">最近48小时</button>
				<button class="btn btn-default btn-sm" onclick="deletemucMsg(4);">一个月以前</button>
				<button class="btn btn-default btn-sm" onclick="deletemucMsg(5);">全部</button>
			</td>
		</tr>
		<tr>
			<label class=" startTimeLab"  style="margin-top:0px;">
				<span>开始时间：</span>
				<input id="startTime" class="datepicker" name="startTime" value="${param.startTime}"  type="text">
			</label>
		  	<label class=" endTimeLab" style="margin-top:0px;">
			  	<span>结束时间：</span>
			  	<input id="endTime" class="datepicker" name="endTime" value="${param.endTime}" type="text" >
		  	</label>
		  	<button type="button" onclick="deletemucMsg(6)" id="searchButton" class="btn btn-info" style="margin-top:0px;">删除消息</button>
		</tr>
	</table>
	<div class="backgrid-container">
			<table class="backgrid" style="background-color: #fff;">
				<thead>
					<tr>
						<td width="180">房间Id</td>
						<td width="10%">发送者Id</td>
						<td width="10%">发送者</td>
						<td width="10%">时间</td>
						<td>内容</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach varStatus="i" var="o" items="${page.pageData}">
						<tr>
							<td>${o.room_jid_id}</td>
							<td>${o.sender}</td>
							<td>${o.fromUserName}</td>
							<td>${utils:format(o.ts,'yyyy-MM-dd HH:mm:ss')}</td>
							<td>${o.content}</td>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="5"><jsp:include page="pageBar.jsp"></jsp:include></td>
					</tr>
				</tbody>
			</table>
		</div>
</div>


<jsp:include page="bottom.jsp"></jsp:include>