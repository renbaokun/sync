package com.dkd.emms.systemManage.service;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.exception.BusinessException;
import com.dkd.emms.core.service.BaseService;
import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.dao.DesignCodeDao;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class DesignCodeService extends BaseService<Design> {

	@Autowired
	private DesignCodeDao designCodeDao;
	@Autowired
	private DesignCodeAttachmentService designCodeAttachmentService;
	@Autowired
	private MaterialsService materialsService;
	public BaseDao<Design> getDao() {
		return designCodeDao;
	}

	public List<Design> selectByMatching(String isMatching) {
		return designCodeDao.selectByMatching(isMatching);
	}

	//批量保存
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Design>


	saveBatchDesign(List<DrawingDetailed> list) {
		//设计院编码
		List<Design> designList = new ArrayList<Design>();
		//洛阳设计院编码
		List<Design> lyDesignList = new ArrayList<Design>();
		//重复洛阳设计院编码
		List<Design> clyDesignList = new ArrayList<Design>();
		//bu重复洛阳设计院编码
		List<Design> bclyDesignList = new ArrayList<Design>();
		//系统编码返回
		Map<String, String> returnlyList =new HashMap<String, String>();
		//设备与部件关系表
		List<DesignCodeAttachment> designCodeAttachmentList = new ArrayList<DesignCodeAttachment>();
		//洛阳设备与部件关系表
		List<DesignCodeAttachment> lyAttachmentList = new ArrayList<DesignCodeAttachment>();
		//设备集合
		Map<String,Object> map = new HashMap<String,Object>();
		//设备ID的位号
		Map<String,String> equipmentNoMap = new HashMap<String, String>();
		//部件与设备关系
		Map<String,String> componentRelationship = new HashMap<String, String>();
		for (DrawingDetailed dd : list) {
			//设计院编码
			Design design = this.setDefultDesign(dd);
			if(dd.getDesignOrgCode().equals("LYSJY")){
				//插入系统物资
				lyDesignList.add(design);
				if(this.checkDesign(dd).size()>0){
					design=this.checkDesign(dd).get(0);
					clyDesignList.add(design);//洛阳院重复
				}else{
					bclyDesignList.add(design);//洛阳院不重复
				}
			}else{
				if(this.checkDesign(dd).size()==0){
					designList.add(design);
				}else{
					design=this.checkDesign(dd).get(0);
				}
			}
			if(null != dd.getParentId()){//如果该条为 部件
				//设备与部件关系表
				DesignCodeAttachment designCodeAttachment = this.setDefultDesignCodeAttachment(design, dd);
				designCodeAttachmentList.add(designCodeAttachment);
				if(dd.getDesignOrgCode().equals("LYSJY")){
					lyAttachmentList.add(designCodeAttachment);
				}
			}
			if(design.getDesignType()!=null && design.getDesignType().equals("s")){
				map.put(dd.getDrawingDetailedId(),design.getDesignId());
				equipmentNoMap.put(design.getDesignId(), dd.getDrawingNumberDeviceNo());//记录设计院id与相应的位号
			}
		}
		//将生成的设备id存入关系表
		for(DesignCodeAttachment dca:designCodeAttachmentList){
			dca.setDesignEId(map.get(dca.getDesignEId()).toString());
			componentRelationship.put(dca.getDesignMId(), dca.getDesignEId());//记录部件的id与相应的设备id
		}
		//返回实体(存入系统id)
		if(lyDesignList.size()>0){
			returnlyList = materialsService.saveMaterialByDesign(lyDesignList, equipmentNoMap, componentRelationship);
			for(Design design:clyDesignList){//循环重复，直接修改update
				if(returnlyList.get(design.getDesignId())!=null){
					design.setIsMatching("1");
					design.setSystemcodeId( returnlyList.get(design.getDesignId()) );
					designCodeDao.update(design);
				}
			}
			for(Design design:bclyDesignList){//循环不重复，直接修改批量插入
				if(returnlyList.get(design.getDesignId())!=null){
					design.setIsMatching("1");
					design.setSystemcodeId(returnlyList.get(design.getDesignId()));
					designList.add(design);
				}
			}
		}
		if(designCodeAttachmentList.size()>0){
			designCodeAttachmentService.insetList(designCodeAttachmentList);
		}
		if(designList.size()>0){
			designCodeDao.insetList(designList);
		}
		return designList;
	}
	//校验设计院编码是否重复
	public List<Design> checkDesign(DrawingDetailed dd){
		Map<String,Object> designMap = new HashMap<String,Object>();
		designMap.put("additional1",dd.getExtra1());
		designMap.put("additional2",dd.getExtra2());
		designMap.put("additional3",dd.getExtra3());
		designMap.put("additional4",dd.getExtra4());
		designMap.put("designDescribe",dd.getDesignDescribe());
		designMap.put("designCode",dd.getDesignCode());
		designMap.put("designOrgId",dd.getDesignOrgId());
		List<Design>list = designCodeDao.checkDesign(designMap);
		return list;
	}
	//设备部件关系表补全字段
	public DesignCodeAttachment setDefultDesignCodeAttachment(Design d, DrawingDetailed dd){
		DesignCodeAttachment designCodeAttachment =new DesignCodeAttachment();
		designCodeAttachment.setAttachmentNo(dd.getDrawingDetailedNo());
		designCodeAttachment.setWbsId(dd.getProjectId());
		designCodeAttachment.setEquipmentNo(dd.getDrawingNumberDeviceNo());
		designCodeAttachment.setAttachmentNumber(dd.getDesignCount());
		designCodeAttachment.setDesignMId(d.getDesignId());
		designCodeAttachment.setDesignEId(dd.getParentId());
		return designCodeAttachment;
	}
	//设计院补全字段
	public Design setDefultDesign(DrawingDetailed dd){
		Design design = new Design();
		design.setDesignId(UUIDGenerator.getUUID());
		design.setIsdel("1");
		design.setIsMatching("0");
		design.setAdditional1(dd.getExtra1());
		design.setAdditional2(dd.getExtra2());
		design.setAdditional3(dd.getExtra3());
		design.setAdditional4(dd.getExtra4());
		design.setDesignType(dd.getDrawingDetailedType());
		design.setDesignDescribe(dd.getDesignDescribe());
		design.setDesignCode(dd.getDesignCode());
		design.setDesignOrgId(dd.getDesignOrgId());
		design.setDesignUnitMain(dd.getDesignUnit());
		design.setWbsCode(dd.getProjectCodeSeq()==null ? null:dd.getProjectCodeSeq().split("\\.")[dd.getProjectCodeSeq().split("\\.").length-1]);
		design.setDesignCount(dd.getDesignCount());
		design.setDeviceNo(dd.getDrawingNumberDeviceNo());
		return design;
	}
}