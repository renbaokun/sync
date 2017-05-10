<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->直达现场->收货单编辑" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="receiptId" name="receiptId"/>
    <div style="padding:10px" >
      <input class="easyui-textbox" id="receiptCode" name="receiptCode" style="width:30%" data-options="label:'收货单编号:',disabled:true">
      <input class="easyui-combobox" id="supplierId" name="supplierId"  style="width:30%" data-options="label:'供应商名称:',required:true,editable:false">
      <b>货物到达时间:</b><input class="easyui-datetimebox date_field" id="arriveTime" name="arriveTime"  style="width:22%" data-options="editable:false,required:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-textbox" id="createUserName" name="createUserName" style="width:30%" data-options="label:'创建人:',disabled:true">
      <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'创建时间:',disabled:true">
      <input class="easyui-combobox" id="receiptOrgId" name="receiptOrgId"  style="width:30%" data-options="label:'施工单位:',required:true,editable:false">
    </div>
    <div style="text-left: center;width:90%">
      <a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="dialog()">添加</a>
      <a href="javascript:void(0)"iconCls='icon-remove' class="easyui-linkbutton" onclick="deleteRow()">删除</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm('dianshou')">保存</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxCommit()">收货完成</a>
      <a href="${emms}/instorage/receiptGoods.do?cmd=directQuery" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
    </div>
  </form>
  <table id="packing_detail"  class="easyui-datagrid" title="点收明细"> </table>

