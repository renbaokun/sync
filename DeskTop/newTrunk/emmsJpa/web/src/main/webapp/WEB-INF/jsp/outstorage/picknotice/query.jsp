<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
	<div class="easyui-panel" title="首页->实物出库管理->领料通知->查询" data-options="fit:true,border:false">
		<form id="query" method="post">
			<div style="margin:20px">
					领用通知编号:
				<input class="easyui-textbox" id="pickNoticeNo"  style="width:22%" >
				<input class="easyui-combobox" id="supplier" style="width:30%" data-options="label:'施工单位:'">
				<input class="easyui-combobox" id="pickNoticeState" style="width:30%" data-options="label:'单据状态:'">
			</div>
			<div style="margin:20px">
				<input class="easyui-textbox" id="createUserName" style="width:30%" data-options="label:'录入人:'">
				<input class="easyui-datebox" id="beginTime" editable="false" style="width:18%" data-options="label:'录入时间:'">~
				<input class="easyui-datebox" id="endTime" editable="false" style="width:12%">
			</div>
			<div style="text-align: center;">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
				<a href="${emms}/outstorage/pickNotice.do?cmd=editPickNoticePage" iconCls='icon-add' class="easyui-linkbutton">新建</a>
			</div>
		</form>
		<table id="table" auto-resize="true" class="easyui-datagrid" title="领料通知列表" width="100%">
		</table>
	</div>
	<script type="text/javascript">
		$('#supplier').combobox({
			url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
			valueField: 'orgId',
			textField: 'orgName',
			multiple:false
		});
		$('#pickNoticeState').combobox({
			url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=pickNoticeState',
			valueField: 'dictionaryCode',
			textField: 'dictionaryName',
			multiple:false
		});
			$(function(){

				query();

			});
			function query(){
				$('#table').datagrid({
				    url:'${emms}/outstorage/pickNotice.do?cmd=loadPickNoticeListData',
				    method: 'POST',
				    pagination: true,
				    fitColumns: true,
				    rownumbers: true,
				    showFooter: true,
					singleSelect:true,
				    queryParams: {
				    	"pickNoticeNo" : $("#pickNoticeNo").val(),
				    	"supplierId" : $("#supplier").combo('getValue'),
						"pickNoticeState" : $("#pickNoticeState").val(),
						"createUserName" : $("#createUserName").val(),
						"beginTime" : $('#beginTime').datebox('getValue'),
						"endTime" : $('#endTime').datebox('getValue')
					},
				    columns:[[
				        {field:'pickNo',sortable:true,title:'领料通知编号',align:'center',width:'15%',
							formatter: function(value,row,index){
								return '<a class="easyui-linkbutton" style="color:blue" href="${emms}/outstorage/pickNotice.do?cmd=viewPickNoticePage&pickNoticeId='+row.pickId+'" target="_self">'+row.pickNo+'</a>';
							}
						},
						{field:'supplier',sortable:true,title:'施工单位',align:'center',width:'15%',
							formatter: function(value,row,index){
//								console.log(value);
								if(null == value){
									return null;
								}else{
									return value.orgName;
								}
							}
						},
				        {field:'pickTime',sortable:true,title:'领用时间',align:'center',width:'15%'},
						{field:'createUserName',sortable:true,title:'录入人',align:'center',width:'14%'},
						{field:'createTime',sortable:true,title:'录入时间',align:'center',width:'14%'},
						{field:'pickNoticeState',sortable:true,title:'单据状态',align:'center',width:'14%',
							formatter: function(value,row,index){
								if(value == 'pickNoticeInvalid'){
									return "失效"
								}else if(value == 'pickNoticeCommit'){
									return "提交"
								}else if(value == 'pickNoticeNew'){
									return "未提交"
								}
							}},
				        {field:'aaa',title:'操作',sortable:true,align:'center',width:'14%',
							formatter: function(value,row,index){
								var show = '';
								if(row.pickNoticeState == 'pickNoticeNew' || row.pickNoticeState == 'pickNoticeInvalid'){
									show = "<a class='easyui-linkbutton' href='${emms}/outstorage/pickNotice.do?cmd=editPickNoticePage&pickNoticeId="
											+ row.pickId
											+ "' target='_self'>编辑</a>&nbsp;&nbsp;&nbsp;";
									show += "<a href=\"javascript:commitNotice(\'"+ row.pickId + "\')\">提交</a>&nbsp;&nbsp;&nbsp;";
								}
								if(row.pickNoticeState == 'pickNoticeNew'){
									show += "<a href=\"javascript:delNotice(\'"+ row.pickId + "\')\">删除</a>&nbsp;&nbsp;&nbsp;";
								}
								return show;
							}}
				    ]]
				});
			}
			function clearForm(){
				$('#query').form('clear');
			}
			function delNotice(pickId){
				$.messager.confirm("操作提示", "确定要删除当前记录吗？", function (data) {
					if(data){
						$.ajax({
							type: "POST",
							url:"${emms}/outstorage/pickNotice.do?cmd=deletePickNotice&pickId="+pickId,
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
			function commitNotice(pickId){
				$.messager.confirm("操作提示", "确定提交吗？", function (data) {
					if(data){
						$.ajax({
							type: "POST",
							url:"${emms}/outstorage/pickNotice.do?cmd=commit&pickId="+pickId,
							type:"GET",
							success: function(data) {
								if(data == 'true'){
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
			function deliveryDetailModal(){
				top.$('#dialog').dialog({
					title: '供应商发货弹出框',
					width: 900,
					height: 540,
					closed: false,
					cache: true,
					href: '${emms}/purchase/delivery/package.do?cmd=modal'
				});
			}
		</script>
</body>
</html>