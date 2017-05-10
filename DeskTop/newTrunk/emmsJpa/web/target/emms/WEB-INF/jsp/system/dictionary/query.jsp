<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/includes.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>列表页</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
</head>

<body>
	<div class="easyui-panel" title="首页->系统管理-><c:if test="${dicType == '1'}">数据字典管理</c:if><c:if test="${dicType == '0'}">业务字典管理</c:if>->查看" data-options="fit:true,border:false">
		<c:choose>
			<c:when test="${dicType == '0'}">
				<a href="${emms}/system/businessDictionary.do?cmd=dictionaryInfo&parentId=${parentId}" iconCls='icon-add' class="easyui-linkbutton">新建</a>
			</c:when>
			<c:when test="${dicType == '1'}">
				<a href="${emms}/system/dataDictionary.do?cmd=dictionaryInfo&parentId=${parentId}" iconCls='icon-add' class="easyui-linkbutton">新建</a>
			</c:when>
		</c:choose>
		<table id="table" auto-resize="true" class="easyui-datagrid" title="字典列表" width="100%">
		</table>
	</div>
	<script type="text/javascript">
			
			var dicType = '${dicType}';
			var queryUrl;
			if(dicType == '0'){
				queryUrl = '${emms}/system/businessDictionary.do?cmd=selectDictionaryByParentId&parentId=${parentId}';
			}else if(dicType == '1'){
				queryUrl = '${emms}/system/dataDictionary.do?cmd=selectDictionaryByParentId&parentId=${parentId}';
			}
			$(function(){
				query();
			});
			function query(){
				$('#table').datagrid({
				    url: queryUrl,
				    method: 'POST',
				    pagination: true,
				    fitColumns: true,
				    rownumbers: true,
				    showFooter: true,
				    fitColumns:true,
				    queryParams: {
					},
				    columns:[[
				        {field:'dictionaryCode',sortable:true,title:'字典编码',align:'center',width:'30%'},
				        {field:'dictionaryName',sortable:true,title:'字典名称',align:'center',width:'30%'},
				        {field:'aaa',title:'操作',sortable:true,align:'center',width:'40%',
							formatter: function(value,row,index){
								var dicType = '${dicType}';
								if(dicType == '0'){
									show = "<a class='easyui-linkbutton' href='${emms}/system/businessDictionary.do?cmd=dictionaryInfo&dictionaryId="
										+ row.dictionaryId
										+ "&parentId=${parentId}' target='_self'>编辑</a>&nbsp;&nbsp;&nbsp;";
									show +='<a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-delete" onclick="deleteRow('
									    + "'" + row.dictionaryId + "'"
									    +',\'0\')">删除</a>';
								}else if(dicType == '1'){
									show = "<a class='easyui-linkbutton' href='${emms}/system/dataDictionary.do?cmd=dictionaryInfo&dictionaryId="
										+ row.dictionaryId
										+ "&parentId=${parentId}' target='_self'>编辑</a>&nbsp;&nbsp;&nbsp;";
									show += '<a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-delete" onclick="deleteRow('
									    + "'" + row.dictionaryId + "'"
									    +',\'1\')">删除</a>';
								}
								return show;
							}}
				    ]]
				});
			}
			function clearForm(){
				$('#query').form('clear');
			}
			
			function deleteRow(dictionaryId, type){
				var deleteUrl; 
				var qUrl;
				if(type=='0'){
					deleteUrl = "${emms}/system/businessDictionary.do?cmd=deleteDictionary&ids=" + dictionaryId;
					qUrl = "${emms}/system/businessDictionary.do?cmd=query&parentId=${parentId}";
				}else if(type=='1'){
					deleteUrl = "${emms}/system/dataDictionary.do?cmd=deleteDictionary&ids=" + dictionaryId;
					qUrl = "${emms}/system/dataDictionary.do?cmd=query&parentId=${parentId}";
				}
				if(confirm('是否确认删除？')){
				$.ajax({
		            type: "GET",
		            url: deleteUrl,
		            async: false,
		            success: function(data) {
		            	alert(data);
		            	window.parent.westFrame.location.reload();
		            	window.location = qUrl;
		            },
		            error:function(XMLHttpRequest, textStatus, errorThrown){
		            	if(XMLHttpRequest.status==403){
		            		window.location = "${emms}/jsp/403.do";
		            	}
		            }
		        });
				}
			}
		</script>
</body>
</html>