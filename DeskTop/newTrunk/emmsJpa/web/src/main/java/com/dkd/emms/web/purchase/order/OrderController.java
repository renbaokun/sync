package com.dkd.emms.web.purchase.order;

import com.dkd.emms.core.util.JsonUtil;
import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.service.*;
import com.dkd.emms.web.purchase.order.queryCondition.OrderCondition;
import com.dkd.emms.web.purchase.order.queryCondition.OrderDetailCondition;
import com.dkd.emms.web.util.page.PageBean;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by YUZH on 2017/2/24.
 */

@Controller
@RequestMapping(value="/purchase/order.do")
@SessionAttributes("currentUser")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProcessRecordService processRecordService;
    @Autowired
    private ProjectService projectService;
    /**
     * 加载采购订单查询页面
     * @return
     */
    @RequestMapping( params = {"cmd=query"},produces = "text/html",method = RequestMethod.GET)
    public String query(){
        return "purchase/order/query";
    }
    /**
     * 加载采购订单数据
     */
    @RequestMapping( params = {"cmd=loadOrderListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<Order> loadOrderListData(@ModelAttribute("currentUser")User user,@RequestParam(value = "page") Integer start,
        @RequestParam(value = "rows") Integer length,OrderCondition orderCondition)
        {
        PageBean<Order> pageBean = new PageBean<Order>();
        pageBean.setTotal(orderService.countByCondition(orderCondition));
        List<Order> orderList = orderService.selectByCondition(orderCondition, pageBean.getTotal(), start, length);
           for(Order order:orderList){
             Boolean flag =  processRecordService.buttonAuthority(user,order.getOrderId());
               if(flag){
                   order.setIsAuthority(true);
               }
           }
        pageBean.setRows(orderList);
        return pageBean;
    }
    /**
     * 删除合同订单
     */
    @RequestMapping( params = {"cmd=deleteOrder"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String deleteOrder(String orderId) throws IOException {
        orderService.delete(orderId);
        orderDetailService.delete(orderId);
        return "删除完成";
    }
    /**
     *提交
     */
    @RequestMapping( params = {"cmd=updateOrderState"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String updateOrderState(String orderId,String state) throws IOException {
        orderService.updateOrderState(orderId,state);
        return "true";
    }
    /**
     *审批通过
     */
    @RequestMapping( params = {"cmd=approve"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String approve(@ModelAttribute("currentUser")User user,String orderId,String state) throws IOException {
        Order order = orderService.selectByPk(orderId);
        orderService.approve(user,order,state);
        return "true";
    }
    /**
     *审批不通过
     */
    @RequestMapping( params = {"cmd=updateOrderStateOpinion"},produces = "text/html ;charset=UTF-8",method = RequestMethod.POST)
    @ResponseBody
    public String updateOrderStateOpinion(@ModelAttribute("currentUser")User user,Order order) throws IOException {
        Order oldOrder = orderService.selectByPk(order.getOrderId());
        oldOrder.setOrderOpinion(order.getOrderOpinion());
        orderService.approve(user, oldOrder, "orderNotPass");
        return "true";
    }
    /**
     * 审核页面
     * @return
     */
    @RequestMapping( params = {"cmd=dialogCheck"},produces = "text/html",method = RequestMethod.GET)
    public String dialogCheck(String orderId, ModelMap model){
        model.addAttribute("orderId", orderId);
        return "purchase/order/dialogCheck";
    }
    //审核页面加载数据
    @RequestMapping( params = {"cmd=loadOrderData"},produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public Order loadOrderData(String orderId){
        Order order = new Order();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("orderId",orderId);
        List<Project> project = projectService.selectAll();
        if(StringUtils.isNotEmpty(orderId)){
            order = orderService.selectByPk(orderId);
            List<OrderDetail> list = orderDetailService.selectDetailByOrderId(map);
            order.setOrderDetailList(list);
            order.setProjectList(project);
        }
        return order;
    }
    /**
     * 编辑
     * @return
     */
    @RequestMapping( params = {"cmd=edit"},produces = "text/html",method = RequestMethod.GET)
    public String edit(String orderId, ModelMap model){
        model.addAttribute("orderId", orderId);
        return "purchase/order/edit";
    }
    /**
     * 查看
     * @return
     */
    @RequestMapping( params = {"cmd=view"},produces = "text/html",method = RequestMethod.GET)
    public String view(String orderId, ModelMap model){
        model.addAttribute("orderId", orderId);
        return "purchase/order/view";
    }
    /**
     * 保存
     */
    @RequestMapping( params = {"cmd=save"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String save(@ModelAttribute("currentUser")User user,@RequestBody Order order) throws IOException{
        if(StringUtils.isEmpty(order.getOrderId())){
            order.setCreateTime(new Date());
            order.setCreateUserId(user.getUserId());
            if(null == user.getEmployee()){
                order.setCreateUserName(user.getUserName());
            }else{
                order.setCreateUserName(user.getEmployee().getEmpName());
            }
        }
        orderService.saveOrder(order);
        return "true";
    }
    /**
     * 采购订单明细公共弹出框
     *
     * @return
     */
    @RequestMapping(params = {"cmd=dialogOrderDetail"}, method = RequestMethod.GET)
    public String modal(String supplierId, ModelMap model) {
        model.addAttribute("supplierId", supplierId);
        return "purchase/order/dialogOrderDetail";
    }
    /**
     * 加载采购订单明细数据
     */
    @RequestMapping( params = {"cmd=loadOrderDetailListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<OrderDetail> loadOrderDetailListData(@RequestParam(value = "page" ,required = false) Integer start,
                                             @RequestParam(value = "rows" ,required = false) Integer length,OrderDetailCondition orderDetailCondition,String ids)
    {
        String[] orderId= ids.split(",");
        List <String> orderIdList = new ArrayList<String>();
        for(String id:orderId){
          if(StringUtils.isNotEmpty(id)){
              orderIdList.add(id);
          }
        }
        orderDetailCondition.setOrderIdList(orderIdList);
        PageBean<OrderDetail> pageBean = new PageBean<OrderDetail>();
        pageBean.setTotal(orderDetailService.countByCondition(orderDetailCondition));
        pageBean.setRows(orderDetailService.selectByCondition(orderDetailCondition,pageBean.getTotal(),start,length));
        return pageBean;
    }
    /**
     * 查询所有的采购订单
     * @return
     */
    @RequestMapping( params = {"cmd=selectBySupplier"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public List<Order> selectBySupplier(String supplier){
        return orderService.selectBySupplier(supplier);

    }
}