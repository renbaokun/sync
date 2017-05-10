/**
 * 
 */
package com.dkd.emms.web.baseinfo.warehouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dkd.emms.systemManage.bo.Reservoirarea;
import com.dkd.emms.systemManage.bo.Storagelocation;
import com.dkd.emms.systemManage.bo.WareHouse;
import com.dkd.emms.systemManage.service.ReservoirareaService;
import com.dkd.emms.systemManage.service.StoragelocationService;
import com.dkd.emms.systemManage.service.WareHouseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.dkd.emms.systemManage.bo.User;
import com.dkd.emms.web.baseinfo.warehouse.queryCondition.ReservoirareaCondition;
import com.dkd.emms.web.baseinfo.warehouse.queryCondition.StoragelocationCondition;
import com.dkd.emms.web.baseinfo.warehouse.queryCondition.WareHouseCondition;
import com.dkd.emms.web.util.page.PageBean;

	/**
 * @Title: WareHouseController
 * @Description:
 * @param 
 * @author:YUZH 
 * @data 2017年2月7日
 */

@Controller
@RequestMapping(value="/baseinfo/warehouse.do")
@SessionAttributes("currentUser")
public class WareHouseController {
	@Autowired
	private WareHouseService wareHouseService;
	@Autowired
	private ReservoirareaService reservoirareaService;
	@Autowired
	private StoragelocationService storagelocationService;
	//初始化Frame
	@RequestMapping( params = {"cmd=warehouse"},produces = "text/html",method = RequestMethod.GET)
	public String warehouse(ModelMap model){
		model.addAttribute("westFrameUrl", "baseinfo/warehouse.do?cmd=warehouseTree");
		model.addAttribute("centerFrameUrl", "baseinfo/warehouse.do?cmd=queryWareHouse");
		return "system/common/frame";
	}
	//初始化树界面
	@RequestMapping( params = {"cmd=warehouseTree"},produces = "text/html",method = RequestMethod.GET)
	public String warehouseTree(ModelMap model){
		model.addAttribute("initTreeUrl", "baseinfo/warehouse.do?cmd=initTree");
		model.addAttribute("navUrl", "baseinfo/warehouse.do?cmd=judgeNodeType&parentId=");
		return "system/common/tree";
	}
		
