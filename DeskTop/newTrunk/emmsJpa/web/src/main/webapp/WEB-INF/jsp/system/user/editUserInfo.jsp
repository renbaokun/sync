<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
	<head>
		<title>编辑页</title>
		<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
	</head>
	<body>
		<div class="easyui-panel" title="首页->设置->修改用户信息" data-options="fit:true,border:false">
			<form id="ff" method="post">
				<input type="hidden" name="employee.organization.orgId" id="orgId"  value="${orgId}"/>
				<input type="hidden" name="userId" value="${userId}"/>
				<input type="hidden" name=employee.empId id="empId"/>
				<div style="margin:20px">
					<input class="easyui-textbox" id="userName"  editable="false" name="userName"  style="width:40%" data-options="label:'登录名:',required:true,validType:['length[1,100]']" >
					<input class="easyui-textbox" id="empName" name="employee.empName"  style="width:40%" data-options="label:'用户姓名:',required:true,validType:['length[1,100]']" >
				</div>
				<div style="margin:20px">
					<input class="easyui-textbox" id="cellPhone" name="employee.cellPhone"  style="width:40%" data-options="label:'手机号码:',required:true,validType:['length[1,20]']" >
					<input class="easyui-textbox" id="email" name="employee.email"  style="width:40%" data-options="label:'电子邮箱:',required:true,validType:['length[1,100]']" >
				</div>
				<div style="text-align: center;">
					<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm()">保存</a>
				</div>
			</form>
		</div>
	<script type="text/javascript">
		$(function(){
			$('#ff').form('load', '${emms}/system/user.do?cmd=userInfo&userId=${userId}');
			$('#ff').form({
				onLoadSuccess:function(data){
					if(null != data.employee){
						$("#empName").textbox("setValue", data.employee.empName);
						$("#cellPhone").textbox("setValue", data.employee.cellPhone);
						$("#email").textbox("setValue", data.employee.email);
						$("#empId").val(data.employee.empId);
					}
				}
			});
		});
	function ajaxSubmitForm() {
		  $("#ff").form("submit", {
             url: "${emms}/system/user.do?cmd=saveUser",
             onsubmit: function () {
                 return $(this).form("validate");
             },
             success: function (result) {
            	alert(result);
                top.$("#dialog").dialog('close');
             }
         }); 
	 } 
	</script>	
	</body>
</html>