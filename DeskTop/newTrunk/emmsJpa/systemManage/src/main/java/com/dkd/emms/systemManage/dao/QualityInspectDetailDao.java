package com.dkd.emms.systemManage.dao;

import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.systemManage.bo.AgreementDetail;
import com.dkd.emms.systemManage.bo.QualityInspectDetail;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by YINXP on 2017/3/10.
 */
@Repository
public class QualityInspectDetailDao extends BaseDao <QualityInspectDetail>{
    /**
     * 批量插入
     */
    public void insertList(List<QualityInspectDetail> list){
        sqlSession.insert("QualityInspectDetail.insertList", list);
    }

    /**
     * 筛选质检单明细中合格的数据
     * */
    public List<QualityInspectDetail> selectQualityInspectDetail(Map<String, Object> map) {
        return sqlSession.selectList("QualityInspectDetail.selectInspectDetail",map);
    }

    /**
     * 提供质检单明细【合格字段】数据进行筛选
     * */
    public List<QualityInspectDetail> selectInspectDetailValidate(String DeliveryId) {
        return sqlSession.selectList("QualityInspectDetail.selectInspectDetailValidate", DeliveryId);
    }

    /**
     * 根据质检单id查询质检单明细信息
     * @param materiaInspectId
     * @return
     */
    public List<QualityInspectDetail> queryMaterialsData(String materiaInspectId){
        return sqlSession.selectList("QualityInspectDetail.selectByMaterialsId",materiaInspectId);
    }
}