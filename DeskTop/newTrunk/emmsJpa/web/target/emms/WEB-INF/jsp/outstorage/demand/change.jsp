<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>编辑页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物出库管理->需用计划管理->需用计划变更" data-options="fit:true,border:false">
  <form id="ff" method="post">
    <input type="hidden" id="demandId" name="demandId"/>
    <div style="padding:10px" >
      <input class="easyui-textbox" id="demandCode" name="demandCode" style="width:30%" data-options="label:'需用计划编号:',readonly:true">
      <input class="easyui-combobox" id="demandSource" name="demandSource"  style="width:30%" data-options="label:'需用来源:',required:true,readonly:true">
      <input class="easyui-combobox " id="demandState" name="demandState"  style="width:30%" data-options="label:'单据状态:',readonly:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-combotree" id="wbsId" name="wbsId" style="width:30%" data-options="label:'项目名称:',method:'get',url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree',readonly:true">
      <input  class="easyui-combobox" id="demandOrgId" name="demandOrgId" style="width:30%" data-options="label:'施工单位:',required:true,readonly:true">
      <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'录入时间:',disabled:true">
    </div>
    <div style="padding:10px">
      <input class="easyui-combobox" id="demandDirection" name="demandDirection"  style="width:30%" data-options="label:'需求属性:',required:true,readonly:true">
      <input class="easyui-combobox" id="dataSource" name="dataSource"  style="width:30%" data-options="label:'数据来源:',required:true,readonly:true">
      <input class="easyui-textbox" id="reason" name="reason" style="width:30%" data-options="label:'变更原因:',multiline:true">
    </div>
    <div style="text-left: center;width:90%">
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="save()">保存</a>
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
    multiple:false
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
        data.unshift({
          'dictionaryCode': '-1',
          'dictionaryName': '-请选择-'
        });//向json数组开头添加自定义数据
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
        {field:'materialsId',hidden:true},
        {field:'wbsId',hidden:true},
        {field:'wbsCode',hidden:true},
        {field:'designCount',hidden:true},
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
        {field:'changeCount',sortable:true,title:'变更后需用数量',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true,precision:4}},
          formatter: function(value,row,index){
            console.log(row);
            if(value && value<row.usedCount){
              $.messager.alert("操作提示", "变更数量必须大于已领用数量！","warning");
              row.changeCount="";
              return null;
            }
            var source = $("#dataSource").combobox('getValue')
            if( value && source=='demandDesign'){
              $.ajax({
                type: 'POST',
                url: "${emms}/outstorage/demandPlan.do?cmd=selectBalanceToChange",
                data: JSON.stringify(row),
                dataType: 'json',
                contentType: "application/json;charset=utf-8",
                async:false,
                success: function (data){
                  row.balanceCount =data;
                }
              });
              edit=true;
            }else{
              row.balanceCount ='';
              edit=true;
            }
            return value;
          }
        },
        {field:'balanceCount',sortable:true,title:'与料表差额',align:'center',width:'10%'},
        {field:'balanceReason',sortable:true,title:'差异原因',align:'center',width:'10%',editor:{type:'textbox'},
          formatter: function(value,row,index){
            return getValueByKey(value);
          },
          editor: {
            type: 'combobox',
            options: {
              valueField: 'dictionaryCode',
              textField: 'dictionaryName',
              data: reasonList,
              editable: false
            }
          }},
        {field:'loseCount',sortable:true,title:'丢失数量',align:'center',width:'10%',editor:{type:'numberbox',options:{precision:4}}},
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

  //保存
  function save() {
    var detailList=$("#detail").datagrid("getRows");
    for(var i=0;i<detailList.length;i++){
      if(detailList[i].changeCount==null || detailList[i].changeCount==''){
        alert("请填写变更后需用数量");
        return false;
      }
      if((detailList[i].balanceCount!='0' && detailList[i].balanceCount!='') && (detailList[i].balanceReason=="" || detailList[i].balanceReason=="-1")){
        alert("请选择差异原因");
        return false;
      }
      if(detailList[i].balanceReason=='demandLose' && null == detailList[i].loseCount){
        alert("请填写丢失数量");
        return false;
      }
    }
    var wbs = $('#wbsId').combotree('tree').tree('getSelected');
    var demand={
      "demandId":$("#demandId").val(),
      "demandCode":$("#demandCode").val(),
      "demandSource":$("#demandSource").combobox('getValue'),
      "demandState":$("#demandState").combobox('getValue'),
      "dataSource":$("#dataSource").combobox('getValue'),
      "demandDirection":$("#demandDirection").combobox('getValue'),
      "demandOrgId":$("#demandOrgId").combobox('getValue'),
      "wbsName":$("#wbsId").combotree('getText'),
      "wbsId":$("#wbsId").combotree('getValue'),
      "wbsCode":wbs.code,
      "changeReason":$("#changeReason").val(),
      "detailList":detailList
    }
    var isValid = $("#ff").form("validate");
    console.log(demand);
    if(isValid){
      $.messager.progress();
      $.ajax({
        type: 'POST',
        url: "${emms}/outstorage/demandPlan.do?cmd=save",
        data: JSON.stringify(demand),
        dataType: 'json',
        contentType: "application/json;charset=utf-8",
        success: function (result) {
          $.messager.progress('close');
          alert("保存成功");
          window.location = "${emms}/outstorage/demandPlan.do?cmd=query";
        },
        error:function(){
          $.messager.progress('close');
          alert("保存失败");
        }
      });
    }
  }
</script>
</body>
</html>
