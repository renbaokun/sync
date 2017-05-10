<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>列表页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->直达现场->收货单查询" data-options="fit:true,border:false">
  <form id="query" method="post">
    <div style="padding:10px">
      <input class="easyui-textbox" id="supplierName" style="width:30%" data-options="label:'供应商名称:'">
      <input class="easyui-textbox" id="receiptCode" style="width:30%" data-options="label:'收货单编号:'">
      <input class="easyui-combobox" id="receiptState"  style="width:30%" data-options="label:'收货单状态:'">
    </div>
    <div style="padding:10px">
      <input class="easyui-textbox" id="deliveryNo" style="width:30%" data-options="label:'发货单编号:'">
      <input class="easyui-textbox" id="createUserName"  style="width:30%" data-options="label:'创建人:'">
      <input class="easyui-datebox" id="createStartTime"  style="width:18%" data-options="label:'创建时间:'">
      <input class="easyui-datebox" id="createEndTime"  style="width:12%">
    </div>
    <div style="text-align: center;">
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
      <a href="${emms}/instorage/receiptGoods.do?cmd=edit&receiptType=delivery&inStorage=direct" iconCls='icon-add' class="easyui-linkbutton">新建有发货单直达现场</a>
      <a href="${emms}/instorage/receiptGoods.do?cmd=edit&receiptType=order&inStorage=direct" iconCls='icon-add' class="easyui-linkbutton">新建无发货单直达现场</a>
    </div>
  </form>
  <table id="table" auto-resize="true" class="easyui-datagrid" title="收货单列表">
  </table>
</div>
<script type="text/javascript">
  $('#receiptState').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=receiptState',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false
  });
  $(function(){

    query();
  });
  function query(){
    $('#table').datagrid({
      url:'${emms}/instorage/receiptGoods.do?cmd=loadReceiptListData',
      method: 'POST',
      pagination: true,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:false,
      queryParams: {
        "supplierName" : $("#supplierName").val(),
        "receiptCode" : $("#receiptCode").val(),
        "deliveryNo" : $("#deliveryNo").val(),
        "inStorage" : "direct",
        "createUserName" : $("#createUserName").val(),
        "receiptState" : $("#receiptState").combobox('getValue'),
        "createStartTime" : $('#createStartTime').datebox('getValue'),
        "createEndTime" : $('#createEndTime').datebox('getValue')
      },
      columns:[[
        {field:'receiptCode',sortable:true,title:'收货单编号',align:'center',width:'17%',
          formatter: function(value,row,index){
              return '<a style="color:blue" href="${emms}/instorage/receiptGoods.do?cmd=view&receiptId=' +row.receiptId+'&receiptType='+row.receiptType+'&inStorage=direct">'+row.receiptCode+'</a>';
          }},
        {field:'deliveryNo',sortable:true,title:'发货单编号',align:'center',width:'15%'},
        {field:'supplierName',sortable:true,title:'供应商名称',align:'center',width:'10%'},
        {field:'arriveTime',sortable:true,title:'货物到达时间',align:'center',width:'15%'},
        {field:'createUserName',sortable:true,title:'创建人',align:'center',width:'10%'},
        {field:'createTime',sortable:true,title:'创建时间',align:'center',width:'10%'},
        {field:'receiptState',sortable:true,title:'状态',align:'center',width:'10%',
          formatter: function(value,row,index){
            if(value=='dianshou'){
              return "正在点收"
            }
            return "收货完成";
          }},
        {field:'aaa',title:'操作',sortable:true,align:'center',width:'15%',
          formatter: function(value,row,index){
            show ="";
            if(row.receiptState=='dianshou'){
              show += "<a class='easyui-linkbutton' href='${emms}/instorage/receiptGoods.do?cmd=edit&receiptId="
              + row.receiptId+"&inStorage=direct&receiptType="+row.receiptType
              + "' target='_self'>点收</a>&nbsp;&nbsp;&nbsp;";
              show += "<a class='easyui-linkbutton' onclick='ajaxDelete(\""+row.receiptId+"\")'"
              + " target='_self'>删除</a>&nbsp;&nbsp;&nbsp;";
            }
            if(row.receiptState=='receiptFinish'){
              show += "<a class='easyui-linkbutton' href='${emms}/instorage/receiptGoods.do?cmd=view&receiptId="
              + row.receiptId+"&inStorage=direct&receiptType="+row.receiptType
              + "' target='_self'>查看</a>&nbsp;&nbsp;&nbsp;";
            }
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
        url:"${emms}/instorage/receiptGoods.do?cmd=deleteReceiptGoods&receiptId="+id,
        async: false,
        success: function(data) {
          if(data=='删除完成'){
            $.messager.alert("操作提示","删除成功","info",function(){
              window.location = "${emms}/instorage/receiptGoods.do?cmd=directQuery";
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