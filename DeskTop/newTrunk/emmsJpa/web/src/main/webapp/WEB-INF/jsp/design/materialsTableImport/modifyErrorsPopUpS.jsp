<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>编辑页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>

<body>

<div class="easyui-panel" title="首页->料表导入->查看并修改错误条目" data-options="fit:true,border:false">

	<table id="MT" auto-resize="true" class="easyui-datagrid" title="设备料表头信息" width="100%">
	</table>

	<table id="containD" auto-resize="true" class="" title="错误条目" width="100%">
	</table>

	<div style="text-align: center;">
		<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmit()">保存</a>
	</div>

</div>
<script type="text/javascript">
	var dictionary= {};
	$.ajax({
		url:'${emms}/baseinfo/project.do?cmd=selectToOrderCombotree',
		dataType : 'json',
		type : 'GET',
		async:false,
		success: function (data){
			wbsTree = data;
			getChildren(data);
		}
	});
	function getChildren(childrens){
		for(var i=0;i<childrens.length;i++){
			var obj=childrens[i].id;
			dictionary[obj]=childrens[i].text;
			if(childrens[i].children.length>0){
				getChildren(childrens[i].children)
			}
		}
	}
	function getValueByKey(key){
		return dictionary[key];
	}
	var lastIndex;
	$(function(){
		$('#MT').datagrid({
			url:'${emms}/design/materialstableImprot.do?cmd=loadMT',
			method: 'POST',
			pagination: false,
			fitColumns: true,
			rownumbers: true,
			showFooter: true,
			singleSelect: true,
			queryParams: {
				"materialsTableId" : "${materialsTableId}"
			},
			columns:[[
				{field:'materialsTableCode',sortable:true,title:'料表名',align:'center',width:'25%'},
				{field:'designOrgName',sortable:true,title:'设计院',align:'center',width:'25%'},
				{field:'materialsTableType',sortable:true,title:'类型',align:'center',width:'25%',
					formatter:function (value, row, index){
						if(value=='w'){
							return '材料';
						}else{
							return '设备';
						}
					}},
				{field:'createTime',sortable:true,title:'导入时间',align:'center',width:'25%'}
			]]
		});

		$('#containD').datagrid({
			url:'${emms}/design/materialstableImprot.do?cmd=loadErrorTypeContainD',
			method: 'POST',
			pagination: false,
			fitColumns: true,
			rownumbers: true,
			showFooter: true,
			singleSelect: true,
			queryParams: {
				"materialsTableId" : "${materialsTableId}"
			},
			toolbar: [{
				iconCls: 'icon-edit',
				handler: function(){
					var table = $('#containD');
					var data = $("#containD").datagrid("getData");
					for(var i=0; i<data.total; i++){
						table.datagrid('beginEdit', i);
					}
				}
			},'-',{
				iconCls: 'icon-ok',
				handler: function(){
					var table = $('#containD');
					var data = $("#containD").datagrid("getData");
					for(var i=0; i<data.total; i++){
						table.datagrid('endEdit', i);
					}
				}
			},'-',{
				iconCls: 'icon-remove',
				handler: function(){
					deleteRow("containD");
				}
			}],
			columns:[[
				{field:'designCode',sortable:true,title:'设备编号', align:'center',width:'15%',editor:{type:'validatebox',options:{required:true, validType:'length[0,30]'}}},
				{field:'drawingNumberCode',sortable:true,title:'设计图编号', align:'center',width:'15%',editor:{type:'validatebox',options:{required:true, validType:'length[0,50]'}}},
				{field:'drawingNumberVersion',sortable:true,title:'版次', align:'center',width:'6%',editor:{type:'numberbox',options:{required:true, validType:'length[0,10]',validtype:"intOrFloat"}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'projectId',sortable:true,title:'WBS编码', align:'center',width:'10%',
					formatter: function(value,row,index){
						console.log(value);
						return getValueByKey(value);
					},
					editor:{type:'combotree',options:{required:true,editable:false,method:'get',url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree'}}
				},
				{field:'drawingDetailedNo',sortable:true,title:'序号', align:'center',width:'8%',editor:{type:'numberbox',options:{required:true, validType:'length[0,10]',validtype:"intOrFloat"}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'drawingNumberDeviceNo',sortable:true,title:'位号', align:'center',width:'10%',editor:{type:'validatebox',options:{validType:'length[0,200]'}}},
				{field:'designDescribe',sortable:true,title:'设备描述', align:'center',width:'15%',editor:{type:'validatebox',options:{required:true, validType:'length[0,200]'}}},
				{field:'totalCount',sortable:true,title:'设备数量', align:'center',editor:{type:'numberbox',options:{required:true, validType:'length[0,10]',validtype:"intOrFloat"}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'designUnit',sortable:true,title:'设备计量单位', align:'center',editor:{type:'validatebox',options:{required:true, validType:'length[0,200]'}}},
			]],
			view: detailview,
			detailFormatter: function(rowIndex, rowData){
				if(null != rowData.errorMessage){
					var v = rowData.errorMessage;
					return v;
				}
			}
		});

	});

	function ajaxSubmit() {
		var mt = $("#MT").datagrid("getData");
		var table =  $("#containD").datagrid("getData");
		console.log($("#containD"));
		for(var i=0;i<table.rows.length;i++){
			console.log(table.rows[i]);
			if(table.rows[i].drawingDetailedType=='s' &&(table.rows[i].drawingNumberVersion==-1  || table.rows[i].drawingNumberCode==null  || table.rows[i].designCode==null  || table.rows[i].drawingDetailedNo==-1 || table.rows[i].designDescribe==null || table.rows[i].designCount==-1 || table.rows[i].designUnit==null)){
				$.messager.alert("提示","设备编号、序号、设备描述、设备数量、设备计量单位为必填项");
				return false;
			}
		}
		var ddl = {
			"detailList":table.rows,
			"designOrgId":mt.rows[0].designOrgId
		};
		$.ajax({
			type: 'POST',
			url: "${emms}/design/materialstableImprot.do?cmd=saveModifyErrors",
			data: JSON.stringify(ddl),
			dataType: 'json',
			contentType: "application/json;charset=utf-8",
			success: function (result) {
				$.messager.alert("提示","保存成功");
				top.$("#dialog").dialog("close");
				window.parent.frames["mainFrame"].query();
			}
		});
	}

	//删除
	function deleteRow(tableName){
		console.log(tableName);
		var row = $('#'+tableName).datagrid('getRowIndex',$('#'+tableName).datagrid('getSelected'));
		console.log(row);
		if (row>-1){
			var drawingDetailedId = $('#'+tableName).datagrid('getSelected').drawingDetailedId;
			if(confirm('是否确认删除？')){
				$.ajax({
					type: 'POST',
					url: "${emms}/design/materialstableImprot.do?cmd=deleteRow&drawingDetailedId=" + drawingDetailedId,
					contentType: "application/json;charset=utf-8",
					success: function (result) {
						if(result==true){
							$('#'+tableName).edatagrid('deleteRow',row);
							$.messager.alert("提示","删除成功");
						}else{
							$.messager.alert("提示","删除失败");
						}
					}
				});
			}
		}else{
			$.messager.alert('提示','请选择一行');
		}
	}

</script>
</body>
</html>
