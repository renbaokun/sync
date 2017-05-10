package com.dkd.emms.systemManage.bo;

import com.dkd.emms.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2017/3/10.
 */
public class InWarehouseDetail  extends BaseEntity {
    /**
     *
     * 入库明细ID（系统生成）
     *
     *
     */
    private String inDetailId;
    /**
     *
     * 入库明细编号
     *
     *
     */
    private String inDetailCode;
    /**
     *
     * 供应商ID
     *
     *
     */
    private String supplierId;
    /**
     *
     * 供应商名称
     *
     *
     */
    private String supplierName;
    /**
     *
     * 入库明细状态
     *
     *
     */
    private String inDetailState;
    /**
     *
     * 入库单ID（系统生成）
     *
     *
     */
    private String inWarehouseId;
    /**
     *
     * 入库单编号
     *
     *
     */
    private String inWarehouseCode;
    /**
     *
     *创建人ID
     *
     *
     */
    private String createUserId;
    /**
     *
     *创建人姓名
     *
     *
     */
    private String createUserName;

    /**
     *
     *入库人员
     *
     *
     *
     */
    private String inWorker;
    /**
     *
     *储位ID
     *
     *
     *
     */
    private String storageId;
    /**
     *
     *储位编码
     *
     *
     *
     */
    private String storageCode;

    /**
     *
     * 物料编码ID（明细单据带入）
     *
     *
     *
     */
    private String materialsId;
    /**
     *
     * 物料编码（明细单据带入）
     *
     *
     *
     */
    private String materialsCode;
    /**
     *
     * WBS编码ID（明细单据带入）
     *
     *
     *
     */
    private String wbsId;
    /**
     *
     * WBS编码（明细单据带入）
     *
     *
     *
     */
    private String wbsCode;
    /**
     *
     * 物料描述（明细单据带入）
     *
     *
     *
     */
    private String materialsDescribe;
    /**
     *
     * 附加1
     *
     *
     *
     */
    private String additional1;
    /**
     *
     * 附加2
     *
     *
     *
     */
    private String additional2;
    /**
     *
     * 附加3
     *
     *
     *
     */
    private String additional3;
    /**
     *
     * 附加4
     *
     *
     *
     */
    private String additional4;
    /**
     *
     * 采购计量单位
     *
     *
     *
     */
    private String materialsUnitMain;
    /**
     *
     * 生产日期（年月日）
     *
     *
     *
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date productionDate;
    /**
     *
     * 保质期
     *
     *
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date bzq;
    /**
     *
     * 采购数量
     *
     *
     */
   private BigDecimal purchaseCount;
    /**
     *
     * 已发货数量
     *
     *
     *
     */
    private BigDecimal deliveryCount;
    /**
     *
     * 这次发货数量
     *
     *
     *
     */
    private BigDecimal thisDeliveryCount;
    /**
     *
     * 点收数量
     *
     *
     *
     */
    private BigDecimal dianshouCount;
    /**
     *
     * 合格数量
     *
     *
     *
     */
    private BigDecimal qualifiedCount;
    /**
     *
     * 不合格数量
     *
     *
     *
     */
    private BigDecimal unqualifiedCount;
    /**
     *
     * 外观检查
     *
     *
     *
     */
    private String visualInspection;
    /**
     *
     * 需要复检
     *
     *
     *
     */
    private String review;
    /**
     *
     * 本次入库数量
     *
     *
     *
     */
    private BigDecimal inWarehouseCount;
    /**
     *
     * 已入库数量
     *
     *
     *
     */
    private BigDecimal alreadyCount;
    /**
     *
     * 质检单ID
     *
     *
     */
    private String zhijianId;
    /**
     *
     * 质检单编号
     *
     *
     */
    private String zhijianCode;
    /**
     *
     * 采购订单ID
     *
     *
     */
    private String orderId;
    /**
     *
     * 采购订单编号
     *
     *
     */
    private String orderCode;
    /**
     *
     * 储位ID
     *
     *
     */
    private String storagelocationId;
    /**
     *
     * 储位编码
     *
     *
     */
    private String storagelocationCode;
    /**
     *
     * 仓库ID
     *
     *
     */
    private String warehouseId;
    /**
     *
     * 库区ID
     *
     *
     */
    private String reservoirareaId;



    public String getInDetailId() {
        return inDetailId;
    }

    public void setInDetailId(String inDetailId) {
        this.inDetailId = inDetailId;
    }

    public String getInDetailCode() {
        return inDetailCode;
    }

