<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->采购管理->合同订单管理->合同订单编辑" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="orderId" name="orderId"/>
    <div style="padding:10px" >
      <B>采购订单编号:</B>
      <input class="easyui-textbox" id="orderCode" name="orderCode" style="width:25%" data-options="disabled:true">
      <input class="easyui-textbox" id="orderContractNo" name="orderContractNo"  style="width:30%" data-options="label:'采购合同号:',required:true,validType:'length[0,50]'">
      <input class="easyui-combobox" id="supplierId" name="supplierId" style="width:30%" data-options="label:'供应商名称:',editable:false,required:true">
    </div>
    <div style="padding:10px">
      <B>订单生成方式:</B>
      <input  class="easyui-combobox" id="orderWay" name="orderWay" style="width:25%" data-options="editable:false,required:true">
     <input class="easyui-datebox date_field" id="deliveryDate" name="deliveryDate"  style="width:30%" data-options="label:'交货时间:',editable:false,required:true">
      <input  class="easyui-combobox" id="dataToSources" name="dataToSources" style="width:30%" data-options="label:'数据来源:',editable:false">
    </div>
    <div style="text-left: center;width:90%">
      <a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="dialog()">添加</a>
      <a href="javascript:void(0)"iconCls='icon-remove' class="easyui-linkbutton" onclick="deleteRow()">删除</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm('orderNoCommit')">保存</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxCommit()">提交</a>
      <a href="${emms}/purchase/order.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
    </div>
  </form>
  <table id="tt"  class="easyui-datagrid" title="物资列表">
  </table>
