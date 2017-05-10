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
      <input class="easyui-textbox" id="demandCode" name="demandCode" style="width:22%" data-options="disabled:true,">
      <input class="easyui-combobox" id="demandSource" name="demandSource"  style="width:30%" data-options="label:'需用来源:',required:true,editable:false">
      <input class="easyui-combobox " id="demandState" name="demandState"  style="width:30%" data-options="label:'单据状态:',disabled:true">
    </div>
    <div style="padding:10px">
      <input  class="easyui-combotree" id="wbsId" name="wbsId" style="width:30%" data-options="label:'项目名称:',method:'get',url: '${emms}/baseinfo/project.do?cmd=selectToOrderCombotree',required:true">
      <input  class="easyui-combobox" id="demandOrgId" name="demandOrgId" style="width:30%" data-options="label:'施工单位:',required:true,editable:false">
      <input class="easyui-datebox date_field" id="createTime" name="createTime"  style="width:30%" data-options="label:'录入时间:',disabled:true">
    </div>
    <div style="padding:10px">
      <input class="easyui-combobox" id="demandDirection" name="demandDirection"  style="width:30%" data-options="label:'需求属性:',required:true,editable:false">
      <input class="easyui-combobox" id="dataSource" name="dataSource"  style="width:30%" data-options="label:'数据来源:',required:true,editable:false">
      <input class="easyui-textbox" id="reason" name="reason" style="width:30%" data-options="label:'说明:',multiline:true">
    </div>
    <div style="text-left: center;width:90%">
      <a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="dialog()">添加</a>
      <a href="javascript:void(0)"iconCls='icon-remove' class="easyui-linkbutton" onclick="deleteRow()">删除</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm('demandNoCommit')">保存</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxCommit()">提交</a>
      <a href="${emms}/outstorage/demandPlan.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton" >返回</a>
    </div>
  </form>
 <table id="detail"  class="easyui-datagrid" > </table>

