package com.dkd.emms.systemManage.bo;

import com.dkd.emms.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by YUZH on 2017/3/20.
 */
public class DemandDetail extends BaseEntity {
    private String demandDetailId;
    private String demandId;
    private String demandCode;
    private String wbsCode;
    private String wbsId;
    private String drawingNumberCode;
    private Integer drawingNumberVersion;
    private String designOrgCode;
    private String materialsId;
    private String designCode;
    private String designDescribe;
    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String drawingNumberDeviceNo;
    private String designUnit;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date demandDate;
    private BigDecimal demandCount;
    private BigDecimal balanceCount;
    private BigDecimal loseCount;
    private BigDecimal changeCount;
    private String balanceReason;
    private BigDecimal designCount;//料表设计数量
    private BigDecimal usedCount;//已领用数量
    /**
     * 库存数量
     */
    private BigDecimal stockNum;
    /**
     * 库存Id
     */
    private String stockId;
    private String drawingDetailedId;

    public String getDemandDetailId() {
        return demandDetailId;
    }

    public void setDemandDetailId(String demandDetailId) {
        this.demandDetailId = demandDetailId;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public String getWbsId() {
        return wbsId;
    }

    public void setWbsId(String wbsId) {
        this.wbsId = wbsId;
    }

    public String getDrawingNumberCode() {
        return drawingNumberCode;
    }

    public void setDrawingNumberCode(String drawingNumberCode) {
        this.drawingNumberCode = drawingNumberCode;
    }

    public Integer getDrawingNumberVersion() {
        return drawingNumberVersion;
    }

    public void setDrawingNumberVersion(Integer drawingNumberVersion) {
        this.drawingNumberVersion = drawingNumberVersion;
    }

    public String getDesignOrgCode() {
        return designOrgCode;
    }

    public void setDesignOrgCode(String designOrgCode) {
        this.designOrgCode = designOrgCode;
    }

    public String getDesignCode() {
        return designCode;
    }

    public void setDesignCode(String designCode) {
        this.designCode = designCode;
    }

    public String getDesignDescribe() {
        return designDescribe;
    }

    public void setDesignDescribe(String designDescribe) {
        this.designDescribe = designDescribe;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getExtra3() {
        return extra3;
    }

    public void setExtra3(String extra3) {
        this.extra3 = extra3;
    }

    public String getExtra4() {
        return extra4;
    }

    public void setExtra4(String extra4) {
        this.extra4 = extra4;
    }

    public String getDrawingNumberDeviceNo() {
        return drawingNumberDeviceNo;
    }

    public void setDrawingNumberDeviceNo(String drawingNumberDeviceNo) {
        this.drawingNumberDeviceNo = drawingNumberDeviceNo;
    }

    public String getDesignUnit() {
        return designUnit;
    }

    public void setDesignUnit(String designUnit) {
        this.designUnit = designUnit;
    }
    @JsonFormat(pattern="yyyy-MM-dd" ,timezone = "GMT+8")
    public Date getDemandDate() {
        return demandDate;
    }

    public void setDemandDate(Date demandDate) {
        this.demandDate = demandDate;
    }

    public BigDecimal getDemandCount() {
        return demandCount;
    }

    public void setDemandCount(BigDecimal demandCount) {
        this.demandCount = demandCount;
    }

    public BigDecimal getBalanceCount() {
        return balanceCount;
    }

    public void setBalanceCount(BigDecimal balanceCount) {
        this.balanceCount = balanceCount;
    }

    public BigDecimal getLoseCount() {
        return loseCount;
    }

    public void setLoseCount(BigDecimal loseCount) {
        this.loseCount = loseCount;
    }

    public BigDecimal getChangeCount() {
        return changeCount;
    }

    public void setChangeCount(BigDecimal changeCount) {
        this.changeCount = changeCount;
    }

    public String getBalanceReason() {
        return balanceReason;
    }

    public void setBalanceReason(String balanceReason) {
        this.balanceReason = balanceReason;
    }

    public String getMaterialsId() {
        return materialsId;
    }

    public void setMaterialsId(String materialsId) {
        this.materialsId = materialsId;
    }

    public BigDecimal getDesignCount() {
        return designCount;
    }

    public void setDesignCount(BigDecimal designCount) {
        this.designCount = designCount;
    }

    public BigDecimal getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(BigDecimal usedCount) {
        this.usedCount = usedCount;
    }

    public String getDemandCode() {
        return demandCode;
    }

    public void setDemandCode(String demandCode) {
        this.demandCode = demandCode;
    }

    public BigDecimal getStockNum() {
        return stockNum;
    }

    public void setStockNum(BigDecimal stockNum) {
        this.stockNum = stockNum;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getDrawingDetailedId() {
        return drawingDetailedId;
    }

    public void setDrawingDetailedId(String drawingDetailedId) {
        this.drawingDetailedId = drawingDetailedId;
    }
}