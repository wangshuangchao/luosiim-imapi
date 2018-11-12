<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/taglibs/samples-uitls"
	prefix="utils"%>

<jsp:include page="top.jsp"></jsp:include>
<script type="text/javascript">
	
</script>
<div
	style="margin-left: auto; margin-right: auto; width: 1000px; margin-top: 10px;">
	<form action="updatePublicNum" method="post" class="form-horizontal">

<input type="hidden"  name="pid" class="form-control" value="${p.id}">
		<div class="form-group">
			<label class="col-sm-2 control-label">公众号Id(新增则为空):</label>
			<div class="col-sm-10">
				<input id="publicId" name="publicId" class="form-control" value="${p.publicId}">
			</div>
		</div>

		<div class="form-group">
			<label class="col-sm-2 control-label">公众号名称:</label>
			<div class="col-sm-10">
				<input name="nickname" class="form-control" value="${p.nickname}">
			</div>
		</div>
		
		<div class="form-group">
			<label class="col-sm-2 control-label">公众号介绍:</label>
			<div class="col-sm-10">
				<input name="introduce" class="form-control" value="${p.introduce}">
			</div>
		</div>

		<div class="form-group">
			<label class="col-sm-2 control-label">客服id</label>
			<div class="col-sm-10">
				<input name="csUserId" class="form-control" value="${p.csUserId}">
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">头像:</label>
			<div class="col-sm-10">
				<input class="form-control" name="portraitUrl"
					value="${p.portraitUrl}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">首页欢迎语:</label>
			<div class="col-sm-10">
				<input class="form-control" name="message"
					value="${p.message}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">首页图片:</label>
			<div class="col-sm-10">
				<input class="form-control" name="messageUrl"
					value="${p.messageUrl}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">主页链接标题:</label>
			<div class="col-sm-10">
				<input class="form-control" name="indexUrlTital"
					value="${p.indexUrlTital}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">跳转主页链接:</label>
			<div class="col-sm-10">
				<input class="form-control" name="indexUrl"
					value="${p.indexUrl}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">公众号类型:</label>
			<div class="col-sm-10">
				<input class="form-control" name="type"
					value="${p.type}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">客服电话:</label>
			<div class="col-sm-10">
				<input class="form-control" name="phone"
					value="${p.phone}" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">是否删除:</label>
			<div class="col-sm-10">
				<input class="form-control" name="isDel"
					value="${p.isDel}" />
			</div>
		</div>
		<p>
			<button class="btn btn-primary btn-lg" type="submit">保存</button>
		</p>

	</form>

</div>

<jsp:include page="bottom.jsp"></jsp:include>