<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2017/3/1
  Time: 18:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
  <title>列表页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>

<body>
<div class="easyui-panel"  data-options="fit:true,border:false">
  <form id="query" method="post">
    <div style="padding:10px">
      <b>社会统一信用代码:</b>
      <input class="easyui-textbox" id="orgCode"  style="width:22%" >
      <input class="easyui-textbox" id="orgName" style="width:30%" data-options="label:'供应商名称:'">

    </div>

    <div style="text-align: center;">
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="confirm()">确认</a>
    </div>
  </form>
  <table id="table" auto-resize="true" class="easyui-datagrid" title="机构列表" width="100%">
  </table>

</div>
<script type="text/javascript">
  $(function(){
    query();

  });
  function query(){
    $('#table').datagrid({
      url:'${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
      method: 'POST',
      pagination: true,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      queryParams: {
        "orgCode" : $("#orgCode").val(),
        "orgName" : $("#orgName").val()

      },
      onLoadSuccess: function(){
        $(this).datagrid('freezeRow',-1).datagrid('freezeRow',-1);
      },
      columns:[[
        {field:'orgCode',sortable:true,title:'社会统一信用代码',align:'center',width:'48%'},
        {field:'orgName',sortable:true,title:'供应商名称',align:'center',width:'48%'},
        {field:'orgId',sortable:true,title:'供应商编号',align:'center',width:'4%',hidden:'true'}
      ]]
    });
  }
  function clearForm(){
    $('#query').form('clear');
  }
  function confirm(){
    var rows = $('#table').datagrid('getSelections');
    if(rows.length == 0){
      $.messager.alert("操作提示","请选择供应商信息","warning");
      return false;
    }else{
      window.parent.frames["mainFrame"].checkOrg(rows);
      top.$("#dialog").dialog("close");
    }
  }


</script>
</body>
</html>