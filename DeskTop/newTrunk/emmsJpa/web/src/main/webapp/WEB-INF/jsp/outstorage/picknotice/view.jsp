<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>领料通知编辑</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
	
<body>
	<div class="easyui-panel" title="首页->实物出库管理->领料通知->查看" data-options="fit:true,border:false">
		<form id="pickNotice" method="post">
			<input class="easyui-textbox" type="hidden" id="pickId" name="pickId"/>
			<div style="margin:20px">
				<B>领用通知编号:</B>
				<input class="easyui-textbox" id="pickNo" name="pickNo"  readonly="true" style="width:22%"   >
				<input  class="easyui-combobox" id="supplier" readonly="true"  style="width:30%" data-options="label:'施工单位:',required:true">
				<input class="easyui-datebox" id="pickTime" name="pickTime"  readonly="true" style="width:30%" data-options="label:'领用时间:',required:true">
			</div>
			<div style="text-left: center;width:90%">
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
									precision:0
								}
							}"width="7%" >领用数量</th>
				</tr>
			</thead>
		</table>
	</div>
	<script type="text/javascript">
		var editIndex = undefined;
		var supplierId = ''
		$('#supplier').combobox({
			url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
			valueField: 'orgId',
			textField: 'orgName',
			multiple:false
		});
		$(function(){
			$('#pickNotice').form('load', '${emms}/outstorage/pickNotice.do?cmd=editPickNotice&pickNoticeId=${pickNoticeId}');
			$('#pickNotice').form({
				onLoadSuccess:function(data){
					if(null != data.supplier){
						$("#supplier").combobox("setValues", data.supplier.orgId);
					}
					loadDataGridData(data.pickNoticeDetailList);
				}
			});
		});
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
						"stockNum":rows[i].stockNum,
						"usedCount":rows[i].demanddetail.usedCount,

						"demandId":rows[i].demandplan.demandId,
						"demandDetailId":rows[i].demanddetail.demandDetailId,
						"projectId":rows[i].wbsId,
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
		}
		function onEndEdit(index, row){
			var ed = $(this).datagrid('getEditor', {
				index: index,
				field: 'productid'
			});
		}
	</script>
</body>
</html>
