<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>列表页</title>
    <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物出库管理->需用计划管理->需用计划查询" data-options="fit:true,border:false">
    <form id="query" method="post">
        <div style="padding:10px">
            <B>需用计划编号:</B>
            <input class="easyui-textbox" id="demandCode" style="width:22%"></select>
            <input class="easyui-combobox" id="demandSource" style="width:30%" data-options="label:'需用来源:'">
            <input class="easyui-combobox" id="demandState" style="width:30%" data-options="label:'需用状态:'"></select>
        </div>
        <div style="padding:10px">
            <input class="easyui-textbox" id="wbsName" style="width:30%" data-options="label:'项目名称:'">
            <input class="easyui-textbox" id="createUserName" style="width:30%" data-options="label:'录入人:'">
            <input class="easyui-datebox" id="createStartTime" style="width:18%" data-options="label:'录入时间:'">
            <input class="easyui-datebox" id="createEndTime" style="width:12%">
        </div>
        <div style="padding:10px">
            <input class="easyui-combobox" id="demandOrgId" style="width:30%" data-options="label:'施工单位:'">
        </div>
        <div style="text-align: center;">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
            <a href="${emms}/outstorage/demandPlan.do?cmd=edit" iconCls='icon-add' class="easyui-linkbutton">新建</a>
        </div>
    </form>
    <table id="table" auto-resize="true" class="easyui-datagrid" title="需用计划列表">
    </table>
</div>
<script type="text/javascript">
    $('#demandSource').combobox({
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=demandSource',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName',
        multiple: false
    });
    $('#demandState').combobox({
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=demandState',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName',
        multiple: false
    });
    $('#demandOrgId').combobox({
        url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=construct',
        valueField: 'orgId',
        textField: 'orgName',
        multiple: false
    });
    $(function () {
        query();
    });
    function query() {
        $('#table').datagrid({
            url: '${emms}/outstorage/demandPlan.do?cmd=loadDemandListData',
            method: 'POST',
            pagination: true,
            fitColumns: true,
            rownumbers: true,
            showFooter: true,
            singleSelect: false,
            queryParams: {
                "demandCode": $("#demandCode").val(),
                "demandSource": $("#demandSource").combobox('getValue'),
                "demandState": $("#demandState").combobox('getValue'),
                "createUserName": $("#createUserName").val(),
                "createStartTime": $('#createStartTime').val(),
                "createEndTime": $('#createEndTime').val(),
                "wbsName": $('#wbsName').val(),
                "demandOrgId": $('#demandOrgId').combobox('getValue')
            },
            columns: [[
                {
                    field: 'demandCode', sortable: true, title: '需用计划编号', align: 'center', width: '15%',
                    formatter: function (value, row, index) {
                        return '<a class="easyui-linkbutton" style="color:blue" href="${emms}/outstorage/demandPlan.do?cmd=view&demandId=' + row.demandId + '" target="_self">' + row.demandCode + '</a>';
                    }
                },
                {
                    field: 'demandSource', sortable: true, title: '需用来源', align: 'center', width: '8%',
                    formatter: function (value, row, index) {
                        if (row.demandSource == 'demandDraw') {
                            return "图纸";
                        } else if (row.demandSource == 'demandOther') {
                            return "其他";
                        }
                    }
                },
                {field: 'wbsCode', sortable: true, title: '工程（WBS）编码', align: 'center',nowrap:'false', width: '12%'},
                {field: 'wbsName', sortable: true, title: '项目名称', align: 'center',nowrap:'false', width: '10%'},
                {field: 'demandOrgId', sortable: true, title: '施工单位', align: 'center',nowrap:'false', width: '12%'},
                {field: 'createTime', sortable: true, title: '创建时间', align: 'center',nowrap:'false', width: '10%'},
                {field: 'createUserName', sortable: true, title: '创建人', align: 'center',nowrap:'false', width: '10%'},
                {
                    field: 'demandState', sortable: true, title: '单据状态', align: 'center',nowrap:'false', width: '8%',
                    formatter: function (value, row, index) {
                        if (row.demandState == 'demandNoCommit') {
                            return "未提交";
                        } else if (row.demandState == 'demandCheck') {
                            return "审核中";
                        } else if (row.demandState == 'demandPass') {
                            return "通过";
                        } else if (row.demandState == 'demandNoPass') {
                            return "不通过";
                        }
                    }
                },
                {
                    field: 'aaa', title: '操作', sortable: true, align: 'center', width: '15%',
                    formatter: function (value, row, index) {
                        show = "";
                        if (row.demandState == 'demandNoCommit' || (row.demandState == 'demandNoPass' && row.isChange==false)) {
                            show += "<a class='easyui-linkbutton' href='${emms}/outstorage/demandPlan.do?cmd=edit&demandId="
                            + row.demandId
                            + "' target='_self'>编辑</a>&nbsp;&nbsp;&nbsp;";
                            show += "<a class='easyui-linkbutton' onclick='ajaxDelete(\"" + row.demandId + "\")'"
                            + " target='_self'>删除</a>&nbsp;&nbsp;&nbsp;";
                            show += "<a class='easyui-linkbutton' onclick='ajaxCommit(\"" + row.demandId + "\",\"" + "demandCheck" + "\")'"
                            + " target='_self'>提交</a>&nbsp;&nbsp;&nbsp;";
                        }
                        if (row.demandState == 'demandCheck') {
                            show += "<a class='easyui-linkbutton' onclick='ajaxCommit(\"" + row.demandId + "\",\"" + "demandPass" + "\")'"
                            + " target='_self'>通过</a>&nbsp;&nbsp;&nbsp;";
                            show += "<a class='easyui-linkbutton' onclick='ajaxCommit(\"" + row.demandId + "\",\"" + "demandNoPass" + "\")'"
                            + " target='_self'>不通过</a>&nbsp;&nbsp;&nbsp;";
                        }
                        if (row.demandState == 'demandPass' || (row.demandState == 'demandNoPass' && row.isChange==true)) {
                            show += "<a class='easyui-linkbutton' href='${emms}/outstorage/demandPlan.do?cmd=change&demandId="
                            + row.demandId
                            + "' target='_self'>变更</a>&nbsp;&nbsp;&nbsp;";
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
        if (window.confirm('确定要删除当前记录吗？')) {
            $.ajax({
                type: "POST",
                url: "${emms}/outstorage/demandPlan.do?cmd=deleteDemand&demandId=" + id,
                async: false,
                success: function (data) {
                    alert(data);
                    window.location = "${emms}/outstorage/demandPlan.do?cmd=query";
                }
            });
        }
    }
    function ajaxCommit(id, state) {
        if (window.confirm('确定要提交当前记录吗？')) {
            $.ajax({
                type: "POST",
                url: "${emms}/outstorage/demandPlan.do?cmd=updateDemandState&demandId=" + id + "&state=" + state,
                async: false,
                success: function (data) {
                    if(state=='demandCheck'){
                        $.messager.alert('提示','提交成功',"info",function(){
                            window.location = "${emms}/outstorage/demandPlan.do?cmd=query";
                        });
                    }else{
                        $.messager.alert('提示','审批成功',"info",function(){
                            window.location = "${emms}/outstorage/demandPlan.do?cmd=query";
                        });
                    }
                }
            });
        }
    }
</script>
</body>

</html>