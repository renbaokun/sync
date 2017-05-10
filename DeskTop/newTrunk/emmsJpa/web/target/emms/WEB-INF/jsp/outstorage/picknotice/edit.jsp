<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>领料通知编辑</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
	
<body>
	<div class="easyui-panel" title="首页->实物出库管理->领料通知->编辑" data-options="fit:true,border:false">
		<form id="pickNotice" method="post">
			<input class="easyui-textbox" type="hidden" id="pickId" name="pickId"/>
			<div style="margin:20px">
				领用通知编号:
				<input class="easyui-textbox" id="pickNo" name="pickNo" editable="false" style="width:22%"   >
				<input  class="easyui-combobox" id="supplier"  style="width:30%" data-options="label:'施工单位:',required:true,editable:false">
				<input class="easyui-datebox" id="pickTime" name="pickTime" editable="false" style="width:30%" data-options="label:'领用时间:',required:true">
			</div>
			<div style="text-left: center;width:90%">
				<a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="dialog()">添加</a>
				<a href="javascript:void(0)"iconCls='icon-remove' class="easyui-linkbutton" onclick="deleteRow()">删除</a>
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm()">保存</a>
				<%--<a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxCommit()">提交</a>--%>
				<a href="${emms}/outstorage/pickNotice.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
			</div>
		</form>
		<table id="pickNoticeDetail"  class="easyui-datagrid" title="领料明细" data-options="
				singleSelect: true,
				toolbar: '#tb',
				method: 'get',
				onClickCell: onClickCell,
				onEndEdit: onEndEdit
			">
			<thead>
				<tr>
					<th data-options="field:'demandCode',align:'center'" width="8%">需用计划编号</th>
					<th data-options="field:'materialsCode',align:'center'" width="9%">物资编码</th>
					<th data-options="field:'materialsDescribe',align:'center'" width="9%">物资描述</th>
					<th data-options="field:'additional1',align:'center'" width="8%">附加1</th>
					<th data-options="field:'additional2',align:'center'" width="8%">附加2</th>
					<th data-options="field:'additional3',align:'center'" width="8%">附加3</th>
					<th data-options="field:'additional4',align:'center'" width="8%">附加4</th>
					<th data-options="field:'equipmentNo',align:'center'" width="7%">位号</th>
					<th data-options="field:'designUnit',align:'center'" width="7%">工程计量单位</th>
					<th data-options="field:'demandDate',align:'center'" width="7%">需用时间</th>
					<th data-options="field:'demandCount',align:'center'" width="7%">需用数量</th>
					<th data-options="field:'usedCount',align:'center'" width="7%">已领用数量</th>
					<th data-options="field:'stockNum',align:'center'" width="7%">可用库存数量</th>
					<th data-options="field:'pickNum',align:'center',editor:{
						type:'numberbox',
						options:{
							precision:4
						}
					}"width="7%" >领用数量</th>
					<th data-options="field:'tallyedNum',align:'center'" width="7%">已理货数量</th>
				</tr>
			</thead>
		</table>
	</div>
	<script type="text/javascript">
		var editIndex = undefined;
		var supplierId = ''
		var lastPickTime = ''
		$('#supplier').combobox({
			url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
			valueField: 'orgId',
			textField: 'orgName',
			multiple:false
		});

		$('#pickTime').datebox({
			onSelect: function(date){
				var nowDate = new Date();
				nowDate.setHours(0);
				nowDate.setMinutes(0);
				nowDate.setSeconds(0);
				nowDate.setMilliseconds(0);
				var difDates = Math.floor((date.getTime()-nowDate.getTime())/(24*3600*1000));
				if(difDates>3){
					$.messager.alert("操作提示", "计划领用时间不能超过建立单据时间的后三天","warning",function(){
						$('#pickTime').datebox('setValue',lastPickTime);
					});
					return false;
				}else if(difDates<0){
					$.messager.alert("操作提示", "计划领用时间不能小于建立单据时间","warning",function(){
						$('#pickTime').datebox('setValue',lastPickTime);
					});
					return false;
				}else{
					lastPickTime = $('#pickTime').datebox('getValue');
				}
			}
		});


		$(function(){
			$('#pickNotice').form('load', '${emms}/outstorage/pickNotice.do?cmd=editPickNotice&pickNoticeId=${pickNoticeId}');
			$('#pickNotice').form({
				onLoadSuccess:function(data){
					if(null != data.supplier){
						$("#supplier").combobox("setValues", data.supplier.orgId);
					}
					if(null != data.pickNoticeDetailList && data.pickNoticeDetailList.length >0){
						$("#supplier").combobox('readonly',true);
					}
					loadDataGridData(data.pickNoticeDetailList);
				}
			});
		});
		function checkDemand(rows){
			$("#supplier").combobox('readonly',true);
			for(var i=0;i<rows.length;i++){
				$('#pickNoticeDetail').datagrid('appendRow',{
					"demandCode": rows[i].demandCode,
					"materialsCode":rows[i].designCode,
					"materialsDescribe":rows[i].designDescribe,
					"additional1":rows[i].extra1,
					"additional2":rows[i].extra2,
					"additional3":rows[i].extra3,
					"additional4":rows[i].extra4,
					"equipmentNo":rows[i].drawingNumberDeviceNo,
					"designUnit":rows[i].designUnit,
					"demandDate":rows[i].demandDate,
					"demandCount":rows[i].demandCount,
					"demandId":rows[i].demandId,
					"demandDetailId":rows[i].demandDetailId,
					"projectId":rows[i].wbsId,
					"materialsId":rows[i].materialsId,
					"stockNum":rows[i].stockNum,
					"stock":rows[i].stockId,
					"usedCount":rows[i].usedCount
				});
			}
		}
		function loadDataGridData(rows){
			if(null != rows){
				for(var i=0;i<rows.length;i++){
					$('#pickNoticeDetail').datagrid('appendRow',{
						"demandCode": rows[i].demandplan.demandCode,
						"materialsCode":rows[i].materials.materialsCode,
						"materialsDescribe":rows[i].materials.materialsDescribe,
						"additional1":rows[i].materials.additional1,
						"additional2":rows[i].materials.additional2,
						"additional3":rows[i].materials.additional3,
						"additional4":rows[i].materials.additional4,
						"equipmentNo":rows[i].demanddetail.drawingNumberDeviceNo,
						"designUnit":rows[i].demanddetail.designUnit,
						"demandDate":rows[i].demanddetail.demandDate,
						"demandCount":rows[i].demanddetail.demandCount,
						"pickNum":rows[i].pickNum,
						"tallyedNum":rows[i].tallyedNum,
						"stockNum":rows[i].stockNum,
						"usedCount":rows[i].demanddetail.usedCount,
						"demandId":rows[i].demandplan.demandId,
						"demandDetailId":rows[i].demanddetail.demandDetailId,
						"projectId":rows[i].wbs.projectId,
						"materialsId":rows[i].materials.materialsId
					});
				}
			}
		}
		function endEditing(){
			if (editIndex == undefined){return true}
			if ($('#pickNoticeDetail').datagrid('validateRow', editIndex)){
				$('#pickNoticeDetail').datagrid('endEdit', editIndex);
				editIndex = undefined;
				return true;
			} else {
				return false;
			}
		}
		function onClickCell(index, field){

			if(field == 'pickNum'){
				if (editIndex != index){
					if (endEditing()){
						$('#pickNoticeDetail').datagrid('selectRow', index)
								.datagrid('beginEdit', index);
						var ed = $('#pickNoticeDetail').datagrid('getEditor', {index:index,field:field});
						if (ed){
							($(ed.target).data('textbox') ? $(ed.target).textbox('textbox') : $(ed.target)).focus();
						}
						editIndex = index;
					} else {
						setTimeout(function(){
							$('#pickNoticeDetail').datagrid('selectRow', editIndex);
						},0);
					}
				}
			}else{
				$('#pickNoticeDetail').datagrid('selectRow', index).datagrid('endEdit', index);
			}
		}
		function onEndEdit(index, row){
			var ed = $(this).datagrid('getEditor', {
				index: index,
				field: 'productid'
			});
		}
		//选择领料通知单明细
		function dialog(){
			var supplierId = $('#supplier').combobox('getValue');
			if(null == supplierId || supplierId == ''){
				$.messager.alert("操作提示", "请选择施工单位","warning");
				return;
			}
			top.$('#dialog').dialog({
				title: '需用计划弹出框',
				width: 900,
				height: 505,
				closed: false,
				href: '${emms}/outstorage/demandPlan.do?cmd=dialogDemand&constructionId='+supplierId
			});
		}
		//删除领料通知单明细
		function deleteRow(){
			var row = $('#pickNoticeDetail').datagrid('getRowIndex',$('#pickNoticeDetail').datagrid('getSelected'))
			if (row>-1){
				$('#pickNoticeDetail').edatagrid('deleteRow',row);
			}else{
				$.messager.alert("操作提示", "请选择一行","warning");
				return false;
			}
			var selectRow = $('#pickNoticeDetail').datagrid('getRows');
			if(selectRow.length == 0){
				$("#supplier").combobox('readonly',false);
			}
		}

		function ajaxSubmitForm(){
			endEditing();
			var detail = $('#pickNoticeDetail').datagrid('getRows');
			var pickNoticeDetailList = [];
			for(var i=0;i<detail.length;i++){
				var pickNoticeDetail = {};
				pickNoticeDetail.demandplan ={};
				pickNoticeDetail.demandplan.demandId = detail[i].demandId;
				pickNoticeDetail.demanddetail ={};
				pickNoticeDetail.demanddetail.demandDetailId = detail[i].demandDetailId;
				pickNoticeDetail.pickNum = detail[i].pickNum;
				pickNoticeDetail.wbs = {};
				pickNoticeDetail.wbs.projectId = detail[i].projectId;
				pickNoticeDetail.materials = {};
				pickNoticeDetail.materials.materialsId = detail[i].materialsId;

				pickNoticeDetail.demandCount = detail[i].demandCount;
				pickNoticeDetail.usedCount = detail[i].usedCount;
				pickNoticeDetail.stockNum = detail[i].stockNum;
				pickNoticeDetail.tallyedNum = detail[i].tallyedNum;
				pickNoticeDetail.stock = {};
				pickNoticeDetail.stock.stockId = detail[i].stock;
				pickNoticeDetailList.push(pickNoticeDetail);
			}
			var supplier = {};
			supplier.orgId = $("#supplier").textbox("getValue");
			var pickNotice = {
				"pickId":$("#pickId").textbox("getValue"),
				"supplier":supplier,
				"pickTime":$("#pickTime").datebox("getValue"),
				"pickNoticeDetailList":pickNoticeDetailList
			}
			if(pickNoticeDetailList.length == 0){
				$.messager.alert("操作提示", "请选择领料通知明细信息","warning");
				return false;
			}
			//前台页面校验
			for(var i=0;i<pickNoticeDetailList.length;i++){
				if(pickNoticeDetailList[i].pickNum == '' || null == pickNoticeDetailList[i].pickNum){
					$.messager.alert("操作提示", "第"+(i+1)+"行明细中:领用数量不能为空","warning");
					return false;
				}
				if(pickNoticeDetailList[i].pickNum > (parseInt(pickNoticeDetailList[i].demandCount) - parseInt(pickNoticeDetailList[i].usedCount))){
					$.messager.alert("操作提示", "第"+(i+1)+"行明细中:领用数量不能大于需用数量与已领用数量之差","warning")
					return false;
				}
				if(pickNoticeDetailList[i].pickNum > pickNoticeDetailList[i].stockNum){
					$.messager.alert("操作提示", "第"+(i+1)+"行明细中:领用数量不能大于库存数量","warning")
					return false;
				}
				if(pickNoticeDetailList[i].pickNum < pickNoticeDetailList[i].tallyedNum){
					$.messager.alert("操作提示", "第"+(i+1)+"行明细中:领用数量不能小于已理货数量","warning")
					return false;
				}
				delete  pickNoticeDetailList[i].demandCount;
				delete  pickNoticeDetailList[i].usedCount;
				delete  pickNoticeDetailList[i].stockNum;
			}
			if($("#pickNotice").form("validate")){
				$.ajax({
					type: 'POST',
					url: "${emms}/outstorage/pickNotice.do?cmd=savePickNotice",
					data: JSON.stringify(pickNotice),
					dataType: 'json',
					contentType: "application/json;charset=utf-8",
					success: function (result) {
						if(result == 'true'){
							$.messager.alert("操作提示", "领料通知保存成功","info",function(){
								window.location = "${emms}/outstorage/pickNotice.do?cmd=query";
							});
						}else{
							$.messager.alert("操作提示", result,"warning");
						}
					}
				});
			}
		}
	</script>
</body>
</html>
