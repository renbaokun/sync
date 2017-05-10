package com.dkd.emms.systemManage.service;

import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.service.BaseService;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.dao.OrderDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by YUZH on 2017/2/24.
 */
@Service
@Transactional
public class OrderService extends BaseService<Order> {
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private ProcessService processService;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    @Autowired
    private OrderDao orderDao;

    @Override
    public BaseDao<Order> getDao() {
        return orderDao;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrder(Order order) {
       if(StringUtils.isEmpty(order.getOrderId())){
           order.setOrderId(UUIDGenerator.getUUID());
           order.setOrderCode(sequenceService.getFlowNoByJudge("order", "PMBHT"+dateFormat.format(new Date()),5));
           orderDao.insert(order);
       }else{
           orderDao.update(order);
       }
        orderDetailService.delete(order.getOrderId());
        List<OrderDetail> list = new ArrayList<OrderDetail>();
        for(OrderDetail detail:order.getOrderDetailList()){
         //   detail.setOrderDetailSequence(sequenceService.getFlowNoByJudge("order","2017-03-02")+"");
            detail.setOrderId(order.getOrderId());
            detail.setOrderDetailId(UUIDGenerator.getUUID());
            BigDecimal totalPrice = detail.getOrderDetailUnitPrice().multiply(detail.getOrderDetailCount());
            detail.setOrderDetailTotalPrice(totalPrice);
            list.add(detail);
        }
        if(list.size()>0){
            orderDetailService.insetList(list);
        }
    }
    public List<Order> selectBySupplier(String supplier){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("supplier",supplier);
        return orderDao.selectBySupplier(map);
    }
    //修改已发货数量(发货单调用)
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDeliveryCount(List<DeliveryPackageDetail> detailList){
        for(DeliveryPackageDetail detail :detailList){
          if(StringUtils.isNotEmpty(detail.getDocSourceDetailId())){
              OrderDetail orderDetail = orderDetailService.selectByPk(detail.getDocSourceDetailId());
              orderDetail.setDeliveryCount(orderDetail.getDeliveryCount().add(detail.getDeliveryMainCount()==null?new BigDecimal(0):detail.getDeliveryMainCount()) );
              orderDetailService.update(orderDetail);
          }
        }
    }
    //修改已发货数量(收货单调用)
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateReceiptCount(List<ReceiptPackingDetail> detailList){
        for(ReceiptPackingDetail detail :detailList){
            if(StringUtils.isNotEmpty(detail.getDocSourceDetailId())){
                OrderDetail orderDetail = orderDetailService.selectByPk(detail.getDocSourceDetailId());
                orderDetail.setDeliveryCount(orderDetail.getDeliveryCount().add(detail.getDianshouCount()==null?new BigDecimal(0):detail.getDianshouCount()));
                orderDetailService.update(orderDetail);
            }
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateOrderState(String orderId,String state){
        Order order = orderDao.selectByPk(orderId);
        if(state.equals("orderCheck")){
         //是否存在审批
          Boolean flag=  processService.isApprove(order,orderId);
            if(flag){
                order.setOrderState(state);
            }else{
                order.setOrderState("orderPass");
            }
        }
        orderDao.update(order);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void approve(User user,Order order,String state){
        String approveState="";
        if(state.equals("orderNotPass")){
            approveState="notPass";
        }else{
            approveState="pass";
        }
        //通过是pass,不通过是notPass
        Boolean flag = processService.isCanApprove(user,order.getOrderId(),approveState);
        if(flag){
            order.setOrderState(state);
        }
        orderDao.update(order);
    }
}