</div>
<script type="text/javascript">
  var lastIndex;
  var reasonList={};
  var dictionary = {};
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
  $('#wbsId').combotree({
    onSelect: function(rec){
      var detailList=[];
      $("#detail").datagrid("loadData", detailList);
    }
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
        var detailList=[];
        $("#detail").datagrid("loadData", detailList);
    }
  });
  $(function(){
    var number=Math.random();
    $('#ff').form('load', '${emms}/outstorage/demandPlan.do?cmd=loadDemandData&demandId=${demandId}&number='+number);
    $('#ff').form({
      onLoadSuccess:function(data) {
        if(data.detailList.length>0){
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
        {field:'designCount',hidden:true},
        {field:'drawingNumberCode',sortable:true,title:'施工图号',align:'center',width:'10%',editor:{type:'textbox'}},
        {field:'designCode',sortable:true,title:'设计院物资编码',align:'center',width:'10%'},
        {field:'designDescribe',sortable:true,title:'物资描述',align:'center',width:'10%'},
        {field:'extra1',sortable:true,title:'附加1',align:'center',width:'10%'},
        {field:'extra2',sortable:true,title:'附加2',align:'center',width:'10%'},
        {field:'extra3',sortable:true,title:'附加3',align:'center',width:'10%'},
        {field:'extra4',sortable:true,title:'附加4',align:'center',width:'10%'},
        {field:'drawingNumberDeviceNo',sortable:true,title:'位号',align:'center',width:'10%'},
        {field:'designUnit',sortable:true,title:'计量单位',align:'center',width:'10%'},
        {field:'demandDate',sortable:true,title:'需用时间',align:'center',width:'10%',editor:{type:'datebox',options:{required:true}}},
        {field:'demandCount',sortable:true,title:'需用数量',align:'center',width:'10%',editor:{type:'numberbox',options:{required:true,precision:4}},
          formatter: function(value,row,index){
            var source = $("#dataSource").combobox('getValue')
            if(value && source=='demandDesign'){
              $.ajax({
                type: 'POST',
                url: "${emms}/outstorage/demandPlan.do?cmd=selectBalanceToEdit",
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
        var dataSource=$("#dataSource").combobox("getValue");
        var e = $("#detail").datagrid('getColumnOption', 'drawingNumberCode');
        if(dataSource=='demandDesign'){
          e.editor = {};
        }
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
  //选择数据源弹出框
  function dialog(){
    var dataSource = $("#dataSource").val();
    if(!dataSource){
      $.messager.alert("操作提示","请选择数据来源","warning");
    }
    if(dataSource=='demandDesign'){
      var wbs = $("#wbsId").combobox("getValue");
      if(!wbs){
        $.messager.alert("操作提示","请选择WBS","warning");
        return false;
      }
      top.$('#dialog').dialog({
        title: '料表图号明细弹出页',
        width: 1000,
        height: 540,
        closed: false,
        cache: true,
        href: '${emms}/design/materialstableImprot.do?cmd=queryAllDetail&wbsId='+wbs
      });
    }else if(dataSource=='demandDataOther'){
      top.$('#dialog').dialog({
        title: '公共物料选择弹出框',
        width: 900,
        height: 540,
        closed: false,
        cache: true,
        href: '${emms}/baseinfo/materials.do?cmd=modal'
      });
    }
  }
  function checkMaterials(rows){
    for(var i=0;i<rows.length;i++){
      $('#detail').datagrid('appendRow',
              {
                designCode:rows[i].materialsCode,
                materialsId:rows[i].materialsId,
                designDescribe:rows[i].materialsDescribe,
                extra1:rows[i].additional1,
                extra2:rows[i].additional2,
                extra3:rows[i].additional3,
                extra4:rows[i].additional4,
                designUnit:rows[i].materialsUnitMain
              }
      );
    }
  }
  function checkDrawing(rows){
    console.log(rows);
    for(var i=0;i<rows.length;i++){
      $('#detail').datagrid('appendRow',
              {
                designCount:rows[i].totalCount,
                designOrgCode:rows[i].designOrgCode,
                drawingNumberVersion:rows[i].drawingNumberVersion,
                drawingNumberCode:rows[i].drawingNumberCode,
                drawingNumberDeviceNo:rows[i].drawingNumberDeviceNo,
                designCode:rows[i].designCode,
                materialsId:rows[i].materialsId,
                designDescribe:rows[i].designDescribe,
                extra1:rows[i].extra1,
                extra2:rows[i].extra2,
                extra3:rows[i].extra3,
                extra4:rows[i].extra4,
                designUnit:rows[i].designUnit,
                wbsId:rows[i].projectId,
                wbsCode:rows[i].projectCodeSeq,
                drawingDetailedId:rows[i].drawingDetailedId
              }
      );
    }
  }
  //删除数据
  function deleteRow(){
    var row = $('#detail').datagrid('getRowIndex',$('#detail').datagrid('getSelected'))
    if (row>-1){
      $('#detail').edatagrid('deleteRow',row);
    }else{
      $.messager.alert("操作提示","请选择一行","warning");
    }
  }

  //保存
  function ajaxSubmitForm(state) {
    var detailList=$("#detail").datagrid("getRows");
    var demandSource=$("#demandSource").combobox("getValue");
    console.log(detailList.length);
    if(detailList.length==0){
      $.messager.alert("操作提示", "请选择物资明细！","warning");
      return false;
    }
    for(var i=0;i<detailList.length;i++){
      if(demandSource=='demandDraw'){
        if(detailList[i].drawingNumberCode==null || detailList[i].drawingNumberCode==""){
          $.messager.alert("操作提示", "请填写施工图号！","warning");
          return false;
        }
      }
      console.log(detailList[i].demandCount);
      if(detailList[i].demandCount==null || detailList[i].demandCount==''){
        $.messager.alert("操作提示", "请填写需用数量！","warning");
        return false;
      }
      if(detailList[i].demandDate==null || detailList[i].demandDate==''){
        $.messager.alert("操作提示", "请选择需用时间！","warning");
        return false;
      }
      console.log(detailList[i]);
      if(detailList[i].demandCount!='' &&(detailList[i].balanceCount!='0' && detailList[i].balanceCount!='') && (detailList[i].balanceReason=="" || detailList[i].balanceReason=="-1")){
        $.messager.alert("操作提示", "请选择差异原因！","warning");
        return false;
      }
      if(detailList[i].balanceReason=='demandLose' && (null == detailList[i].loseCount || "" == detailList[i].loseCount)){
        $.messager.alert("操作提示", "请填写丢失数量！","warning");
        return false;
      }
    }
    var wbs = $('#wbsId').combotree('tree').tree('getSelected');
    var demand={
      "demandId":$("#demandId").val(),
      "demandState":state,
      "demandCode":$("#demandCode").val(),
      "demandSource":$("#demandSource").combobox('getValue'),
      "dataSource":$("#dataSource").combobox('getValue'),
      "demandDirection":$("#demandDirection").combobox('getValue'),
      "demandOrgId":$("#demandOrgId").combobox('getValue'),
      "wbsName":$("#wbsId").combotree('getText'),
      "wbsId":$("#wbsId").combotree('getValue'),
      "wbsCode":wbs.code,
      "reason":$("#reason").val(),
      "detailList":detailList
    }
    var isValid = $("#ff").form("validate");
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
          if(state=="demandCheck"){
            $.messager.alert('提示','提交成功',"info",function(){
              window.location = "${emms}/outstorage/demandPlan.do?cmd=query";
            });
          }else{
            $.messager.alert('提示','保存成功',"info",function(){
              window.location = "${emms}/outstorage/demandPlan.do?cmd=query";
            });
          }
        },
        error:function(){
          $.messager.alert("操作提示","保存失败","error",function(){
            $.messager.progress('close');
          });
        }
      });
    }
  }
  //提交
  function ajaxCommit() {
    if(window.confirm('确定提交吗？')){
      var state="demandCheck";
      ajaxSubmitForm(state);
    }
  }
</script>
</body>
</html>