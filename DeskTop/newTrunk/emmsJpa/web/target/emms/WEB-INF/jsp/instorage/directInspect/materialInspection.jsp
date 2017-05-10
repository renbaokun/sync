<%--
  Created by IntelliJ IDEA.
  User: YINXP
  Date: 2017/3/7
  Time: 13:59
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" >

<html>
<head>
    <title>物资检验管理</title>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->直达质检管理-> 直达质检单列表" data-options="fit:true,border:false">
    <form id="query" method="post">
        <div style="margin:20px">
            <input class="easyui-textbox" id="inspectNo" name="inspectNo" style="width:30%"
                   data-options="label:'质检单编号:'">
            <input class="easyui-combobox" id="supplierName" name="supplierName" style="width:30%"
                   data-options="label:'供应商名称:'">
            <input class="easyui-combobox" id="inspectStaus" name="inspectStaus" style="width:30%"
                   data-options="label:'单据状态:'">
        </div>
        <div style="margin:20px">
            <input class="easyui-textbox" id="createUserName" name="createUserName" style="width:30%"
                   data-options="label:'创建人:'">
            <input class="easyui-datebox" id="createStartTime" name="createStartTime" style="width:18%"
                   data-options="label:'创建时间:'">-
            <input class="easyui-datebox" id="createEndTime" name="createEndTime" style="width:11%">
        </div>
        <div style="text-align:center">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
            <a href="${emms}/instorage/materialManag.do?cmd=modalDirect" iconCls='icon-add' class="easyui-linkbutton">新建</a>
        </div>
    </form>
    <table id="table" auto-resize="true" class="easyui-datagrid" title="质检单列表">
    </table>
</div>
<script type="text/javascript">
    $('#inspectStaus').combobox({//获取业务字典数据
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=billStaus',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName',
        multiple: false
    });
    $(function () {

        query();
    });
    function query() {
        var e_supplierName = $("#supplierName").combobox('getText');
        $('#table').datagrid({
            url: '${emms}/instorage/materialManag.do?cmd=loadDirectInspectListData',
            method: 'POST',
            pagination: true,
            fitColumns: true,
            rownumbers: true,
            showFooter: true,
            singleSelect: false,
            queryParams: {
                "inspectNo": $("#inspectNo").val(),
                "inspectStaus": $("#inspectStaus").combobox('getValue'),
                "supplierName": e_supplierName,
                "createUserName": $('#createUserName').val(),
                "createStartTime": $("#createStartTime").datebox('getValue'),
                "createEndTime": $('#createEndTime').datebox('getValue'),
                "DataSource": "direct"
            },
            columns: [[

                {
                    field: 'inspectNo', sortable: true, title: '质检单号', align: 'center', width: '20%',
                    formatter: function (value, row, index) {
                        return '<a class="easyui-linkbutton" style="color:blue" href="${emms}/instorage/materialManag.do?cmd=CheckDirectInspect&materiaInspectId=' + row.materiaInspectId + '" target="_self">' + row.inspectNo + '</a>';
                    }
                },
                {field: 'supplierName', sortable: true, title: '供应商名称', align: 'center', width: '20%'},
                {field: 'createUserName', sortable: true, title: '创建人', align: 'center', width: '15%'},
                {field: 'createTime', sortable: true, title: '创建时间', align: 'center', width: '15%'},
                {field: 'inspectStaus', sortable: true, title: '单据状态', align: 'center', width: '15%'},
                {
                    field: 'operate', title: '操作', sortable: true, align: 'center', width: '15%',
                    formatter: function (value, row) {
                        show = "";
                        if (row.inspectStaus == '正在质检') {
                            show += "<a class='easyui-linkbutton' href='${emms}/instorage/materialManag.do?cmd=editDirect&materiaInspectStuate=Checking&materiaInspectId="
                            + row.materiaInspectId
                            + "' target='_self'>质检</a>&nbsp;&nbsp;&nbsp;";
                            show += "<a class='easyui-linkbutton' onclick='ajaxDelete(\"" + row.materiaInspectId + "\")'"
                            + " target='_self'>删除</a>&nbsp;&nbsp;&nbsp;";
                        }
                        if (row.inspectStaus == '质检完成') {
                            show += "<a class='easyui-linkbutton'  href='${emms}/instorage/materialManag.do?cmd=CheckDirectInspect&materiaInspectId="
                            + row.materiaInspectId
                            + "' target='_self'>查看</a>&nbsp;&nbsp;&nbsp;";
                        }
                        return show;
                    }
                }
            ]]
        });
    }
    function clearForm() {
        $('#query').form('clear');
    }

    function ajaxDelete(id) {
        $.messager.confirm('确认对话框', '确定要删除当前记录吗？', function (result) {
            if (result) {
                $.ajax({
                    type: "POST",
                    url: "${emms}/instorage/materialManag.do?cmd=deleteDirect&materiaInspectId=" + id,
                    async: false,
                    success: function () {
                        $.messager.alert("操作提示", "删除成功！", "info", function () {
                            window.location = "${emms}/instorage/materialManag.do?cmd=queryDirect";
                        });
                    }
                });
            }
            return false;
        });
    }

    $('#supplierName').combobox({
        url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
        valueField: 'orgId',
        textField: 'orgName',
        multiple: false
    });
</script>
</body>
</html>