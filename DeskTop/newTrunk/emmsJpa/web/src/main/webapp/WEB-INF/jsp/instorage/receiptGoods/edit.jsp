<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->有发货单物资收货->收货单编辑" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="receiptId" name="receiptId"/>
    <input type="hidden" id="materiaReceiptStuate" name="materiaReceiptStuate" value="${materiaReceiptStuate}"/>
    <div style="padding:10px" >
      <input class="easyui-textbox" id="receiptCode" name="receiptCode" style="width:30%" data-options="label:'收货单编号:',disabled:true">
      <input class="easyui-combobox" id="supplierId" name="supplierId"  style="width:30%" data-options="label:'供应商名称:',editable:false">
      <b>货物到达时间:</b><input class="easyui-datetimebox date_field" id="arriveTime" name="arriveTime"  style="width:22%" data-options="editable:false,required:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-textbox" id="createUserName" name="createUserName" style="width:30%" data-options="label:'创建人:',disabled:true">
      <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'创建时间:',disabled:true">
      <input  class="easyui-textbox" id="deliveryNo" name="deliveryNo" style="width:30%" data-options="label:'发货单编号:'">
      <input class="easyui-textbox" id="deliveryId" name="deliveryId" type="hidden">
    </div>
    <div style="text-left: center;width:90%">
      <a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="dialog()">添加</a>
      <a href="javascript:void(0)"iconCls='icon-remove' class="easyui-linkbutton" onclick="deleteRow()">删除</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm('dianshou')">保存</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxCommit()">收货完成</a>
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
  var receiptPacking={};
  receiptPacking.rows=[];
  //校验选物资不可重复
  var checkPacking=[];
  //判断包装还是明细储位
  var mark=null;
  var materiaReceiptStuate = $("#materiaReceiptStuate").val();
