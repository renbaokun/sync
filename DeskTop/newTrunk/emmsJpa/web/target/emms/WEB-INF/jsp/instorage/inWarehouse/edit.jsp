<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2017/3/13
  Time: 11:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <title>列表页</title>
  <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>
<body>
<div class="easyui-panel" title="首页->实物入库管理->实物入库管理->编辑" data-options="fit:true,border:false">
  <form id="edit" method="post">
    <input type="hidden" id="inWarehouseId" name="inWarehouseId" value="${inWarehouseId}"/>

    <div style="padding:10px">
      <input class="easyui-textbox" id="inWarehouseCode" name="inWarehouseCode" style="width:30%"
             data-options="disabled:true,label:'入库单编号:'">
      <input class="easyui-textbox" id="supplierName" editable="false" name="supplierName" style="width:30%"
             data-options="label:'供应商:',required:true">
      <input class="easyui-textbox" id="createUserName" readonly="true" name="createUserName" style="width:30%"
             data-options="disabled:true,label:'创建人:'">
    </div>
    <div style="padding:10px">

      <input class="easyui-datebox" editable="false" id="inWarehouseTime" name="inWarehouseTime" style="width:30%"
             data-options="label:'入库日期:'">
      <input class="easyui-textbox" id="inWorker" name="inWorker" style="width:30% "
             data-options="label:'入库人员:'">
    </div>
    <div style="text-left: center;width:90%">
      <a href="javascript:void(0)" iconCls='icon-add' class="easyui-linkbutton" onclick="modal()">添加入库明细</a>
      <a href="${emms}/instorage/inWarehouse.do?cmd=query" iconCls='icon-back' class="easyui-linkbutton">返回</a>
    </div>
  </form>
  <table id="table" auto-resize="true" class="easyui-datagrid" title="入库明细">
  </table>
  <div style="text-align: center;">
    <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxSubmitForm('weiruku')">保存</a>
    <a href="javascript:void(0)" class="easyui-linkbutton" iconCls='icon-save' onclick="ajaxCommit()">入库完成</a>
  </div>

