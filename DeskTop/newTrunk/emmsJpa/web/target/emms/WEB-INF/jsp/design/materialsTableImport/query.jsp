<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>

<body>
	<div class="easyui-panel" title="首页->料表导入" data-options="fit:true,border:false">
		<form id="query" method="post">
			<div style="padding:10px">
				<input class="easyui-textbox" id="materialsTableCode"  style="width:30%" data-options="label:'料表编号:'">
				<select class="easyui-combobox" id="designOrgId" editable="false" style="width:30%" data-options="label:'设计院:'"></select>
				<select class="easyui-combobox" id="materialsTableType" editable="false" style="width:30%" data-options="label:'物资类别:'"></select></br></br>
				<input class="easyui-datebox" id="startCreatTime"  editable="false" style="width:18%" data-options="label:'导入时间:'"> -
				<input class="easyui-datebox" id="endCreatTime"  editable="false" style="width:10%">
			</div>
			<div style="text-align: center;">
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
			</div>
		</form>
		
		<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="popUpBox()">导入料表</a>
		<a href="${emms}/upload/material.xlsx" class="easyui-linkbutton" iconCls='icon-save'>下载物资料表导入模板</a>
		<a href="${emms}/upload/equipment.xlsx" class="easyui-linkbutton" iconCls='icon-save'>下载设备料表导入模板</a>
		<table id="table" auto-resize="true" class="easyui-datagrid" title="料表列表" width="100%">
		</table>
	</div>
	<script type="text/javascript">
		$('#designOrgId').combobox({
			url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=design',
			valueField: 'orgId',
			textField: 'orgName',
			multiple:false
		});
		$('#materialsTableType').combobox({
			url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=MaterialsTypeCategory',
			valueField: 'dictionaryCode',
			textField: 'dictionaryName'
		});
		$(function(){
				query();
			});
			function query(){
				$('#table').datagrid({
				    url:'${emms}/design/materialstableImprot.do?cmd=loadMTData',
				    method: 'POST',
				    pagination: true,
				    fitColumns: true,
				    rownumbers: true,
				    showFooter: true,
				    singleSelect: true,
					toolbar: [{
						iconCls: 'icon-remove',
						handler: function(){
							deleteRow("table");
						}
					}],
				    queryParams: {
				    	"materialsTableCode" : $("#materialsTableCode").val(),
				    	"designOrgId" : $("#designOrgId").combobox('getValue'),
				    	"materialsTableType" : $('#materialsTableType').combobox('getValue'),
				    	"startCreatTime" : $('#startCreatTime').datebox('getValue'),
				    	"endCreatTime" : $('#endCreatTime').datebox('getValue')
					},
				    columns:[[
				       	{field:'materialsTableCode',sortable:true,title:'料表编号',align:'center',width:'15%',
							formatter: function (value, row, index) {
								if(row.isMatch == 1 && row.materialsTableState == 'drawingNumber'){
                                 return '<a class="easyui-linkbutton" onclick="MTdetail('+ "'"+ row.materialsTableId + "'" +  ",'" + row.materialsTableType + "'" + ",'" + "view" + "'" + ')">'
										 + row.materialsTableCode+'</a>';
								}else{
									return value;
								}
							}},
				       	{field:'designOrgName',sortable:true,title:'设计院',align:'center',width:'15%'},
				       	{field:'materialsTableType',sortable:true,title:'类型',align:'center',width:'10%',
							formatter:function (value, row, index){
								if(value=='w'){
									return '材料';
								}else{
									return '设备';
								}
							}},
				       	{field:'createTime',sortable:true,title:'导入时间',align:'center',width:'15%'},
						{field:'createUserName',sortable:true,title:'录入人',align:'center',width:'15%'},
						{field:'materialsTableState',sortable:true,title:'导入状态',align:'center',width:'10%',
							formatter:function (value, row, index){
								if(value=="checkPass"){
									return "校验通过";
								}else if(value=="checkNotPass"){
									return "校验不通过";
								}else if(value=="drawingNumber"){
									return "已提交";
								}
							}
						},
				        {field:'aaa',title:'操作',sortable:true,align:'center',width:'20%',
							formatter: function(value,row,index){
								var type = row.materialsTableType;
								show = '<a class="easyui-linkbutton" href="${emms}/design/materialstableImprot.do?'
										 + 'cmd=downloadOriginalFile&materialsTableId=' + row.materialsTableId
										 + '">'
										 + '下载文件</a>&nbsp;&nbsp;&nbsp;';
								if(row.materialsTableState == 'checkPass'){
									if(row.isMatch!=1){
									show += '<a class="easyui-linkbutton" onclick="MTdetail('
									+ "'" + row.materialsTableId + "'"
									+  ",'" + type + "'" + ",'" + "edit" + "'"
									+ ')">'
									+ '编辑</a>&nbsp;&nbsp;&nbsp;';
									}
									show += '<a class="easyui-linkbutton" onclick="submitMT('
									+  "'" + row.materialsTableId + "'"
									+  ',this)">提交</a>&nbsp;&nbsp;&nbsp;';
								}else if(row.materialsTableState == 'checkNotPass'){
									show +=	'<a class="easyui-linkbutton" onclick="viewAndModifyErrors('
											+  "'" + row.materialsTableId + "'"
											+  ",'" + type + "'"
											+  ')">查看错误报告并修改</a>&nbsp;&nbsp;&nbsp;';
								}else if(row.isMatch == 1){
									show += '<a class="easyui-linkbutton" onclick="MTdetail('
											+ "'" + row.materialsTableId + "'"
											+  ",'" + type + "'" + ",'" + "view" + "'"
											+ ')">'
											+ '查看</a>';
								}
								return show;
							}}
				    ]]
				});
			}
			function clearForm(){
				$('#query').form('clear');
			}
			//查看和更正错误弹窗
			function viewAndModifyErrors(id, type){
				top.$('#dialog').dialog({
				    title: '查看错误报告并修改',
				    width: 1000,
				    height: 600,
				    closed: false,
				    cache: true,
				    href: '${emms}/design/materialstableImprot.do?cmd=viewAndModifyErrors&materialsTableId='+ id +'&materialsTableType='+ type
				});
			}
			//文件上传弹窗
			function popUpBox(){
				top.$('#dialog').dialog({
				    title: '文件上传弹窗',
				    width: 500,
				    height: 230,
				    closed: false,
				    cache: true,
				    href: "${emms}/design/materialstableImprot.do?cmd=uploadPopUpBox"
				});
			}
			function MTdetail(id, type, operation){
				top.$('#dialog').dialog({
				    title: '查看料表详情',
				    width: 1000,
				    height: 500,
				    closed: false,
				    cache: true,
				    href: '${emms}/design/materialstableImprot.do?cmd=queryMTdetailed&materialsTableId='+ id +'&materialsTableType='+ type + '&operation=' + operation
				});
			}
			function submitMT(id){
				var url;
					url = '${emms}/design/materialstableImprot.do?cmd=commitMT&materialsTableId='+ id;
					$.ajax({
						type: "POST",
						url: url,
						async: false,
						success: function(data) {
							if(data > 0){
								query(id);
								notMatch(id)
							}else{
								$.messager.confirm("操作提示", "您确定要执行操作吗？", function (data) {
									if(data){
										console.log(1,data);
										url = '${emms}/design/materialstableImprot.do?cmd=updateMTtoAlreadyGenerateDrawingNumber&materialsTableId='+ id;
										$.ajax({
											type: "POST",
											url: url,
											async: false,
											success: function(data) {
												if(data=='true'){
													$.messager.alert("提示","提交成功","info",function(){
														query();
													});
												}
											}
										});
									}else{
										console.log(2,data);
									}
								})
							}
						}
					});
			}
		    function notMatch(id){
				url = '${emms}/design/materialstableImprot.do?cmd=viewTheResultsOfThisSubmission&materialsTableId='+ id;
				viewTheResultsOfThisSubmission(url);
			}
			function viewTheResultsOfThisSubmission(url){
				top.$('#dialog').dialog({
					title: '查看料表详情',
					width: 1000,
					height: 500,
					closed: false,
					cache: true,
					href: url
				});
			}

			function queryAllDetail(){
				top.$('#dialog').dialog({
					title: '料表图号明细弹出页',
					width: 1000,
					height: 500,
					closed: false,
					cache: true,
					href: '${emms}/design/materialstableImprot.do?cmd=queryAllDetail'
				});
			}


			//删除
			function deleteRow(tableName){
				var row = $('#'+tableName).datagrid('getRowIndex',$('#'+tableName).datagrid('getSelected'));
				if (row>-1){
					var materialsTableId = $('#'+tableName).datagrid('getSelected').materialsTableId;
					var state = $('#'+tableName).datagrid('getSelected').materialsTableState
					if(state!="checkPass" && state!="checkNotPass"){
						$.messager.alert("提示","该料表已经过提交无法删除！");
					}else{
						if(confirm('是否确认删除？')){
							$.ajax({
								type: 'POST',
								url: "${emms}/design/materialstableImprot.do?cmd=deleteEntireMT&materialsTableId=" + materialsTableId,
								contentType: "application/json;charset=utf-8",
								success: function (result) {
									if(result==true){
										$('#'+tableName).edatagrid('deleteRow',row);
										$.messager.alert("提示","删除成功");
										query();
									}else{
										$.messager.alert("提示","删除失败");
									}
								}
							});
						}
					}
				}else{
					$.messager.alert('提示','请选择一行');
				}
			}

		</script>

</body>
</html>
