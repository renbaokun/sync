<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>编辑页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
	
<body>
	
	<div class="easyui-panel" title="首页->物资料表编辑" data-options="fit:true,border:false">

		<table id="MT" auto-resize="true" class="" title="物资料表信息" width="100%">
		</table>

		<table id="detailTable" auto-resize="true" class="" title="物资明细" width="100%">
		</table>

		<div style="text-align: center;">
			<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmit()">保存</a>
		</div>

	</div>
	<script type="text/javascript">
	$(function(){

		var dictionary= {};
		$.ajax({
			url:'${emms}/baseinfo/project.do?cmd=selectToOrderCombotree',
			dataType : 'json',
			type : 'GET',
			async:false,
			success: function (data){
				wbsTree = data;
				getChildren(data);
				console.log(dictionary);
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
		$('#detailTable').datagrid({
			url:'${emms}/design/materialstableImprot.do?cmd=loadMTdetailed',
			method: 'POST',
			pagination: false,
			fitColumns: true,
			rownumbers: true,
			showFooter: true,
			singleSelect: true,
			queryParams: {
				"materialsTableId" : "${materialsTableId}"
			},
			onDblClickRow:function(index){//运用双击事件实现对一行的编辑
				$('#detailTable').edatagrid('beginEdit', index);
			},
			onClickRow:function(index) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
				$('#detailTable').edatagrid('endEdit', lastIndex);
				$('#detailTable').edatagrid('endEdit', index);
				lastIndex = index;
			},
			toolbar: [{
				iconCls: 'icon-edit',
				handler: function(){
					var table = $('#detailTable');
					var data = $("#detailTable").datagrid("getData");
					for(var i=0; i<data.total; i++){
						table.datagrid('beginEdit', i);
					}
				}
			},'-',{
				iconCls: 'icon-ok',
				handler: function(){
					var table = $('#detailTable');
					var data = $("#detailTable").datagrid("getData");
					for(var i=0; i<data.total; i++){
						table.datagrid('endEdit', i);
					}
				}
			},'-',{
				iconCls: 'icon-remove',
				handler: function(){
					deleteRow("detailTable");
				}
			}],
			columns:[[
				{field:'drawingNumberCode',sortable:true,title:'设计图编号',align:'center',width:'10%'},
				{field:'drawingNumberVersion',sortable:true,title:'版次',align:'center',
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'projectId',sortable:true,title:'WBS编码',align:'center',
					formatter: function(value,row,index){
						console.log(value);
						return getValueByKey(value);
					},
					editor:{
						type:'combotree',
						options:{
							required:true,
							editable:false,
							method:'get',
							url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree'
						}
					}
				},
				{field:'drawingDetailedNo',sortable:true,title:'序号',align:'center',
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'designCode',sortable:true,title:'设计院物资编码',align:'center',editor:{type:'validatebox',options:{required:true, validType:'length[0,50]'}}},
				{field:'designDescribe',sortable:true,title:'物资描述',align:'center',width:'10%',editor:{type:'validatebox',options:{required:true, validType:'length[0,200]'}}},
				{field:'designUnit',sortable:true,title:'计量单位',align:'center',editor:{type:'validatebox',options:{required:true, validType:'length[0,200]'}}},
				{field:'designCount',sortable:true,title:'设计数量',align:'center',editor:{type:'numberbox',options:{validType:'length[0,30]'}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'overrun',sortable:true,title:'裕量',align:'center',editor:{type:'numberbox',options:{validType:'length[0,30]'}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'totalCount',sortable:true,title:'总量',align:'center',editor:{type:'numberbox',options:{validType:'length[0,30]'}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'extra1',sortable:true,title:'附加1',align:'center',width:'8%',editor:{type:'validatebox',options:{validType:'length[0,100]'}}},
				{field:'extra2',sortable:true,title:'附加2',align:'center',width:'8%',editor:{type:'validatebox',options:{validType:'length[0,100]'}}},
				{field:'extra3',sortable:true,title:'附加3',align:'center',width:'7%',editor:{type:'validatebox',options:{validType:'length[0,100]'}}},
				{field:'extra4',sortable:true,title:'附加4',align:'center',width:'7%',editor:{type:'validatebox',options:{validType:'length[0,100]'}}}
			]]
		});

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
					}
				},
				{field:'createTime',sortable:true,title:'导入时间',align:'center',width:'25%'}
			]]
		});
	});


	//删除设备信息
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


	//提交
	function ajaxSubmit() {
		var mt = $("#detailTable").datagrid("getData");
		for(var i=0; i<mt.total; i++){
			$("#detailTable").datagrid('endEdit', i);
		}
		var ddl = {
			"detailList":$("#detailTable").datagrid("getData").rows,
			"designOrgId":null
		};
		$.ajax({
			type: 'POST',
			url: "${emms}/design/materialstableImprot.do?cmd=batchUpdate",
			data: JSON.stringify(ddl),
			dataType: 'json',
			contentType: "application/json;charset=utf-8",
			success: function (result) {
				if(result =="true"){
					$.messager.alert("提示","保存成功");
					top.$("#dialog").dialog("close");
					window.parent.frames["mainFrame"].query();
				}
			}
		});
	}
	</script>
</body>
</html>
