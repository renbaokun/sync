package com.dkd.emms.systemManage.service;

import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.exception.BusinessException;
import com.dkd.emms.core.service.BaseService;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.dao.InWarehouseDao;
import com.dkd.emms.systemManage.dao.InWarehouseDetailDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/3/10.
 */
@Service
@Transactional
public class InWarehouseService extends BaseService<InWarehouse> {
    @Autowired
    private InWarehouseDao inWarehouseDao;
    @Autowired
    private InWarehouseDetailDao inWarehouseDetailDao;
    @Autowired
    private InWarehouseDetailService inWarehouseDetailService;
    @Autowired
    private StockService stockService;
    @Autowired
    private SequenceService sequenceService;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    @Override
    public BaseDao<InWarehouse> getDao() {
        return inWarehouseDao;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveInWarehouse(InWarehouse inWarehouse,User user){
        this.setDefault(inWarehouse,user);
        if( inWarehouse.getInWarehouseId()==null || inWarehouse.getInWarehouseId().equals("")){
            inWarehouse.setInWarehouseId(UUIDGenerator.getUUID());
            inWarehouse.setInWarehouseCode(sequenceService.getFlowNoByJudge("inWarehouse", "PMB"+"RK" + dateFormat.format(new Date()), 5));

            inWarehouseDao.insert(inWarehouse);
        }else{
            inWarehouseDao.update(inWarehouse);
        }
        inWarehouseDetailService.delete(inWarehouse.getInWarehouseId());
        List<InWarehouseDetail> list=new ArrayList<InWarehouseDetail>();
        for(InWarehouseDetail detail:inWarehouse.getInWarehouseDetailList()){
            detail.setInWarehouseId(inWarehouse.getInWarehouseId());
            detail.setAlreadyCount(detail.getInWarehouseCount());
            list.add(detail);
        }
        if(list.size()>0){
           inWarehouseDetailService.insertList(list);
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveInStock(InWarehouse inWarehouse,User user){
        this.saveInWarehouse(inWarehouse, user);
        String inWarehouseId=inWarehouse.getInWarehouseId();
        InWarehouseDetail inWarehouseDetail=new InWarehouseDetail();
        List<InWarehouseDetail> list = inWarehouseDetailDao.selectByInWarehouseId(inWarehouseId);
        for(InWarehouseDetail detail:list){
            vailidate(detail);
                Stock stock = new Stock();
                Materials materials = new Materials();
                materials.setMaterialsId(detail.getMaterialsId());
                materials.setMaterialsUnitMain(detail.getMaterialsUnitMain());
                stock.setMaterials(materials);
            stock.setStockNum(detail.getAlreadyCount());
                Project wbs = new Project();
                wbs.setProjectId(detail.getWbsId());
                stock.setWbs(wbs);
                Storagelocation storagelocation = new Storagelocation();
                storagelocation.setStoragelocationId(detail.getStoragelocationId());
                stock.setStoragelocation(storagelocation);
                Reservoirarea reservoirarea = new Reservoirarea();
                reservoirarea.setReservoirareaId(detail.getReservoirareaId());
                stock.setReservoirarea(reservoirarea);
                WareHouse wareHouse = new WareHouse();
                wareHouse.setWarehouseId(detail.getWarehouseId());
                stock.setWarehouse(wareHouse);
                stockService.saveStock(stock);

        }
}
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveStock(InWarehouse inWarehouse){
        this.update(inWarehouse);
        String inWarehouseId=inWarehouse.getInWarehouseId();
        InWarehouseDetail inWarehouseDetail=new InWarehouseDetail();
        List<InWarehouseDetail> list = inWarehouseDetailDao.selectByInWarehouseId(inWarehouseId);
        for(InWarehouseDetail detail:list){
            vailidate(detail);
            Stock stock = new Stock();
            Materials materials = new Materials();
            materials.setMaterialsId(detail.getMaterialsId());
            materials.setMaterialsUnitMain(detail.getMaterialsUnitMain());
            stock.setMaterials(materials);
            stock.setStockNum(detail.getAlreadyCount());
            Project wbs = new Project();
            wbs.setProjectId(detail.getWbsId());
            stock.setWbs(wbs);
            Storagelocation storagelocation = new Storagelocation();
            storagelocation.setStoragelocationId(detail.getStoragelocationId());
            stock.setStoragelocation(storagelocation);
            Reservoirarea reservoirarea = new Reservoirarea();
            reservoirarea.setReservoirareaId(detail.getReservoirareaId());
            stock.setReservoirarea(reservoirarea);
            WareHouse wareHouse = new WareHouse();
            wareHouse.setWarehouseId(detail.getWarehouseId());
            stock.setWarehouse(wareHouse);
            stockService.saveStock(stock);

        }
    }
    /**
     * 设置默认值
     * @param
     */

    private void setDefault(InWarehouse inWarehouse,User user){
        if(org.apache.commons.lang.StringUtils.isEmpty(inWarehouse.getInWarehouseId())) {

                inWarehouse.setCreateTime(new Date());

        }

        if(org.apache.commons.lang.StringUtils.isEmpty(inWarehouse.getCreateUserId())){
            inWarehouse.setCreateUserId(user.getUserId());
        }
        if(org.apache.commons.lang.StringUtils.isEmpty(inWarehouse.getCreateUserName())){
            if(null == user.getEmployee()){
                inWarehouse.setCreateUserName(user.getUserName());
            }else{
                inWarehouse.setCreateUserName(user.getEmployee().getEmpName());
            }
        }

    }
    public void vailidate(InWarehouseDetail detail){
        if(StringUtils.isEmpty(detail.getMaterialsId())){
            throw new BusinessException("数据异常，请检查物资相关信息是否齐全");
        }
        if (StringUtils.isEmpty(detail.getWbsId())){
            throw new BusinessException("数据异常,请检查WBS信息是否齐全");
        }
    }
}