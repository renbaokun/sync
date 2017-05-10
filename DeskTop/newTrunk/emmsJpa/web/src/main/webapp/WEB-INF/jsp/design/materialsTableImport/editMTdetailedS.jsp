<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>编辑页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
	
<body>
	<div class="easyui-panel" title="首页->设备料表编辑" data-options="fit:true,border:false">

		<table id="MT" auto-resize="true" class="" title="设备料表信息" width="100%">
		</table>

		<table id="ST" auto-resize="true" class="" title="设备信息" width="100%"></table>

		<div style="text-align: center;">
			<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmit()">保存</a>
		</div>
		
	</div>
	<script type="text/javascript">
		$('#MT').datagrid({
			url:'${emms}/design/materialstableImprot.do?cmd=loadMT',
			method: 'POST',
			pagination: false,
			fitColumns: true,
			rownumbers: true,
			showFooter: false,
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

		//为array添加 根据id移除元素的方法
		Array.prototype.remove=function(dx)
		{
			if(isNaN(dx)||dx>this.length){return false;}
			for(var i=0,n=0;i<this.length;i++)
			{
				if(this[i]!=this[dx])
				{
					this[n++]=this[i]
				}
			}
			this.length-=1
		}
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

		var lastIndexMain = null;
		var lastIndexs = new Array();
		var parentIsEdit = new Array();//0：处于表格状态，1：处于编辑状态
		var isReload = false;
		var childrenNode = new Array();
		var childrenTotal = 0;
		var ddv = null;
		$('#ST').datagrid({
			url:'${emms}/design/materialstableImprot.do?cmd=loadMTdetailed&materialsTableId=${materialsTableId}',
			method: 'POST',
			pagination: false,
			fitColumns: true,
			rownumbers: true,
			showFooter: true,
			singleSelect: true,
			onLoadSuccess: function(data){//加载后对数据进行处理，仅留下父节点
				if(!isReload){
					var total = data.total
					for(var i=0; i<total; i++){
						if(data.rows[i]._parentId!=null){
							childrenNode[childrenTotal] = data.rows[i];
							data.rows.remove(i);
							childrenTotal++;
							i--;
							total--;
						}
					}
					isReload = !isReload;
					$('#ST').datagrid('loadData', data);
				}
			},
			onExpandRow: function(index,row){//初始化嵌套网格
				ddv = $(this).datagrid('getRowDetail',index).find('table.ddv');
				if(ddv==null){//如果没有数据则不执行
					return;
				}
				var n = 0;
				var childrenData = new Object();
				childrenData.rows = new Array();
				var pid = $('#ST').datagrid('getRows')[index].drawingDetailedId;
				for(var i=0; i<childrenTotal; i++){
					if(pid == childrenNode[i]._parentId){
						childrenData.rows[n] = childrenNode[i];
						n++;
					}
				}
				childrenData.total = n;

				//展开行时执行嵌套网格的初始化代码
				ddv.datagrid({
					fitColumns:true,
					singleSelect:true,
					rownumbers:true,
					loadMsg:'',
					height:'auto',
					toolbar: [{
						iconCls: 'icon-edit',
						handler: function(){
							var table = ddv;
							var data = ddv.datagrid("getData");
							for(var i=0; i<data.total; i++){
								table.datagrid('beginEdit', i);
							}
						}
					},'-',{
						iconCls: 'icon-ok',
						handler: function(){
							var table = ddv;
							var data = ddv.datagrid("getData");
							for(var i=0; i<data.total; i++){
								table.datagrid('endEdit', i);
							}
						}
					},'-',{
						iconCls: 'icon-remove',
						handler: function(){
							deleteRowB();
						}
					}],
					columns:[[
						{field:'drawingDetailedNo',sortable:true,title:'序号', align:'center',editor:{type:'numberbox',options:{validType:'length[0,10]'}},
							formatter:function (value, row, index){
								if(value==-1){
									return "";
								}else{
									return value;
								}
							}
						},
						{field:'designCode',sortable:true,title:'部件编号', align:'center',width:'15%',editor:{type:'validatebox',options:{validType:'length[0,30]'}}},
						{field:'designDescribe',sortable:true,title:'部件描述', align:'center',width:'15%',editor:{type:'validatebox',options:{validType:'length[0,200]'}}},
						{field:'designCount',sortable:true,title:'部件数量',width:'10%', align:'center',editor:{type:'numberbox',options:{validType:'length[0,30]'}},
							formatter:function (value, row, index){
								if(value==-1){
									return "";
								}else{
									return value;
								}
							}
						},
						{field:'designUnit',sortable:true,title:'部件计量单位', align:'center',width:'10%',editor:{type:'validatebox',options:{validType:'length[0,200]'}}},
						{field:'unitWeight',sortable:true,title:'单位重量', align:'center',width:'10%',editor:{type:'numberbox',options:{validType:'length[0,30]'}},
							formatter:function (value, row, index){
								if(value==-1){
									return "";
								}else{
									return value;
								}
							}
						},
						{field:'totalWeight',sortable:true,title:'总重', align:'center',width:'10%',editor:{type:'numberbox',options:{validType:'length[0,30]'}},
							formatter:function (value, row, index){
								if(value==-1){
									return "";
								}else{
									return value;
								}
							}
						},
						{field:'partAttributes',sortable:true,title:'部件属性',width:'10%', align:'center',editor:{type:'validatebox',options:{validType:'length[0,10]'}}},
						{field:'remark',sortable:true,title:'备注', align:'center',width:'15%',editor:{type:'validatebox',options:{validType:'length[0,200]'}}},
						/*{field:'aaa',title:'操作',sortable:true,align:'center',width:'10%',
							formatter: function(value,row,index){
								show = '<a class="easyui-linkbutton" onclick="moveNode('
								+ "'" + row.materialsTableId + "',this"
								+ ')">'
								+ '移动节点</a>&nbsp;&nbsp;&nbsp;';
								return show;
							}
						}*/
					]],
					onResize:function(){
						$('#ST').datagrid('fixDetailRowHeight',index);
					},
					onLoadSuccess:function(){
						setTimeout(function(){
							$('#ST').datagrid('fixDetailRowHeight',index);
						},0);
					}
				});
				$('#ST').datagrid('fixDetailRowHeight',index);
				ddv.edatagrid("loadData", childrenData);
			},
			toolbar: [{
				iconCls: 'icon-edit',
				handler: function(){
					var table = ddv;
					if(table !=undefined ){
						console.log(1);
						var data = ddv.datagrid("getData");
						console.log(3);
						if(data !=undefined){
							for(var i=0; i<data.total; i++){
								table.datagrid('endEdit', i);
							}
						}
					}
					var table = $('#ST');
					var data = $("#ST").datagrid("getData");
					for(var i=0; i<data.total; i++){
						table.datagrid('beginEdit', i);
						$('.datagrid-row-expander').eq(i).click();
					}
				}
			},'-',{
				iconCls: 'icon-ok',
				handler: function(){
					var table = $('#ST');
					var data = $("#ST").datagrid("getData");
					for(var i=0; i<data.total; i++){
						table.datagrid('endEdit', i);
						$('.datagrid-row-expander').eq(i).click();
					}
				}
			},'-',{
				iconCls: 'icon-remove',
				handler: function(){
					deleteRowS();
				}
			}],
			columns:[[
				{field:'designCode',sortable:false,title:'设备编号', align:'center',width:'15%',editor:{type:'validatebox',options:{validType:'length[0,50]'}}},
				{field:'drawingNumberCode',sortable:false,title:'设计图编号',width:'15%', align:'center'},
				{field:'drawingNumberVersion',sortable:false,title:'版次', align:'center',
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'projectId',sortable:false,title:'WBS编码', align:'center',width:'15%',
					formatter: function(value,row,index){
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
				{field:'drawingDetailedNo',sortable:false,title:'序号', align:'center',editor:{type:'numberbox',options:{ validType:'length[0,10]',validtype:"intOrFloat"}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'drawingNumberDeviceNo',sortable:false,title:'位号',width:'10%', align:'center'},
				{field:'designDescribe',sortable:false,title:'设备描述',width:'15%', align:'center',editor:{type:'validatebox',options:{ validType:'length[0,200]'}}},
				{field:'designCount',sortable:false,title:'设备数量', align:'center',editor:{type:'numberbox',options:{ validType:'length[0,10]',validtype:"intOrFloat"}},
					formatter:function (value, row, index){
						if(value==-1){
							return "";
						}else{
							return value;
						}
					}
				},
				{field:'designUnit',sortable:false,title:'设备计量单位', align:'center',editor:{type:'validatebox',options:{validType:'length[0,200]'}}}
			]]
			,view: detailview,
			detailFormatter: function(rowIndex, rowData){
				var n = 0;
				var pid = rowData.drawingDetailedId;
				for(var i=0; i<childrenTotal; i++){
					if(pid == childrenNode[i]._parentId){
						n++;
					}
				}
				//判断是否存在数据
				if(n==0){
					return '<span style="font-size:10px;">该条设备不存在部件</span>';
				}else{
					return '<div style="padding:2px"><table class="ddv"></table></div>';
				}
			}
		});

		//删除设备信息
		function deleteRowS(){
			var row = $('#ST').datagrid('getRowIndex',$('#ST').datagrid('getSelected'));
			console.log(row);
			if (row>-1){
				var drawingDetailedId = $('#ST').datagrid('getSelected').drawingDetailedId;
				if(confirm('是否确认删除？')){
					$.ajax({
						type: 'POST',
						url: "${emms}/design/materialstableImprot.do?cmd=deleteRowS&drawingDetailedId=" + drawingDetailedId,
						contentType: "application/json;charset=utf-8",
						success: function (result) {
							if(result==true){
								$('#ST').edatagrid('deleteRow',row);
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

		//删除部件信息
		function deleteRowB(){
			var row = ddv.datagrid('getRowIndex',ddv.datagrid('getSelected'));
			console.log(row);
			if (row>-1){
				var drawingDetailedId = ddv.datagrid('getSelected').drawingDetailedId;
				if(confirm('是否确认删除？')){
					$.ajax({
						type: 'POST',
						url: "${emms}/design/materialstableImprot.do?cmd=deleteRow&drawingDetailedId=" + drawingDetailedId,
						contentType: "application/json;charset=utf-8",
						success: function (result) {
							if(result==true){
								ddv.edatagrid('deleteRow',row);
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

		/*
		//移动节点
		function moveNode(drawingDetailedId, button){
			//获取所有父节点所在行
			var parentNodesRow =  $('table').eq(10).children().children('.datagrid-row');
			//
			var childrenNodeRows =  $('table').eq(10).children().children('tr[class!="datagrid-row"]');
			//获取当前选中的子节点所在行
			var thisRow = $(button).parent().parent().parent();
			thisRow.css("opacity","0.3");
			console.log(parentNodesRow);
			console.log(childrenNodeRows);
			console.log(thisRow);
		}
		*/

		//提交
		function ajaxSubmit() {
			var mt = $("#ST").datagrid("getData");
			for(var i=0; i<mt.total; i++){
				$("#ST").datagrid('endEdit', i);
			}
			var plus = $('.datagrid-row-expander');
			for(var i=0; i<plus.length; i++){//保存时展开所有部件以便获取数据
				if(plus.eq(i).attr("class").split(" ")[1]!="datagrid-row-collapse"){
					plus.eq(i).click();
				}
			}
			var detail = $('.ddv')
			var total = new Array();
			for(var i=0; i<detail.length; i++){//关闭编辑模式并拼接数据
				var tempData = detail.eq(i).datagrid("getData");
				for(var j=0; j<tempData.total; j++){
					detail.datagrid('endEdit', j);
				}
				total = total.concat(detail.eq(i).datagrid("getData").rows);
			}
			total = total.concat($("#ST").datagrid("getData").rows);
			for(var i=0;i<total.length;i++){
				if(total[i].drawingDetailedType=='s' &&(total[i].designCode==''  || total[i].drawingDetailedNo=='' || total[i].designDescribe=='' || total[i].designCount=='' || total[i].designUnit=='')){
					$.messager.alert("提示","设备编号、序号、设备描述、设备数量、设备计量单位为必填项");
					return false;
				}
			}
			var ddl = {
				"detailList":total,
				"designOrgId":null
			};
			console.log(total);
			$.ajax({
				type: 'POST',
				url: "${emms}/design/materialstableImprot.do?cmd=batchUpdate",
				data: JSON.stringify(ddl),
				dataType: 'json',
				contentType: "application/json;charset=utf-8",
				success: function (result) {
					if(result == "true"){
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
