<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/taglibs/samples-uitls" prefix="utils"%>
<jsp:include page="top.jsp"></jsp:include>
<div class="container" style="margin-top: 10px; margin-bottom: 10px; padding-left: 0px; padding-right: 0px;">
	<table style="margin-bottom: 10px;">
		<tr>
			<td>
				<form class="form-inline">
					<input id="pageIndex" name="pageIndex" type="hidden" value="0" /> 
					<input id="pageSize" name="pageSize" type="hidden" value="25" /> 
					<div style="margin-left:30px" class="col-lg-1">
						<button type="submit"  class="btn btn-danger">搜索</button>
					</div>
					<div style="margin-left:30px" class="col-lg-3">
					 <a id=“userList"  href="/console/updatePublicNum"><button  class="btn btn-info" type="button">新增公众号</button></a>
					</div>
			
					<c:if test="${sessionScope.IS_ADMIN == 1}">
						<!-- 						<button type="button" class="btn btn-default btn-sm" onclick="deleteQueryResult();">清空用户</button> -->
					</c:if>
				</form>
			</td>
	</table>
	<div class="backgrid-container">
		<table class="backgrid" style="background-color: #fff;">
			<thead>
				<tr>
					<th width="30"></th>
					<th width="100">公众号Id</th>
					<th width="200">昵称</th>
					<th width="100">注册时间</th>
					<th width="100">客服Id</th>
					<th width="100">是否已注销</th>
					<th width="100">公众号类型</th>
					<th width="100">公众号客服电话</th>
					<th width="">操作</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach varStatus="i" var="o" items="${page.pageData}">
					<tr>
						<td></td>
						<td  style="display:none">${o.id}</td>
						<td>${o.publicId}</td>
						<td>${o.nickname}</td>
						<td>${utils:format(o.createTime*1000,'yyyy-MM-dd HH:mm:ss')}</td>
						<td>${o.csUserId}</td>
						<td>${o.isDel}</td>
						<td>${o.type}</td>
						<td>${o.phone}</td>
						<td>
							<c:if test="${sessionScope.IS_ADMIN == 1}">
								<a href='javascript:if(confirm("是否确认删除？"))window.location.href="/console/deletePublicNum
									?pid=${o.id}&pageIndex=${page.pageIndex}"'>删除</a>
								<a href="/console/updatePublicNum
									?id=${o.id}">修改</a>
							</c:if>
						</td>
						<td></td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="6"><jsp:include page="pageBar.jsp"></jsp:include></td>
				</tr>
			</tbody>
		</table>
	</div>
</div> 
<jsp:include page="bottom.jsp"></jsp:include>