</div>
<script type="text/javascript">
  var orderWay=null;
  var lastIndex;
  var dictionary= {};
  var dictionary1= {};
  $('#dataToSources').combobox({
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false,
    onSelect: function(rec){
      if(rec.dictionaryCode=='kuangjiaxieyi'){
        kuangjia();
      }else if(rec.dictionaryCode=='orderOther' || rec.dictionaryCode=='xorderOther'){
        other();
      }else if(rec.dictionaryCode=='caigou' || rec.dictionaryCode=='xcaigou'){
        caigou();
      }
      if(orderWay !=null){
        orderWay =null;
      }else{
        var orderDetailList=[];
        $("#tt").edatagrid("loadData", loadFilter(orderDetailList));
      }
    }
  });
  $('#orderWay').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=orderWay',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false,
    onSelect: function(rec){
      console.log(rec.dictionaryCode);
      if(orderWay !=null){
        url='${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode='+orderWay
        $("#dataToSources").combobox("reload",url);
      }else{
        $("#dataToSources").combobox("clear",true);
        url='${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode='+rec.dictionaryCode
        $("#dataToSources").combobox("reload",url);
      }
    }
  });

  $('#supplierId').combobox({
    url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
    valueField: 'orgId',
    textField: 'orgName',
    multiple:false
  });
  $(function(){
    console.log(Math.random());
    var number=Math.random();
    $('#ff').form('load', '${emms}/purchase/order.do?cmd=loadOrderData&orderId=${orderId}&number='+number);
      $('#ff').form({
      onLoadSuccess:function(data) {
        orderWay=data.orderWay;
        //根据数据源显示datagrid
        if (data.dataToSources == 'kuangjiaxieyi') {
          kuangjia();
        } else if (data.dataToSources == 'orderOther' || data.dataToSources == 'xorderOther') {
          other();
        } else if (data.dataToSources == 'caigou' || data.dataToSources == 'xcaigou') {
          caigou();
        }
        //从后台为datagrid加载数据
        if(null !=data.orderDetailList && data.orderDetailList.length>0){
          $("#tt").edatagrid("loadData", loadFilter(data.orderDetailList));
        }else{
          var orderDetailList=[];
          $("#tt").edatagrid("loadData", loadFilter(orderDetailList));
        }
      }
    });
    $.ajax({
      url:'${emms}/baseinfo/project.do?cmd=selectToOrderCombotree',
      dataType : 'json',
      type : 'GET',
      async:false,
      success: function (data){
        getChildren(data);
      }
    });

  });
  function getChildren(childrens){
    console.log(childrens);
    for(var i=0;i<childrens.length;i++){
      var obj=childrens[i].id;
      dictionary[obj]=childrens[i].text;
      dictionary1[obj]=childrens[i].code;
      if(childrens[i].children.length>0){
        getChildren(childrens[i].children)
      }
    }
  }
  function getValueByKey(key){
    return dictionary[key];
  }
  function getCodeByKey(key){
    return dictionary1[key];
  }
  function kuangjia(){
    $('#tt').edatagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      columns:[[
        {field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
        {field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'additional1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'additional2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'additional3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'additional4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'wbsCode',hidden:true},
        {field:'wbsId',sortable:true,title:'工程(WBS)编码',align:'center',width:'10%',
          formatter: function(value,row,index){
            console.log(value);
            return getValueByKey(value);
          },
          editor:{type:'combotree',options:{required:true,editable:false,method:'get',url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree'}}},
        {field:'orderDetailUnit',sortable:true,title:'采购计量单位',align:'center',width:'10%'},
        {field:'orderDetailUnitPrice',sortable:true,title:'采购单价',align:'center',width:'10%'},
        {field:'orderDetailCount',sortable:true,title:'采购数量',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true,precision:4}}},
        {field:'orderDetailTotalPrice',sortable:true,title:'总价',align:'center',width:'10%'}
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#tt').edatagrid('beginEdit', row);
      }, onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        $('#tt').edatagrid('endEdit', lastIndex);
        $('#tt').edatagrid('endEdit', row);
        lastIndex = row;
      }
    });
  }
  function caigou(){
    $('#tt').edatagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      columns:[[
        {field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
        {field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'additional1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'additional2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'additional3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'additional4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'wbsCode',sortable:true,title:'工程(WBS)编码',align:'center',width:'10%'},
        {field:'orderDetailUnit',sortable:true,title:'采购计量单位',align:'center',width:'10%'},
        {field:'orderDetailUnitPrice',sortable:true,title:'采购单价',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true,precision:2}}},
        {field:'orderDetailCount',sortable:true,title:'采购数量',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true}}},
        {field:'orderDetailTotalPrice',sortable:true,title:'总价',align:'center',width:'10%'}
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#tt').edatagrid('beginEdit', row);
      }, onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        $('#tt').edatagrid('endEdit', lastIndex);
        $('#tt').edatagrid('endEdit', row);
        lastIndex = row;
      }
    });
  }
  function other(){
    $('#tt').datagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      columns:[[
        {field:'materialsCode',sortable:true,title:'物资编码',align:'center',width:'10%'},
        {field:'materialsDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'additional1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'additional2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'additional3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'additional4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'wbsCode',hidden:true},
        {field:'wbsId',sortable:true, title:'工程(WBS)编码',align:'center',width:'10%',
          formatter: function(value,row,index){
            return getValueByKey(value);
          }
          ,editor:{type:'combotree',options:{required:true,editable:false,method:'get',url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree'}}},
        {field:'orderDetailUnit',sortable:true,title:'采购计量单位',align:'center',width:'10%'},
        {field:'orderDetailUnitPrice',sortable:true,title:'采购单价',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true,precision:2}}},
        {field:'orderDetailCount',sortable:true,title:'采购数量',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true,precision:4}}},
        {field:'orderDetailTotalPrice',sortable:true,title:'总价',align:'center',width:'10%'}
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#tt').edatagrid('beginEdit', row);
      }, onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
              $('#tt').edatagrid('endEdit', lastIndex);
              $('#tt').edatagrid('endEdit', row);
              lastIndex = row;
          }
    });
  }
  //后台List处理为
  function loadFilter(data) {//重新组织datagrid数据，把符合条件的内容加到定义的json字符串中。
    var value = {
      total: data.length,
      rows: data
    };
    return value;
  }
  //选择数据源弹出框
  function dialog(){
    var type= $("#dataToSources").combobox('getValue');
    if(type=='kuangjiaxieyi'){
      modalKuangjia();
    }else if(type=='caigou'){

    }else if(type=='orderOther' || type=='xorderOther'){
      modalOther();
    }
  }
  function modalKuangjia(){
    var supplier = $("#supplierId").combobox("getValue");
    if(!supplier){
      alert("请选择供应商");
      return false;
    }
    top.$('#dialog').dialog({
      title: '框架协议选择弹出框',
      width: 900,
      height: 500,
      closed: false,
      cache: false,
      href: '${emms}/purchase/agreement.do?cmd=modalagreement&supplier='+supplier
    });
  }
  //数据源框架协议添加数据
  function checkAgreement(rows){
    console.log(1,rows);
    for(var i=0;i<rows.length;i++){
      $('#tt').edatagrid('appendRow',
              {
                materialsCode:rows[i].materialsCode,
                materialsId:rows[i].materialsId,
                materialsDescribe:rows[i].materialsDescribe,
                additional1:rows[i].additional1,
                additional2:rows[i].additional2,
                additional3:rows[i].additional3,
                additional4:rows[i].additional4,
                orderDetailUnit:rows[i].materialsUnitMain,
                orderDetailUnitPrice:rows[i].unitPrice
              }
      );
    }
  }
  //数据源其他添加数据
  function modalOther(){
    top.$('#dialog').dialog({
      title: '公共物料选择弹出框',
      width: 900,
      height: 540,
      closed: false,
      cache: true,
      href: '${emms}/baseinfo/materials.do?cmd=modal'
    });
  }
  function checkMaterials(rows){
    console.log(rows);
    for(var i=0;i<rows.length;i++){
      $('#tt').edatagrid('appendRow',
        {
          materialsCode:rows[i].materialsCode,
          materialsId:rows[i].materialsId,
          materialsDescribe:rows[i].materialsDescribe,
          additional1:rows[i].additional1,
          additional2:rows[i].additional2,
          additional3:rows[i].additional3,
          additional4:rows[i].additional4,
          orderDetailUnit:rows[i].materialsUnitMain
        }
      );
    }
  }
  //删除数据
  function deleteRow(){
    var row = $('#tt').datagrid('getRowIndex',$('#tt').datagrid('getSelected'))
    if (row>-1){
      $('#tt').edatagrid('deleteRow',row);
    }else{
      $.messager.confirm("操作提示",'请选择一行',"warning");
    }
  }
  //保存
  function ajaxSubmitForm(state) {

    $('#tt').edatagrid('endEdit', lastIndex);
    var detailList = $('#tt').datagrid('getData').rows;
    var isValid = $("#ff").form("validate");
    for(var i=0;i<detailList.length;i++){
      detailList[i].wbsCode=getCodeByKey( detailList[i].wbsId);
      if(detailList[i].wbsId==null || detailList[i].orderDetailUnitPrice==null || detailList[i].orderDetailCount==null){
        alert(detailList[i].materialsCode+"物资WBS编码、单价、数量为必填字段");
        return false;
      }
    }
    console.log(detailList);
    var order={
      "orderId":$("#orderId").val(),
      "orderDetailList":detailList,
      "orderCode":$("#orderCode").val(),
      "orderContractNo":$("#orderContractNo").val(),
      "supplierId":$("#supplierId").combobox('getValue'),
      "supplierName":$("#supplierId").combobox('getText'),
      "orderWay":$("#orderWay").combobox('getValue'),
      "dataToSources":$("#dataToSources").combobox('getValue'),
      "deliveryDate":$("#deliveryDate").val(),
      "orderState":state
    }
   if(isValid){
      $.ajax({
        type: 'POST',
        url: "${emms}/purchase/order.do?cmd=save",
        data: JSON.stringify(order),
        dataType: 'json',
        contentType: "application/json;charset=utf-8",
        success: function (result) {
          $.messager.alert("操作提示","保存成功","info",function(){
            window.location = "${emms}/purchase/order.do?cmd=query";
          });
        },
        error:function(){
          alert("保存失败");
        }
      });
    }
  }
  //提交
  function ajaxCommit() {
    $('#tt').edatagrid('endEdit', lastIndex);
    var state="orderCheck";
    var type=$("#orderWay").combobox('getValue');
    if(type='xianxia'){
       state="orderPass";
    }
    $.messager.confirm("操作提示","确定要提交当前记录吗？", function (data) {
      if(data) {
        ajaxSubmitForm(state);
      }
    });
  }
</script>
</body>
</html>