</div>
<script type="text/javascript">

  var lastIndex;
  $('#inWarehouseState').combobox({
    url: '${emms}/system/businessDictionary.do?cmd=loadDicByCode&dicCode=inWarehouseState',
    valueField: 'dictionaryCode',
    textField: 'dictionaryName',
    multiple: false
  });


  $('#supplierName').combobox({
    url: '${emms}/system/organization.do?cmd=selectOrgByType&orgTypeCode=supplier',
    valueField: 'orgId',
    textField: 'orgName',
    multiple: false
  });

  $(function () {
    var warehouseId="${warehouseId}";
    $('#edit').form('load', '${emms}/instorage/inWarehouse.do?cmd=loadInWarehouseData&inWarehouseId=${inWarehouseId}');
    $('#edit').form({
      onLoadSuccess: function (data) {
        if(null != data.inWarehouseDetailList){
          if (data.inWarehouseDetailList.length > 0) {
            $("#table").datagrid("loadData", loadFilter(data.inWarehouseDetailList));
          } else {
            var inWarehouseDetailList = [];
            $("#table").datagrid("loadData", loadFilter(inWarehouseDetailList));
          }
        }
      }
    })




    $('#table').datagrid({
      pagination: false,
      fitColumns: true,
      rownumbers: true,
      showFooter: true,
      singleSelect: true,
      idField: 'inWarehouseId',
      onClickCell: onClickCell,
      columns: [[
        {field: 'zhijianId', hidden: true},
        {field: 'zhijianCode', sortable: true, title: '质检单编号', align: 'center', width: '15%'},
        {field: 'materialsId', hidden: true},
        {field: 'materialsCode', sortable: true, title: '物资编码', align: 'center', width: '10%'},
        {field: 'materialsDescribe', sortable: true, title: '物资描述', align: 'center', width: '10%'},
        {field: 'additional1', sortable: true, title: '附加1', align: 'center', width: '10%'},
        {field: 'additional2', sortable: true, title: '附加2', align: 'center', width: '10%'},
        {field: 'additional3', sortable: true, title: '附加3', align: 'center', width: '10%'},
        {field: 'additional4', sortable: true, title: '附加4', align: 'center', width: '10%'},
        {field: 'wbsId', hidden: true},
        {field: 'wbsCode', sortable: true, title: '工程（WBS）编码', align: 'center', width: '12%'},
        {field: 'docSourceDetailId', hidden: true},
        {field: 'materialsUnitMain', sortable: true, title: '采购计量单位', align: 'center', width: '10%'},
        {field: 'productionDate', sortable: true, title: '生产日期', align: 'center', width: '10%'},
        {field: 'bzq', sortable: true, title: '保质期', align: 'center', width: '10%'},
        {field: 'orderId', hidden: true},
        {field: 'orderCode', hidden: true},
        {field: 'purchaseCount', sortable: true, title: '采购数量', align: 'center', width: '10%'},
        {field: 'deliveryCount', sortable: true, title: '已发货数量', align: 'center', width: '10%'},
        {field: 'dianshouCount', sortable: true, title: '点收数量', align: 'center', width: '10%'},
        {field: 'qualifiedCount', sortable: true, title: '合格数量', align: 'center', width: '10%'},
        {field: 'unqualifiedCount', sortable: true, title: '不合格数量', align: 'center', width: '10%'},
        {field: 'visualInspection', sortable: true, title: '外观检查', align: 'center', width: '10%', formatter: function(value,row,index){
          if(row.appearanceInspect =="y"){
            return "是";
          }else {
            return "否";
          }
        }},
        {field: 'review', sortable: true, title: '需要复检', align: 'center', width: '10%' ,formatter: function(value,row,index){
          if(row.appearanceInspect =="y"){
            return "是";
          }else {
            return "否";
          }
        }},
        {field: 'inWarehouseCount', sortable: true, title: '本次入库数量', align: 'center', width: '10%', editor:{type:'numberbox',options:{precision:4}}},
        {field: 'alreadyCount', sortable: true, title: '已入库数量', align: 'center', width: '10%'},
        {field: 'storagelocationCode', sortable: true, title: '储位', align: 'center', width: '22%'},
        {field: 'storagelocationId', hidden: true},
        {field: 'warehouseId', hidden: true},
        {field: 'reservoirareaId', hidden: true},
        {
          field: 'aaa', title: '操作', sortable: true, align: 'center', width: '10%',
          formatter: function (value, row, index) {
            show = "<a class='easyui-linkbutton' onclick='bbb()'"
            + " target='_self'>删除</a>&nbsp;&nbsp;&nbsp;";
            return show;
          }
        }
      ]], onDblClickRow: function (row) {//运用双击事件实现对一行的编辑
        $('#table').datagrid('beginEdit', row);
      },
      onClickRow: function (row) {//运用单击事件实现一行的编辑结束，在该事件触发前会先执行onAfterEdit事件
        $('#table').datagrid('endEdit', lastIndex);
        $('#table').datagrid('endEdit', row);
        lastIndex = row;
      }
    });
  });

  function onClickCell(index, field) {
    if (field == 'storagelocationCode') {
      top.$('#dialog').dialog({
        title: '公共仓库库区树',
        width: 900,
        height: 510,
        closed: false,
        href: '${emms}/baseinfo/warehouse.do?cmd=dialogWarehouse&index=' + index
      });
    }
  }
  function checkStoragelocation(rows, index) {
    console.log(rows);
    var row = $('#table').datagrid("selectRow", index).datagrid("getSelected");
    row.storagelocationCode = rows[0].storagelocationCode;
    row.storagelocationId = rows[0].storagelocationId;
    row.warehouseId = rows[0].warehouseId;
    row.reservoirareaId = rows[0].reservoirareaId;
    $('#table').datagrid('refreshRow',index);
  }


  function ajaxSubmitForm(state) {
    var zhijianInfo = $("#table").datagrid("getData");
    if (zhijianInfo.rows.length == 0) {//物资明细列表不能为空
      $.messager.alert("操作提示","质检明细列表不能为空","warning");
      return false;
    }
    for (var i = 0; i < zhijianInfo.rows.length; i++) {
      if (zhijianInfo.rows[i].inWarehouseCount == null || zhijianInfo.rows[i].inWarehouseCount == "" || zhijianInfo.rows[i].inWarehouseCount == undefined) {
        $.messager.alert("操作提示","入库数量不能为空！","warning");
        return false;
      }
    }
    for (var i = 0; i < zhijianInfo.rows.length; i++) {
      if (zhijianInfo.rows[i].storagelocationCode == null || zhijianInfo.rows[i].storagelocationCode == "" || zhijianInfo.rows[i].storagelocationCode == undefined) {
        $.messager.alert("操作提示","储位不能为空","warning");
        return false;
      }
    }
    for (var i = 0; i < zhijianInfo.rows.length; i++) {
        if (zhijianInfo.rows[i].inWarehouseCount > zhijianInfo.rows[i].qualifiedCount) {
          $.messager.alert("操作提示", "入库数量不能大于合格数量", "warning");
          return false;
        }

    }
    for (var i = 0; i < zhijianInfo.rows.length; i++) {
      if (zhijianInfo.rows[i].inWarehouseCount <0) {
        $.messager.alert("操作提示","入库数量不能小于0","warning");
        return false;
      }
    }
    var inWarehouse = {
      "inWarehouseId": $("#inWarehouseId").val(),
      "inWarehouseCode": $("#inWarehouseCode").val(),
      "supplierName": $("#supplierName").combobox("getText"),
      "inWarehouseState": state,
      "createUserName": $("#createUserName").val(),
      "inWarehouseTime": $("#inWarehouseTime").datebox('getValue'),
      "inWorker": $("#inWorker").val(),
      "inWarehouseDetailList": zhijianInfo.rows
    };
    console.log(inWarehouse);
    var isValid = $("#edit").form("validate");//验证此form表单中的输入框合理性，不合理：不能提交并页面提示；
    if (isValid) {
      $.ajax({
        type: 'POST',
        url: "${emms}/instorage/inWarehouse.do?cmd=save",
        data: JSON.stringify(inWarehouse),
        dataType: 'json',
        contentType: "application/json;charset=utf-8",
        onsubmit: function () {
          return $(this).form("validate");
        },
        success: function (result) {
          console.log(1,result);
          $.messager.alert("操作提示","保存成功","info",function(){
            window.location = "${emms}/instorage/inWarehouse.do?cmd=query";
          });
        },
        error: function (result) {
          console.log(2,result);
          $.messager.alert("操作提示",result.responseText,"error");
        }
      });
    }
  }

  function loadFilter(data) {//重新组织datagrid数据，把符合条件的内容加到定义的json字符串中。
    var value = {
      total: data.length,
      rows: data
    };
    return value;
  }


  //入库完成
  function ajaxCommit() {
    var state="yiruku";
    $('#table').datagrid('endEdit', lastIndex);
    $.messager.confirm("操作提示", "确定要提交当前记录吗？", function (data) {
      if(data) {
        ajaxSubmitForm(state);
      }
    });
  }
  <%--function ajaxCommit() {--%>
    <%--var id = $("#inWarehouseId").val();--%>
    <%--if (id == null || id == "") {--%>
      <%--alert("请先保存");--%>
    <%--}--%>
    <%--else {--%>
      <%--var state = "yiruku";--%>
      <%--if (window.confirm('确定要将当前记录进行入库处理吗？')) {--%>
        <%--$.ajax({--%>
          <%--type: "POST",--%>
          <%--url: "${emms}/instorage/inWarehouse.do?cmd=updateInWarehouseState&inWarehouseId=" + id + "&state=" + state,--%>
          <%--async: false,--%>
          <%--success: function (result) {--%>
            <%--console.log(result);--%>
            <%--$.messager.alert("操作提示","入库成功","info",function(){--%>
              <%--window.location = "${emms}/instorage/inWarehouse.do?cmd=query";});--%>

          <%--},--%>
          <%--error: function () {--%>
            <%--$.messager.alert("操作提示","入库失败","error");--%>
          <%--}--%>
        <%--});--%>
      <%--}--%>
    <%--}--%>
  <%--}--%>


  function modal() {
    var supplier = $("#supplierName").combobox("getValue");
    if(!supplier){
      alert("请选择供应商");
      return false;
    }
    top.$('#dialog').dialog({
      title: '入库明细弹出框',
      width: 900,
      height: 540,
      closed: false,
      cache: true,
      href: '${emms}/instorage/materialManag.do?cmd=MaterialDetailDialog&supplierId='+supplier  //instorage/materialManag/materialDetail&'+supplier
    });

  }
  function checkMaterialDetail(rows) {
    console.log(rows);
    for (var i = 0; i < rows.length; i++) {
      $('#table').datagrid('appendRow', {
        "zhijianId": rows[i].materiaInspectId,
        "zhijianCode": rows[i].inspectNo,
        "materialsId": rows[i].materialsId,
        "materialsCode": rows[i].materialsCode,
        "materialsDescribe": rows[i].materialsDescribe,
        "additional1": rows[i].additional1,
        "additional2": rows[i].additional2,
        "additional3": rows[i].additional3,
        "additional4": rows[i].additional4,
        "wbsId": rows[i].wbsId,
        "wbsCode": rows[i].wbsCode,
        "materialsUnitMain": rows[i].materialsUnitMain,
        "productionDate": rows[i].productDate,
        "bzq": rows[i].qualityDate,
        "purchaseCount": rows[i].purchaseCount,
        "deliveryCount": rows[i].deliveryQty,
        "dianshouCount": rows[i].dianshouCount,
        "qualifiedCount": rows[i].qualifiedQty,
        "unqualifiedCount": rows[i].unQualifiedQty,
        "visualInspection": rows[i].appearanceInspect,
        "review": rows[i].recheckInspect
      });
    }
  }

  function bbb() {
    var rows = $('#table').datagrid("getSelections");
    for (var i = rows.length - 1; i >= 0; i--) {
      var index = $('#table').datagrid('getRowIndex', rows[i]);
      $('#table').datagrid('deleteRow', index);
    }
  }

</script>
</body>
</html>