<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>列表页</title>
    <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->采购管理->合同订单管理->合同订单查询" data-options="fit:true,border:false">
    <form id="query" method="post">
        <div style="padding:10px">
            <b>采购订单编号:</b><input class="easyui-textbox" id="orderCode" style="width:20%"></select>
            <input class="easyui-textbox" id="orderContractNo" style="width:30%" data-options="label:'采购合同号:'">
            <input class="easyui-textbox" id="supplierName" style="width:30%" data-options="label:'供应商名称:'">
        </div>
        <div style="padding:10px">
            <b>订单生成方式:</b><input class="easyui-combobox" id="orderWay" style="width:20%">
            <input class="easyui-textbox" id="createUserName" style="width:30%" data-options="label:'创建人:'">
            <input class="easyui-datebox" id="createStartTime" style="width:18%" data-options="label:'创建时间:'">
            <input class="easyui-datebox" id="createEndTime" style="width:12%">
        </div>
        <div style="padding:10px">
            <input class="easyui-datebox" id="deliveryStartDate" style="width:18%" data-options="label:'交货时间:'">
            <input class="easyui-datebox" id="deliveryEndDate" style="width:10%">
            <input class="easyui-combobox" id="orderState" style="width:30%" data-options="label:'单据状态:'">
        </div>
        <div style="text-align: center;">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-search' onclick="query()">查询</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-reload' onclick="clearForm()">重置</a>
            <a href="${emms}/purchase/order.do?cmd=edit" iconCls='icon-add' class="easyui-linkbutton">新建订单</a>
        </div>
    </form>
    <table id="table" auto-resize="true" class="easyui-datagrid" title="采购合同">
    </table>