</div>
<script type="text/javascript">
  var lastIndex;
  //校验选物资不可重复
  var checkOrder=[];
  $(function(){
    $('#supplierId').combobox({
      url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
      valueField: 'orgId',
      textField: 'orgName',
      multiple:false
    });
    $('#receiptOrgId').combobox({
      url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
      valueField: 'orgId',
      textField: 'orgName',
      multiple:false
    });
    var number=Math.random();
    $('#ff').form('load', '${emms}/instorage/receiptGoods.do?cmd=loadReceiptGoodsData&receiptId=${receiptId}&number='+number);
    $('#ff').form({
      onLoadSuccess:function(data) {
        if(null!=data.detailList&&data.detailList.length>0){
          var detail={};
          detail.rows=data.detailList;
          $('#packing_detail').datagrid('loadData',detail );
          for(var i=0;i<data.detailList.length;i++){
            checkOrder.push(data.detailList.docSourceDetailId);
          }
        }
      }
    });

    $('#packing_detail').datagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      onClickCell:onClickCell,
      columns:[[
        {field:'materialsId',hidden:true},
        {field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
        {field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'15%'},
        {field:'additional1',sortable:true,title:'附加1',align:'center',width:'6%'},
        {field:'additional2',sortable:true,title:'附加2',align:'center',width:'6%'},
        {field:'additional3',sortable:true,title:'附加3',align:'center',width:'6%'},
        {field:'additional4',sortable:true,title:'附加4',align:'center',width:'6%'},
        {field:'wbsId',hidden:true},
        {field:'wbsCode',sortable:true,title:'工程（WBS）编码',align:'center',width:'10%'},
        {field:'docSourceDetailId',hidden:true},
        {field:'docSourceNo',sortable:true,title:'采购订单号',align:'center',width:'15%'},
        {field:'deMainUnit',sortable:true,title:'采购计量单位',align:'center',width:'8%'},
        {field:'purchaseCount',sortable:true,title:'采购数量',align:'center',width:'6%'},
        {field:'deliveryCount',sortable:true,title:'已发货数量',align:'center',width:'8%'},
        {field:'thisDeliveryCount',sortable:true,title:'本次发货数量',align:'center',width:'10%'},
        {field:'deviceNo',sortable:true,title:'位号',align:'center',width:'10%',editor:{type:'textbox'}},
        {field:'productionDate',sortable:true,title:'生产日期',align:'center',width:'10%',editor:{type:'datebox'}},
        {field:'bzq',sortable:true,title:'保质期',align:'center',width:'10%',editor:{type:'datebox'}},
        {field:'dianshouCount',sortable:true,title:'点收数量',align:'center',width:'6%',
          formatter: function(value,row,index){
            if(value>0 && value<=row.purchaseCount){
              return value;
            }else{
              row.dianshouCount=null;
              return null;
            }
          },editor:{type:'numberbox',options:{required:true,precision:4}}},
        {field:'storageCode',sortable:true,title:'储位',align:'center',width:'17%',
          formatter: function(value,row,index){
            if (null !=value && value.length>20){
              return "<span title='" + value + "'>" + value.substring(0, 20)+"..." + "</span>";
            } else {
              return value;
            }}},
        {field:'storageId',hidden:true},
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#packing_detail').edatagrid('beginEdit', row);
      }
      ,
      onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        $('#packing_detail').edatagrid('endEdit', lastIndex);
        $('#packing_detail').edatagrid('endEdit', row);
        lastIndex = row;
      }
    });
  });
  function onClickCell(index, field){
    if(field=='storageCode'){
      top.$('#dialog').dialog({
        title: '公共仓库库区树',
        width: 900,
        height: 510,
        closed: false,
        href: '${emms}/baseinfo/warehouse.do?cmd=dialogWarehouse&index='+index
      });
    }
  }
  function checkStoragelocation(rows,index){
    var row = $('#packing_detail').datagrid("selectRow", index).datagrid("getSelected");
    row.storageCode =rows[0].storagelocationCode;
    row.storageId =rows[0].storagelocationId;
    $('#packing_detail').datagrid('refreshRow', index);
  }
  //选择数据源弹出框
  function dialog(){
    var supplierName = $("#supplierId").combobox('getValue');
    if(!supplierName){
      $.messager.alert("操作提示","请选择供应商","warning");
      return;
    }
    top.$('#dialog').dialog({
      title: '公共采购订单明细选择弹出框',
      width: 900,
      height: 540,
      closed: false,
      href: '${emms}/purchase/order.do?cmd=dialogOrderDetail&supplierId='+supplierName
    });
  }
  function checkOrderDetail(rows){
    console.log(rows);
    for(var i=0;i<rows.length;i++){
        if (checkOrder.indexOf(rows[i].orderDetailId) != -1) {
          continue;
        }else{
          checkOrder.push(rows[i].orderDetailId);
        }
      $('#packing_detail').datagrid('appendRow',
              {
                materialsCode:rows[i].materialsCode,
                materialsId:rows[i].materialsId,
                materialsDescribe:rows[i].materialsDescribe,
                additional1:rows[i].additional1,
                additional2:rows[i].additional2,
                additional3:rows[i].additional3,
                additional4:rows[i].additional4,
                wbsId:rows[i].wbsId,
                wbsCode:rows[i].wbsCode,
                docSourceDetailId:rows[i].orderDetailId,
                docSourceNo:rows[i].orderCode,
                deMainUnit:rows[i].orderDetailUnit,
                purchaseCount:rows[i].orderDetailCount
              }
      );
    }
  }
  //删除数据
  function deleteRow(){
    var row = $('#packing_detail').datagrid('getRowIndex',$('#packing_detail').datagrid('getSelected'))
    if (row>-1){
      $('#packing_detail').edatagrid('deleteRow',row);
    }else{
      $.messager.alert("操作提示",'请选择一行',"warning");
    }
  }
  //保存
  function ajaxSubmitForm(state) {
    $('#packing_detail').datagrid('endEdit', lastIndex);
    var resultDetail = $("#packing_detail").datagrid("getRows");
    var detailList=[];
    if(resultDetail.length>0){
      for(var j=0;j<resultDetail.length;j++){
        detailList.push(resultDetail[j]);
      }
    }
    if(detailList.length==0){
      $.messager.alert("操作提示", "请选择采购单物资！","warning");
      return false;
    }
    var receiptGoods={
      "receiptId":$("#receiptId").val(),
      "receiptState":state,
      "receiptType":"order",
      "detailList":detailList,
      "receiptCode":$("#receiptCode").val(),
      "supplierId":$("#supplierId").combobox('getValue'),
      "supplierName":$("#supplierId").combobox('getText'),
      "arriveTime":$("#arriveTime").val(),
      "deliveryNo":$("#deliveryNo").val(),
      "inStorage":"direct",
      "receiptOrgId":$("#receiptOrgId").combobox('getValue')
    }
    var isValid = $("#ff").form("validate");
    console.log(receiptGoods);
    if(isValid){
      $.ajax({
        type: 'POST',
        url: "${emms}/instorage/receiptGoods.do?cmd=save",
        data: JSON.stringify(receiptGoods),
        dataType: 'json',
        contentType: "application/json;charset=utf-8",
        success: function (result) {
          $.messager.alert("操作提示","保存成功","info",function(){
            window.location = "${emms}/instorage/receiptGoods.do?cmd=directQuery";
          });
        },
        error:function(data){
          console.log(1,data.responseText);
          $.messager.alert("操作提示", data.responseText,"error");
        }
      });
    }
  }
  //提交
  function ajaxCommit() {
    $.messager.confirm("操作提示", "确定收货完成吗？", function (data) {
      if(data) {
        var state = "receiptFinish";
        ajaxSubmitForm(state);
      }
    });
  }
</script>
</body>
</html>
