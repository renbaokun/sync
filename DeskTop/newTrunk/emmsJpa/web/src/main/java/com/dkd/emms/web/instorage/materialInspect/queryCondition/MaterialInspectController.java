package com.dkd.emms.web.instorage.materialInspect.queryCondition;

import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.service.InspectPicFileService;
import com.dkd.emms.systemManage.service.MaterialInspectService;
import com.dkd.emms.systemManage.service.QualityInspectDetailService;
import com.dkd.emms.web.util.page.PageBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

;import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by YINXP on 2017/3/15.
 */

@Controller
@RequestMapping(value="/instorage/materialManag.do")
@SessionAttributes("currentUser")
public class MaterialInspectController {
    @Autowired
    private MaterialInspectService materialInspectService;

    @Autowired
    private QualityInspectDetailService qualityInspectDetailService;

    @Autowired
    private InspectPicFileService inspectPicFileService;
    @RequestMapping( params = {"cmd=query"},method = RequestMethod.GET)
    public String query(){
        return "instorage/materialManag/materialInspection";
    }

    @RequestMapping( params = {"cmd=queryDirect"},method = RequestMethod.GET)
    public String queryDirect(){
        return "instorage/directInspect/materialInspection";
    }
    /**
     * 加载物资质检数据
     * @return
     */
    @RequestMapping( params ={"cmd=loadMaterialInspectListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<MaterialInspect> loadMaterialListData(@RequestParam(value = "page") Integer start,@RequestParam(value = "rows") Integer length,
                                                           MaterialInspectCondition  materialInspect){
        PageBean<MaterialInspect> pageBean = new PageBean<>();
        pageBean.setTotal(materialInspectService.countByCondition(materialInspect));
        pageBean.setRows(materialInspectService.selectByCondition(materialInspect,pageBean.getTotal(),start,length));
        return pageBean;
    }

    /**
     * 加载直达质检数据 (直达质检)
     * @return
     */
    @RequestMapping( params ={"cmd=loadDirectInspectListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<MaterialInspect> loadMaterialListData_Direct(@RequestParam(value = "page") Integer start,@RequestParam(value = "rows") Integer length,MaterialInspectCondition  materialInspect){
        PageBean<MaterialInspect> pageBean = new PageBean<>();
        pageBean.setTotal(materialInspectService.countByCondition(materialInspect));
        pageBean.setRows(materialInspectService.selectByCondition(materialInspect,pageBean.getTotal(),start,length));
        return pageBean;
    }

    /**
     * 新建质检单
     *
     * @return
     */
    @RequestMapping(params = {"cmd=modal"}, method = RequestMethod.GET)
    public String modal() {
        return "instorage/materialManag/edit";
    }
    /**
     * 新建质检单(直达质检)
     * @return
     */
    @RequestMapping(params = {"cmd=modalDirect"}, method = RequestMethod.GET)
    public String modalDirect() {
        return "instorage/directInspect/edit";
    }

    /**
     * 加载物资质检明细数据(没有地方调用的方法)
     * @return
     */
    @RequestMapping( params ={"cmd=loadQualityInspectListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<QualityInspectDetail> loadQualityInspectListData(@RequestParam(value = "page") Integer start,@RequestParam(value = "rows") Integer length,
                                                                     QualityInspectDetailCondition qualityInspectDetail){
        PageBean<QualityInspectDetail> pageBean = new PageBean<>();
        pageBean.setTotal(qualityInspectDetailService.countByCondition(qualityInspectDetail));
        pageBean.setRows(qualityInspectDetailService.selectByCondition(qualityInspectDetail,pageBean.getTotal(),start,length));
        return pageBean;
    }

    /**
     * 物资明细弹出框请求
     *
     * @return
     */
    @RequestMapping(params = {"cmd=MaterialDetailDialog"}, method = RequestMethod.GET)
    public String addDetail(String supplierId, ModelMap model) {
       model.addAttribute("supplierId",supplierId);
        return "instorage/materialManag/materialDetail";
    }

    /**
     * 加载物资明细弹出框数据
     * @return
     */
    @RequestMapping( params ={"cmd=loadMaterialDetailListData"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public PageBean<QualityInspectDetail> loadAddDetailListData(@RequestParam(value = "page",required = false) Integer start,@RequestParam(value = "rows",required = false) Integer length,
                                                           QualityInspectDetailCondition qualityInspectDetail,String materiaInspectNo){
        if(materiaInspectNo != null &&  !"".equals(materiaInspectNo)){
            qualityInspectDetail.setInspectNo(materiaInspectNo);
        }
        PageBean<QualityInspectDetail> pageBean = new PageBean<>();
        pageBean.setTotal(qualityInspectDetailService.countByCondition(qualityInspectDetail));
        pageBean.setRows(qualityInspectDetailService.selectQualityInspectDetail(qualityInspectDetail, pageBean.getTotal(), start, length));
        return pageBean;
    }

    /**
     * 保存质检单数据(系统自动生成创建人等数据)
     * @return
     */

    @RequestMapping( params = {"cmd=saveInspect"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String save(@ModelAttribute("currentUser")User user,@RequestBody MaterialInspect materialInspect) throws IOException {
        if(StringUtils.isEmpty(materialInspect.getMateriaInspectId())){
            materialInspect.setCreateTime(new Date());
            materialInspect.setCreateUserId(user.getUserId());
            if(null == user.getEmployee()){
                materialInspect.setCreateUserName(user.getUserName());
            }else{
                materialInspect.setCreateUserName(user.getEmployee().getEmpName());
            }
        }
      //  List<QualityInspectDetail> list = materialInspect.getQualityInspectDetailList();

      //  materialInspect.setInspectStaus("billStaus_a");
        materialInspectService.saveMaterialInspect(materialInspect,user);
//        String InspectStuate = materialInspect.getInspectStaus();
//        if (InspectStuate.equals("billStaus_b")){
//            CreateGoDownEntity(materialInspect.getMateriaInspectId(),user);//生成出、入库单
//        }
        return "保存成功";
    }

    /**
     * 保存质检单数据(系统自动生成创建人等数据)(直达质检)
     * @return
     */

    @RequestMapping( params = {"cmd=saveDirectInspect"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String saveDirect(@ModelAttribute("currentUser")User user,@RequestBody MaterialInspect materialInspect) throws IOException {
        if(StringUtils.isEmpty(materialInspect.getMateriaInspectId())){
            materialInspect.setCreateTime(new Date());
            materialInspect.setCreateUserId(user.getUserId());
            if(null == user.getEmployee()){
                materialInspect.setCreateUserName(user.getUserName());
            }else{
                materialInspect.setCreateUserName(user.getEmployee().getEmpName());
            }
        }
//        materialInspect.setInspectStaus("billStaus_a");
        materialInspectService.saveMaterialInspect(materialInspect,user);
        String InspectStuate = materialInspect.getInspectStaus();
        if (InspectStuate.equals("billStaus_b")){
            CreateGoDownEntity(materialInspect.getMateriaInspectId(),user);//生成出、入库单
        }
        return "保存成功";
    }


    @RequestMapping(params = {"cmd=upload"}, method = RequestMethod.GET)
    public String upload() {
        return "instorage/materialManag/file";
    }

    /**
     * 解析圖片文件，并保存到表（直达质检和物资质检通用）
     * @return
     */
    @RequestMapping( params = {"cmd=savePic"},produces = "text/html ;charset=UTF-8",consumes="multipart/form-data",method = RequestMethod.POST)
    @ResponseBody
    public String savePic(@RequestParam MultipartFile picList){
        String m_materiaInspectId = MaterialInpectPicSingleton.getMaterialInpectPicSingleton().getMaterialInspectId();
        inspectPicFileService.saveOriginalFile(picList,m_materiaInspectId);
        return "保存成功";
    }

    /**
     * 删除
     */
    @RequestMapping( params = {"cmd=delete"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String deleteMaterialInspect(String materiaInspectId) throws IOException {
        materialInspectService.delete(materiaInspectId);
        qualityInspectDetailService.delete(materiaInspectId);
        inspectPicFileService.delete(materiaInspectId);
        return "删除完成";
    }

    /**
     * 删除(直达质检)
     */
    @RequestMapping( params = {"cmd=deleteDirect"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String deleteDirectInspect(String materiaInspectId) throws IOException {
        materialInspectService.delete(materiaInspectId);
        qualityInspectDetailService.delete(materiaInspectId);
        inspectPicFileService.delete(materiaInspectId);
        return "删除完成";
    }

    /**
     * 查看
     */
    @RequestMapping( params = {"cmd=dialogMaterialInspect"},produces = "text/html",method = RequestMethod.GET)
    public String dialogEquipmentTree(String materiaInspectId,ModelMap model){
        model.addAttribute("materiaInspectId",materiaInspectId);
        return "instorage/materialManag/materialView";
    }

    /**
     * 查看(直达质检)
     */
    @RequestMapping( params = {"cmd=CheckDirectInspect"},produces = "text/html",method = RequestMethod.GET)
    public String CheckDirectInspect(String materiaInspectId,ModelMap model){
        model.addAttribute("materiaInspectId",materiaInspectId);
        return "instorage/directInspect/materialView";
    }

//    /**
//     * 点击质检完成按钮
//     */
//    @RequestMapping( params = {"cmd=updateMaterial"},produces = "application/json",method = RequestMethod.POST)
//    @ResponseBody
//    public String updateMaterial(String materiaInspectId,String state,@ModelAttribute("currentUser")User user) throws IOException {
//        MaterialInspect materialInspect = materialInspectService.selectByPk(materiaInspectId);
//        materialInspect.setInspectStaus(state);
//        materialInspectService.update(materialInspect);
//        if(StringUtils.isEmpty(materialInspect.getMateriaInspectId())){
//            materialInspect.setCreateTime(new Date());
//            materialInspect.setCreateUserId(user.getUserId());
//            if(null == user.getEmployee()){
//                materialInspect.setCreateUserName(user.getUserName());
//            }else{
//                materialInspect.setCreateUserName(user.getEmployee().getEmpName());
//            }
//        }
////        materialInspectService.saveMaterialInspect(materialInspect,user);
//        CreateGoDownEntity(materiaInspectId,user);//生成出、入库单
//        return "质检完成";
//    }

//    /**
//     * 点击质检完成按钮 (直达质检)
//     */
//    @RequestMapping( params = {"cmd=updateMaterialDirect"},produces = "application/json",method = RequestMethod.POST)
//    @ResponseBody
//    public String updateMaterialDirect(String materiaInspectId,String state,@ModelAttribute("currentUser")User user) throws IOException {
//        MaterialInspect materialInspect = materialInspectService.selectByPk(materiaInspectId);
//        materialInspect.setInspectStaus(state);
//        materialInspectService.update(materialInspect);
//        if(StringUtils.isEmpty(materialInspect.getMateriaInspectId())){
//            materialInspect.setCreateTime(new Date());
//            materialInspect.setCreateUserId(user.getUserId());
//            if(null == user.getEmployee()){
//                materialInspect.setCreateUserName(user.getUserName());
//            }else{
//                materialInspect.setCreateUserName(user.getEmployee().getEmpName());
//            }
//        }
//       // materialInspectService.validate(materiaInspectId);//校验需用计划
//        CreateGoDownEntity(materiaInspectId,user);//生成出、入库单
//        return "质检完成";
//    }

        //生成出、入库单
    public void CreateGoDownEntity(String materiaInspectId,User user){
        MaterialInspect materialInspect = materialInspectService.selectByPk(materiaInspectId);//质检单
        materialInspectService.CreateInToEntity(materiaInspectId, materialInspect, user);//生成入库单
        materialInspectService.CreateOuToEntity(materiaInspectId, materialInspect, user);//生成出库单


    }

    /**
     * 接收质检请求
     * @return
     */
    @RequestMapping( params = {"cmd=edit"},produces = "text/html;charset=UTF-8",method = RequestMethod.GET)
    public String edit(String materiaInspectId,String materiaInspectStuate, ModelMap model){
        model.addAttribute("materiaInspectId", materiaInspectId);
        model.addAttribute("materiaInspectStuate", materiaInspectStuate);
        return "instorage/materialManag/edit";
    }
    /**
     * 质检按钮后台数据交互
     * @return
     */
    @RequestMapping( params = {"cmd=loadMaterialInspectData"},produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public MaterialInspect loadMaterialInspectData(String materiaInspectId,@ModelAttribute("currentUser")User user){
        MaterialInspect materialInspect = new MaterialInspect();
        QualityInspectDetailCondition qualityInspectDetailCondition = new QualityInspectDetailCondition();
        QualityInspectPicFileCondition qualityInspectPicFileCondition = new QualityInspectPicFileCondition();//图片信息的Condition
        //List<Project> project = projectService.selectAll();
        if(StringUtils.isNotEmpty(materiaInspectId)){
            materialInspect.setCreateTime(new Date());
            materialInspect = materialInspectService.selectByPk(materiaInspectId);//查询质检单
            qualityInspectDetailCondition.setMateriaInspectId(materiaInspectId);
            qualityInspectPicFileCondition.setMateriaInspectId(materiaInspectId);
//            List<QualityInspectDetail> list = qualityInspectDetailService.selectByCondition(qualityInspectDetailCondition,0,1,-1);
            List<QualityInspectDetail> list1 =  materialInspectService.selectByInspectId(materiaInspectId);//查询质检明细信息
            List<InspectPicFile> list2 =  inspectPicFileService.selectByInspectId(materiaInspectId);//查询质检明细下的图片信息
            materialInspect.setQualityInspectDetailList(list1);
            materialInspect.setInspectPicFileList(list2);
        }else{
            materialInspect.setCreateUserId(user.getUserId());
            if(null!= user.getEmployee()){
                materialInspect.setCreateUserName(user.getEmployee().getEmpName());
                materialInspect.setCreateTime(new Date());
            }else{
                materialInspect.setCreateUserName(user.getUserName());
                materialInspect.setCreateTime(new Date());
            }
        }
        return materialInspect;//ee9d8e1bab5543d89a1449dee2f23faa
    }


    /**
     * 接收质检请求（直达质检）
     * @return
     */
    @RequestMapping( params = {"cmd=editDirect"},produces = "text/html;charset=UTF-8",method = RequestMethod.GET)
    public String editDirect(String materiaInspectId,String materiaInspectStuate, ModelMap model){
        model.addAttribute("materiaInspectId", materiaInspectId);
        model.addAttribute("materiaInspectStuate", materiaInspectStuate);
        return "instorage/directInspect/edit";
    }
    /**
     * 查询页点击质检按钮（直达质检）
     * @return
     */
    @RequestMapping( params = {"cmd=loadDirectInspectData"},produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public MaterialInspect loadDirectInspectData(String materiaInspectId){
        MaterialInspect materialInspect = new MaterialInspect();
        QualityInspectDetailCondition qualityInspectDetailCondition = new QualityInspectDetailCondition();//质检明细信息Condition
        QualityInspectPicFileCondition qualityInspectPicFileCondition = new QualityInspectPicFileCondition();//图片信息的Condition
        //List<Project> project = projectService.selectAll();
        if(StringUtils.isNotEmpty(materiaInspectId)){
            materialInspect = materialInspectService.selectByPk(materiaInspectId);//查询质检单
            qualityInspectDetailCondition.setMateriaInspectId(materiaInspectId);
            qualityInspectPicFileCondition.setMateriaInspectId(materiaInspectId);
//            List<QualityInspectDetail> list = qualityInspectDetailService.selectByCondition(qualityInspectDetailCondition,0,1,-1);
            List<QualityInspectDetail> list1 =  materialInspectService.selectByInspectId(materiaInspectId);//查询质检明细信息
            List<InspectPicFile> list2 =  inspectPicFileService.selectByInspectId(materiaInspectId);//查询质检明细下的图片信息
            materialInspect.setQualityInspectDetailList(list1);
            materialInspect.setInspectPicFileList(list2);
        }
        return materialInspect;//ee9d8e1bab5543d89a1449dee2f23faa
    }


    //提供原始文件的下载
    @RequestMapping( params = {"cmd=DownPicReceipt"}, method = RequestMethod.GET)
    public void downloadOriginalFile(String InspectPicId, HttpServletResponse response){
        InspectPicFile inspectPicFile = inspectPicFileService.selectByInspectPicId(InspectPicId);
        try {
            response.addHeader("Content-Disposition", "attachment;filename=" +
                    new String(inspectPicFile.getRealFileName().replaceAll(" ", "").getBytes("GB2312"), "iso8859-1"));
            byte[] contents = inspectPicFile.getContents();
            OutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(contents);//输出文件
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //提供原始文件的下载( 直达质检 )
    @RequestMapping( params = {"cmd=DownPicDirect"}, method = RequestMethod.GET)
    public void downloadOriginalFileDirect(String InspectPicId, HttpServletResponse response){
        InspectPicFile inspectPicFile = inspectPicFileService.selectByInspectPicId(InspectPicId);
        try {
            response.addHeader ("Content-Disposition", "attachment;filename=" +
                    new String(inspectPicFile.getRealFileName().replaceAll(" ", "").getBytes("GB2312"), "iso8859-1"));
            byte[] contents = inspectPicFile.getContents();
            OutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(contents);//输出文件
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //删除上传的图片
    @RequestMapping( params = {"cmd=DeletePic"},produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public String deletePic(String InspectPicId){
        inspectPicFileService.delete(InspectPicId);
        return "删除完成";
    }

    //删除上传的图片( 直达质检 )
    @RequestMapping( params = {"cmd=DeletePicDirect"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String deleteFileDirect(String InspectPicId){
        inspectPicFileService.delete(InspectPicId);
        return "删除完成";
    }


    /**
     * 提供质检单明细【合格字段】数据进行筛选
     * */
    @RequestMapping( params = {"cmd=CheckOutReceipt"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String selectInspectCheck(@RequestBody QualityInspectDetail detail){
        String  result = "";
        BigDecimal qualifiedQty = detail.getQualifiedQty();//合格数量
        String deliveryId  = detail.getDeliveryId();//收货单ID
        String materiaInspectId = detail.getMateriaInspectId();
        if(qualifiedQty != null && deliveryId != null && materiaInspectId == null){
            result = qualityInspectDetailService.selectInspectDetailCheckOut(detail);
        }

        return result;
    }

    /**
     * 提供质检单明细【合格字段】数据进行筛选（直达质检）
     * */
    @RequestMapping( params = {"cmd=CheckOutDirect"},produces = "application/json",method = RequestMethod.POST)
    @ResponseBody
    public String selectInspectDetailCheck(@RequestBody QualityInspectDetail detail){
        String  result = "";
        BigDecimal qualifiedQty = detail.getQualifiedQty();//合格数量
        String deliveryId  = detail.getDeliveryId();//收货单ID
        String materiaInspectId = detail.getMateriaInspectId();
        if(qualifiedQty != null && deliveryId != null && materiaInspectId == null){
            result = qualityInspectDetailService.selectInspectDetailCheckOut(detail);
        }

        return result;
    }



}
