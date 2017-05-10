package com.dkd.emms.systemManage.bo;

import com.dkd.emms.core.entity.Tree;

import java.util.Date;


/**
 * 项目表信息(WBS)
 * @author WANGQ
 *
 */
public class Project extends Tree {
	/**
	 * 项目id
	 */
	private String projectId;
	/**
	 * 项目Code
	 */
	private String projectCode;
	
	/**
	 * 项目名称
	 */
	private String projectName;

	/**
	 * 项目类型
	 */
	private String projectType;
	/**
	 * 项目状态（新建、进行中、完结）
	 */
	private String projectState;
	
	/**
	 * 是否是主项目(0否,1是)
	 * 默认值为0
	 */
	private String isMain;
	
	/**
	 * 是否完结(0否,1是)
	 * 默认值为0
	 */
	private String isFinish;
	/**
	 * 提交人姓名
	 */
	private String submitUserName;
	
	/**
	 * 提交人Id
	 */
	private String submitUserId;
	
	/**
	 * 提交时间
	 */
	private Date submitTime ;
	
	/**
	 * 项目id seq
	 */
	private String projectIdSeq;

	/**
	 * 项目code seq
	 */
	private String projectCodeSeq;
	/**
	 * 组织机构类型
	 */
	private String orgType;
	/**
	 * 供应商Name
	 */
	private String orgName;
	/**
	 * 施工单位
	 */
	private String construct;
	/**
	 * 监理公司
	 */
	private String manager;
	/**
	 * 建设单位
	 */
	private String supervision;
	/**
	 * 设计院
	 */
	private String design;
	/**
	 * 业主
	 */
	private String owner;
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectState() {
		return projectState;
	}

	public void setProjectState(String projectState) {
		this.projectState = projectState;
	}

	public String getIsMain() {
		return isMain;
	}

	public void setIsMain(String isMain) {
		this.isMain = isMain;
	}

	public String getIsFinish() {
		return isFinish;
	}

	public void setIsFinish(String isFinish) {
		this.isFinish = isFinish;
	}

	public String getSubmitUserName() {
		return submitUserName;
	}

	public void setSubmitUserName(String submitUserName) {
		this.submitUserName = submitUserName;
	}

	public String getSubmitUserId() {
		return submitUserId;
	}

	public void setSubmitUserId(String submitUserId) {
		this.submitUserId = submitUserId;
	}

	public Date getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(Date submitTime) {
		this.submitTime = submitTime;
	}

	public String getProjectIdSeq() {
		return projectIdSeq;
	}

	public void setProjectIdSeq(String projectIdSeq) {
		this.projectIdSeq = projectIdSeq;
	}

	public String getProjectCodeSeq() {
		return projectCodeSeq;
	}

	public void setProjectCodeSeq(String projectCodeSeq) {
		this.projectCodeSeq = projectCodeSeq;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOrgType() {
		return orgType;
	}

	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}

	public String getConstruct() {
		return construct;
	}

	public void setConstruct(String construct) {
		this.construct = construct;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getSupervision() {
		return supervision;
	}

	public void setSupervision(String supervision) {
		this.supervision = supervision;
	}

	public String getDesign() {
		return design;
	}

	public void setDesign(String design) {
		this.design = design;
	}
}