</div>
<script type="text/javascript">
    $('#orderState').combobox({
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=orderState',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName',
        multiple: false
    });
    $('#orderWay').combobox({
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=orderWay',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName',
        multiple: false
    });
    $(function () {


        query();
    });
    function query() {
        $('#table').datagrid({
            url: '${emms}/purchase/order.do?cmd=loadOrderListData',
            method: 'POST',
            pagination: true,
            fitColumns: true,
            rownumbers: true,
            showFooter: true,
            singleSelect: false,
            queryParams: {
                "orderCode": $("#orderCode").val(),
                "orderContractNo": $("#orderContractNo").val(),
                "supplierName": $("#supplierName").val(),
                "orderWay": $("#orderWay").combobox('getValue'),
                "createUserName": $("#createUserName").val(),
                "createStartTime": $('#createStartTime').datebox('getValue'),
                "deliveryStartDate": $('#deliveryStartDate').datebox('getValue'),
                "createEndTime": $('#createEndTime').datebox('getValue'),
                "deliveryEndDate": $('#deliveryEndDate').datebox('getValue'),
                "orderState": $('#orderState').combobox('getValue')
            },
            columns: [[
                {
                    field: 'orderCode', sortable: true, title: '采购订单编号', align: 'center', width: '15%',
                    formatter: function (value, row, index) {
                        return '<a class="easyui-linkbutton" style="color:blue" href="${emms}/purchase/order.do?cmd=view&orderId=' + row.orderId + '" target="_self">' + row.orderCode + '</a>';
                    }
                },
                {field: 'orderContractNo', sortable: true, title: '采购合同号', align: 'center', width: '10%'},
                {field: 'supplierId', sortable: true, title: '供应商名称', align: 'center', width: '10%'},
                {field: 'orderWay', sortable: true, title: '采购方式', align: 'center', width: '10%'},
                {field: 'deliveryDate', sortable: true, title: '交货日期', align: 'center', width: '10%'},
                {
                    field: 'createTime', sortable: true, title: '创建时间', align: 'center', width: '10%'
                },
                {field: 'createUserName', sortable: true, title: '创建人', align: 'center', width: '10%'},
                {field: 'orderState', sortable: true, title: '状态', align: 'center', width: '10%'},
                {
                    field: 'aaa', title: '操作', sortable: true, align: 'center', width: '15%',
                    formatter: function (value, row, index) {
                        show = "";
                        if (row.orderState == '未提交' || row.orderState == '审核不通过') {
                            show += "<a class='easyui-linkbutton' href='${emms}/purchase/order.do?cmd=edit&orderId="
                            + row.orderId
                            + "' target='_self'>编辑</a>&nbsp;&nbsp;&nbsp;";
                            show += "<a class='easyui-linkbutton' onclick='ajaxDelete(\"" + row.orderId + "\")'"
                            + " target='_self'>删除</a>&nbsp;&nbsp;&nbsp;";
                            show += "<a class='easyui-linkbutton' onclick='ajaxCommit(\"" + row.orderId + "\",\"" + row.orderWay + "\")'"
                            + " target='_self'>提交</a>&nbsp;&nbsp;&nbsp;";
                        }
                        if (row.orderState == '审核中' && row.orderWay == '线上生成') {
                            show += "<a class='easyui-linkbutton' onclick='ajaxCheckView(\"" + row.orderId + "\")'"
                            + " target='_self'>审批查看</a>&nbsp;&nbsp;&nbsp;";
                            if(row.isAuthority){
                                show += "<a class='easyui-linkbutton' onclick='ajaxCheckPass(\"" + row.orderId + "\",\"" + "orderPass" + "\")'"
                                + " target='_self'>通过</a>&nbsp;&nbsp;&nbsp;";
                                show += "<a class='easyui-linkbutton' onclick='ajaxCheckNotPass(\"" + row.orderId + "\")'"
                                + " target='_self'>不通过</a>&nbsp;&nbsp;&nbsp;";
                            }
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
        $.messager.confirm("操作提示", "确定要删除当前记录吗？", function (data) {
            if(data){
            $.ajax({
                type: "POST",
                url: "${emms}/purchase/order.do?cmd=deleteOrder&orderId=" + id,
                async: false,
                success: function (data) {
                    $.messager.alert("操作提示","删除成功","info",function(){
                        window.location = "${emms}/purchase/order.do?cmd=query";
                    });

                },
                error:function(data){
                    $.messager.alert("操作提示",data,"error");
                }
            });
            }
        });
    }
    function ajaxCommit(id, way) {
        console.log(way);
        if (way == '线上生成') {
            var state = "orderCheck";
        } else {
            var state = "orderPass";
        }
        $.messager.confirm("操作提示", "确定要提交当前记录吗？", function (data) {
            if(data){
            $.ajax({
                type: "POST",
                url: "${emms}/purchase/order.do?cmd=updateOrderState&orderId=" + id + "&state=" + state,
                async: false,
                success: function (data) {
                    console.log(data);
                    $.messager.alert("操作提示","提交成功","info",function(){
                        window.location = "${emms}/purchase/order.do?cmd=query";
                    });
                }
            });
            }
        });
    }
    function ajaxCheckPass(id, state) {
        $.messager.confirm("操作提示", "确定要通过当前记录吗？", function (data) {
            if(data){
                $.ajax({
                    type: "POST",
                    url: "${emms}/purchase/order.do?cmd=approve&orderId=" + id + "&state=" + state,
                    async: false,
                    success: function (data) {
                        if(data=='true'){
                            $.messager.alert("操作提示","通过成功","info",function(){window.location = "${emms}/purchase/order.do?cmd=query";});
                        }else{
                            $.messager.alert("操作提示",data,"warning");
                        }

                    }
                });
            }
        });
    }
    function ajaxCheckView(id) {
        $('#dialog').dialog({
            title: '查看',
            width: 900,
            height: 640,
            closed: false,
            cache: false,
            href: '${emms}/system/process.do?cmd=approveRecord&id=' + id
        });
    }
    //审批
    function ajaxCheckNotPass(id) {
        $('#dialog').dialog({
            title: '审核',
            width: 300,
            height: 200,
            closed: false,
            cache: false,
            href: '${emms}/purchase/order.do?cmd=dialogCheck&orderId=' + id
        });
    }
</script>
</body>

</html>