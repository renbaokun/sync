package com.dkd.emms.systemManage.service;


import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.service.BaseService;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.systemManage.bo.Delivery;
import com.dkd.emms.systemManage.bo.DeliveryPackageDetail;
import com.dkd.emms.systemManage.bo.ReceiptGoods;
import com.dkd.emms.systemManage.bo.ReceiptPackingDetail;
import com.dkd.emms.systemManage.dao.DeliveryPackageDetailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * 承包商管理
 * @author wangqian
 *
 */
@Service
public class DeliveryPackageDetailService extends BaseService<DeliveryPackageDetail> {

	@Autowired
	private DeliveryPackageDetailDao deliveryPackageDetailDao;
	@Override
	public BaseDao<DeliveryPackageDetail> getDao() {
		// TODO Auto-generated method stub
		return deliveryPackageDetailDao;
	}

	/**
	 * 根据包装id查询其中的物料明细
	 * @param packageId
	 * @return
	 */
	public List<DeliveryPackageDetail> queryDetailByPackageId(String packageId){
		System.out.print("1234");
		return deliveryPackageDetailDao.queryDetailByPackageId(packageId);
	}

	/**
	 * 根据发货单id查询所有的明细信息
	 * @param deliveryId
	 * @return
	 */
	public List<DeliveryPackageDetail> queryDetailByDeliveryId(String deliveryId){
		return deliveryPackageDetailDao.queryDetailByDeliveryId(deliveryId);
	}
	/**
	 * 保存包装明细信息
	 * @param delivery
	 */
	@Transactional
	public void saveDeliveryPackageDetail(Delivery delivery){
		deliveryPackageDetailDao.delDeliveryPackageDetail(delivery.getDeliveryId());
		List<DeliveryPackageDetail> deliveryPackingList = new ArrayList<DeliveryPackageDetail>();
		for(DeliveryPackageDetail deliveryPackageDetail:delivery.getPackageDetail()){
			this.setDefault(deliveryPackageDetail,delivery);
			deliveryPackageDetail.setDeDetailId(UUIDGenerator.getUUID());
			deliveryPackingList.add(deliveryPackageDetail);
		}
		if(deliveryPackingList.size()>0){
			deliveryPackageDetailDao.insertList(deliveryPackingList);
		}
	}

	/**
	 * 更新发货单中的已收货数量
	 * @param receiptPackingDetailList
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveReceivedNum(List<ReceiptPackingDetail> receiptPackingDetailList){
		for(ReceiptPackingDetail receiptPackingDetail : receiptPackingDetailList){
			DeliveryPackageDetail deliveryPackageDetail = this.selectByPk(receiptPackingDetail.getDocSourceDetailId());
			deliveryPackageDetail.setReceivedNum(deliveryPackageDetail.getReceivedNum().add(receiptPackingDetail.getDianshouCount()==null?new BigDecimal(0):receiptPackingDetail.getDianshouCount()));
			this.update(deliveryPackageDetail);
		}
	}

	/**
	 * 设置默认值
	 * @param deliveryPackageDetail
	 */
	private  void setDefault(DeliveryPackageDetail deliveryPackageDetail,Delivery delivery){
		if(StringUtils.isEmpty(deliveryPackageDetail.getDeliveryId())){
			deliveryPackageDetail.setDeliveryId(delivery.getDeliveryId());
		}
		if(StringUtils.isEmpty(deliveryPackageDetail.getDeliveryNo())){
			deliveryPackageDetail.setDeliveryNo(delivery.getDeliveryNo());
		}
		if(StringUtils.isEmpty(deliveryPackageDetail.getSupplierId())){
			deliveryPackageDetail.setSupplierId(delivery.getSupplierId());
		}
		if(StringUtils.isEmpty(deliveryPackageDetail.getSupplierName())){
			deliveryPackageDetail.setSupplierName(delivery.getSupplierName());
		}
	}
}
