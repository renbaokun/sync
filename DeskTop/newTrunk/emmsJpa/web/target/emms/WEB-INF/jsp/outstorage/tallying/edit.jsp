<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>理货明细编辑</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>

<body>
	<div class="easyui-panel" title="首页->实物出库管理->理货管理->理货明细" data-options="fit:true,border:false">
		<form id="query" method="post">
			<div style="text-align: center;margin:20px">
				<a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="selectPickDetail()">选择领料通知明细</a>
				<a href="javascript:void(0)" iconCls='icon-save' class="easyui-linkbutton" onclick="generateOutStorage()">生成出库单</a>
			</div>
		</form>
		<table id="tallyingTable" auto-resize="true" class="easyui-datagrid" title="理货明细列表" >
			<%--<thead>
			<tr>
				<th data-options="field:'pickNo',align:'center'" width="8%">领料通知编号</th>
				<th data-options="field:'wbsName',align:'center'" width="8%">项目名称</th>
				<th data-options="field:'supplierName',align:'center'" width="8%">施工单位</th>
				<th data-options="field:'materialsCode',align:'center'" width="9%">物资编码</th>
				<th data-options="field:'materialsDescribe',align:'center'" width="10%">物资描述</th>
				<th data-options="field:'additional1',align:'center'" width="10%">附加1</th>
				<th data-options="field:'additional2',align:'center'" width="10%">附加2</th>
				<th data-options="field:'additional3',align:'center'" width="9%">附加3</th>
				<th data-options="field:'additional4',align:'center'" width="8%">附加4</th>
				<th data-options="field:'tallyingUser',align:'center'" width="7%">理货人</th>
				<th data-options="field:'tallyingTime',align:'center'" width="7%">理货时间</th>
				<th data-options="field:'tallyingCount',align:'center'" width="7%">理货数量</th>
			</tr>
			</thead>--%>
		</table>
	</div>
	<script type="text/javascript">
		$(function(){
			loadTallyDetail();
		});
		/*function formatData(metaDatas){
			console.log(1234);
			var result ={}
			var resultList = [];
			for(var i=0;i<metaDatas.length;i++){
				var result = {};
				result.pickNo = metaDatas[i].pickNotice.pickNo;
				if(null != metaDatas[i].wbs){
					result.wbs = metaDatas[i].wbs.projectName;
				}else{
					result.wbs =null ;
				}
				result.contractor = metaDatas[i].contractor.orgName;
				result.materialsCode = metaDatas[i].materials.materialsCode;
				result.materialsDescribe = metaDatas[i].materials.materialsDescribe;
				result.additional1 = metaDatas[i].materials.additional1;
				result.additional2 = metaDatas[i].materials.additional2;
				result.additional3 = metaDatas[i].materials.additional3;
				result.additional4 = metaDatas[i].materials.additional4;
				result.tallyingCount = metaDatas[i].tallyingNum;
				result.tallyingUser = metaDatas[i].tallyingUser;
				result.tallyingTime = metaDatas[i].tallyingDate;
				resultList.push(result);
			}
			result.total = resultList.length;
			result.rows = resultList;
			return result;
		}*/
		function loadTallyDetail(){
			$('#tallyingTable').datagrid({
				url:'${emms}/outstorage/tallying.do?cmd=query',
				method: 'POST',
				fitColumns: true,
				rownumbers: true,
				showFooter: true,
				loadFilter: function(data){
					console.log(data);
					var result ={}
					var resultList = [];
					for(var i=0;i<data.length;i++){
						var result = {};
						result.tallyingId = data[i].tallyingId;
						result.pickNo = data[i].pickNotice.pickNo;
						result.pickId = data[i].pickNotice.pickId;
						result.pickDetailId = data[i].pickNoticeDetail.pickDetailId;
						if(null != data[i].wbs){
							result.wbs = data[i].wbs.projectName;
							result.wbsId = data[i].wbs.projectId;
						}else{
							result.wbs =null ;
							result.wbsId = null;
						}
						result.contractor = data[i].contractor.orgName;
						result.contractorId = data[i].contractor.orgId;
						result.materialsId = data[i].materials.materialsId;
						result.materialsCode = data[i].materials.materialsCode;
						result.materialsDescribe = data[i].materials.materialsDescribe;
						result.additional1 = data[i].materials.additional1;
						result.additional2 = data[i].materials.additional2;
						result.additional3 = data[i].materials.additional3;
						result.additional4 = data[i].materials.additional4;
						result.tallyingCount = data[i].tallyingNum;
						result.tallyingUser = data[i].tallyingUser;
						result.tallyingTime = data[i].tallyingDate;
						if(null != data[i].storagelocation){
							result.storagelocation = data[i].storagelocation.storagelocationCode;
							result.storagelocationId = data[i].storagelocation.storagelocationId;
						}
						resultList.push(result);
					}
					result.total = resultList.length;
					result.rows = resultList;
					return result;
				},
				columns:[[
					{field:'pickNo',sortable:true,title:'领料通知编号',align:'center',width:'10%'},
					{field:'wbs',sortable:true,title:'项目名称',align:'center',width:'10%'},
					{field:'contractor',sortable:true,title:'施工单位',align:'center',width:'10%'},
					{field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
					{field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
					{field:'tallyingUser',sortable:true,title:'理货人',align:'center',width:'10%'},
					{field:'tallyingTime',sortable:true,title:'理货时间',align:'center',width:'10%'},
					{
						field: 'tallyingCount', sortable: true, title: '理货数量', align: 'center', width: '10%',
						editor: {
							type: 'numberbox',
							options: {
								precision: 4
							}
						}
					},
					{field:'storagelocation',sortable:true,title:'来源储位',align:'center',width:'10%'},
					{field:'aaa',title:'操作',sortable:true,align:'center',width:'10%',
						formatter: function(value,row,index){
							show = "<a href=\"javascript:delTally(\'"+ row.tallyingId + "\')\">删除</a>&nbsp;&nbsp;&nbsp;";
							return show;
						}}
				]]
			});
		}
		function selectPickDetail(){
			$('#dialog').dialog({
				title: '领料通知明细弹出框',
				width: 900,
				height: 590,
				closed: false,
				href: '${emms}/outstorage/pickNotice.do?cmd=modalPickNotice'
			});
		}

		function generateOutStorage(){
			var selectRow = $('#tallyingTable').datagrid('getSelections');
			if(selectRow.length == 0){
				alert("请选择需要生成出库单的理货明细");
				return;
			}
			var contractId = selectRow[0].contractorId;
			for(var i=0;i<selectRow.length;i++){
				if(selectRow[i].contractorId != contractId){
					alert("第"+(i+1)+"条明细不属于同一施工单位,请重新选择");
					return ;
				}
			}
			var outWareList = [];
			for(var i=0;i<selectRow.length;i++){
				var outWare = {};
				outWare.materials = {};
				outWare.materials.materialsId = selectRow[i].materialsId;

				outWare.wbs = {};
				outWare.wbs.projectId = selectRow[i].wbsId;

				outWare.pickNotice = {};
				outWare.pickNotice.pickId = selectRow[i].pickId;

				outWare.pickNoticeDetail = {};
				outWare.pickNoticeDetail.pickDetailId = selectRow[i].pickDetailId;

				outWare.outNum = selectRow[i].tallyingCount;
				outWare.tallying ={};
				outWare.tallying.tallyingId = selectRow[i].tallyingId;
				outWare.storagelocation = {};
				outWare.storagelocation.storagelocationId = selectRow[i].storagelocationId;
				outWareList.push(outWare);
			}
			var outWarehouse = {
				"outWarehouseDetailList":outWareList
			};
			outWarehouse.contractor = {};
			outWarehouse.contractor.orgId = contractId;
			$.ajax({
				type: 'POST',
				url: "${emms}/outstorage/outwarehouse.do?cmd=saveOutWarehouse",
				data: JSON.stringify(outWarehouse),
				dataType: 'json',
				contentType: "application/json;charset=utf-8",
				success: function (result) {
					if(result == 'true'){
						$.messager.alert("操作提示", "成功生成出库单！","info");
						window.location = "${emms}/outstorage/outwarehouse.do?cmd=query";
					}else{
						$.messager.alert("操作提示", result,"warning");
					}
				}
			});
		}
		function checkPickDetail(rows){
			for(var i=0;i<rows.length;i++){
				$('#tallyingTable').datagrid('appendRow',{
					"pickId": rows[i].pickId,
					"pickNo": rows[i].pickNo,

					"wbsId": rows[i].wbsId,
					"wbsName": rows[i].wbsName,

					"supplierId": rows[i].supplierId,
					"supplierName": rows[i].supplierName,

					"materialsId": rows[i].materialsId,
					"materialsCode" :  rows[i].materialsCode,
					"materialsDescribe" : rows[i].materialsDescribe,
					"additional1" : rows[i].additional1,
					"additional2" : rows[i].additional2,
					"additional3" : rows[i].additional3,
					"additional4" : rows[i].additional4
				});
			}
		}
		function delTally(tallyId){
			$.messager.confirm("操作提示", "您确定要执行操作吗？", function (data) {
				if (data) {
					$.ajax({
						type: "GET",
						url:"${emms}/outstorage/tallying.do?cmd=delTally&tallyId="+tallyId,
						success: function(data) {
							if(data == 'true'){
								$.messager.alert("操作提示", "理货明细删除成功！","info");
								loadTallyDetail();
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
