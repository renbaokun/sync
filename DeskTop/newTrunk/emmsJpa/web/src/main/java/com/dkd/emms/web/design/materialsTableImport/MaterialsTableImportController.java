/**
 * 
 */
package com.dkd.emms.web.design.materialsTableImport;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dkd.emms.core.entity.ExcleFileForm;
import com.dkd.emms.core.exception.BusinessException;
import com.dkd.emms.web.design.materialsTableImport.queryCondition.DrawingDetailedCondition;
import com.dkd.emms.web.design.materialsTableImport.queryCondition.DrawingNumberCondition;
import com.dkd.emms.web.design.materialsTableImport.queryCondition.MaterialsTableCondition;
import com.dkd.emms.web.util.page.PageBean;


/**
 * @Title: MaterialsTableControlle
 * @Description:
 * @param
 * @author:YUZH
 * @data 2017年1月24日
 */

@Controller
@RequestMapping(value = "/design/materialstableImprot.do")
@SessionAttributes("currentUser")
public class MaterialsTableImportController {
	
	@Autowired
	private MaterialsTableService materialsTableImportService;
	
	@Autowired
	private DrawingNumberService drawingNumberService;
	
	@Autowired
	private DrawingDetailedService drawingDetailedService;
	
	@Autowired
	private ExcelParsingService excelParsingService;
	
	@Autowired
	private MaterialsTableDesignCodeService MtDcService;

	@Autowired
	private DesignCodeService designCodeService;


	//判断页签，给出对应的跳转页
	@RequestMapping( params = {"cmd=qureyFrame"},produces = "text/html",method = RequestMethod.GET)
	public String judgeNodeType(ModelMap model){
		return "design/materialsTableImport/queryFrame";
	}
	
	//返回料表查询页面
	@RequestMapping( params = {"cmd=queryMatrialsTable"},produces = "text/html",method = RequestMethod.GET)
	public String query(){
		return "design/materialsTableImport/query";
	}
	
