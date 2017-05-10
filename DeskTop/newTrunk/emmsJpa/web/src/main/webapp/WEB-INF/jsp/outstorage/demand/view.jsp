<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物出库管理->需用计划管理->需用计划编辑" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="demandId" name="demandId"/>
    <div style="padding:10px" >
      <B>需用计划编号:</B>
      <input class="easyui-textbox" id="demandCode" name="demandCode" style="width:22%" data-options="readonly:true">
      <input class="easyui-combobox" id="demandSource" name="demandSource"  style="width:30%" data-options="label:'需用来源:',readonly:true">
      <input class="easyui-combobox " id="demandState" name="demandState"  style="width:30%" data-options="label:'单据状态:',readonly:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-combotree" id="wbsId" name="wbsId" style="width:30%" data-options="label:'项目名称:',method:'get',url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree',readonly:true">
      <input  class="easyui-combobox" id="demandOrgId" name="demandOrgId" style="width:30%" data-options="label:'施工单位:',readonly:true">
      <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'录入时间:',readonly:true">
    </div>
    <div style="padding:10px">
      <input class="easyui-combobox" id="demandDirection" name="demandDirection"  style="width:30%" data-options="label:'需求属性:',readonly:true">
      <input class="easyui-combobox" id="dataSource" name="dataSource"  style="width:30%" data-options="label:'数据来源:',readonly:true">
    </div>
    <div style="text-left: center;width:90%">
      <a href="${emms}/outstorage/demandPlan.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
    </div>
  </form>
  <table id="detail"  class="easyui-datagrid" > </table>

</div>
<script type="text/javascript">
  var lastIndex;
  var reasonList={};
  var dictionary = {};
  var firstRefresh=true;
  $('#demandSource').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=demandSource',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false
  });
  $('#demandState').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=demandState',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false
  });
  $('#demandOrgId').combobox({
    url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
    valueField: 'orgId',
    textField: 'orgName',
    multiple:false
  });
  $('#demandDirection').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=demandDirection',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false
  });
  $('#dataSource').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=dataSource',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple:false,
    onSelect: function(rec){
      if(rec.dictionaryCode=='demandDataOther'){
        $('#reason').textbox({ required: true });
      }else{
        $('#reason').textbox({ required: false });
      }
      if(firstRefresh){
        var detailList=[];
        $("#detail").datagrid("loadData", detailList);
      }else{
        firstRefresh=true;
      }
    }
  });
  $(function(){
    $('#ff').form('load', '${emms}/outstorage/demandPlan.do?cmd=loadDemandData&demandId=${demandId}');
    $('#ff').form({
      onLoadSuccess:function(data) {
        if(data.detailList.length>0){
          firstRefresh=false;
          $('#detail').datagrid('loadData',data.detailList );
        }
      }
    });
    $.ajax({
      url:'${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=demandReason',
      dataType : 'json',
      type : 'POST',
      async:false,
      success: function (data){
        for(var i=0;i<data.length;i++){
          var obj=data[i].dictionaryCode;
          dictionary[obj]=data[i].dictionaryName;
        }
        reasonList = data;
      }
    });
    var edit=false;
    $('#detail').datagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect:true,
      columns:[[
        {field:'designOrgCode',hidden:true},
        {field:'drawingNumberVersion',hidden:true},
        {field:'drawingNumberCode',sortable:true,title:'施工图号',align:'center',width:'10%'},
        {field:'designCode',sortable:true,title:'设计院物资编码',align:'center',width:'10%'},
        {field:'designDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'extra1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'extra2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'extra3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'extra4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'drawingNumberDeviceNo',sortable:true,title:'位号',align:'center',width:'10%'},
        {field:'designUnit',sortable:true,title:'计量单位',align:'center',width:'10%'},
        {field:'demandDate',sortable:true,title:'需用时间',align:'center',width:'10%'},
        {field:'demandCount',sortable:true,title:'需用数量',align:'center',width:'10%'},
        {field:'balanceCount',sortable:true,title:'与料表差额',align:'center',width:'10%'},
        {field:'balanceReason',sortable:true,title:'差异原因',align:'center',width:'10%',
          formatter: function(value,row,index){
            return getValueByKey(value);
          }},
        {field:'loseCount',sortable:true,title:'丢失数量',align:'center',width:'10%'},
      ]],onDblClickRow:function(row){//运用双击事件实现对一行的编辑
        $('#detail').datagrid('beginEdit', row);
      },
      onClickRow:function(row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        if(edit){
          $('#detail').datagrid('refreshRow', lastIndex);
          edit=false;
        }
        $('#detail').datagrid('endEdit', lastIndex);
        $('#detail').datagrid('endEdit', row);
        lastIndex = row;
      }
    });
  });
  function getValueByKey(key){
    return dictionary[key];
  }

</script>
</body>
</html>
