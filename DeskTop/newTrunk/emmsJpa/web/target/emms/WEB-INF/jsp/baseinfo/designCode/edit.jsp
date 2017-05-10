<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>编辑页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
	<div class="easyui-panel" title="首页->基础信息管理->设计院编码管理->设计院编码编辑" data-options="fit:true,border:false">
		<form id="ff" method="post">
			<input class="easyui-textbox" type="hidden" id="designId" name="designId"/>
			<div style="padding:10px" >
				<input class="easyui-textbox" id="designCode" name="designCode"  style="width:40%" maxlength="3" data-options="label:'设计院编码:',readonly:true" >
				<input class="easyui-textbox" id="designName" name="designName" style="width:40%" data-options="label:'物资名称:',validType:'length[0,50]'">
			</div>
			<div style="padding:10px">
				<input class="easyui-combobox" id="designOrgId" style="width:40%" name="designOrgId" data-options="label:'设计院:',required:true">
			    <input class="easyui-combobox" id="designType" style="width:40%" name="designType" data-options="label:'物资类型:'">
			</div>
			<div style="padding:10px">
				<input class="easyui-textbox" id="additional1" name="additional1" style="width:40%" data-options="label:'附加1:',validType:'length[0,100]'">
				<input class="easyui-textbox" id="additional2" name="additional2" style="width:40%" data-options="label:'附加2:',validType:'length[0,100]'">
			</div>
			<div style="padding:10px">
				<input class="easyui-textbox" id="additional3" name="additional3" style="width:40%" data-options="label:'附加3:',validType:'length[0,100]'" >
                <input class="easyui-textbox" id="additional4" name="additional4" style="width:40%" data-options="label:'附加4:',validType:'length[0,100]'" >
			</div>
			<div style="padding:10px">
				<input class="easyui-combobox" id="designUnitMain" style="width:40%" name="designUnitMain" data-options="label:'计量单位:',required:true">
				<input class="easyui-textbox" id="designConversion" name="designConversion" style="width:40%" data-options="label:'主辅换算率:',validType:['length[0,20]','intOrFloat']">
			</div>
			<div style="padding:10px">
				<input class="easyui-textbox" id="designDescribe" name="designDescribe" style="width:40%" data-options="label:'物资描述:',required:true,multiline:true,validType:'length[0,500]'">
			</div>
			<div style="text-align: center;width:90%">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm()">保存</a>
				<a href="javascript:void(0)" iconCls='icon-back' class="easyui-linkbutton" onclick="cancel()">关闭</a>
			</div>
		</form>
	</div>
	<script type="text/javascript">
		$('#designType').combobox({
			url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=MaterialsTypeCategory',
			valueField: 'dictionaryCode',
			textField: 'dictionaryName',
			multiple:false
		});
		$('#designOrgId').combobox({
			url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=design',
			valueField: 'orgId',
			textField: 'orgName',
			multiple:false
		});
		$('#designUnitMain,#designUnitSecondary').combobox({
			url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=unit',
			valueField: 'dictionaryCode',
			textField: 'dictionaryName'
		});
		$(function(){
			$('#ff').form('load', '${emms}/baseinfo/designCode.do?cmd=loadDesignCodeData&designId=${designId}');
		});
	function ajaxSubmitForm() {
		  $("#ff").form("submit", {
             url: "${emms}/baseinfo/designCode.do?cmd=save",
             onsubmit: function () {
                 return $(this).form("validate");
             },
             success: function (result) {
				 console.log(result);
				 $.messager.alert("操作提示","保存成功","info",function(){
            		window.frames["mainFrame"].location = "${emms}/baseinfo/designCode.do?cmd=query";
					top.$("#dialog").dialog("close");
                });
               },
			  error: function () {
				  $.messager.alert("操作提示","保存失败","error");

			  }
         }); 
	 }
		function cancel(){
			$("#dialog").dialog("close");
		}
	</script>
</body>	
</html>