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
					<th width="80">用户Id</th>
					<th width="80">昵称</th>
					<th width="80">哦了号</th>
					<th width="100">标题</th>
					<th width="500">内容</th>
					<th width="120">留言时间</th>
					<th width="60">是否已处理</th>
					<th width="">操作</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach varStatus="i" var="c" items="${page.pageData}">
					<tr>
						<td></td>
						<td  style="display:none">${c.id}</td>
						<td>${c.userId}</td>
						<td>${c.nickname}</td>
						<td>${c.lsId}</td>
						<td>${c.title}</td>
						<td>${c.content}</td>
						<td>${utils:format(c.createTime*1000,'yyyy-MM-dd HH:mm:ss')}</td>
						<td>${c.isHandle}</td>
						<td>
							<c:if test="${sessionScope.IS_ADMIN == 1}">
								<a href="/console/updateComplint
									?id=${c.id}">修改处理状态</a>
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