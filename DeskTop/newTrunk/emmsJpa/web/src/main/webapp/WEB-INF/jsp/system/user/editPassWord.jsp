<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>

<body>
		<div class="easyui-panel" title="首页->设置->修改用户信息" data-options="fit:true,border:false">
		<form id="ff" method="post">
			<input type="hidden" id="userId" name="userId" value="${userId}"/>
			<div style="margin:20px">
				<input class="easyui-textbox" id="oldPwd" type="password"  style="width:80%" data-options="label:'原密码:',required:true" validType='checkOldPwd' >
			</div>
			<div style="margin:20px">
				<input class="easyui-textbox" id="newPassWord" type="password" style="width:40%" data-options="label:'新密码:',required:true" >
				<input class="easyui-textbox" id="confirmPwd" type="password"  style="width:40%" data-options="label:'确认密码:',required:true"  validType="equalTo['#newPassWord']">
			</div>
			<div style="text-align: center;">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="savePwd()">保存</a>
			</div>
		</form>
	</div>
	<script type="text/javascript">
	function savePwd() {
		  $("#ff").form("submit", {
             url: "${emms}/system/user.do?cmd=savePwd&newPwd="+$("#newPassWord").val(),
			 type:'POST',
			 onsubmit: function () {
                 return $(this).form("validate");
             },
             success: function (result) {
				 if(result){
					 $.messager.alert("操作提示", "提交成功！","info",function(){
						 top.$("#dialog").dialog('close');
					 });
				 }else{
					 $.messager.alert("操作提示", result,"warning");
				 }
             }
         }); 
	 } 
	</script>	
	</body>
</html>
