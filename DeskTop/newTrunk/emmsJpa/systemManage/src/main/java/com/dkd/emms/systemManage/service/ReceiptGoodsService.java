package com.dkd.emms.systemManage.service;

import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.exception.BusinessException;
import com.dkd.emms.core.service.BaseService;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.dao.ReceiptGoodsDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by YUZH on 2017/3/6.
 */
@Service
@Transactional
public class  ReceiptGoodsService extends BaseService<ReceiptGoods> {
    @Autowired
    private ReceiptGoodsDao receiptGoodsDao;
    @Autowired
    private ReceiptPackingService receiptPackingService;
    @Autowired
    private ReceiptPackingDetailService receiptPackingDetailService;
    @Autowired
    private DrawingDetailedService drawingDetailedService;
    @Autowired
    private DemandDetailService demandDetailService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private DeliveryPackageDetailService deliveryPackageDetailService;
    @Autowired
    private DeliveryPackageService deliveryPackageService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    @Override
    public BaseDao<ReceiptGoods> getDao() {
        return receiptGoodsDao;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveReceiptGoods(ReceiptGoods receiptGoods) {
        String uuid=null;
        if(StringUtils.isEmpty(receiptGoods.getReceiptId())){
            uuid=UUIDGenerator.getUUID();
        }else{
            uuid=receiptGoods.getReceiptId();
        }
        //包装信息
        List<ReceiptPacking> receiptPackingList = new ArrayList<ReceiptPacking>();
        if(null != receiptGoods.getReceiptPackingList()){
            for(ReceiptPacking packing:receiptGoods.getReceiptPackingList()){
                packing.setReceiptId(uuid);
                packing.setPackingId(UUIDGenerator.getUUID());
                packing.setIsDianshou("0");
                if(receiptGoods.getReceiptState().equals("receiptFinish")){
                    packing.setIsDianshou("1");
                    DeliveryPacking  deliveryPacking= deliveryPackageService.selectByPk(packing.getDeliveryPackingId());
                    deliveryPacking.setIsReceipt("1");
                    deliveryPackageService.update(deliveryPacking);
                }
                receiptPackingList.add(packing);
            }
        }
        //物资明细
        List<ReceiptPackingDetail> detailList = new  ArrayList<ReceiptPackingDetail>();
        Set<String> orderNoSeq=new HashSet<String>();
        if(null != receiptGoods.getDetailList()){
            for(ReceiptPackingDetail detail:receiptGoods.getDetailList()){
                detail.setReceiptId(uuid);
                detail.setPackingDetailId(UUIDGenerator.getUUID());
                orderNoSeq.add(detail.getDocSourceNo());
                detailList.add(detail);
            }
        }
        receiptGoods.setOrderNoSeq(orderNoSeq.toString());
        if(receiptGoods.getInStorage().equals("direct") && receiptGoods.getReceiptState().equals("receiptFinish")){
            validate(detailList,receiptGoods.getReceiptOrgId());
        }
        if(receiptGoods.getReceiptState().equals("receiptFinish")){
            //更新已收货数量
            updateReceiptCount(detailList,receiptGoods.getReceiptType());
        }
        String numberBefore="";
        if(receiptGoods.getInStorage().equals("direct")){
             numberBefore="PMBZD";
        }else{
            numberBefore="PMBSH";
        }
        if(StringUtils.isEmpty(receiptGoods.getReceiptId())){
            receiptGoods.setReceiptId(uuid);
            receiptGoods.setReceiptCode(sequenceService.getFlowNoByJudge("receipt", numberBefore+dateFormat.format(new Date()),5));
            receiptGoodsDao.insert(receiptGoods);
        }else{
            receiptGoodsDao.update(receiptGoods);
        }
        receiptPackingService.delete(receiptGoods.getReceiptId());
        receiptPackingService.insetList(receiptPackingList);
        receiptPackingDetailService.delete(receiptGoods.getReceiptId());
        receiptPackingDetailService.insetList(detailList);

    }
    /*
     * 当点收类型为包裹时，明细点收数量等于本次发货数量
     * */
    public void updateDetail(String id,List<ReceiptPackingDetail> detailList){

    }
    /*
     * 当为发货单收货时，更新发货单收货数量
     * 当为合同订单时，更新合同订单发货数量；
     * */
    public void updateReceiptCount(List<ReceiptPackingDetail> detailList,String state){
          for(ReceiptPackingDetail detail :detailList){
              List<ReceiptPackingDetail> list = new ArrayList<ReceiptPackingDetail>();
              list.add(detail);
              BigDecimal dianshou=new BigDecimal(0);
              if(detail.getDianshouCount()==null){
                  dianshou = new BigDecimal(0);
              }else{
                  dianshou = detail.getDianshouCount();
              }
              if(state.equals("delivery")){
                  DeliveryPackageDetail deliveryPackageDetail =  deliveryPackageDetailService.selectByPk(detail.getDocSourceDetailId());
                  if(deliveryPackageDetail.getDeliveryMainCount().subtract(deliveryPackageDetail.getReceivedNum()).compareTo(dianshou)<0){
                      throw new BusinessException("收货物资"+detail.getMaterialsCode()+"超额");
                  }else{
                      deliveryPackageDetailService.saveReceivedNum(list);
                  }
              }else if(state.equals("order")){
                  OrderDetail orderDetail = orderDetailService.selectByPk(detail.getDocSourceDetailId());
                  if(orderDetail.getOrderDetailCount().subtract(orderDetail.getDeliveryCount()).compareTo(dianshou)<0){
                      throw new BusinessException("收货物资"+detail.getMaterialsCode()+"超额");
                  }else{
                      orderService.updateReceiptCount(list);
                  }

          }
      }
    }
        /*
       * 当为直达现场时，对于有位号的设备，需要校验有无设备料表或者需用计划；
       * 对于没有位号的设备或者普通材料需要校验有无需用计划；
       *
       * */
    public void validate(List<ReceiptPackingDetail> detailList,String orgId){
         for(ReceiptPackingDetail detail:detailList){
             Map<String,Object> map =new HashMap<String,Object>();
             if(StringUtils.isNotEmpty(detail.getDeviceNo())){
               map.put("materialsId",detail.getMaterialsId());
               map.put("drawingNumberDeviceNo",detail.getDeviceNo());
               map.put("wbsId",detail.getWbsId());
               Boolean drawingFlag = drawingDetailedService.checkStoNo(map);
               List<DemandDetail>demandDetailList = demandDetailService.selectByReceipt(map);
               if(demandDetailList.size()>0){
                   drawingFlag=true;
               }
               if(!drawingFlag){
                   throw new BusinessException("需用计划或料表中"+detail.getMaterialsCode()+"物资不存在");
               }
           }else{
               map.put("materialsId",detail.getMaterialsId());
               map.put("demandOrgId",orgId);
               map.put("wbsId",detail.getWbsId());
               List<DemandDetail>demandDetailList = demandDetailService.selectByReceipt(map);
               if(demandDetailList.size()==0){
                   throw new BusinessException("需用计划中"+detail.getMaterialsCode()+"物资不存在");
             }
           }
         }
    }
}