	//获取整棵权限树数据
	@RequestMapping(params = {"cmd=initTree"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public List <Object> initTree(@ModelAttribute("currentUser")User user) throws Exception{
		List<WareHouse> arealist =new ArrayList<WareHouse>();
		arealist = wareHouseService.selectAll();
		/*if(null == user.getEmployee()){
		    arealist = wareHouseService.selectAll();

		}else{
			*//*List<WareHouse> orgList = user.getEmployee().getOrganization().getAllWareHouseForOrg();
			List<WareHouse> commonList = wareHouseService.selectCommom();
			arealist.addAll(orgList);
			arealist.addAll(commonList);*//*
		}*/
		List<Reservoirarea> reslist = reservoirareaService.selectAll();
		for(WareHouse wareHouse:arealist){
			wareHouse.setId(wareHouse.getWarehouseId());
			wareHouse.setName(wareHouse.getWarehouseName());
			wareHouse.setParentId("0");
			wareHouse.setLevel("1");
		}
		for(Reservoirarea reservoirarea:reslist){
			reservoirarea.setId(reservoirarea.getReservoirareaId());
			reservoirarea.setName(reservoirarea.getReservoirareaName());
			reservoirarea.setParentId(reservoirarea.getWarehouseId());
			reservoirarea.setLevel("2");
		}
		List <Object> list = new ArrayList<Object>();
		list.addAll(arealist);
		list.addAll(reslist);
		return list;
	}
	//跳转仓库查询页面
	@RequestMapping( params = {"cmd=queryWareHouse"},method = RequestMethod.GET)
	public String query(){
		return "baseinfo/warehouse/queryWareHouse";
	}
	/**
	 * 加载数据	
	 * @param model
	 * @return
	 */
	@RequestMapping( params = {"cmd=selectWareHouse"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<WareHouse> selectWareHouse(@ModelAttribute("currentUser")User user, @RequestParam(value = "page") int start,@RequestParam(value = "rows") int length,
		WareHouseCondition wareHouseCondition,ModelMap model){
		List<WareHouse> arealist =new ArrayList<WareHouse>();
		List<String> ids =new ArrayList<String>();
		arealist = wareHouseService.selectAll();
		/*if(null == user.getEmployee()){
		    arealist = wareHouseService.selectAll();
		}else{
			*//*List<WareHouse> orgList = user.getEmployee().getOrganization().getAllWareHouseForOrg();
			List<WareHouse> commonList = wareHouseService.selectCommom();
			arealist.addAll(orgList);
			arealist.addAll(commonList);*//*
		}*/
		for(WareHouse warehouse:arealist){
			ids.add(warehouse.getWarehouseId());
		}
		wareHouseCondition.setWarehouseIds(ids);
		PageBean<WareHouse> pageBean = new PageBean<WareHouse>();
		pageBean.setTotal(wareHouseService.countByCondition(wareHouseCondition));
		pageBean.setRows(wareHouseService.selectByCondition(wareHouseCondition,pageBean.getTotal(),start,length));
		return pageBean;
	}
	//判断该节点的类型，给出对应的跳转页
	@RequestMapping( params = {"cmd=judgeNodeType"},produces = "text/html",method = RequestMethod.GET)
	public String judgeNodeType(String selfId,String level,  ModelMap model){
		if(selfId!=null && selfId.equals("0")){	//如果是根节点
			return "redirect:warehouse.do?cmd=queryWareHouse"; 
		} else if(selfId!=null && level.equals("1")){
			return "redirect:warehouse.do?cmd=queryReservoirarea&parentId="+ selfId; 
		} else if(selfId!=null && level.equals("2")){
			return "redirect:warehouse.do?cmd=queryStoragelocation&parentId="+ selfId; 
		}else{
			return "redirect:warehouse.do?cmd=queryWareHouse"; 
		}
	}
	//跳转仓库查询页面
	@RequestMapping( params = {"cmd=queryReservoirarea"},method = RequestMethod.GET)
	public String queryReservoirarea(String parentId, ModelMap model){
		model.addAttribute("parentId", parentId);
		return "baseinfo/warehouse/queryReservoirarea";
	}
	/**
	 * 加载数据	
	 * @param model
	 * @return
	 */
	@RequestMapping( params = {"cmd=selectReservoirarea"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<Reservoirarea> selectReservoirarea(@RequestParam(value = "page") int start,@RequestParam(value = "rows") int length,
			String parentId,ModelMap model){
		ReservoirareaCondition res = new ReservoirareaCondition();
		res.setWarehouseId(parentId);
		PageBean<Reservoirarea> pageBean = new PageBean<Reservoirarea>();
		pageBean.setTotal(reservoirareaService.countByCondition(res));
		pageBean.setRows(reservoirareaService.selectByCondition(res,pageBean.getTotal(),start,length));
		return pageBean;
	}
	//跳转储位查询页面
	@RequestMapping( params = {"cmd=queryStoragelocation"},method = RequestMethod.GET)
	public String queryStoragelocation(String parentId, ModelMap model){
		model.addAttribute("parentId", parentId);
		return "baseinfo/warehouse/queryStoragelocation";
	}
	/**
	 * 加载数据	
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping( params = {"cmd=selectStoragelocation"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<Storagelocation> selectStoragelocation(@RequestParam(value = "page") int start,@RequestParam(value = "rows") int length,
			StoragelocationCondition storagelocationCondition,String parentId,ModelMap model,HttpServletRequest request){
		storagelocationCondition.setReservoirareaId(parentId);
		PageBean<Storagelocation> pageBean = new PageBean<Storagelocation>();
		pageBean.setTotal(storagelocationService.countByCondition(storagelocationCondition));
		pageBean.setRows(storagelocationService.selectByCondition(storagelocationCondition,pageBean.getTotal(),start,length));
		return pageBean;
	}
	//跳转仓库查询页面
	@RequestMapping( params = {"cmd=queryWareHouseInfo"},method = RequestMethod.GET)
	public String queryWareHouseInfo(String warehouseId,ModelMap model){
		model.addAttribute("warehouseId", warehouseId);
		return "baseinfo/warehouse/editWareHouse";
	}
	//跳转到仓库编辑页面并获取相应数据
	@RequestMapping( params = {"cmd=wareHouseInfo"},produces = "application/json",method = RequestMethod.GET)
	@ResponseBody
	public WareHouse wareHouseInfo(String warehouseId){
		WareHouse warehouse = new WareHouse();
		if(StringUtils.isNotEmpty(warehouseId)){
			warehouse = wareHouseService.selectByPk(warehouseId);
		}
		return warehouse;
	}
	//保存仓库信息
	@RequestMapping( params = {"cmd=wareHouseEdit"},produces = "text/html ;charset=UTF-8",method = RequestMethod.POST)
	@ResponseBody
	public String wareHouseEdit(@ModelAttribute("currentUser")User user ,WareHouse wareHouse) throws IOException{
		wareHouseService.save(wareHouse,user);
		return "保存成功";
	}


	/**
	 * 校验是否重复
	 */
	@RequestMapping( params = {"cmd=checkWareNo"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public boolean checkWareNo(String warehouseCode) throws IOException{
		return wareHouseService.checkStoNo(warehouseCode,null);
	}
        /*————————————————————————————————公共仓库库区弹出框———————————————————————————————*/
		/**
		 * 物理删除仓库
		 * @return
		 * @throws IOException
		 */
		@RequestMapping( params = {"cmd=deleteWareHouse"},produces = "application/json",method = RequestMethod.POST)
		@ResponseBody
		public String deleteWareHouse(String id) throws IOException{
			wareHouseService.delete(id);
			reservoirareaService.deleteByWareId(id);
			storagelocationService.deleteByWareId(id);
			return "删除完成";
		}
	//初始化Frame
	@RequestMapping( params = {"cmd=dialogWarehouse"},produces = "text/html",method = RequestMethod.GET)
	public String dialogWarehouse(ModelMap model,String index){
		model.addAttribute("dialogFrame", "baseinfo/warehouse.do?cmd=dialogWarehouseFrame&index="+index);
		return "system/common/treeDialogFrame";
	}
	//初始化Frame
	@RequestMapping( params = {"cmd=dialogWarehouseFrame"},produces = "text/html",method = RequestMethod.GET)
	public String dialogWarehouseFrame(ModelMap model,String index){
		model.addAttribute("westFrameUrl", "baseinfo/warehouse.do?cmd=dialogWarehouseTree&index="+index);
		model.addAttribute("centerFrameUrl", "baseinfo/warehouse.do?cmd=dialogQueryWareHouse");
		return "system/common/frame";
	}
	//初始化树界面
	@RequestMapping( params = {"cmd=dialogWarehouseTree"},produces = "text/html",method = RequestMethod.GET)
	public String dialogWarehouseTree(ModelMap model,String index){
		model.addAttribute("initTreeUrl", "baseinfo/warehouse.do?cmd=initTree");
		model.addAttribute("navUrl", "baseinfo/warehouse.do?cmd=dialogJudgeNodeType&index="+index+"&parentId=");
		return "system/common/tree";
	}
	//判断该节点的类型，给出对应的跳转页
	@RequestMapping( params = {"cmd=dialogJudgeNodeType"},produces = "text/html",method = RequestMethod.GET)
	public String dialogJudgeNodeType(String selfId,String level,String index,  ModelMap model){
		if(selfId!=null && selfId.equals("0")){	//如果是根节点
			return "redirect:warehouse.do?cmd=dialogQueryWareHouse"; 
		} else if(selfId!=null && level.equals("1")){//根据仓库查储位
			return "redirect:warehouse.do?cmd=dialogQueryStoragelocation&index="+index+"&parentId="+ selfId;
		} else if(selfId!=null && level.equals("2")){//根据库区查储位
			return "redirect:warehouse.do?cmd=dialogQueryStoragelocation1&index="+index+"&parentId="+ selfId;
		}else{
			return "redirect:warehouse.do?cmd=dialogQueryWareHouse"; 
		}
	}
	//跳转仓库查询页面
	@RequestMapping( params = {"cmd=dialogQueryWareHouse"},method = RequestMethod.GET)
	public String dialogQueryWareHouse(){
		return "baseinfo/warehouse/dialogQueryWarehouse";
	}
	//跳转储位查询页面
	@RequestMapping( params = {"cmd=dialogQueryStoragelocation"},method = RequestMethod.GET)
	public String dialogQueryStoragelocation(String index,String parentId, ModelMap model){
		model.addAttribute("parentId", parentId);
		model.addAttribute("index", index);
		return "baseinfo/warehouse/dialogQueryStoragelocation";
	}
	//跳转储位查询页面
	@RequestMapping( params = {"cmd=dialogQueryStoragelocation1"},method = RequestMethod.GET)
	public String dialogQueryStoragelocation1(String parentId,String index, ModelMap model){
		model.addAttribute("parentId", parentId);
		model.addAttribute("index", index);
		return "baseinfo/warehouse/dialogQueryStoragelocation1";
	}	
	@RequestMapping( params = {"cmd=dialogSelectStoragelocation"},produces = "application/json",method = RequestMethod.POST)
	@ResponseBody
	public PageBean<Storagelocation> dialogSelectStoragelocation(@RequestParam(value = "page") int start,@RequestParam(value = "rows") int length,
			StoragelocationCondition storagelocationCondition,String parentId,ModelMap model,HttpServletRequest request){
		storagelocationCondition.setWarehouseId(parentId);
		PageBean<Storagelocation> pageBean = new PageBean<Storagelocation>();
		pageBean.setTotal(storagelocationService.countByCondition(storagelocationCondition));
		pageBean.setRows(storagelocationService.selectByCondition(storagelocationCondition,pageBean.getTotal(),start,length));
		return pageBean;
	}	
}
