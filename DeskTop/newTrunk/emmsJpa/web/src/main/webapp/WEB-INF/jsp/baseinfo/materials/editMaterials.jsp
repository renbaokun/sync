<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
    <title>编辑页</title>
    <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
    <style>
        .textbox-label{
            width: 100px;
        }
    </style>
</head>
<body>
<div class="easyui-panel" title="首页->基本信息管理->物资编码管理->查看" data-options="fit:true,border:false">
    <form id="ff" method="post">
        <input type="hidden" class="easyui-textbox" name="materialsId" />

        <div style="margin:20px">
            <c:if test="${empty materialsId}">
                <B>系统物资编码:</B>
                <input class="easyui-textbox" id="materialsCode" maxlength="50" name="materialsCode" style="width:21%"
                       data-options="required:true,
                       validType:{
                        length:[1,50],
                        remote:['${emms}/baseinfo/materials.do?cmd=checkMaterials','materialsCode']
                       }
                       ">
            </c:if>
            <c:if test="${not empty materialsId}">
                <B>系统物资编码:</B>
                <input class="easyui-textbox" id="materialsCode"editable="false" name="materialsCode" style="width:21%">
            </c:if>
            <input class="easyui-textbox" id="materialsDescribe" name="materialsDescribe"
                   style="width:60%" data-options="label:'物资描述:',required:true,validType:['length[1,200]']">
        </div>
        <div style="margin:20px">
            <select class="easyui-combobox" id="materialsCategory" editable="false" name="materialsCategory" style="width:30%"
                    data-options="label:'物资类别:',required:true"></select>
            <input class="easyui-textbox" id="additional1" name="additional1" style="width:30%"
                   data-options="label:'附加1:',validType:['length[0,100]']">
            <input class="easyui-textbox" id="additional2" name="additional2" style="width:30%"
                   data-options="label:'附加2:',validType:['length[0,100]']">
        </div>
        <div style="margin:20px">
            <input class="easyui-textbox" id="additional3" name="additional3" style="width:30%"
                   data-options="label:'附加3:',validType:['length[0,100]']">
            <input class="easyui-textbox" id="additional4"  name="additional4" style="width:30%"
                   data-options="label:'附加4:',validType:['length[0,100]']">
            <B>统计计量单位:</B>
            <select class="easyui-combobox" id="materialsUnitMain" editable="false" name="materialsUnitMain" style="width:21%"
                    data-options="required:true"></select>
        </div>
            <table id="table" auto-resize="true" title="常用计量单位换算（换算关系=统计计量单位数量/计量单位数量）"></table>
        <div style="text-align: center;">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm()">保存</a>
            <a href="javascript:void(0)" iconCls='icon-back' class="easyui-linkbutton" onclick="cancel()">关闭</a>
        </div>
    </form>
</div>
<script type="text/javascript">

    var lastIndex;
    $('#table').datagrid({
        url:'${emms}/baseinfo/materials.do?cmd=loadMaterialsUnitData',
        method: 'POST',
        fitColumns: true,
        rownumbers: true,
        singleSelect: true,
        queryParams: {
            "materialsId": "${materialsId}"
        },
        onDblClickRow:function(index){//运用双击事件实现对一行的编辑
            $('#table').edatagrid('beginEdit', index);
        },
        onClickRow:function(index) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
            $('#table').edatagrid('endEdit', lastIndex);
            $('#table').edatagrid('endEdit', index);
            lastIndex = index;
        },
        toolbar: [{
            iconCls: 'icon-add',
            handler: function(){
                addRow();
            }
        },'-',{
            iconCls: 'icon-remove',
            handler: function(){
                deleteRow("table");
            }
        }],
        columns:[[
            {field:'unitOfMeasurement',sortable:true,title:'计量单位名称',align:'center', editor:{type:'validatebox',options:{required:true,validType:'length[1,50]'}}},
            {field:'materialsConversion',sortable:true,title:'换算关系',align:'center', editor:{type:'numberbox',options:{required:true,validType:'length[1,28]'}}}
        ]]
    });

    //加入新行
    function addRow(){
        $('#table').datagrid('appendRow',{
            "unitOfMeasurement": null,
            "materialsConversion": null,
            "materialsUnitId": null,
            "materialsId": null
        });
        $('#table').datagrid('loadData', $('#table').datagrid('getData'));
        $('#table').edatagrid('beginEdit',$('#table').datagrid('getData').total-1);
    }

    //删除
    function deleteRow(tableName){
        var row = $('#'+tableName).datagrid('getRowIndex',$('#'+tableName).datagrid('getSelected'));
        if (row>-1){
            if(confirm('是否确认删除？')){
                $('#'+tableName).edatagrid('deleteRow',row);
            }
        }else{
            $.messager.alert("操作提示","请选择一行","warning");
        }
    }

    $('#materialsUnitMain').combobox({
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=unit',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName'
    });
    $('#materialsCategory').combobox({
        url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=MaterialsTypeCategory',
        valueField: 'dictionaryCode',
        textField: 'dictionaryName'
    });
    $(function () {
        $('#ff').form('load', '${emms}/baseinfo/materials.do?cmd=loadMaterialsData&materialsId=${materialsId}');
    });
    function ajaxSubmitForm() {
        var total = $('#table').datagrid('getData').total;
        //提交前验证数据网格内的值
        for(var i=0; i<total; i++){
            $('#table').edatagrid('beginEdit',i);
        }
        for(var i=0; i<total; i++){
            $('#table').edatagrid('endEdit',i);
        }
        //组装需提交的对象
        console.log($('#table').datagrid('getData').rows);
        var materials = {
            "materialsId": "${materialsId}",
            "unitList": $('#table').datagrid('getData').rows,
            "materialsCode": $('#materialsCode').val(),
            "materialsName": $('#materialsName').val(),
            "materialsDescribe": $('#materialsDescribe').val(),
            "materialsCategory": $('#materialsCategory').combobox('getValue'),
            "additional1": $('#additional1').val(),
            "additional2": $('#additional2').val(),
            "additional3": $('#additional3').val(),
            "additional4": $('#additional4').val(),
            "materialsUnitMain": $('#materialsUnitMain').combobox('getValue')
        };
        //如果通过校验则提交
        if($("#ff").form("validate")){
            $.ajax({
                type: 'POST',
                url: "${emms}/baseinfo/materials.do?cmd=saveMaterials",
                data: JSON.stringify(materials),
                dataType: 'json',
                contentType: "application/json;charset=utf-8",
                success: function (result) {
                    console.log(result);
                    if (result == 'true') {
                        $.messager.alert("提示", "保存成功", "info",function(){
                            top.$("#dialog").dialog("close");
                            window.parent.frames["mainFrame"].query();
                        });
                    } else {
                        $.messager.alert("操作提示", result, "warning");
                    }
                }
            });
        }

    }
    function cancel(){
        $("#dialog").dialog("close");
    }
</script>
</body>
</html>