	//加载料表查询页面数据
	@RequestMapping( params = {"cmd=loadMTData"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<MaterialsTable> loadMTData(@RequestParam(value = "page",required = false) Integer start,@RequestParam(value = "rows",required = false) Integer length,
			MaterialsTableCondition materialsTableCondition){
		List<Organization> designList = materialsTableImportService.selectOrgByUser();
		if(materialsTableCondition.getDesignOrgId()== null || materialsTableCondition.getDesignOrgId().equals("")){
			/*materialsTableCondition.setDesignOrgs(designList);*/
		}
		PageBean<MaterialsTable> pageBean = new PageBean<MaterialsTable>();
		pageBean.setTotal(materialsTableImportService.countByCondition(materialsTableCondition));
		pageBean.setRows(materialsTableImportService.selectByCondition(materialsTableCondition,pageBean.getTotal(),start,length));
		return pageBean;
	}
	
	//加载设计院组织列表
	@RequestMapping( params = {"cmd=loadOrg"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List<Organization> loadDicByCode(String dicCode) throws IOException{
		 return materialsTableImportService.selectOrgByUser();
	}
	
	//返回图号查询页面
	@RequestMapping( params = {"cmd=queryDrawingNumber"},produces = "text/html",method = RequestMethod.GET)
	public String queryDrawingNumber(){
		return "design/materialsTableImport/queryDrawingNumber";
	}
	
	//加载图号查询页面数据
	@RequestMapping( params = {"cmd=loadDNData"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<DrawingNumber> loadDNData(@RequestParam(value = "page",required = false) Integer start,@RequestParam(value = "rows",required = false) Integer length,
			DrawingNumberCondition drawingNumberCondition){
		List<Organization> designList = materialsTableImportService.selectOrgByUser();
		if(drawingNumberCondition.getDesignOrgId()== null || drawingNumberCondition.getDesignOrgId().equals("")){
			drawingNumberCondition.setDesignOrgs(designList);
		}
		PageBean<DrawingNumber> pageBean = new PageBean<DrawingNumber>();
		pageBean.setTotal(drawingNumberService.countByCondition(drawingNumberCondition));
		pageBean.setRows(drawingNumberService.selectByCondition(drawingNumberCondition,pageBean.getTotal(),start,length));
		return pageBean;
	}
	
	//文件上传弹出框
	@RequestMapping( params = {"cmd=uploadPopUpBox"},produces = "text/html",method = RequestMethod.GET)
	public String uploadPopUpBox(){
		return "design/materialsTableImport/upload";
	}
	
	//解析excel文件，并存入数据库
	@RequestMapping( params = {"cmd=poi"},produces = "text/html ;charset=UTF-8",consumes="multipart/form-data",method = RequestMethod.POST)
	@ResponseBody
	public String poi(@ModelAttribute("currentUser")User user, ExcleFileForm excleFileForm,@RequestParam MultipartFile excel, HttpServletResponse response){
		String fileName = excel.getOriginalFilename();
		if(!fileName.equals("")){
			String ext = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
			ext = ext.toLowerCase();
			if(!ext.equals("xlsx")){
				throw new BusinessException("请上传格式正确的文件！");
			}
		}
		//读取文件，解析错误信息，存储数据，存储原始文件
		if(excleFileForm.getMaterialsTableType().equals("w")){
			excelParsingService.saveOriginalFile(excel, excelParsingService.checkAndSaveListW(excelParsingService.poiW(excel),
					excleFileForm.getDesignOrgId(), excleFileForm.getMaterialsTableType(), user, "Upload"));
			return "文件上传完毕";
		}else{
			Map<String, Object> parsingResult = excelParsingService.poiS(excel);
			String errorRow = (String) parsingResult.get("errorRow");
			List<DrawingDetailed[]> excelList = (List<DrawingDetailed[]>) parsingResult.get("List");
			try {
				String materialsTableId = excelParsingService.checkAndSaveListS(errorRow,excelList, excleFileForm.getDesignOrgId(), excleFileForm.getMaterialsTableType(), user, "Upload");
				excelParsingService.saveOriginalFile(excel, materialsTableId);//保存文件
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return "文件上传完毕";
		}
	}

	//返回料表明细页面 编辑或查看
	@RequestMapping( params = {"cmd=queryMTdetailed"},produces = "text/html",method = RequestMethod.GET)
	public String queryMTdetailed(String materialsTableId, String materialsTableType, String operation, ModelMap model){
		model.addAttribute("materialsTableId", materialsTableId);
		if(operation.equals("edit")){
			if(materialsTableType.equals("w")){
				return "design/materialsTableImport/editMTdetailedW";
			}else{
				return "design/materialsTableImport/editMTdetailedS";
			}
		}else if(operation.equals("view")){
			if(materialsTableType.equals("w")){
				return "design/materialsTableImport/queryMTdetailedW";
			}else{
				return "design/materialsTableImport/queryMTdetailedS";
			}
		}
		return null;
	}
	
	//根据料表ID加载料表明细数据
	@RequestMapping( params = {"cmd=loadMTdetailed"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> loadMTdetailed(String materialsTableId){
		DrawingDetailedCondition ddc = new DrawingDetailedCondition();
		ddc.setMaterialsTableId(materialsTableId);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("DrawingDetailedCondition", ddc);
		List<DrawingDetailed> list = drawingDetailedService.selectByConditionNoPage(map);
		for(DrawingDetailed dd: list){
			dd.set_parentId(dd.getParentId());
			if(null ==dd.getDrawingDetailedNo() || dd.getDrawingDetailedNo().toString().equals("-1")){
				dd.setDrawingDetailedNo(null);
			}
			if(null ==dd.getOverrun() || dd.getOverrun().toString().equals("-1")){
				dd.setOverrun(null);
			}
		}
		Map<String,Object> returnMap = new HashMap<String,Object>();
		returnMap.put("rows",list);
		returnMap.put("total",list.size());
		return returnMap;
	}

	//查看更正错误条目弹出框
	@RequestMapping( params = {"cmd=viewAndModifyErrors"},produces = "text/html",method = RequestMethod.GET)
	public String viewAndModifyErrors(String materialsTableId, String materialsTableType, ModelMap model){
		model.addAttribute("materialsTableId", materialsTableId);
		if(materialsTableType.equals("w")){
			return "design/materialsTableImport/modifyErrorsPopUpW";
		}else{
			return "design/materialsTableImport/modifyErrorsPopUpS";
		}
	}
	
	//返回料表主表信息
	@RequestMapping( params = {"cmd=loadMT"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List<MaterialsTable> loadMT(String materialsTableId){
		MaterialsTable mt = materialsTableImportService.selectByPk(materialsTableId);
		List<MaterialsTable> MT = new ArrayList<MaterialsTable>();
		MT.add(mt);
		return MT;
	}
	//获取错误类型1 onlyAandB
	@RequestMapping( params = {"cmd=loadErrorTypeOnlyAandB"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List<DrawingDetailed> loadErrorTypeOnlyAandB(String materialsTableId) throws Exception{
		DrawingDetailedCondition ddc = new DrawingDetailedCondition();
		ddc.setMaterialsTableId(materialsTableId);
		ddc.setErrorType("1");
		List<DrawingDetailed> onlyAandB = drawingDetailedService.selectByErrorType(ddc);
		return onlyAandB;
	}

	//获取错误类型2 onlyAandB
	@RequestMapping( params = {"cmd=loadErrorTypeContainCnotD"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List<DrawingDetailed> loadErrorTypeContainCnotD(String materialsTableId) throws Exception{
		DrawingDetailedCondition ddc = new DrawingDetailedCondition();
		ddc.setMaterialsTableId(materialsTableId);
		ddc.setErrorType("2");
		List<DrawingDetailed> result = drawingDetailedService.selectByErrorType(ddc);
		return result;
	}

	//获取错误类型3 containD
	@RequestMapping( params = {"cmd=loadErrorTypeContainD"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List<DrawingDetailed> loadErrorTypeContainD(String materialsTableId) throws Exception{
		DrawingDetailedCondition ddc = new DrawingDetailedCondition();
		ddc.setMaterialsTableId(materialsTableId);
		ddc.setErrorType("3");
		List<DrawingDetailed> containD = drawingDetailedService.selectByErrorType(ddc);
		return containD;
	}
	
	//修改错误信息后保存
	@RequestMapping( params = {"cmd=saveModifyErrors"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public String saveModifyErrors(@RequestBody DrawingDetailedList ddl) throws Exception{
		if(ddl.getDetailList().get(0).getDrawingDetailedType().equals("w")){
			excelParsingService.checkAndSaveListW(ddl.getDetailList(), ddl.getDesignOrgId(), "", null, "modifyErrors");
		}else{
			String errorRow = "";
			List<DrawingDetailed[]> excelList = new ArrayList<DrawingDetailed[]>();
			for(DrawingDetailed dd: ddl.getDetailList()){
				DrawingDetailed[] dg = new DrawingDetailed[2];
				dg[0] = dd;
				excelList.add(dg);
			}
			excelParsingService.checkAndSaveListS(errorRow,excelList, ddl.getDesignOrgId(), "", null, "modifyErrors");
		}
		return "true";
	}

	//合并C，D类重复项（合并本质上是删除多余重复项只留一条）
	@RequestMapping( params = {"cmd=mergeDuplicates"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public String mergeDuplicates(String ids){
		String[] id = ids.split(",");
		for(int i=0; i<id.length; i++){

		}
		return "合并成功";
	}
	
	//料表提交
	@RequestMapping( params = {"cmd=commitMT"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public int commitMT(String materialsTableId){
		MaterialsTable mt = materialsTableImportService.selectByPk(materialsTableId);
		DrawingDetailedCondition ddc = new DrawingDetailedCondition();
		ddc.setMaterialsTableId(materialsTableId);
		//需要回填的明细列表
		List<DrawingDetailed> needCompleteInfoList = new ArrayList<DrawingDetailed>();
		if( mt.getIsMatch().equals("0")){//未经过单向同步
			//获取查询条件列表
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DrawingDetailedCondition", ddc);
			List<DrawingDetailed> conditionList = drawingDetailedService.selectByConditionNoPage(map);
			//返回已匹配项Id列表 相当于 设计院编码里面 有
			List<DrawingDetailed> matchingItem = MtDcService.findMatching(conditionList);
			//返回未匹配项Id列表 相当于 设计院编码里面 没有
			List<DrawingDetailed> mismatchItem = MtDcService.findMismatch(conditionList);
			//此处调用辉辉的方法 传入mismatchItem 将未匹配的项目插入设计院编码表
			designCodeService.saveBatchDesign(conditionList);

			//校验单位 （暂不开发）
			MtDcService.checkUnit(matchingItem);

			//更新料表状态为已同步
			mt.setIsMatch("1");
			materialsTableImportService.update(mt);
			needCompleteInfoList.addAll(conditionList);
		}else{
			//准备再次更新所有未匹配项
			ddc.setDrawingDetailedState("notMatch");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DrawingDetailedCondition", ddc);
			needCompleteInfoList = drawingDetailedService.selectByConditionNoPage(map);
		}
		//为已同步的明细回填信息
		MtDcService.makeInformationComplete(needCompleteInfoList);
		//返回未匹配数量
		ddc.setDrawingDetailedState("notMatch");
		return drawingDetailedService.countByCondition(ddc);
	}

	//返回本次提交结果页面
	@RequestMapping( params = {"cmd=viewTheResultsOfThisSubmission"},produces = "text/html",method = RequestMethod.GET)
	public String viewTheResultsOfThisSubmission(String materialsTableId, ModelMap model){
		model.addAttribute("materialsTableId", materialsTableId);
		return "design/materialsTableImport/ResultsOfThisSubmission";
	}

	//加载本次提交结果
	@RequestMapping( params = {"cmd=loadResultsOfThisSubmission"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List<DrawingDetailed> loadResultsOfThisSubmission(String materialsTableId){
		DrawingDetailedCondition ddc = new DrawingDetailedCondition();
		ddc.setMaterialsTableId(materialsTableId);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("DrawingDetailedCondition",ddc);
		return drawingDetailedService.selectNotConform(map);
	}

	//更新料表状态为已生成图号
	@RequestMapping( params = {"cmd=updateMTtoAlreadyGenerateDrawingNumber"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public String updateMTtoAlreadyGenerateDrawingNumber(@ModelAttribute("currentUser")User user, String materialsTableId){
		MaterialsTable mt = new MaterialsTable();
		mt.setMaterialsTableId(materialsTableId);
		mt.setMaterialsTableState("drawingNumber");
		//更新料表状态
		materialsTableImportService.update(mt);
		//批量更新所有图号确认信息
		drawingNumberService.updateConfirmTime(materialsTableId, user);
		return "true";
	}

	//提供原始文件的下载
	@RequestMapping( params = {"cmd=downloadOriginalFile"}, method = RequestMethod.GET)
	public void downloadOriginalFile(String materialsTableId, HttpServletResponse response){
		MaterialsFile materialsFile = excelParsingService.selectMFbyId(materialsTableId);
		try {
			response.addHeader("Content-Disposition", "attachment;filename=" +
					new String(materialsFile.getRealFileName().replaceAll(" ", "").getBytes("GB2312"), "iso8859-1"));
			byte[] contents = materialsFile.getContents();
			OutputStream out = new BufferedOutputStream(response.getOutputStream());
			out.write(contents);//输出文件
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//删除料表物资明细
	@RequestMapping( params = {"cmd=deleteRow"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public boolean deleteRow(String drawingDetailedId){
		drawingDetailedService.delete(drawingDetailedId);
		return true;
	}

	//删除料表设备和部件
	@RequestMapping( params = {"cmd=deleteRowS"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public boolean deleteRowS(String drawingDetailedId){
		drawingDetailedService.delete(drawingDetailedId);
		drawingDetailedService.deletePartsBySid(drawingDetailedId);
		return true;
	}

	//删除整张料表
	@RequestMapping( params = {"cmd=deleteEntireMT"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public boolean deleteEntireMT(String materialsTableId){
		//删除该料表下的包括文件明细在内的一切信息
		materialsTableImportService.delete(materialsTableId);
		drawingNumberService.deleteByMTid(materialsTableId);
		drawingDetailedService.deleteByMTid(materialsTableId);
		excelParsingService.deleteFile(materialsTableId);
		return true;
	}

	//批量更新明细
	@RequestMapping( params = {"cmd=batchUpdate"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public String batchUpdate(@RequestBody DrawingDetailedList ddl){
		drawingDetailedService.batchUpdate(ddl.getDetailList());
		return "true";
	}

	//text interceptor
	@RequestMapping( params = {"cmd=textInterceptor"},produces = "text/html",method = RequestMethod.POST)
	public void textInterceptor(@RequestBody MaterialsTable mt, ModelMap model){
		model.addAttribute("materialsTableId", mt);
	}

	//查看更正错误条目弹出框
	@RequestMapping( params = {"cmd=queryAllDetail"},produces = "text/html",method = RequestMethod.GET)
	public String queryAllDetail(String wbsId, ModelMap model){
		model.addAttribute("wbsId", wbsId);
		return "design/materialsTableImport/queryAllDetail";
	}

	//按条件加载图号明细数据
	@RequestMapping( params = {"cmd=loadDrawingDetailByCondition"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<DrawingDetailed> loadDrawingDetailByCondition(DrawingDetailedCondition ddc){
		PageBean<DrawingDetailed> pageBean = new PageBean<DrawingDetailed>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("DrawingDetailedCondition", ddc);
		pageBean.setTotal(drawingDetailedService.countByConditionPage(map));
		pageBean.setRows(drawingDetailedService.selectByCondition(ddc,pageBean.getTotal()));
		return pageBean;
	}
}