<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->基本信息管理->物资编码管理->查询" data-options="fit:true,border:false">
	<form id="query" method="post">
		<div style="margin:20px">
			<input class="easyui-textbox" id="materialsCode"   style="width:25%" data-options="label:'物资编码:'">
			<input class="easyui-textbox" id="materialsDescribe"  style="width:25%" data-options="label:'物资描述:'">
			<select class="easyui-combobox" editable="false" id="materialsCategory"  style="width:25%" data-options="label:'物料类别:'"></select>
		</div>
		<div style="margin:20px">
			<select class="easyui-combobox" id="materialsState" editable="false"  style="width:25%" data-options="label:'物资状态:'"></select>
		</div>
		<div style="text-align: center;">
			<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
			<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
			<a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="creat()">新建</a>
		</div>
	</form>
	<table id="table" auto-resize="true" class="easyui-datagrid" title="物资列表" >
	</table>
</div>
<script type="text/javascript">
	$('#materialsCategory').combobox({
		url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=MaterialsTypeCategory',
		valueField: 'dictionaryCode',
		textField: 'dictionaryName',
		multiple:true
	});
	$('#materialsState').combobox({
		url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=materialsState',
		valueField: 'dictionaryCode',
		textField: 'dictionaryName',
		multiple:true
	});
	$(function(){
		query();
	});
	function query(){
		$('#table').datagrid({
			url:'${emms}/baseinfo/materials.do?cmd=loadMaterialListData',
			method: 'POST',
			pagination: true,
			fitColumns: false,
			rownumbers: true,
			showFooter: true,
			singleSelect: true,
			queryParams: {
				"materialsCode" : $("#materialsCode").val(),
				"materialsDescribe" : $("#materialsDescribe").val(),
				"materialsCategory" : $('#materialsCategory').combobox('getValue'),
				"materialsState" : $('#materialsState').combobox('getValue')

			},
			/*toolbar: [{
				iconCls: 'icon-remove',
				handler: function(){
					deleteRow("table");
				}
			}],*/
			onLoadSuccess: function(){
				$(this).datagrid('freezeRow',-1).datagrid('freezeRow',-1);
			},
			columns:[[
				{field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
				{field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'25%'},
				{field:'materialsCategory',sortable:true,title:'是否设备',align:'center',width:'10%',
					formatter: function(value,row,index){
						if (value == "w") {
							return "N";
						} else if (value == "s") {
							return "Y";
						}
					}
				},
				{field:'materialsUnitMain',sortable:true,title:'统计计量单位',align:'center'},
				{field:'additional1',sortable:true,title:'附加1',align:'center',width:'10%'},
				{field:'additional2',sortable:true,title:'附加2',align:'center',width:'10%'},
				{field:'additional3',sortable:true,title:'附加3',align:'center',width:'10%'},
				{field:'additional4',sortable:true,title:'附加4',align:'center',width:'10%'},
				{field:'materialsState',sortable:true,title:'编码状态',align:'center',
					formatter: function(value,row,index){
						if (value == "new") {
							return "未提交";
						} else if (value == "commit") {
							return "已提交";
						}else if (value == "approving") {
							return "审批中";
						}else if (value == "success") {
							return "审批通过";
						}else if (value == "failure") {
							return "审批不通过";
						}
					}
				},
				{field:'aaa',title:'操作',sortable:true,align:'center',width:'12%',
					formatter: function(value,row,index){
						var show = '';
						if(row.materialsState == 'new'){
							show = '<a href="javascript:void(0)" class="easyui-linkbutton" '
							+ ' target="_self" onclick="view(' + "'" + row.materialsId + "'" + ')">编辑</a>&nbsp;&nbsp;&nbsp;';
							show += "<a href=\"javascript:commit(\'"+ row.materialsId + "\')\">提交</a>&nbsp;&nbsp;&nbsp;";
							show += "<a href=\"javascript:deleteRow(\'"+ row.materialsId + "\')\">删除</a>&nbsp;&nbsp;&nbsp;";
						}
						return show;
					}
				}
			]]
		});
	}
	//新建
	function creat(){
		top.$('#dialog').dialog({
			title: '新建物资',
			width: 1000,
			height: 600,
			closed: false,
			cache: true,
			href: '${emms}/baseinfo/materials.do?cmd=edit'
		});
	}
	//查看
	function view(materialsId){
		top.$('#dialog').dialog({
			title: '新建物资',
			width: 1000,
			height: 600,
			closed: false,
			cache: true,
			href: '${emms}/baseinfo/materials.do?cmd=edit&materialsId=' + materialsId
		});
	}
	function clearForm(){
		$('#query').form('clear');
	}
	function checkMaterials(rows){
		for(var i=0;i<rows.length;i++){
			$('#table').datagrid('appendRow',{
				"materialsCode": rows[i].materialsCode,
				"materialsDescribe": rows[i].materialsDescribe
			});
		}
	}
	//删除部件信息
	function deleteRow(id){
			$.messager.confirm("操作提示", "确定要提交当前记录吗？", function (data) {
				if(data){
					$.ajax({
						type: 'POST',
						url: "${emms}/baseinfo/materials.do?cmd=delete&materialsId=" + id,
						contentType: "application/json;charset=utf-8",
						success: function (result) {
							if(result=='true'){
								$.messager.alert("操作提示","删除成功","info");
								query();
							}else{
								$.messager.alert("操作提示","删除失败","error");
							}
						}
					});
				}
			});
	}

	function commit(materialsId){
		$.messager.confirm("操作提示", "确定要提交物料吗？", function (data) {
			if(data){
				$.ajax({
					type: "POST",
					url:"${emms}/baseinfo/materials.do?cmd=commit&materialsId="+materialsId,
					async: false,
					success: function(data) {
						if(data == 'true'){
							$.messager.alert("操作提示", "提交成功！","info",function(data){
								query();
							});
						}else{
							$.messager.alert("操作提示", data,"warning");
						}
					}
				});
			}
		});
	}
</script>
</body>
</html>