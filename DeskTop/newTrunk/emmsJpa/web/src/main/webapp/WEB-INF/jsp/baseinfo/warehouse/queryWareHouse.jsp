<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
		<div class="easyui-panel" title="首页->基本信息管理->仓库管理->仓库查询" data-options="fit:true,border:false">
			<form id="query" method="post">
				<div style="padding:10px">
					<input class="easyui-textbox" id="warehouseCode"  style="width:30%" data-options="label:'仓库编码:'">
					<input class="easyui-textbox" id="warehouseName" style="width:30%" data-options="label:'仓库名称:'">
				    <select class="easyui-combobox" id="warehouseType"  style="width:30%" data-options="label:'仓库类型:',editable:false"></select>
				</div>
				<div style="text-align: center;">
					<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
					<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
					<a href="${emms}/baseinfo/warehouse.do?cmd=queryWareHouseInfo" iconCls='icon-add' class="easyui-linkbutton">新建</a>
				</div>
			</form>
			<table id="table" auto-resize="true" class="easyui-datagrid" title="仓库列表">
			</table>
		</div> 
		<script type="text/javascript">
			$('#warehouseType').combobox({
				url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=warehousetype',
				valueField: 'dictionaryCode',
				textField: 'dictionaryName',
				multiple:false
			});
			$(function(){

				query();
			});
			function query(){
				$('#table').datagrid({
				    url:'${emms}/baseinfo/warehouse.do?cmd=selectWareHouse',
				    method: 'POST',
				    pagination: true,
				    fitColumns: true,
				    rownumbers: true,
				    showFooter: true,
				    singleSelect:true,
				    queryParams: {
				    	"warehouseCode" : $("#warehouseCode").val(),
				    	"warehouseName" : $("#warehouseName").val(),
				    	"warehouseType" : $('#warehouseType').combobox('getValue')
					},
				    columns:[[
				        {field:'warehouseCode',sortable:true,title:'仓库编码',align:'center',width:'15%'},
				        {field:'warehouseName',sortable:true,title:'仓库名称',align:'center',width:'15%'},
				        {field:'country',sortable:true,title:'国家',align:'center',width:'15%'},
				        {field:'city',sortable:true,title:'市',align:'center',width:'12%'},
				        {field:'area',sortable:true,title:'区',align:'center',width:'12%'},
				        {field:'acreage',sortable:true,title:'面积',align:'center',width:'10%'},
				        {field:'warehouseType',sortable:true,title:'仓库类型',align:'center',width:'12%'},
				        {field:'aaa',title:'操作',sortable:true,align:'center',width:'10%',
							formatter: function(value,row,index){
								show = "<a class='easyui-linkbutton' href='${emms}/baseinfo/warehouse.do?cmd=queryWareHouseInfo&warehouseId="
										+ row.warehouseId
										+ "' target='_self'>编辑</a>&nbsp;&nbsp;&nbsp;";
								show += "<a class='easyui-linkbutton' onclick='ajaxDelete(\""+row.warehouseId+"\")'"
									+ " target='_self'>删除</a>&nbsp;&nbsp;&nbsp;";
								return show;
							}}
				    ]]
				});
			}
			function clearForm(){
				$('#query').form('clear');
			}
			function ajaxDelete(id) {
				$.messager.confirm("操作提示", "确定要删除当前记录吗？", function (data) {
					if(data){
						$.ajax({
				            type: "POST",
				            url:"${emms}/baseinfo/warehouse.do?cmd=deleteWareHouse&id="+id,
				            async: false,
				            success: function(data) {
//				            	alert(data);
								if(data=='删除完成'){
									$.messager.alert("操作提示","删除成功","info",function(){
										parent.document.getElementById('westFrame').src=parent.document.getElementById('westFrame').src;
										window.location = "${emms}/baseinfo/warehouse.do?cmd=queryWareHouse";
									});
								}
								else{
									$.messager.alert("操作提示",data,"warning");
								}
				            }
				        });
					}
					});
		    }
		</script>
	</body>

</html>