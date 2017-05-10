package com.dkd.emms.systemManage.dao;

import java.util.List;
import java.util.Map;

import com.dkd.emms.systemManage.bo.DrawingDetailed;
import org.springframework.stereotype.Repository;

import com.dkd.emms.core.dao.BaseDao;



@Repository
public class DrawingDetailedDao extends BaseDao<DrawingDetailed>{

	//物资 查找重复
	public List<String> findRepeatW(List<String> codeNo) {
		return sqlSession.selectList("DrawingDetailed.findRepeatW", codeNo);
	}

	//设备 查找重复
	public List<String> findRepeatS(List<String> codeNo) {
		return sqlSession.selectList("DrawingDetailed.findRepeatS", codeNo);
	}

	public void insertList(List<DrawingDetailed> allDetailList) {
		sqlSession.insert("DrawingDetailed.insertList", allDetailList);
	}

	public List<DrawingDetailed> selectByErrorType(Map<String, Object> map) {
		return sqlSession.selectList("DrawingDetailed.selectByErrorType", map);
	}
	/**
	 * 判断设计院材料是否全部匹配
	 * @return
	 */
	public List<DrawingDetailed> selectNotConform(Map map) {
		return sqlSession.selectList("DrawingDetailed.selectNotConform", map);
	}

	//返回匹配项Id列表
	public List<DrawingDetailed> findMatching(List<DrawingDetailed> conditionList) {
		return sqlSession.selectList("DrawingDetailed.findMatching", conditionList);
	}

	//返回未匹配项Id列表
	public List<DrawingDetailed> findMismatch(List<DrawingDetailed> conditionList) {
		return sqlSession.selectList("DrawingDetailed.findMismatch", conditionList);
	}

	//为已匹配的项目回填
	public void makeInformationComplete(DrawingDetailed dd) {
		sqlSession.update("DrawingDetailed.makeInformationComplete", dd);
	}

	//根据料表id删除图号明细
	public void deleteByMTid(String materialsTableId) {
		sqlSession.update("DrawingDetailed.deleteByMTid", materialsTableId);
	}

	//根据设备id删除部件
	public void deletePartsBySid(String drawingDetailedId) {
		sqlSession.update("DrawingDetailed.deletePartsBySid", drawingDetailedId);
	}

	/**
	 * 校验Code是否重复
	 * @return
	 */
	public int checkStoNo(Map<String,Object> paramMap){
		return sqlSession.selectOne("WareHouse.checkStoNo", paramMap);
	}

	public List<DrawingDetailed> selectByConditionNoPage(Map<String, Object> map) {
		return sqlSession.selectList("DrawingDetailed.selectByConditionNoPage", map);
	}

	public int countByConditionPage(Map<String, Object> map) {
		return sqlSession.selectOne("DrawingDetailed.countByConditionPage", map);
	}
	/**
	 * 根据设备查询部件
	 * @return
	 */
	public List<DrawingDetailed> selectByEquipmentId(String drawingDetailedId) {
		return sqlSession.selectList("DrawingDetailed.selectByEquipmentId", drawingDetailedId);
	}
}
