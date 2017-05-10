package com.dkd.emms.systemManage.bo;


import com.dkd.emms.core.entity.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Created by YINXP on 2017/3/8.
 * 质检明細
 */
public class QualityInspectDetail extends BaseEntity {
    private String qualityInspectId;//质检明细单id
    private String materiaInspectId;//质检单id
    private String materialsId;//物料id
    private String wbsId;
    private String receiptCode;//收货单编号
    private String inspectNo;//质检单编号
    private String materialsCode;//物资编码
    private String packingNo;//包裝編號
    private String materialsDescribe;//物资描述
    private String additional1;//附加1
    private String additional2;//附加2
    private String additional3;//附加3
    private String additional4;//附加4
    private String wbsCode;
    private String materialsUnitMain;//采购计量单位


    private BigDecimal dianshouCount;//点收数量
    private BigDecimal purchaseCount;//采购数量
    private BigDecimal qualifiedQty;//合格数量
    private BigDecimal unQualifiedQty;//不合格数量
    private String appearanceInspect;//外观检查(0 不合格 1 合格)
    private String recheckInspect;//需要复检（0 是 1 否）
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date productDate;//生产日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date qualityDate;//保质期
    private BigDecimal deliveryQty;//已发货数量
    private BigDecimal currentDeliveryQty;//本次发货数量
    private String receiptOrgId;//施工单位
    private String storageId;//储位ID
    private String storageCode;//储位
    private String deliveryId;//收货单ID
    private String packingDetailId;//收货单明细ID
    public String getMaterialsId() {
        return materialsId;
    }

    public void setMaterialsId(String materialsId) {
        this.materialsId = materialsId;
    }

    public String getWbsId() {
        return wbsId;
    }

    public void setWbsId(String wbsId) {
        this.wbsId = wbsId;
    }

    public String getMateriaInspectId() {
        return materiaInspectId;
    }

    public void setMateriaInspectId(String materiaInspectId) {
        this.materiaInspectId = materiaInspectId;
    }

    public String getPackingNo() {
        return packingNo;
    }

    public void setPackingNo(String packingNo) {
        this.packingNo = packingNo;
    }

    public String getQualityInspectId() {
        return qualityInspectId;
    }

    public void setQualityInspectId(String qualityInspectId) {
        this.qualityInspectId = qualityInspectId;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getMaterialsCode() {
        return materialsCode;
    }

    public void setMaterialsCode(String materialsCode) {
        this.materialsCode = materialsCode;
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

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public String getMaterialsUnitMain() {
        return materialsUnitMain;
    }

    public void setMaterialsUnitMain(String materialsUnitMain) {
        this.materialsUnitMain = materialsUnitMain;
    }

    public String getAppearanceInspect() {
        return appearanceInspect;
    }

    public void setAppearanceInspect(String appearanceInspect) {
        this.appearanceInspect = appearanceInspect;
    }

    public String getRecheckInspect() {
        return recheckInspect;
    }

    public void setRecheckInspect(String recheckInspect) {
        this.recheckInspect = recheckInspect;
    }

    public Date getProductDate() {
        return productDate;
    }

    public void setProductDate(Date productDate) {
        this.productDate = productDate;
    }

    public Date getQualityDate() {
        return qualityDate;
    }

    public void setQualityDate(Date qualityDate) {
        this.qualityDate = qualityDate;
    }

    public String getInspectNo() {
        return inspectNo;
    }

    public void setInspectNo(String inspectNo) {
        this.inspectNo = inspectNo;
    }

    public BigDecimal getDianshouCount() {
        return dianshouCount;
    }

    public void setDianshouCount(BigDecimal dianshouCount) {
        this.dianshouCount = dianshouCount;
    }

    public BigDecimal getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(BigDecimal purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public BigDecimal getQualifiedQty() {
        return qualifiedQty;
    }

    public void setQualifiedQty(BigDecimal qualifiedQty) {
        this.qualifiedQty = qualifiedQty;
    }

    public BigDecimal getUnQualifiedQty() {
        return unQualifiedQty;
    }

    public void setUnQualifiedQty(BigDecimal unQualifiedQty) {
        this.unQualifiedQty = unQualifiedQty;
    }

    public BigDecimal getDeliveryQty() {
        return deliveryQty;
    }

    public void setDeliveryQty(BigDecimal deliveryQty) {
        this.deliveryQty = deliveryQty;
    }

    public BigDecimal getCurrentDeliveryQty() {
        return currentDeliveryQty;
    }

    public void setCurrentDeliveryQty(BigDecimal currentDeliveryQty) {
        this.currentDeliveryQty = currentDeliveryQty;
    }

    public String getReceiptOrgId() {
        return receiptOrgId;
    }

    public void setReceiptOrgId(String receiptOrgId) {
        this.receiptOrgId = receiptOrgId;
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

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getPackingDetailId() {
        return packingDetailId;
    }

    public void setPackingDetailId(String packingDetailId) {
        this.packingDetailId = packingDetailId;
    }
}