//  if(materiaReceiptStuate=="Checking"){
//    $("#supplierId").combobox('disable');
//  }else{
//    $("#supplierId").combobox('enable');
//  }
  $('#supplierId').combobox({
    url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
    valueField: 'orgId',
    textField: 'orgName',
    multiple:false
  });
  $(function(){
    var number=Math.random();
   $('#ff').form('load', '${emms}/instorage/receiptGoods.do?cmd=loadReceiptGoodsData&receiptId=${receiptId}&number='+number);
    $('#ff').form({
      onLoadSuccess:function(data) {
        console.log(data);
        if(null!=data.receiptPackingList && data.receiptPackingList.length>0){
          receiptPacking.rows=data.receiptPackingList;
          $('#packing').treegrid('loadData',receiptPacking );
          for(var i=0;i<data.receiptPackingList.length;i++){
            checkPacking.push(data.receiptPackingList[i].deliveryPackingId);
          }
        }
        if(null!=data.detailList && data.detailList.length>0){
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
        {field:'dianshouType',sortable:true,title:'点收形式',align:'center',width:'10%',
          formatter: function(value,row,index){
            if(value=='包裹'){
              row.dianshouCount=1;
            }else{
              row.dianshouCount="";
              row.storageCode="";
              row.storageId="";
            }
            return value;
          },
          editor:{
            type:'combobox',
            options:{
            valueField:'dictionaryCode',
            textField:'dictionaryName',
            url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=dianshouType',
            required:true,
            editable:false
          }
        }},
        {field:'storageCode',sortable:true,title:'储位',align:'center',width:'25%'},
        {field:'storageId',hidden:true}
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
          $('#packing').treegrid('beginEdit', row.deliveryPackingId);
      }, onClickCell : function(index, field, value) {
        if(index=='storageCode' && field.dianshouType=='包裹'){
          top.$('#dialog').dialog({
            title: '公共仓库库区树',
            width: 900,
            height: 510,
            closed: false,
            href: '${emms}/baseinfo/warehouse.do?cmd=dialogWarehouse&index='+field.deliveryPackingId
          });
        }
      },
      onClickRow:function(row,value) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        var resultDetail = $("#packing_detail").datagrid("getRows");
        if(resultDetail.length>0){
          for(var j=0;j<resultDetail.length;j++){
            detailList.push(resultDetail[j]);
          }
        }
        $('#packing').treegrid('endEdit', lastIndex);
        $('#packing').treegrid('endEdit', row.deliveryPackingId);
        lastIndex = row.deliveryPackingId;
        mark="b";
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
        {field:'wbsCode',sortable:true,title:'工程（WBS）编码',align:'center',width:'12%'},
        {field:'deMainUnit',sortable:true,title:'计量单位',align:'center',width:'6%'},
        {field:'purchaseCount',sortable:true,title:'采购数量',align:'center',width:'6%'},
        {field:'deliveryCount',sortable:true,title:'已发货数量',align:'center',width:'8%'},
        {field:'thisDeliveryCount',sortable:true,title:'本次发货数量',align:'center',width:'8%'},
        {field:'productionDate',sortable:true,title:'生产日期',align:'center',width:'10%' ,editor:{type:'datebox'}},
        {field:'bzq',sortable:true,title:'保质期',align:'center',width:'10%',editor:{type:'datebox'}},
        {field:'dianshouCount',sortable:true,title:'点收数量',align:'center',width:'10%',
          formatter: function(value,row,index){
            if(value>0 && value<=row.thisDeliveryCount){
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
        {field:'storageId',hidden:true}
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#packing_detail').datagrid('beginEdit', row);
      },
      onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        $('#packing_detail').datagrid('endEdit', lastIndex);
        $('#packing_detail').datagrid('endEdit', row);
        lastIndex = row;
        mark="x";
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
   if(mark=='x'){
     var row = $('#packing_detail').datagrid("selectRow", index).datagrid("getSelected");
      row.storageCode =rows[0].storagelocationCode;
      row.storageId =rows[0].storagelocationId;
      $('#packing_detail').datagrid('refreshRow', index);
   }
    if(mark=='b'){
      var row = $('#packing').treegrid("selectRow", index).treegrid("getSelected");
      row.storageCode =rows[0].storagelocationCode;
      row.storageId =rows[0].storagelocationId;
      $('#packing').treegrid('refreshRow', index);
    }
  }
  //选择数据源弹出框
  function dialog(){
    var code = $("#deliveryNo").val();
    var supplier = $("#supplierId").combobox("getValue");
    if(!supplier){
      $.messager.alert("操作提示","请选择供应商","warning");
      return false;
    }
    var flag=true;
   if(code){
     $.ajax({
       type: "POST",
       url:"${emms}/purchase/supplierDelivery.do?cmd=checkCode&code="+code,
       async: false,
       contentType: "application/json;charset=utf-8",
       success: function(data) {
         if(data){
           $.messager.alert("操作提示","发货单不存在","warning");
           flag=false;
         }
       }
     });
   }
    if(flag){
      top.$('#dialog').dialog({
        title: '供应商发货弹出框',
        width: 900,
        height: 540,
        closed: false,
        cache: true,
        href: '${emms}/purchase/delivery/package.do?cmd=modal&supplierId='+supplier+'&deliveryNo='+code
      });
    }
  }
  function checkDelivery(rows){
    if(!$("#deliveryNo").val()){
      receiptPacking={};
      receiptPacking.rows=[];
      detailList=[];
    }
    $("#deliveryNo").textbox("setValue", rows[0].deliveryNo);
    $("#deliveryId").textbox("setValue", rows[0].deliveryId);
       for(var i=0;i<rows.length;i++){
        if(checkPacking.indexOf(rows[i].packingId)!=-1){
          continue;
        }else{
          checkPacking.push(rows[i].packingId);
        }
      var packing={
        _parentId:null,
        deliveryPackingId:rows[i].packingId,
        packingNo:rows[i].packingNo,
        packingSize:rows[i].packingSize,
        packingWeight:rows[i].packingWeight,
        packingId:rows[i].packingId,
        packingType:rows[i].packingType
      }
      receiptPacking.rows.push(packing);
      for(var j=0;j<rows[i].deliveryPackageDetailList.length;j++){
        var detail={
          packingId:rows[i].deliveryPackageDetailList[j].packingId,
          materialsId:rows[i].deliveryPackageDetailList[j].materialsId,
          materialsCode:rows[i].deliveryPackageDetailList[j].materialsCode,
          materialsDescribe:rows[i].deliveryPackageDetailList[j].materialsDescribe,
          additional1:rows[i].deliveryPackageDetailList[j].additional1,
          additional2:rows[i].deliveryPackageDetailList[j].additional2,
          additional3:rows[i].deliveryPackageDetailList[j].additional3,
          additional4:rows[i].deliveryPackageDetailList[j].additional4,
          wbsId:rows[i].deliveryPackageDetailList[j].wbsId,
          wbsCode:rows[i].deliveryPackageDetailList[j].wbsCode,
          deMainUnit:rows[i].deliveryPackageDetailList[j].deMainUnit,
          purchaseCount:rows[i].deliveryPackageDetailList[j].purchaseNum,
          deliveryCount:rows[i].deliveryPackageDetailList[j].deliveryCount,
          thisDeliveryCount:rows[i].deliveryPackageDetailList[j].deliveryMainCount,
          productionDate:rows[i].deliveryPackageDetailList[j].productionDate,
          docSourceDetailId:rows[i].deliveryPackageDetailList[j].deDetailId,
          bzq:rows[i].deliveryPackageDetailList[j].bzq
        }
        detailList.push(detail);
      }
      getChild(rows[i].packingId,rows[i].childPacking);
      $('#packing').treegrid('loadData',receiptPacking );
      console.log(detailList);
    }
  }
  function getChild(id,childs){
    for(var i=0;i<childs.length;i++){
      var packing={
        _parentId:id,
        deliveryPackingId:childs[i].packingId,
        packingNo:childs[i].packingNo,
        packingSize:childs[i].packingSize,
        packingWeight:childs[i].packingWeight,
        packingId:childs[i].packingId,
        packingType:childs[i].packingType
      }
      receiptPacking.rows.push(packing);
      for(var j=0;j<childs[i].deliveryPackageDetailList.length;j++){
        var detail={
          packingId:childs[i].deliveryPackageDetailList[j].packingId,
          materialsId:childs[i].deliveryPackageDetailList[j].materialsId,
          materialsCode:childs[i].deliveryPackageDetailList[j].materialsCode,
          materialsDescribe:childs[i].deliveryPackageDetailList[j].materialsDescribe,
          additional1:childs[i].deliveryPackageDetailList[j].additional1,
          additional2:childs[i].deliveryPackageDetailList[j].additional2,
          additional3:childs[i].deliveryPackageDetailList[j].additional3,
          additional4:childs[i].deliveryPackageDetailList[j].additional4,
          wbsId:childs[i].deliveryPackageDetailList[j].wbsId,
          wbsCode:childs[i].deliveryPackageDetailList[j].wbsCode,
          deMainUnit:childs[i].deliveryPackageDetailList[j].deMainUnit,
          purchaseCount:childs[i].deliveryPackageDetailList[j].purchaseCount,
          deliveryCount:childs[i].deliveryPackageDetailList[j].deliveryCount,
          thisDeliveryCount:childs[i].deliveryPackageDetailList[j].deliveryMainCount,
          productionDate:childs[i].deliveryPackageDetailList[j].productionDate,
          bzq:childs[i].deliveryPackageDetailList[j].bzq
        }
        detailList.push(detail);
      }
      if(childs[i].childPacking.length==0){ //递归停止条件

      }else{
          getChild(childs[i].packingId,childs[i].childPacking)
      }
    }
  }
  //删除数据
  function deleteRow(){
    var node = $('#packing').treegrid('getSelected');
    if(node==null){
      $.messager.alert("操作提示","请选择删除的记录","warning");
      return false;
    }else if(node._parentId !=null){
      $.messager.alert("操作提示","请选择最大包装","warning");
      return false;
    }else if(node._parentId==null){
      var childrenNodes = $('#packing').treegrid('getChildren',node.deliveryPackingId);
      childrenNodes.push(node);
      $('#packing').treegrid('remove', node.deliveryPackingId);
      for(var j=0;j<childrenNodes.length;j++){
        receiptPacking.rows.splice(j,1);
        for(var z=0;z<checkPacking.length;){
          if(childrenNodes[j].deliveryPackingId==checkPacking[z]){
            checkPacking.splice(z,1)
          }else{
            z++;
          }
        }
        for(var i=0;i<detailList.length;){
          if(childrenNodes[j].deliveryPackingId==detailList[i].packingId){
            detailList.splice(i,1);
          }else{
            i++;
          }
        }
      }
    }else{
      $.messager.alert("操作提示","数据无效","warning");
    }
  }

  //保存
  function ajaxSubmitForm(state) {
    var returnDetailList=[];;
    for(var i=0;i<detailList.length;i++){
      returnDetailList.push(detailList[i]);
    }
    $('#packing').treegrid('endEdit', lastIndex);
    var packageInfo = [];
    var package = $("#packing").treegrid("getChildren");
    for(var i=0;i<package.length;i++){
      if(package[i].id !='0'){
        delete package[i].state;
        delete package[i].children;
        packageInfo.push(package[i]);
      }
    }
    if(packageInfo.length==0){
      $.messager.alert("操作提示", "请选择发货单物资！","warning");
      $('#packing').treegrid('loadData',receiptPacking );
      return false;
    }
    var resultDetail = $("#packing_detail").datagrid("getRows");
    if(resultDetail.length>0){
      for(var j=0;j<resultDetail.length;j++){
        returnDetailList.push(resultDetail[j]);
      }
    }
    var receiptGoods={
      "receiptId":$("#receiptId").val(),
      "receiptState":state,
      "receiptType":"delivery",
      "receiptPackingList":packageInfo,
      "detailList":returnDetailList,
      "receiptCode":$("#receiptCode").val(),
      "supplierId":$("#supplierId").combobox('getValue'),
      "supplierName":$("#supplierId").combobox('getText'),
      "arriveTime":$("#arriveTime").val(),
      "deliveryNo":$("#deliveryNo").val(),
      "inStorage":"receipt"
    }
    var isValid = $("#ff").form("validate");
    if(isValid){
      $.ajax({
        type: 'POST',
        url: "${emms}/instorage/receiptGoods.do?cmd=save",
        data: JSON.stringify(receiptGoods),
        dataType: 'json',
        contentType: "application/json;charset=utf-8",
        success: function (result) {
          $.messager.alert("操作提示","保存成功","info",function(){
            $('#packing').treegrid('loadData',receiptPacking );
            window.location = "${emms}/instorage/receiptGoods.do?cmd=query";
           });

        },error:function(result){
          console.log(result);
          $.messager.alert("操作提示",result.responseText,"warning");
          $('#packing').treegrid('loadData',receiptPacking );
        }
      });
    }
  }
  //提交
  function ajaxCommit() {
    $.messager.confirm("操作提示", "确定要提交当前记录吗？", function (data) {
      if(data) {
        var state = "receiptFinish";
        ajaxSubmitForm(state);
      }
    });
  }
</script>
</body>
</html>