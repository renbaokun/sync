<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->有发货单物资收货->收货单查看" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="receiptId" name="receiptId"/>
    <div style="padding:10px" >
      <input class="easyui-textbox" id="receiptCode" name="receiptCode" style="width:30%" data-options="label:'收货单编号:',readonly:true">
      <input class="easyui-combobox" id="supplierId" name="supplierId"  style="width:30%" data-options="label:'供应商名称:',readonly:true">
      <b>货物到达时间:</b><input class="easyui-datetimebox date_field" id="arriveTime" name="arriveTime"  style="width:22%" data-options="editable:false,readonly:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-textbox" id="createUserName" name="createUserName" style="width:30%" data-options="label:'创建人:',readonly:true">
      <%-- <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'创建时间:',disabled:true">--%>
      <input  class="easyui-textbox" id="deliveryNo" name="deliveryNo" style="width:30%" data-options="label:'发货单编号:',readonly:true">
    </div>
    <div style="text-left: center;width:90%">
      <a href="${emms}/instorage/receiptGoods.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
    </div>
  </form>
  <table id="packing"  class="easyui-treegrid" title="点收明细"> </table>
  <br>
  <table id="packing_detail"  class="easyui-datagrid" title=""> </table>

</div>
<script type="text/javascript">
  var lastIndex;
  var detailList=[];
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
        if(data.receiptPackingList.length>0){
          var receiptPacking={};
          receiptPacking.rows=data.receiptPackingList;
          $('#packing').treegrid('loadData',receiptPacking );
        }
        if(data.detailList.length>0){
          detailList=data.detailList;
        }
      }
    });


    $('#packing').treegrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      idField:'deliveryPackingId',
      treeField:'packingNo',
      columns:[[
        {field:'packingNo',sortable:true,title:'包装编号',align:'center',width:'20%'},
        {field:'packingType',sortable:true,title:'包装形式',align:'center',width:'10%',
          formatter: function(value,row,index){
            if(value=='package'){
              return "包装"
            }
            return "散装";
          }},
        {field:'packingWeight',sortable:true,title:'包装重量',align:'center',width:'13%'},
        {field:'packingSize',sortable:true,title:'包装尺寸',align:'center',width:'13%'},
        {field:'dianshouCount',sortable:true,title:'点收数量',align:'center',width:'10%'},
        {field:'dianshouType',sortable:true,title:'点收形式',align:'center',width:'10%'},
        {field:'storageCode',sortable:true,title:'储位',align:'center',width:'25%'},
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#packing').treegrid('beginEdit', row.deliveryPackingId);
      },
      onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        var resultDetail = $("#packing_detail").treegrid("getRows");
        if(resultDetail.length>0){
          for(var j=0;j<resultDetail.length;j++){
            detailList.push(resultDetail[j]);
          }
        }
        $('#packing').treegrid('endEdit', lastIndex);
        $('#packing').treegrid('endEdit', row.deliveryPackingId);
        lastIndex = row.deliveryPackingId;
        var detailView={};
        var detailViewList=[];
        if(row.dianshouType=='明细'){
          for(var i=0;i<detailList.length;i++){
            if(detailList[i].packingId==row.deliveryPackingId){
              detailViewList.push(detailList[i]);
            }
          }
          for(var z=0;z<detailList.length;){
            if(detailList[z].packingId==row.deliveryPackingId){
              detailList.splice(z,1)
            }else{
              z++;
            }
          }
        }
        detailView.rows=detailViewList;
        $('#packing_detail').datagrid('loadData',detailView);
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
        {field:'deMainUnit',sortable:true,title:'计量单位',align:'center',width:'10%'},
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
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#packing_detail').edatagrid('beginEdit', row);
      },
      onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        $('#packing_detail').edatagrid('endEdit', lastIndex);
        $('#packing_detail').edatagrid('endEdit', row);
        lastIndex = row;
      }
    });
  });

</script>
</body>
</html>