    public void setInDetailCode(String inDetailCode) {
        this.inDetailCode = inDetailCode;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getInDetailState() {
        return inDetailState;
    }

    public void setInDetailState(String inDetailState) {
        this.inDetailState = inDetailState;
    }

    public String getInWarehouseId() {
        return inWarehouseId;
    }

    public void setInWarehouseId(String inWarehouseId) {
        this.inWarehouseId = inWarehouseId;
    }

    public String getInWarehouseCode() {
        return inWarehouseCode;
    }

    public void setInWarehouseCode(String inWarehouseCode) {
        this.inWarehouseCode = inWarehouseCode;
    }

    public String getCreateUserId() {
        return createUserId;
    }


    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getInWorker() {
        return inWorker;
    }

    public void setInWorker(String inWorker) {
        this.inWorker = inWorker;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStorageCode() {
        return storageCode;
    }

    public void setStorageCode(String storageCode) {
        this.storageCode = storageCode;
    }

    public String getMaterialsId() {
        return materialsId;
    }

    public void setMaterialsId(String materialsId) {
        this.materialsId = materialsId;
    }

    public String getMaterialsCode() {
        return materialsCode;
    }

    public void setMaterialsCode(String materialsCode) {
        this.materialsCode = materialsCode;
    }

    public String getWbsId() {
        return wbsId;
    }

    public void setWbsId(String wbsId) {
        this.wbsId = wbsId;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public String getMaterialsDescribe() {
        return materialsDescribe;
    }

    public void setMaterialsDescribe(String materialsDescribe) {
        this.materialsDescribe = materialsDescribe;
    }

    public String getAdditional1() {
        return additional1;
    }

    public void setAdditional1(String additional1) {
        this.additional1 = additional1;
    }

    public String getAdditional2() {
        return additional2;
    }

    public void setAdditional2(String additional2) {
        this.additional2 = additional2;
    }

    public String getAdditional3() {
        return additional3;
    }

    public void setAdditional3(String additional3) {
        this.additional3 = additional3;
    }

    public String getAdditional4() {
        return additional4;
    }

    public void setAdditional4(String additional4) {
        this.additional4 = additional4;
    }

    public String getMaterialsUnitMain() {
        return materialsUnitMain;
    }

    public void setMaterialsUnitMain(String materialsUnitMain) {
        this.materialsUnitMain = materialsUnitMain;
    }
    @JsonFormat(pattern="yyyy-MM-dd")
    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }
    @JsonFormat(pattern="yyyy-MM-dd")
    public Date getBzq() {
        return bzq;
    }

    public void setBzq(Date bzq) {
        this.bzq = bzq;
    }

    public BigDecimal getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(BigDecimal deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    public BigDecimal getThisDeliveryCount() {
        return thisDeliveryCount;
    }

    public void setThisDeliveryCount(BigDecimal thisDeliveryCount) {
        this.thisDeliveryCount = thisDeliveryCount;
    }


    public BigDecimal getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(BigDecimal qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public BigDecimal getUnqualifiedCount() {
        return unqualifiedCount;
    }

    public void setUnqualifiedCount(BigDecimal unqualifiedCount) {
        this.unqualifiedCount = unqualifiedCount;
    }

    public String getVisualInspection() {
        return visualInspection;
    }

    public void setVisualInspection(String visualInspection) {
        this.visualInspection = visualInspection;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }


    public BigDecimal getInWarehouseCount() {
        return inWarehouseCount;
    }

    public void setInWarehouseCount(BigDecimal inWarehouseCount) {
        this.inWarehouseCount = inWarehouseCount;
    }

    public String getZhijianId() {
        return zhijianId;
    }

    public void setZhijianId(String zhijianId) {
        this.zhijianId = zhijianId;
    }

    public String getZhijianCode() {
        return zhijianCode;
    }

    public void setZhijianCode(String zhijianCode) {
        this.zhijianCode = zhijianCode;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }


    public String getCreateUserName() {
        return createUserName;
    }


    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(BigDecimal purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public BigDecimal getDianshouCount() {
        return dianshouCount;
    }

    public void setDianshouCount(BigDecimal dianshouCount) {
        this.dianshouCount = dianshouCount;
    }

    public String getStoragelocationId() {
        return storagelocationId;
    }

    public void setStoragelocationId(String storagelocationId) {
        this.storagelocationId = storagelocationId;
    }

    public String getStoragelocationCode() {
        return storagelocationCode;
    }

    public void setStoragelocationCode(String storagelocationCode) {
        this.storagelocationCode = storagelocationCode;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getReservoirareaId() {
        return reservoirareaId;
    }

    public void setReservoirareaId(String reservoirareaId) {
        this.reservoirareaId = reservoirareaId;
    }

    public BigDecimal getAlreadyCount() {
        return alreadyCount;
    }

    public void setAlreadyCount(BigDecimal alreadyCount) {
        this.alreadyCount = alreadyCount;
    }
}
