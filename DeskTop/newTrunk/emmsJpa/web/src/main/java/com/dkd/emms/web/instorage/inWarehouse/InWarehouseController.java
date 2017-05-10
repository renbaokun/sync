package com.dkd.emms.web.instorage.inWarehouse;


import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.service.InWarehouseDetailService;
import com.dkd.emms.systemManage.service.InWarehouseService;
import com.dkd.emms.systemManage.service.StockService;
import com.dkd.emms.web.instorage.inWarehouse.queryCondition.InWarehouseCondition;
import com.dkd.emms.web.instorage.inWarehouse.queryCondition.InWarehouseDetailCondition;
import com.dkd.emms.web.util.page.PageBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/3/13.
 */

@Controller
@RequestMapping(value="/instorage/inWarehouse.do")
@SessionAttributes("currentUser")
public class InWarehouseController {
    @Autowired
    private InWarehouseService inWarehouseService;
    @Autowired
    private InWarehouseDetailService inWarehouseDetailService;
    @Autowired
    private StockService stockService;
    /**
     * 跳转查询页面
     * @return
     */
    @RequestMapping( params = {"cmd=query"},method = RequestMethod.GET)
    public String query(){return "instorage/inWarehouse/query";}

    /**
     * 加载入库单数据
     * @return
     */
    @RequestMapping( params ={"cmd=loadInWarehouseListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<InWarehouse> loadInWarehouseListData(@RequestParam(value = "page") Integer start,@RequestParam(value = "rows") Integer length,
                                                     InWarehouseCondition inWarehouseCondition){
        PageBean<InWarehouse> pageBean = new PageBean<InWarehouse>();
        pageBean.setTotal(inWarehouseService.countByCondition(inWarehouseCondition));
        pageBean.setRows(inWarehouseService.selectByCondition(inWarehouseCondition,pageBean.getTotal(),start,length));
        return pageBean;
    }
    /**
     *删除入库单数据
     *
     * @param
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping( params = {"cmd=delete"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String delete(String inWarehouseId) throws IOException {
        inWarehouseService.delete(inWarehouseId);
        inWarehouseDetailService.delete(inWarehouseId);
        return "删除完成";
    }
    /**
     * 提交框架协议数据
     */
    @RequestMapping( params = {"cmd=updateInWarehouseState"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String updateInWarehouseState(String inWarehouseId,String state) throws IOException {
        InWarehouse inWarehouse = inWarehouseService.selectByPk(inWarehouseId);
        inWarehouse.setInWarehouseState(state);
        inWarehouseService.saveStock(inWarehouse);
        return "提交成功";
    }

    /**
     * 查看
     * @return
     */
    @RequestMapping( params = {"cmd=view"},produces = "text/html",method = RequestMethod.GET)
    public String view(String inWarehouseId, ModelMap model){
        model.addAttribute("inWarehouseId", inWarehouseId);
        return "instorage/inWarehouse/view";
    }
    /**
     * 跳转框架协议编辑页面
     * @return
     */
    @RequestMapping( params = {"cmd=edit"},produces = "text/html",method = RequestMethod.GET)
    public String edit(String inWarehouseId,ModelMap model){
        model.addAttribute("inWarehouseId", inWarehouseId);
        return "instorage/inWarehouse/edit";
    }
    /**
     * 框架协议编辑页面加载数据
     *
     */
    @RequestMapping( params = {"cmd=loadInWarehouseData"},produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public InWarehouse loadInWarehouseData(String inWarehouseId,@ModelAttribute("currentUser")User user) {
        InWarehouse inWarehouse = new InWarehouse();
        InWarehouseDetailCondition inWarehouseDetailCondition = new InWarehouseDetailCondition();
        if(StringUtils.isNotEmpty(inWarehouseId)){
            inWarehouse = inWarehouseService.selectByPk(inWarehouseId);
            inWarehouseDetailCondition.setInWarehouseId(inWarehouseId);
            List<InWarehouseDetail> list = inWarehouseDetailService.selectByCondition( inWarehouseDetailCondition,0,1,-1);
            inWarehouse.setInWarehouseDetailList(list);
        }else{
            inWarehouse.setCreateUserID(user.getUserId());
            if(null != user.getEmployee()){
                inWarehouse.setCreateUserName(user.getEmployee().getEmpName());
            }else {
                inWarehouse.setCreateUserName(user.getUserName());
            }
        }
        return inWarehouse;
    }
    /**
     * 框架协议编辑页面保存
     *
     */
    @RequestMapping( params = {"cmd=save"}, produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String save(@ModelAttribute("currentUser")User user, @RequestBody InWarehouse inWarehouse) throws IOException{
        if(inWarehouse.getInWarehouseState().equals("yiruku")) {
            inWarehouseService.saveInStock(inWarehouse,user);
            return "提交成功";
        }
        else{
            inWarehouseService.saveInWarehouse(inWarehouse, user);
            return "保存成功";
        }
    }
    /**
     * 物资明细弹出框
     *
     * @return
     */
    @RequestMapping(params = {"cmd=modal"}, method = RequestMethod.GET)
    public String modal() {
        return "instorage/materialManag/materialDetail";
    }


}
