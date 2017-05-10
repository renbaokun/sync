<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
	<div class="easyui-panel" title="首页->实物出库管理->出库管理->查询" data-options="fit:true,border:false">
		<form id="query" method="post">
			<div style="margin:20px">
				<input class="easyui-textbox" id="outWarehouseNo"  style="width:30%" data-options="label:'出库单编号:'">
				<input class="easyui-combobox" id="contractor" style="width:30%" data-options="label:'施工单位:'">
				<input class="easyui-combobox" id="outWarehouseState" style="width:30%" data-options="label:'单据状态:'">
			</div>
			<div style="margin:20px">
				<input class="easyui-textbox" id="createUserName" style="width:30%" data-options="label:'录入人:'">
				<input class="easyui-datebox" id="beginTime" editable="false" style="width:18%" data-options="label:'录入时间:'">~
				<input class="easyui-datebox" id="endTime" editable="false" style="width:12%">
			</div>
			<div style="text-align: center;">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
			</div>
		</form>
		<table id="table" auto-resize="true" class="easyui-datagrid" title="出库单列表" width="100%">
		</table>
	</div>
	<script type="text/javascript">
		$('#contractor').combobox({
			url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
			valueField: 'orgId',
			textField: 'orgName',
			multiple:false
		});
		$('#outWarehouseState').combobox({
			url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=outWarehouse_state',
			valueField: 'dictionaryCode',
			textField: 'dictionaryName',
			multiple:false
		});
			$(function(){
				query();

			});
			function query(){
				$('#table').datagrid({
				    url:'${emms}/outstorage/outwarehouse.do?cmd=loadOutWarehouseListData',
				    method: 'POST',
				    pagination: true,
				    fitColumns: true,
				    rownumbers: true,
				    showFooter: true,
					singleSelect:true,
				    queryParams: {
				    	"outWarehouseNo" : $("#outWarehouseNo").val(),
				    	"supplierId" : $("#contractor").combo('getValue'),
						"outWarehouseState" : $("#outWarehouseState").val(),
						"createUserName" : $("#createUserName").val(),
						"beginTime" : $('#beginTime').datebox('getValue'),
						"endTime" : $('#endTime').datebox('getValue')
					},
				    columns:[[
				        {field:'outWarehouseNo',sortable:true,title:'出库单编号',align:'center',width:'15%',
							formatter: function(value,row,index){
								return '<a class="easyui-linkbutton" style="color:blue" href="${emms}/outstorage/outwarehouse.do?cmd=editOutWarehousePage&outWarehouseId='+row.outWarehouseId+'" target="_self">'+row.outWarehouseNo+'</a>';
							}
						},
						{field:'contractor',sortable:true,title:'施工单位',align:'center',width:'15%',
							formatter: function(value,row,index){
								if(null == value){
									return null;
								}else{
									return value.orgName;
								}
							}
						},
				        {field:'outTime',sortable:true,title:'出库时间',align:'center',width:'15%'},
						{field:'createUserName',sortable:true,title:'录入人',align:'center',width:'14%'},
						{field:'createTime',sortable:true,title:'录入时间',align:'center',width:'14%'},
						{field:'outWarehouseState',sortable:true,title:'单据状态',align:'center',width:'14%'},
				        {field:'aaa',title:'操作',sortable:true,align:'center',width:'14%',
							formatter: function(value,row,index){
								var show = '';
								if(row.outWarehouseState == '新建'){
									show += "<a href=\"javascript:commitOutWarehouse(\'"+ row.outWarehouseId + "\')\">提交</a>&nbsp;&nbsp;&nbsp;";
									show += "<a href=\"javascript:delOutWarehouse(\'"+ row.outWarehouseId + "\')\">删除</a>&nbsp;&nbsp;&nbsp;";
								}
								if(row.outWarehouseState == '提交'){
									show += "<a href=\"javascript:confirmOutWarehouse(\'"+ row.outWarehouseId + "\')\">出库确认</a>&nbsp;&nbsp;&nbsp;";
								}
								return show;
							}}
				    ]]
				});
			}
			function clearForm(){
				$('#query').form('clear');
			}
			function delOutWarehouse(outWarehouseId){
				$.messager.confirm("操作提示", "确定要删除当前记录吗？", function (data) {
					if(data){
						$.ajax({
							type: "POST",
							url:"${emms}/outstorage/outwarehouse.do?cmd=delete&&outWarehouseId="+outWarehouseId,
							type:"GET",
							success: function(data) {
								if(data == 'true'){
									$.messager.alert("操作提示", "删除成功！","info");
									query();
								}else{
									$.messager.alert("操作提示", data,"warning");
								}
							}
						});
					}
				});
			}
			function commitOutWarehouse(outWarehouseId){
				$.messager.confirm("操作提示", "确定提交吗？", function (data) {
					if(data){
						$.ajax({
							type: "POST",
							url:"${emms}/outstorage/outwarehouse.do?cmd=commit&outWarehouseId="+outWarehouseId,
							type:"GET",
							success: function(data) {
								if(data =='true'){
									$.messager.alert("操作提示", "提交成功！","info");
									query();
								}else{
									$.messager.alert("操作提示", data,"warning");
								}
							}
						});
					}
				});
			}
			function confirmOutWarehouse(outWarehouseId){
				$.messager.confirm("操作提示", "确定出库完成吗？", function (data) {
					if(data){
						$.ajax({
							type: "POST",
							url:"${emms}/outstorage/outwarehouse.do?cmd=confirm&outWarehouseId="+outWarehouseId,
							type:"GET",
							success: function(data) {
								if(data == 'true'){
									$.messager.alert("操作提示", "出库完成！","info");
									query();
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