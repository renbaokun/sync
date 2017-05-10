package com.dkd.emms.systemManage.service;

import com.dkd.emms.core.Constant;
import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.exception.BusinessException;
import com.dkd.emms.core.service.BaseService;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.systemManage.bo.Project;
import com.dkd.emms.systemManage.bo.ProjectEnum;
import com.dkd.emms.systemManage.bo.User;
import com.dkd.emms.systemManage.dao.ProjectDao;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 项目管理Service
 * @author WANGQ
 *
 */
@Service
public class ProjectService extends BaseService<Project> {

	@Autowired
	private ProjectDao projectDao;
	//储存或更新一个实体
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(Project project){
		edit(project, project.getProjectId());
	}
	public void selectProjectByParent(String id){
	     int count =  projectDao.selectProjectByParent(id);
	     if(count>1){
	    	 throw new BusinessException("该项目下的子项目没有全部完结");
	     }
	}
	/**
	 * 保存主项目信息
	 * @param project
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveProject(Project project,User user){
		this.setDefault(project,user);
		this.validate(project);
		if(StringUtils.isEmpty(project.getProjectId())){
			project.setProjectId(UUIDGenerator.getUUID());
			projectDao.insert(project);
		}else{
			projectDao.update(project);
		}
	}
	/**
	 * 赋默认值
	 * @param project
	 */
	private void setDefault(Project project,User currentUser){
		//层级默认值
		Project parentProject = this.selectByPk(project.getParentId());
		if(StringUtils.equals(project.getParentId(),"0")){
			project.setProjectCodeSeq(project.getProjectCode());
		}else{
			if(StringUtils.isEmpty(parentProject.getProjectCodeSeq())){
				project.setProjectCodeSeq(project.getProjectCode());
			}else{
				project.setProjectCodeSeq(parentProject.getProjectCodeSeq()+"."+project.getProjectCode());
			}
		}
		if(null != parentProject){
			project.setLevel((Integer.parseInt(parentProject.getLevel())+1)+"");
		}else{
			project.setLevel("0");
		}
		project.setIsLeaf("1");
		if(null == project.getCreateTime()){
			project.setCreateTime(new Date());
		}
		if(StringUtils.isEmpty(project.getCreateUserId())){
			project.setCreateUserId(currentUser.getUserId());
		}
		if(StringUtils.isEmpty(project.getCreateUserId())){
			project.setCreateUserName(currentUser.getEmployee().getEmpName());
		}
		if(StringUtils.isEmpty(project.getProjectState())){
			project.setProjectState(Constant.PROJECT_STATE_NEW);
		}
		if(StringUtils.isEmpty(project.getIsFinish())){
			project.setIsFinish("0");
		}
	}
	/**
	 * 后台校验
	 * @param project
	 */
	private void validate(Project project){
		if(StringUtils.isEmpty(project.getLevel())){
			throw new BusinessException("项目层级不能为空");
		}
		if(StringUtils.isEmpty(project.getProjectCode())){
			throw new BusinessException("项目编码不能为空");
		}
		if(StringUtils.isEmpty(project.getProjectName())){
			throw new BusinessException("项目名称不能为空");
		}
		if(StringUtils.isEmpty(project.getProjectState())){
			throw new BusinessException("项目状态不能为空");
		}
		if(StringUtils.isEmpty(project.getCreateUserId())){
			throw new BusinessException("录入人不能为空");
		}
		if(null == project.getCreateTime()){
			throw new BusinessException("录入时间不能为空");
		}
	}
	/**
	 * 校验Code是否重复
	 * @param projectCode
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public boolean  checkProjectCode(String projectCode){
		int count=projectDao.countByProjectCode(projectCode);
		return count>0?false:true;
	}
	/**
	 * 项目启用
	 * @param projectId
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void startProject(String projectId){
		Project project = this.selectByPk(projectId);
		project.setProjectState(ProjectEnum.wbspass.toString());
		projectDao.update(project);
	}

	/**
	 * 项目完结
	 * @param id
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void finishProject(String id) {
		Project project = this.selectByPk(id);
		this.selectProjectByParent(project.getProjectCodeSeq());
		project.setIsFinish("1");
		project.setProjectState(ProjectEnum.wbsfinish.toString());
		this.save(project);
	}

	@Override
	public BaseDao<Project> getDao() {
		// TODO Auto-generated method stub
		return projectDao;
	}

	/**
	 * 根据项目Id查询项目信息(需要携带供应商信息)
	 * @param projectId
	 * @return
	 */
	public Project selectProjectOrgById(String projectId){
		String ownerName = "";
		String supplierName = "";
		String construct = "";
		String manager = "";
		String supervision = "";
		String design = "";
		List<Project> projectList = projectDao.selectProjectOrgById(projectId);
		for(Project project:projectList){
			if(StringUtils.isNotEmpty(project.getOrgType()) && StringUtils.equals(project.getOrgType(),"owner")){
				if(StringUtils.equals(ownerName, "")){
					ownerName = project.getOrgName();
				}else{
					ownerName += ","+project.getOrgName();
				}
			}else if(StringUtils.isNotEmpty(project.getOrgType()) && StringUtils.equals(project.getOrgType(),"supplier")){
				if(StringUtils.equals(supplierName, "")){
					supplierName = project.getOrgName();
				}else{
					supplierName += ","+project.getOrgName();
				}
			}
			else if(StringUtils.isNotEmpty(project.getOrgType()) && StringUtils.equals(project.getOrgType(),"construct")){
				if(StringUtils.equals(construct, "")){
					construct = project.getOrgName();
				}else{
					construct += ","+project.getOrgName();
				}
			}else if(StringUtils.isNotEmpty(project.getOrgType()) && StringUtils.equals(project.getOrgType(),"manager")){
				if(StringUtils.equals(manager, "")){
					manager = project.getOrgName();
				}else{
					manager += ","+project.getOrgName();
				}
			}else if(StringUtils.isNotEmpty(project.getOrgType()) && StringUtils.equals(project.getOrgType(),"supervision")){
				if(StringUtils.equals(supervision, "")){
					supervision = project.getOrgName();
				}else{
					supervision += ","+project.getOrgName();
				}
			}else if(StringUtils.isNotEmpty(project.getOrgType()) && StringUtils.equals(project.getOrgType(),"design")){
				if(StringUtils.equals(design, "")){
					design = project.getOrgName();
				}else{
					design += ","+project.getOrgName();
				}
			}


		}
		projectList.get(0).setOwner(ownerName);
		projectList.get(0).setOrgName(supplierName);
		projectList.get(0).setConstruct(construct);
		projectList.get(0).setManager(manager);
		projectList.get(0).setSupervision(supervision);
		projectList.get(0).setDesign(design);
		return projectList.get(0);
	}

    public List<Project> selectProjectByParentId(String id){
		return projectDao.selectProjectByParentId(id);
	}
	//更具code查询项目
	public List<Project> selectProjectByCode(String code){
		return projectDao.selectProjectByCode(code);
	}
	//更具codeSeq查询项目
	public List<Project> selectProjectByCodeSeq(String code){
		return projectDao.selectProjectByCodeSeq(code);
	}

}