<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>列表页</title>
    <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" data-options="fit:true,border:false">
  <input  type="hidden" id="supplierId" value="${supplierId}">
  <form id="query" method="post">
    <div style="padding:10px">
      <b>采购订单编号:</b><input class="easyui-combobox" id="orderId"   style="width:20%" >
      <input class="easyui-textbox" id="materialsCode"  style="width:30%" data-options="label:'物资编码:'">
      <input class="easyui-textbox" id="materialsDescribe" style="width:30%" data-options="label:'物资描述:'">
    </div>
    <div style="padding:10px">
      <input class="easyui-textbox" id="wbsCode" style="width:30%" data-options="label:'WBS:'">
    </div>
    <div style="text-align: center;">
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="confirm()">确认</a>
    </div>
  </form>
  <table id="table" class="easyui-datagrid" title="明细列表"  style="height:350px">
  </table>
</div>
<script type="text/javascript">
  $(function(){
    $('#orderId').combobox({
      url: '${emms}/purchase/order.do?cmd=selectBySupplier&supplier=${supplierId}',
      valueField: 'orderId',
      textField: 'orderCode',
      multiple:true
    });
    query();
  });
  function query(){
    var list=$('#orderId').combobox('getValues');
    var ids='';
    for(var i=0;i<list.length;i++){
      ids=ids+list[i]+',';
    }
    console.log(ids);
    $('#table').datagrid({
      url:'${emms}/purchase/order.do?cmd=loadOrderDetailListData&ids='+ids,
      method: 'POST',
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      selectOnCheck:true,
      checkOnSelect:true,
      queryParams: {
        "materialsCode" : $("#materialsCode").val(),
        "materialsDescribe" : $("#materialsDescribe").val(),
        "wbsCode" : $("#wbsCode").val(),
        "supplierId" : $("#supplierId").val()
      },
      onLoadSuccess: function(){
        $(this).datagrid('freezeRow',-1).datagrid('freezeRow',-1);
      },
      columns:[[
        {field:'orderDetailId',checkbox:true},
        {field:'materialsId',hidden:true},
        {field:'supplierId',hidden:true},
        {field:'wbsId',hidden:true},
        {field:'supplierName',sortable:true,title:'供应商',align:'center',width:'10%'},
        {field:'wbsCode',sortable:true,title:'WBS',align:'center',width:'10%'},
        {field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
        {field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'additional1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'additional2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'additional3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'additional4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'orderDetailUnit',sortable:true,title:'计量单位',align:'center',width:'10%'},
        {field:'orderDetailCount',sortable:true,title:'采购数量',align:'center',width:'10%'},
        {field:'orderDetailUnitPrice',sortable:true,title:'单价',align:'center',width:'10%'},
        {field:'orderDetailTotalPrice',sortable:true,title:'总价',align:'center',width:'10%'},
        {field:'orderCode',sortable:true,title:'采购订单',align:'center',width:'10%'}
      ]]
    });
  }
  function confirm(){
    var rows = $('#table').datagrid('getSelections');
    if(rows.length == 0){
      $.messager.alert("操作提示","请选择明细信息","warning");
      return false;
    }else{
      window.parent.frames["mainFrame"].checkOrderDetail(rows);
      top.$("#dialog").dialog("close");
    }
  }
  function clearForm(){
    $('#query').form('clear');
  }
</script>
</body>
</html>
