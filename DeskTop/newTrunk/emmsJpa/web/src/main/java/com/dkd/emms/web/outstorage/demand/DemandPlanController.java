package com.dkd.emms.web.outstorage.demand;

import com.dkd.emms.systemManage.bo.DemandDetail;
import com.dkd.emms.systemManage.bo.DemandDrawing;
import com.dkd.emms.systemManage.bo.DemandPlan;
import com.dkd.emms.systemManage.bo.User;
import com.dkd.emms.systemManage.service.DemandDetailService;
import com.dkd.emms.systemManage.service.DemandDrawingService;
import com.dkd.emms.systemManage.service.DemandPlanService;
import com.dkd.emms.web.outstorage.demand.queryCondition.DemandDetailCondition;
import com.dkd.emms.web.outstorage.demand.queryCondition.DemandPlanCondition;
import com.dkd.emms.web.util.page.PageBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YUZH on 2017/3/16.
 */

@Controller
@RequestMapping(value="/outstorage/demandPlan.do")
@SessionAttributes("currentUser")
public class DemandPlanController {
    @Autowired
    private DemandPlanService demandPlanService;
    @Autowired
    private DemandDetailService demandDetailService;
    @Autowired
    private DemandDrawingService demandDrawingService;
    /**
     * 需用计划查询页面
     * @return
     */
    @RequestMapping( params = {"cmd=query"},produces = "text/html",method = RequestMethod.GET)
    public String query(){
        return "outstorage/demand/query";
    }
    /**
     * 加载需用计划数据
     */
    @RequestMapping( params = {"cmd=loadDemandListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<DemandPlan> loadDemandListData(@RequestParam(value = "page") Integer start,
                                             @RequestParam(value = "rows") Integer length,DemandPlanCondition demandPlanCondition)
    {
        PageBean<DemandPlan> pageBean = new PageBean<DemandPlan>();
        pageBean.setTotal(demandPlanService.countByCondition(demandPlanCondition));
        pageBean.setRows(demandPlanService.selectByCondition(demandPlanCondition, pageBean.getTotal(), start, length));
        return pageBean;
    }
    /**
     * 删除需用计划
     */
    @RequestMapping( params = {"cmd=deleteDemand"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String deleteDemand(String demandId) {
        demandPlanService.delete(demandId);
        demandDetailService.delete(demandId);
        return "删除完成";
    }
    /**
     * 编辑
     * @return
     */
    @RequestMapping( params = {"cmd=edit"},produces = "text/html",method = RequestMethod.GET)
    public String edit(String demandId, ModelMap model){
        model.addAttribute("demandId", demandId);
        return "outstorage/demand/edit";
    }
    /**
     * 查看
     * @return
     */
    @RequestMapping( params = {"cmd=view"},produces = "text/html",method = RequestMethod.GET)
    public String view(String demandId, ModelMap model){
        model.addAttribute("demandId", demandId);
        return "outstorage/demand/view";
    }
    /**
     * 变更
     * @return
     */
    @RequestMapping( params = {"cmd=change"},produces = "text/html",method = RequestMethod.GET)
    public String change(String demandId, ModelMap model){
        model.addAttribute("demandId", demandId);
        return "outstorage/demand/change";
    }
    //编辑\查看页面加载数据
    @RequestMapping( params = {"cmd=loadDemandData"},produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public DemandPlan loadDemandData(String demandId){
        DemandPlan demandPlan = demandPlanService.selectByPk(demandId);
        if(null != demandPlan){
            List<DemandDetail> detailList = demandDetailService.selectByDemandId(demandId);
            demandPlan.setDetailList(detailList);
        }
        return demandPlan;
    }
    /**
     * 保存
     */
    @RequestMapping( params = {"cmd=save"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String save(@ModelAttribute("currentUser")User user,@RequestBody DemandPlan demandPlan) throws IOException{
        demandPlanService.save(demandPlan,user);
        return "true";
    }
    /**
     * 提交、审批通过，审批不通过
     */
    @RequestMapping( params = {"cmd=updateDemandState"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String updateDemandState(String demandId,String state){
        demandPlanService.updateState(demandId,state);
        return "保存成功";
    }
    /**
     * 计算与料表差额  查看
     */
    @RequestMapping( params = {"cmd=selectBalanceToView"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public BigDecimal selectBalanceToView(@RequestBody DemandDetail demandDetail) {
        DemandDrawing demandDrawing=demandPlanService.selectByDemand(demandDetail);
        BigDecimal balanceCount =(demandDrawing.getDemandCount()==null?new BigDecimal(0):demandDrawing.getDemandCount()).subtract(demandDrawing.getDesignCount()==null?new BigDecimal(0):demandDrawing.getDesignCount());
        return balanceCount;
    }
    /**
     * 计算与料表差额  编辑
     */
    @RequestMapping( params = {"cmd=selectBalanceToEdit"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public BigDecimal selectBalanceToEdit(@RequestBody DemandDetail demandDetail) {
        DemandDrawing demandDrawing=demandPlanService.selectByDemand(demandDetail);
        BigDecimal balanceCount = demandDetail.getDemandCount().add(demandDrawing.getDemandCount()==null?new BigDecimal(0):demandDrawing.getDemandCount()).subtract(demandDrawing.getDesignCount()==null?new BigDecimal(0):demandDrawing.getDesignCount());
        return balanceCount;
    }
    /**
     * 变更计算与料表差额  变更
     */
    @RequestMapping( params = {"cmd=selectBalanceToChange"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public BigDecimal selectBalanceToChange(@RequestBody DemandDetail demandDetail) {
        DemandDrawing demandDrawing=demandPlanService.selectByDemand(demandDetail);
        BigDecimal balanceCount = demandDetail.getChangeCount().add(demandDrawing.getDemandCount()).subtract(demandDrawing.getDesignCount()).subtract(demandDetail.getDemandCount());
        return balanceCount;
    }
    /**
     * 弹出框
     * @return
     */
    @RequestMapping( params = {"cmd=dialogDemand"},produces = "text/html",method = RequestMethod.GET)
    public String dialogDemand(String constructionId, ModelMap model){
        model.addAttribute("constructionId", constructionId);
        return "outstorage/demand/dialogDemand";
    }
    /**
     * 弹出框需用计划数据
     */
    @RequestMapping( params = {"cmd=loadDemandDetailListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<DemandDetail> loadDemandDetailListData(@RequestParam(value = "page" ,required = false) Integer start,
                                                   @RequestParam(value = "rows" ,required = false) Integer length,DemandDetailCondition demandDetailCondition)
    {
        PageBean<DemandDetail> pageBean = new PageBean<DemandDetail>();
        pageBean.setTotal(demandDetailService.countByCondition(demandDetailCondition));
        pageBean.setRows(demandDetailService.selectByCondition(demandDetailCondition,pageBean.getTotal(),start,length));
        return pageBean;
    }
}
