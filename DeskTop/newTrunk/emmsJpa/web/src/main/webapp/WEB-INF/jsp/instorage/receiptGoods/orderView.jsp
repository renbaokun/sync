<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->无发货单物资收货->收货单查看" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="receiptId" name="receiptId"/>
    <div style="padding:10px" >
      <input class="easyui-textbox" id="receiptCode" name="receiptCode" style="width:30%" data-options="label:'收货单编号:',readonly:true,validType:'length[0,50]'">
      <input class="easyui-combobox" id="supplierId" name="supplierId"  style="width:30%" data-options="label:'供应商名称:',readonly:true">
      <b>货物到达时间:</b><input class="easyui-datetimebox date_field" id="arriveTime" name="arriveTime"  style="width:22%" data-options="editable:false,readonly:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-textbox" id="createUserName" name="createUserName" style="width:30%" data-options="label:'创建人:',readonly:true">
      <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'创建时间:',readonly:true">
    </div>
    <div style="text-left: center;width:90%">
      <a href="${emms}/instorage/receiptGoods.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
    </div>
  </form>
  <table id="packing_detail"  class="easyui-datagrid" title="点收明细"> </table>

</div>
<script type="text/javascript">
  $('#supplierId').combobox({
    url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
    valueField: 'orgId',
    textField: 'orgName',
    multiple:false
  });
  $(function(){
    $('#ff').form('load', '${emms}/instorage/receiptGoods.do?cmd=loadReceiptGoodsData&receiptId=${receiptId}');
    $('#ff').form({
      onLoadSuccess:function(data) {
        if(data.detailList.length>0){
          var detail={};
          detail.rows=data.detailList;
          $('#packing_detail').datagrid('loadData',detail );
        }
      }
    });


    $('#packing_detail').edatagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      columns:[[
        {field:'materialsId',hidden:true},
        {field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
        {field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'additional1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'additional2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'additional3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'additional4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'wbsId',hidden:true},
        {field:'wbsCode',sortable:true,title:'工程（WBS）编码',align:'center',width:'10%'},
        {field:'docSourceDetailId',hidden:true},
        {field:'docSourceNo',sortable:true,title:'采购订单号',align:'center',width:'10%'},
        {field:'deMainUnit',sortable:true,title:'采购计量单位',align:'center',width:'10%'},
        {field:'purchaseCount',sortable:true,title:'采购数量',align:'center',width:'10%'},
        {field:'deliveryCount',sortable:true,title:'已发货数量',align:'center',width:'10%'},
        {field:'thisDeliveryCount',sortable:true,title:'本次发货数量',align:'center',width:'10%'},
        {field:'productionDate',sortable:true,title:'生产日期',align:'center',width:'10%'},
        {field:'bzq',sortable:true,title:'保质期',align:'center',width:'10%'},
        {field:'dianshouCount',sortable:true,title:'点收数量',align:'center',width:'10%'},
        {field:'storageCode',sortable:true,title:'储位',align:'center',width:'17%',
          formatter: function(value,row,index){
            if (null !=value && value.length>20){
              return "<span title='" + value + "'>" + value.substring(0, 20)+"..." + "</span>";
            } else {
              return value;
            }}}
      ]]
    });
  });
</script>
</body>
</html>
