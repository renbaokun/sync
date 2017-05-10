/**
 *
 */
package com.dkd.emms.systemManage.service;

import com.dkd.emms.core.exception.BusinessException;
import com.dkd.emms.core.util.bean.DozerMapperSingleton;
import com.dkd.emms.core.util.uuid.UUIDGenerator;
import com.dkd.emms.systemManage.bo.*;
import com.dkd.emms.systemManage.dao.MaterialsFileDao;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


/**
* @Title: DrawingNumberService
* @Description:
* @param
* @author:YUZH
* @data 2017年1月24日
*/
@SuppressWarnings("unused")
@Service
@Transactional
public class ExcelParsingService{

    /*@Autowired
    private SequenceService sequenceService;
    @Autowired
    private OrganizationService organizationService;*/
    @Autowired
    private DrawingNumberService drawingNumberService;
    @Autowired
    private MaterialsTableService materialsTableService;
    @Autowired
    private DrawingDetailedService drawingDetailedService;
    @Autowired
    private MaterialsFileDao materialsFileDao;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SequenceService sequenceService;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    //使用对应的方式从单元格中取出数据
    @SuppressWarnings("deprecation")
    private String getArrivalDate(XSSFCell arrivalDate ){
        if(null == arrivalDate || StringUtils.equals("", arrivalDate.toString())){
            return "";
        }
        switch (arrivalDate.getCellType()) {
        case HSSFCell.CELL_TYPE_NUMERIC:
            Object inputValue = null;// 单元格值
            Long longVal = Math.round(arrivalDate.getNumericCellValue());
            Double doubleVal = arrivalDate.getNumericCellValue();
            if(Double.parseDouble(longVal + ".0") == doubleVal){   //判断是否含有小数位.0
                inputValue = longVal;
            }else{
                inputValue = doubleVal;
            }
            return inputValue + "";
        case HSSFCell.CELL_TYPE_STRING:
            return arrivalDate.getStringCellValue();
        case HSSFCell.CELL_TYPE_FORMULA:
            return arrivalDate.getCellFormula();
        case HSSFCell.CELL_TYPE_BLANK:
            return "";
        case HSSFCell.CELL_TYPE_BOOLEAN:
            return arrivalDate.getBooleanCellValue() + "";
        case HSSFCell.CELL_TYPE_ERROR:
            return arrivalDate.getErrorCellValue() + "";
        }
        return "";
    }

    //将物资导入的excel文件转为List
    @SuppressWarnings({ "resource", "deprecation" })
    public List<DrawingDetailed> poiW(MultipartFile excel){
        try{
            InputStream is = excel.getInputStream();
            XSSFWorkbook xwb = new XSSFWorkbook(is);
            XSSFSheet sheet = xwb.getSheetAt(0);//获取第一个页签
            checkoutTemplateW(sheet);//检验模板正确性

            //上传料表内设计图纸明细列表
            List<DrawingDetailed> excelListW = new ArrayList<DrawingDetailed>();
            //记录表格总行数
            int rowCount = sheet.getLastRowNum();
            for(int i = 3; i <= rowCount; i++){//循环行
                DrawingDetailed drawingDetailed = new DrawingDetailed();
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(0) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(0)).equals("")){
                    drawingDetailed.setDrawingNumberCode(this.getArrivalDate(sheet.getRow(i).getCell(0)));//设计图编号
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(1) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(1)).equals("") &&
                           sheet.getRow(i).getCell(1).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    drawingDetailed.setDrawingNumberVersion(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(1))));//版次
                }else{
                    drawingDetailed.setDrawingNumberVersion(new BigDecimal(-1));
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(2) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(2)).equals("")){
                    drawingDetailed.setProjectCodeSeq(this.getArrivalDate(sheet.getRow(i).getCell(2)));//WBS编码
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(3) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(3)).equals("") &&
                           sheet.getRow(i).getCell(3).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    drawingDetailed.setDrawingDetailedNo((int)Double.parseDouble(this.getArrivalDate(sheet.getRow(i).getCell(3))));//序号
                }else{
                    drawingDetailed.setDrawingDetailedNo(-1);
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(4) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(4)).equals("")){
                    drawingDetailed.setDesignCode(this.getArrivalDate(sheet.getRow(i).getCell(4)));//物资编码
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(5) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(5)).equals("")){
                    drawingDetailed.setDesignDescribe(this.getArrivalDate(sheet.getRow(i).getCell(5)));//物资描述
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(6) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(6)).equals("")){
                    drawingDetailed.setDesignUnit(this.getArrivalDate(sheet.getRow(i).getCell(6)));//计量单位
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(7) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(7)).equals("") &&
                           sheet.getRow(i).getCell(7).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    drawingDetailed.setDesignCount(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(7))));//设计数量
                }else{
                    drawingDetailed.setDesignCount(new BigDecimal("-1"));
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(8) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(8)).equals("") &&
                           sheet.getRow(i).getCell(8).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    drawingDetailed.setOverrun(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(8))));//裕量
                }else{
                    drawingDetailed.setOverrun(new BigDecimal("-1"));;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(9) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(9)).equals("") &&
                           sheet.getRow(i).getCell(9).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    drawingDetailed.setTotalCount(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(9))));//总量
                }else{
                    drawingDetailed.setTotalCount(new BigDecimal("-1"));
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(10) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(10)).equals("")){
                    drawingDetailed.setExtra1(this.getArrivalDate(sheet.getRow(i).getCell(10)));//附加1
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(11) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(11)).equals("")){
                    drawingDetailed.setExtra2(this.getArrivalDate(sheet.getRow(i).getCell(11)));//附加2
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(12) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(12)).equals("")){
                    drawingDetailed.setExtra3(this.getArrivalDate(sheet.getRow(i).getCell(12)));//附加3
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(13) != null &&
                           !this.getArrivalDate(sheet.getRow(i).getCell(13)).equals("")){
                    drawingDetailed.setExtra4(this.getArrivalDate(sheet.getRow(i).getCell(13)));//附加4
                }
                excelListW.add(drawingDetailed);
            }
            return excelListW;
        }catch (IOException e) {
            throw new BusinessException("导入失败请重新导入");
        }
    }

    //校验 物资 List生成错误信息并存入数据库
    public String checkAndSaveListW(List<DrawingDetailed> excelList, String designOrgId, String materialsTableType, User user, String NeedToDo){

        MaterialsTable mt;
        Organization org = materialsTableService.selectOrgById(designOrgId);
        if(NeedToDo.equals("Upload")){
            //为上传的文件创建料表
            mt = this.creatMaterialsTable4Upload(org, materialsTableType, user);
        }else{
            mt = materialsTableService.selectByPk(excelList.get(0).getMaterialsTableId());
        }
        //料表状态标志
        boolean hasError = false;

        //上传料表内设计图纸明细列表
        List<DrawingDetailed> allDetailList = new ArrayList<DrawingDetailed>();
        //上传料表内设计图纸明细列表Map
        Map<String,List<DrawingDetailed>> allDetailMap = new HashMap<String, List<DrawingDetailed>>();
        //上传料表内设计图纸列表
        List<DrawingNumber> allDrawingList = new ArrayList<DrawingNumber>();

        //上传料表内设计图纸Code去重集合
        Set<String> DrawingCodeSet = new HashSet<String>();
        //临时记录图纸Code对应的Id
        Map<String, String> DrawingNumberIds = new HashMap<String, String>();
        //记录DrawingCodeSet中旧的元素数
        int oldNumberDrawingCodeSet = 0;

        //记录表格总行数
        int rowCount = excelList.size();
        for(int i = 0; i < rowCount; i++){//循环行
            //为新读取的明细条目生成对象
            DrawingDetailed drawingDetailed = new DrawingDetailed();
            String errorMessage = "";//错误信息
            int countBlankColumn = 0;//记录空列

            /*A、B类错误校验*/
            //逐行校验 (A:数据为空,B:数据超长)
            if(excelList.get(i)!= null && excelList.get(i).getDrawingNumberCode() != null &&
               !excelList.get(i).getDrawingNumberCode().equals("")){
                if(excelList.get(i).getDrawingNumberCode().length() <= 50){
                    drawingDetailed.setDrawingNumberCode(excelList.get(i).getDrawingNumberCode());
                }else{
                    hasError = true;
                    errorMessage += "B图号";
                }
            }else{
                hasError = true;
                errorMessage += "A图号";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getDrawingNumberVersion() != null &&
                    !"-1".equals(excelList.get(i).getDrawingNumberVersion().toString())){
                if((excelList.get(i).getDrawingNumberVersion()+"").length() <= 10){
                    drawingDetailed.setDrawingNumberVersion(excelList.get(i).getDrawingNumberVersion());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B版次";
                    drawingDetailed.setDrawingNumberVersion(new BigDecimal("-1"));
                }
            }else{
                hasError = true;
                errorMessage += "|A版次";
                drawingDetailed.setDrawingNumberVersion(new BigDecimal("-1"));
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getProjectCodeSeq() != null &&
               !excelList.get(i).getProjectCodeSeq().equals("")){
                if(excelList.get(i).getProjectCodeSeq().length() <= 100){
                    drawingDetailed.setProjectCodeSeq(excelList.get(i).getProjectCodeSeq());
                    drawingDetailed.setProjectId(excelList.get(i).getProjectId());
                    //验证WBS合法性
                    Project p = null;
                    List<Project> pList = new ArrayList<Project>();
                    if(NeedToDo.equals("Upload")){
                         pList = projectService.selectProjectByCodeSeq(drawingDetailed.getProjectCodeSeq());
                    }else{
                        Project project = projectService.selectByPk(drawingDetailed.getProjectId());
                        pList.add(project);
                    }
                    if(pList.size()>0){
                        p = pList.get(0);
                    }
                    if(p==null || !p.getProjectState().equals("wbspass")){
                        hasError = true;
                        errorMessage += "|WBS编码";
                    }else{
                        errorMessage += '|';
                        drawingDetailed.setProjectId(p.getProjectId());
                        drawingDetailed.setProjectCodeSeq(p.getProjectCodeSeq());
                    }
                }else{
                    hasError = true;
                    errorMessage += "|BWBS编码";
                }
            }else{
                hasError = true;
                errorMessage += "|AWBS编码";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getDrawingDetailedNo() != null &&
                    excelList.get(i).getDrawingDetailedNo() != -1){
                if((excelList.get(i).getDrawingDetailedNo()+"").length() <= 10){
                    drawingDetailed.setDrawingDetailedNo(excelList.get(i).getDrawingDetailedNo());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B序号";
                    drawingDetailed.setDrawingDetailedNo(-1);
                }
            }else{
                hasError = true;
                errorMessage += "|A序号";
                drawingDetailed.setDrawingDetailedNo(-1);
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getDesignCode() != null &&
               !excelList.get(i).getDesignCode().equals("")){
                if(excelList.get(i).getDesignCode().length() <= 30){
                    drawingDetailed.setDesignCode(excelList.get(i).getDesignCode());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B物资编码";
                }
            }else{
                hasError = true;
                errorMessage += "|A物资编码";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getDesignDescribe() != null &&
               !excelList.get(i).getDesignDescribe().equals("")){
                if(excelList.get(i).getDesignDescribe().length() <= 200){
                    drawingDetailed.setDesignDescribe(excelList.get(i).getDesignDescribe());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B物资描述";
                }
            }else{
                hasError = true;
                errorMessage += "|A物资描述";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getDesignUnit() != null &&
               !excelList.get(i).getDesignUnit().equals("")){
                if(excelList.get(i).getDesignUnit().length() <= 10){
                    drawingDetailed.setDesignUnit(excelList.get(i).getDesignUnit());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B计量单位";
                }
            }else{
                hasError = true;
                errorMessage += "|A计量单位";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getDesignCount() != null &&
               !"-1".equals(excelList.get(i).getDesignCount().toString())){
                if(excelList.get(i).getDesignCount().toString().length() <= 30){
                    drawingDetailed.setDesignCount(excelList.get(i).getDesignCount());
                    errorMessage += '|';
                }else{
                    drawingDetailed.setDesignCount(new BigDecimal("-1"));
                    hasError = true;
                    errorMessage += "|B设计数量";
                }
            }else{
                drawingDetailed.setDesignCount(new BigDecimal("-1"));
                hasError = true;
                errorMessage += "|A设计数量";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getOverrun() != null &&
               !"-1".equals(excelList.get(i).getOverrun().toString())){
                if(excelList.get(i).getOverrun().toString().length() <= 30){
                    drawingDetailed.setOverrun(excelList.get(i).getOverrun());
                    errorMessage += '|';
                }else{
                    drawingDetailed.setOverrun(new BigDecimal("-1"));
                    hasError = true;
                    errorMessage += "|B裕量";
                }
            }else{
                drawingDetailed.setOverrun(new BigDecimal("-1"));
                errorMessage += '|';
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getTotalCount() != null &&
               !"-1".equals(excelList.get(i).getTotalCount().toString())){
                if(excelList.get(i).getTotalCount().toString().length() <= 30){
                    drawingDetailed.setTotalCount(excelList.get(i).getTotalCount());
                    errorMessage += '|';
                }else{
                    drawingDetailed.setTotalCount(new BigDecimal("-1"));
                    hasError = true;
                    errorMessage += "|B总量";
                }
            }else{
                drawingDetailed.setTotalCount(new BigDecimal("-1"));
                hasError = true;
                errorMessage += "|A总量";
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getExtra1() != null &&
               !excelList.get(i).getExtra1().equals("")){
                if(excelList.get(i).getExtra1().length() <= 10){
                    drawingDetailed.setExtra1(excelList.get(i).getExtra1());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B附加1";
                }
            }else{
                errorMessage += '|';
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getExtra2() != null &&
               !excelList.get(i).getExtra2().equals("")){
                if(excelList.get(i).getExtra2().length() <= 10){
                    drawingDetailed.setExtra2(excelList.get(i).getExtra2());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B附加2";
                }
            }else{
                errorMessage += '|';
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getExtra3() != null &&
               !excelList.get(i).getExtra3().equals("")){
                if(excelList.get(i).getExtra3().length() <= 10){
                    drawingDetailed.setExtra3(excelList.get(i).getExtra3());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B附加3";
                }
            }else{
                errorMessage += '|';
                countBlankColumn++;
            }
            if(excelList.get(i)!= null && excelList.get(i).getExtra4() != null &&
               !excelList.get(i).getExtra4().equals("")){
                if(excelList.get(i).getExtra4().length() <= 10){
                    drawingDetailed.setExtra4(excelList.get(i).getExtra4());
                    errorMessage += '|';
                }else{
                    hasError = true;
                    errorMessage += "|B附加4";
                }
            }else{
                errorMessage += '|';
                countBlankColumn++;
            }
            /*A、B类错误校验*/

            //忽略正行为空
            if(countBlankColumn != 14){
                if(NeedToDo.equals("Upload")){
                    drawingDetailed.setDrawingDetailedId(UUIDGenerator.getUUID());
                    drawingDetailed.setMaterialsTableId(mt.getMaterialsTableId());
                    drawingDetailed.setMaterialsTableCode(mt.getMaterialsTableCode());
                    drawingDetailed.setDesignOrgId(org.getOrgId());
                    drawingDetailed.setDesignOrgCode(org.getOrgCode());
                    drawingDetailed.setDrawingDetailedType(materialsTableType);
                }else{
                    drawingDetailed.setDrawingDetailedId(excelList.get(i).getDrawingDetailedId());
                    drawingDetailed.setMaterialsTableId(excelList.get(i).getMaterialsTableId());
                    drawingDetailed.setMaterialsTableCode(excelList.get(i).getMaterialsTableCode());
                }
                drawingDetailed.setDrawingDetailedState("notMatch");//alreadyMatch已匹配 notMatch未匹配
                if(hasError == true){
                    drawingDetailed.setErrorType("1");//暂时认为该条明细只有A或B型错误
                }

                /*为上传数据生成新的图号对象*/
                String DNcode = drawingDetailed.getDrawingNumberCode();
                if(DNcode!=null && !DNcode.equals("")){//先得不为空
                    DrawingCodeSet.add(DNcode);
                    if(DrawingCodeSet.size() != oldNumberDrawingCodeSet){//本料表内是否已经存在改图号
                        oldNumberDrawingCodeSet = DrawingCodeSet.size();
                        int countDN = drawingNumberService.countByCode(DNcode);
                        if(countDN == 0){//本料表未出现过，数据库中同样不存在  放入列表 准备插入
                            DrawingNumber dn = this.creatDrawingNumber4Upload(DNcode,
                                    drawingDetailed.getProjectCodeSeq(), drawingDetailed.getDrawingNumberVersion(),
                                    designOrgId, materialsTableType);
                            allDrawingList.add(dn);
                            DrawingNumberIds.put(DNcode, dn.getDrawingNumberId());
                            drawingDetailed.setDrawingNumberId(dn.getDrawingNumberId());
                            drawingDetailed.setDrawingNumberCode(dn.getDrawingNumberCode());
                        }else{//本料表内未出现过，但是数据库中已存在该记录 更新该条数据状态
                            //更新已有图号的状态为部分确认
                            DrawingNumber dn = drawingNumberService.selectByCode(DNcode);
                            dn.setDrawingNumberState("partConfirm");
                            drawingNumberService.updateByCode(dn);
                            DrawingNumberIds.put(DNcode, dn.getDrawingNumberId());
                            //维护本条明细
                            drawingDetailed.setDrawingNumberId(dn.getDrawingNumberId());
                            drawingDetailed.setDrawingNumberCode(dn.getDrawingNumberCode());
                        }
                    }else{//本表内已经存在该数据
                        drawingDetailed.setDrawingNumberId(DrawingNumberIds.get(DNcode));
                    }
                }
                /*为上传数据生成新的图号对象*/

                /*C类错误校验*/
                //C类为上传文件内重复性校验
                String newDetail = drawingDetailed.getDrawingNumberCode() + "," +  drawingDetailed.getDrawingDetailedNo()+","+drawingDetailed.getDrawingNumberVersion()+","+designOrgId;
                List<DrawingDetailed> mapItem = new ArrayList<DrawingDetailed>();
                if(allDetailMap.get(newDetail)!=null){//表明出现了重复项
                    hasError = true;
                    String tempEM = "";
                    //在已有列表中找出重复项
                    for(DrawingDetailed detailedItem : allDetailMap.get(newDetail)){
                        String em = detailedItem.getErrorMessage();
                        //检验已经过校验的对象的错误类型
                        if(em != null){//至少存在一项A，B，C类错误信息
                            String[] ems = em.split(",");
                            if(ems[0].split("|")[0].equals("C")){//已经存在部分C类错误信息
                                //为新条目记载C类错误信息
                                tempEM = tempEM + "|" + detailedItem.getDrawingDetailedId();
                                //更新已经过校验的对象的错误信息
                                ems[0] = ems[0] + "|" + drawingDetailed.getDrawingDetailedId();
                                em = ems[0] + "," + ems[1];
                                detailedItem.setErrorMessage(em);
                            }else{//尚未添加过C类错误信息
                                //为新条目记载C类错误信息
                                tempEM = tempEM + "|" + detailedItem.getDrawingDetailedId();
                                //更新已经过校验的对象的错误信息
                                em = "C|" + drawingDetailed.getDrawingDetailedId() + "," + ems[1];
                                detailedItem.setErrorMessage(em);
                            }
                        } else{//完全不存在A，B，C类错误信息
                            //为新条目记载C类错误信息
                            tempEM = tempEM + "|" + detailedItem.getDrawingDetailedId();
                            //更新已经过校验的对象的错误信息
                            em = "C|" + drawingDetailed.getDrawingDetailedId() + errorMessage;
                            detailedItem.setErrorMessage(em);
                        }
                        detailedItem.setErrorType("2");

                    }
                    //更新新条目的错误信息
                    drawingDetailed.setErrorType("2");//暂时认为该条明细含有C型错误 但不含D型错误
                    drawingDetailed.setErrorMessage("C" + tempEM + "," + errorMessage);
                    allDetailMap.get(newDetail).add(drawingDetailed);
                }else{
                    drawingDetailed.setErrorMessage("," + errorMessage);
                    mapItem.add(drawingDetailed);
                    allDetailMap.put(drawingDetailed.getDrawingNumberCode() + "," + drawingDetailed.getDrawingDetailedNo()+","+drawingDetailed.getDrawingNumberVersion()+","+designOrgId, mapItem);
                }
                /*C类错误校验*/

            }

        }//for end

        /*D类错误校验*/
        //D类错误：上传数据与数据库内查重
        //查询条件list
        List<String> codeNo = new ArrayList<String>(allDetailMap.keySet());
        //获取查询结果list
        List<String> codeNoResults = drawingDetailedService.findRepeatW(codeNo);
        if(codeNoResults.size()!=0){
            hasError = true;
        }
        //更新D类错误信息
        for(String codeNoResult: codeNoResults){//循环查询结果
            for(DrawingDetailed detailedItem: allDetailMap.get(codeNoResult)){//循环
                detailedItem.setErrorType("3");//含有D型错误
                detailedItem.setErrorMessage(detailedItem.getErrorMessage()+ ",D");//在错误信息中添加D类错误
            }
        }
        /*D类错误校验*/

        //如果未发现错误 置料表状态为 校验通过
        if(!hasError){
            mt.setMaterialsTableState("checkPass");
        }

        //此处准备批量插入或更新
        for(String key: codeNo){
            for(DrawingDetailed detailedItem: allDetailMap.get(key)){
                if(detailedItem.getErrorMessage().equals(",|||||||||||||")){
                    detailedItem.setErrorMessage(null);
                    detailedItem.setErrorType(null);
                }
                allDetailList.add(detailedItem);
            }
        }
        //materialsTable插入
        if(NeedToDo.equals("Upload")){

            materialsTableService.insert(mt);
        }else{
            materialsTableService.update(mt);
        }

        if(NeedToDo.equals("Upload")){
            this.variousInsertList(allDrawingList, allDetailList);
        }else{
            this.variousUpdateList(allDetailList);
        }


        return mt.getMaterialsTableId();

    }

    //各种批量更新数据库
    private void variousUpdateList(List<DrawingDetailed> allDetailList) {
        //allDetailList批量更新
        if(allDetailList.size()!=0){
            for(DrawingDetailed dd: allDetailList){
                drawingDetailedService.update(dd);
            }
        }
    }

    //各种批量插入数据库
    private void variousInsertList(List<DrawingNumber> allDrawingList, List<DrawingDetailed> allDetailList) {
        //allDrawingList批量插入
        if(allDrawingList.size()!=0){
            drawingNumberService.insertList(allDrawingList);
        }
        //allDetailList批量插入
        if(allDetailList.size()!=0){
            drawingDetailedService.insertList(allDetailList);
        }
    }

    //检验上传的文件是否使用给定 物资导入 模板填写
    public void checkoutTemplateW(XSSFSheet sheet){
        //确认模板行数与列数
        if(sheet.getLastRowNum()<2 ){
            throw new BusinessException("物资类别和模板不一致，请修改！");
        }else{
            if(sheet.getRow(2).getPhysicalNumberOfCells()<13){
                throw new BusinessException("物资类别和模板不一致，请修改！");
            }
        }
        //检验模板表头内容
        if(sheet.getRow(2) == null ||
           !this.getArrivalDate(sheet.getRow(2).getCell(0)).equals("设计图编号") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(1)).equals("版次") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(2)).equals("WBS编码") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(3)).equals("序号") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(4)).equals("物资编码") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(5)).equals("物资描述") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(6)).equals("计量单位") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(7)).equals("设计数量") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(8)).equals("裕量") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(9)).equals("总量") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(10)).equals("附加1") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(11)).equals("附加2") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(12)).equals("附加3") ||
           !this.getArrivalDate(sheet.getRow(2).getCell(13)).equals("附加4")){
            throw new BusinessException("物资类别和模板不一致，请修改！");
        }
        //检验表格中是否有数据条目
        boolean isEmpty = true;
        for(int i=3; i<=sheet.getLastRowNum(); i++){
            for(int j=0; j<=13; j++){
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(j) != null &&
                   !this.getArrivalDate(sheet.getRow(i).getCell(j)).equals("")){
                    isEmpty = false;
                    break;
                }
            }
            if(!isEmpty){
                break;
            }
        }
        if(isEmpty){
            throw new BusinessException("上传的文件中至少需要存在一条数据！");
        }
    }

    //为上传的文件创建料表对象
    public MaterialsTable creatMaterialsTable4Upload(Organization org, String materialsTableType, User user){

        MaterialsTable mt = new MaterialsTable();
        mt.setMaterialsTableId(UUIDGenerator.getUUID());
        mt.setMaterialsTableCode(sequenceService.getFlowNoByJudge("MaterialsTable", "LB" + dateFormat.format(new Date()), 5));
        mt.setDesignOrgId(org.getOrgId());
        mt.setDesignOrgName(org.getOrgName());
        mt.setMaterialsTableState("checkNotPass");//默认校验不成功
        mt.setMaterialsTableType(materialsTableType);
        mt.setIsMatch("0");//新生成的料表必为未经过同步的
        mt.setCreateTime(new Date());
        mt.setCreateUserId(user.getUserId());
        mt.setCreateUserName(user.getUserName());
        return mt;
    }
    //为上传的文件创建设计图对象
    public DrawingNumber creatDrawingNumber4Upload(String drawingNumberCode, String projectCodeSeq,
            BigDecimal drawingNumberVersion, String designOrgId, String drawingNumberType){
        DrawingNumber dn = new DrawingNumber();
        dn.setDrawingNumberId(UUIDGenerator.getUUID());
        dn.setDrawingNumberCode(drawingNumberCode);
        dn.setDrawingNumberVersion(drawingNumberVersion + "");
        dn.setDesignOrgId(designOrgId);
        //dn.setDesignOrgName(organizationService.selectByPk(designOrgId).getOrgName());
        dn.setDrawingNumberState("notConfirm");
        dn.setDrawingNumberType(drawingNumberType);
        return dn;
    }

    //保存上传的原始文件
    public void saveOriginalFile( MultipartFile excel, String materialsTableId){
        try {
            MaterialsFile blobField = new MaterialsFile();
            //获取上传文件真实名称
            String realFileName = excel.getOriginalFilename();
            blobField.setMaterialsTableId(materialsTableId);
            blobField.setRealFileName(realFileName);
            blobField.setContents(excel.getBytes());
            materialsFileDao.insert(blobField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据料表id删除上传的文件
    public void deleteFile(String materialsTableId) {
        materialsFileDao.delete(materialsTableId);
    }

    //根据料表ID加载原始文件
    public MaterialsFile selectMFbyId(String materialsTableId){
        return materialsFileDao.selectByPk(materialsTableId);
    }


    //将 设备导入 的excel文件转为List
    public Map<String, Object> poiS(MultipartFile excel){
        try{
            InputStream is = excel.getInputStream();
            XSSFWorkbook xwb = new XSSFWorkbook(is);
            XSSFSheet sheet = xwb.getSheetAt(0);//获取第一个页签
            checkoutTemplateS(sheet);//检验模板正确性

            //需要返回的Map包含錯誤信息和生成list
            Map<String, Object> result = new HashMap<String, Object>();
            //记录需要错误行数
            String errorRow = "";

            //上传料表内设计图纸明细列表
            List<DrawingDetailed[]> excelListS = new ArrayList<DrawingDetailed[]>();
            //记录表格总行数
            int rowCount = sheet.getLastRowNum();
            for(int i = 4; i <= rowCount; i++){//循环行
                //是否存在错误flag
                int errorCount=0;
                boolean hasError = false;
                String device = "";
                DrawingDetailed[] DDgroup = new DrawingDetailed[2];
                DDgroup[0] = new DrawingDetailed();
                DDgroup[1] = new DrawingDetailed();
                //扫描设备区域
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(0) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(0)).equals("") &&
                        this.getArrivalDate(sheet.getRow(i).getCell(0)).length()<=50){
                    DDgroup[0].setDrawingNumberCode(this.getArrivalDate(sheet.getRow(i).getCell(0)));//设计图编号
                }else{
                    hasError = true;
                    errorCount++;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(1) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(1)).equals("") &&
                        sheet.getRow(i).getCell(1).getCellType() == HSSFCell.CELL_TYPE_NUMERIC &&
                        this.getArrivalDate(sheet.getRow(i).getCell(1)).length()<=10){
                    DDgroup[0].setDrawingNumberVersion(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(1))));//版次
                }else{
                    errorCount++;
                    hasError = true;
                    DDgroup[0].setDrawingNumberVersion(new BigDecimal(-1));
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(2) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(2)).equals("") &&
                        this.getArrivalDate(sheet.getRow(i).getCell(2)).length()<=100){
                    DDgroup[0].setProjectCodeSeq(this.getArrivalDate(sheet.getRow(i).getCell(2)));//WBS编码
                }else{
                    errorCount++;
                    hasError = true;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(3) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(3)).equals("") &&
                        sheet.getRow(i).getCell(3).getCellType() == HSSFCell.CELL_TYPE_NUMERIC &&
                        this.getArrivalDate(sheet.getRow(i).getCell(3)).length()<=10){
                    DDgroup[0].setDrawingDetailedNo((int) Double.parseDouble(this.getArrivalDate(sheet.getRow(i).getCell(3))));//序号
                }else{
                    errorCount++;
                    hasError = true;
                    DDgroup[0].setDrawingDetailedNo(-1);
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(4) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(4)).equals("") &&
                        this.getArrivalDate(sheet.getRow(i).getCell(4)).length()<=30){
                    DDgroup[0].setDesignCode(this.getArrivalDate(sheet.getRow(i).getCell(4)));//设备编码(物资编码)
                }else{
                    errorCount++;
                    hasError = true;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(5) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(5)).equals("") &&
                        this.getArrivalDate(sheet.getRow(i).getCell(5)).length()<=200){
                    String regex= "^([A-Z]+)\\b(-[0-9]+)\\b(/[0-9]+)*$";
                    if(this.getArrivalDate(sheet.getRow(i).getCell(5)).matches(regex)){
                        /*DDgroup[0].setDrawingNumberDeviceNo(this.getArrivalDate(sheet.getRow(i).getCell(5)));//位号*/
                        device=this.getArrivalDate(sheet.getRow(i).getCell(5));
                    }else{
                        errorCount++;
                        hasError = true;
                    }
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(6) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(6)).equals("") &&
                        this.getArrivalDate(sheet.getRow(i).getCell(6)).length()<=200){
                    DDgroup[0].setDesignDescribe(this.getArrivalDate(sheet.getRow(i).getCell(6)));//设备描述(物资描述)
                }else{
                    errorCount++;
                    hasError = true;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(7) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(7)).equals("") &&
                        sheet.getRow(i).getCell(7).getCellType() == HSSFCell.CELL_TYPE_NUMERIC &&
                        this.getArrivalDate(sheet.getRow(i).getCell(7)).length()<=30){
                    DDgroup[0].setDesignCount(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(7))));
                    DDgroup[0].setTotalCount(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(7))));//设备数量(总量)
                }else{
                    errorCount++;
                    hasError = true;
                    DDgroup[0].setTotalCount(new BigDecimal("-1"));
                    DDgroup[0].setDesignCount(new BigDecimal("-1"));
                }
                if (sheet.getRow(i) != null && sheet.getRow(i).getCell(8) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(8)).equals("") &&
                        this.getArrivalDate(sheet.getRow(i).getCell(8)).length()<=200){
                    DDgroup[0].setDesignUnit(this.getArrivalDate(sheet.getRow(i).getCell(8)));//设备计量单位(计量单位)
                }else{
                    errorCount++;
                    hasError = true;
                }

                //扫描部件区域
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(9) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(9)).equals("") &&
                        sheet.getRow(i).getCell(9).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    DDgroup[1].setDrawingDetailedNo((int) Double.parseDouble(this.getArrivalDate(sheet.getRow(i).getCell(9))));//件号(序号)
                }else{
                    DDgroup[1].setDrawingDetailedNo(-1);
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(10) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(10)).equals("")){
                    DDgroup[1].setDesignCode(this.getArrivalDate(sheet.getRow(i).getCell(10)));//部件编码(物资编码)
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(11) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(11)).equals("")){
                    DDgroup[1].setDesignDescribe(this.getArrivalDate(sheet.getRow(i).getCell(11)));//部件描述(物资描述)
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(12) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(12)).equals("") &&
                        sheet.getRow(i).getCell(12).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    DDgroup[1].setDesignCount(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(12))));//部件数量(设计数量)
                    DDgroup[1].setTotalCount(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(12))));
                }else{
                    DDgroup[1].setDesignCount(new BigDecimal("-1"));
                    DDgroup[1].setTotalCount(new BigDecimal("-1"));
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(13) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(13)).equals("")){
                    DDgroup[1].setDesignUnit(this.getArrivalDate(sheet.getRow(i).getCell(13)));//部件计量单位(计量单位)
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(14) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(14)).equals("") &&
                        sheet.getRow(i).getCell(14).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    DDgroup[1].setUnitWeight(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(14))));//单位重量
                }else{
                    DDgroup[1].setUnitWeight(new BigDecimal("-1"));;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(15) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(15)).equals("") &&
                        sheet.getRow(i).getCell(15).getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                    DDgroup[1].setTotalWeight(new BigDecimal(this.getArrivalDate(sheet.getRow(i).getCell(15))));//总重
                }else{
                    DDgroup[1].setTotalWeight(new BigDecimal("-1"));;
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(16) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(16)).equals("")){
                    DDgroup[1].setPartAttributes(this.getArrivalDate(sheet.getRow(i).getCell(16)));//部件属性
                }
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(17) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(17)).equals("")){
                    DDgroup[1].setRemark(this.getArrivalDate(sheet.getRow(i).getCell(17)));//备注
                }
                if(errorCount>7){
                    continue;
                }
                if(hasError==true){
                    errorRow += (i+1) + ", ";
                }
                if(null !=device){
                    String [] devices = device.split("/");
                    String [] part = devices[0].split("-");
                    List<String> checkDevice = new ArrayList<String>();
                    for(int j=0;j<devices.length;j++){
                        if(checkDevice.indexOf(devices[j])==-1){
                            checkDevice.add(devices[j]);
                        }else{
                            continue;
                        }
                        DrawingDetailed[] NewDDgroup = new DrawingDetailed[2];
                        NewDDgroup[0] = new DrawingDetailed();
                        NewDDgroup[1] = new DrawingDetailed();
                        DozerMapperSingleton.map(DDgroup[0],NewDDgroup[0]);
                        DozerMapperSingleton.map(DDgroup[1],NewDDgroup[1]);
                        if(DDgroup[0].getDesignCount().toString().equals("-1")){
                            NewDDgroup[0].setDesignCount(new BigDecimal("-1"));
                            NewDDgroup[0].setTotalCount(new BigDecimal("-1"));
                        }else{
                            NewDDgroup[0].setDesignCount(new BigDecimal("1"));
                            NewDDgroup[0].setTotalCount(new BigDecimal("1"));
                        }
                        if(j==0){
                            NewDDgroup[0].setDrawingNumberDeviceNo(devices[0]);
                        }else{
                            NewDDgroup[0].setDrawingNumberDeviceNo(part[0]+"-"+devices[j]);
                        }
                        //返回所有数据
                        excelListS.add(NewDDgroup);
                    }
                }
            }
            result.put("List",excelListS);
            result.put("errorRow", errorRow);
            return result;
        }catch (IOException e) {
            throw new BusinessException("导入失败请重新导入");
        }
    }
    //校验 设备 List生成错误信息并存入数据库
    public String checkAndSaveListS(String errorRow,List<DrawingDetailed[]> excelList, String designOrgId, String materialsTableType, User user, String NeedToDo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MaterialsTable mt;
        Organization org = materialsTableService.selectOrgById(designOrgId);
        if(NeedToDo.equals("Upload")){
            //为上传的文件创建料表
            mt = this.creatMaterialsTable4Upload(org, materialsTableType, user);
        }else{
            mt = materialsTableService.selectByPk(excelList.get(0)[0].getMaterialsTableId());
        }
        //料表状态标志
        boolean hasError = false;
        //上传料表内设计图纸全部明细列表
        List<DrawingDetailed> allDetailList = new ArrayList<DrawingDetailed>();

        /*
         * 上传料表内设计图纸明细设备列表Map
         * map
         *  |_key:"DNcode+DDno",value:Map
         *                              |_DDs1设备
         *       allDetailMap           |_List部件
         *         层级结构                 |_DDb1
         *                                  |_DDb2
         *                              ...      ...
         * 一级List记录重复，二级List分别记录部件
         */

        Map<String,Map<String,Object>> allDetailMap = new HashMap<String,Map<String,Object>>();
        //上传料表内设计图纸列表
        List<DrawingNumber> allDrawingList = new ArrayList<DrawingNumber>();

        //上传料表内设计图纸Code去重集合
        Set<String> DrawingCodeSet = new HashSet<String>();
        //临时记录图纸Code对应的Id
        Map<String, String> DrawingNumberIds = new HashMap<String, String>();
        //记录DrawingCodeSet中旧的元素数
        int oldNumberDrawingCodeSet = 0;

        //记录表格总行数
        int rowCount = excelList.size();
        for(int i = 0; i < rowCount; i++) {//循环行
            DrawingDetailed dds = excelList.get(i)[0];
            DrawingDetailed ddb = excelList.get(i)[1];

            if(NeedToDo.equals("Upload")){
                dds.setDrawingDetailedId(UUIDGenerator.getUUID());
                dds.setMaterialsTableId(mt.getMaterialsTableId());
                dds.setMaterialsTableCode(mt.getMaterialsTableCode());
                dds.setDesignOrgId(org.getOrgId());
                dds.setDesignOrgCode(org.getOrgCode());
                dds.setDrawingDetailedType(materialsTableType);
                dds.setErrorMessage("");
                //检查该条记录是否存在部件信息
                int notNullCount = 0;
                Field[] field = ddb.getClass().getDeclaredFields();//获取实体类的所有属性，返回Field数组
                for(int j=0 ; j<field.length ; j++) {//遍历所有属性
                    String name = field[j].getName();//获取属性的名字
                    name = name.substring(0,1).toUpperCase()+name.substring(1);//属性名首字母大写
                    String type = field[j].getGenericType().toString();//获取属性类型
                    if(type.equals("class java.lang.String")){//如果type是类类型，则前面包含"class "，后面跟类名
                        Method m = ddb.getClass().getMethod("get"+name);
                        String value = (String) m.invoke(ddb);//调用getter方法获取属性值
                        if(value != null){
                            notNullCount++;
                        }
                    }
                    if(type.equals("class java.lang.Integer")){
                        Method m = ddb.getClass().getMethod("get"+name);
                        Integer value = (Integer) m.invoke(ddb);
                        if(value != null && value != -1){
                            notNullCount++;
                        }
                    }
                    if(type.equals("class java.math.BigDecimal")){
                        Method m = ddb.getClass().getMethod("get"+name);
                        BigDecimal value = (BigDecimal) m.invoke(ddb);
                        if(value != null && value.compareTo(new BigDecimal("-1")) != 0){
                            notNullCount++;
                        }
                    }
                }
                if(notNullCount>0){//有一个不空的就算写了，否则无法在后续流程向料表同步
                    ddb.setDrawingDetailedId(UUIDGenerator.getUUID());
                    ddb.setMaterialsTableId(mt.getMaterialsTableId());
                    ddb.setMaterialsTableCode(mt.getMaterialsTableCode());
                    ddb.setDesignOrgId(org.getOrgId());
                    ddb.setDesignOrgCode(org.getOrgCode());
                    ddb.setDrawingDetailedType("w");
                    ddb.setDrawingDetailedState("notMatch");//alreadyMatch已匹配 notMatch未匹配
                }else{
                    ddb = null;
                }
            }
            dds.setDrawingDetailedState("notMatch");//alreadyMatch已匹配 notMatch未匹配

             /*为上传数据生成新的图号对象*/
            String DNcode = dds.getDrawingNumberCode();
            if(DNcode!=null && !DNcode.equals("")){//先得不为空
                DrawingCodeSet.add(DNcode);
                if(DrawingCodeSet.size() != oldNumberDrawingCodeSet){//本料表内是否已经存在改图号
                    oldNumberDrawingCodeSet = DrawingCodeSet.size();
                    int countDN = drawingNumberService.countByCode(DNcode);
                    if(countDN == 0){//本料表未出现过，数据库中同样不存在  放入列表 准备插入
                        DrawingNumber dn = this.creatDrawingNumber4Upload(DNcode,
                                dds.getProjectCodeSeq(), dds.getDrawingNumberVersion(),
                                designOrgId, materialsTableType);
                        allDrawingList.add(dn);
                        DrawingNumberIds.put(DNcode, dn.getDrawingNumberId());
                        dds.setDrawingNumberId(dn.getDrawingNumberId());
                        dds.setDrawingNumberCode(dn.getDrawingNumberCode());
                        if(ddb!=null){
                            ddb.setDrawingNumberId(dn.getDrawingNumberId());
                            ddb.setDrawingNumberCode(dn.getDrawingNumberCode());
                        }
                    }else{//本料表内未出现过，但是数据库中已存在该记录 更新该条数据状态
                        //更新已有图号的状态为部分确认
                        DrawingNumber dn = drawingNumberService.selectByCode(DNcode);
                        dn.setDrawingNumberState("partConfirm");
                        drawingNumberService.updateByCode(dn);
                        DrawingNumberIds.put(DNcode, dn.getDrawingNumberId());
                        //维护本条明细
                        dds.setDrawingNumberId(dn.getDrawingNumberId());
                        dds.setDrawingNumberCode(dn.getDrawingNumberCode());
                        if(ddb!=null){
                            ddb.setDrawingNumberId(dn.getDrawingNumberId());
                            ddb.setDrawingNumberCode(dn.getDrawingNumberCode());
                        }
                    }
                }else{//本表内已经存在该数据
                    dds.setDrawingNumberId(DrawingNumberIds.get(DNcode));
                }
            }
            /*为上传数据生成新的图号对象*/

            //构造父子关系，为D类错误校验做准备
            String key = dds.getDesignCode() + "," + dds.getDrawingNumberDeviceNo();//按照新需求以设备号和位号判重
            if(NeedToDo.equals("Upload")){
                if(allDetailMap.get(key)==null){//本条记录中的设备尚未被转化
                    //一个设备 对应一个部件列表
                    Map<String,Object> DDSMap = new HashMap<String,Object>();
                    //为这个设备初始化一个List
                    List<DrawingDetailed> DDBlist = new ArrayList<DrawingDetailed>();
                    if(ddb!=null){
                        DDBlist.add(ddb);
                    }
                    DDSMap.put("DDb", DDBlist);//放入部件列表
                    DDSMap.put("DDs", dds);//放入设备
                    allDetailMap.put(key, DDSMap);
                }else{//本条记录中的设备已经被转化
                    //将新的部件添加进相应的list
                    List<DrawingDetailed> tempDDb = (List<DrawingDetailed>)allDetailMap.get(key).get("DDb");
                    if(ddb!=null){
                        tempDDb.add(ddb);
                    }
                    allDetailMap.get(key).put("DDb",tempDDb);
                }
            }else{
                Map<String,Object> DDSMap = new HashMap<String,Object>();
                DDSMap.put("DDs", dds);//放入设备
                allDetailMap.put(key, DDSMap);
            }
        }

        /*D类错误校验*/
        //D类错误：上传数据与数据库内查重
        //查询条件list
        List<String> codeNo = new ArrayList<String>(allDetailMap.keySet());
        //获取查询结果list
        List<String> codeNoResults = drawingDetailedService.findRepeatS(codeNo);
        if(codeNoResults.size()!=0){
            hasError = true;
        }
        //更新D类错误信息
        for(String codeNoResult: codeNoResults){//循环查询结果
            hasError = true;
            Map<String,Object> DDSMapItem = allDetailMap.get(codeNoResult);
            DrawingDetailed DDStemp = (DrawingDetailed)DDSMapItem.get("DDs");
            DDStemp.setErrorType("3");//含有D型错误
            DDStemp.setErrorMessage("设备与历史记录冲突请修改设备编号和位号");//在错误信息中添加D类错误
            DDSMapItem.put("DDs", DDStemp);
            allDetailMap.put(codeNoResult, DDSMapItem);
        }
        /*D类错误校验*/

        //转换完毕
        for(String cn: codeNo){
            DrawingDetailed d = (DrawingDetailed)allDetailMap.get(cn).get("DDs");
            //验证WBS合法性
            Project p = null;
            List<Project> pList = new ArrayList<Project>();
            if(NeedToDo.equals("Upload")){
                pList = projectService.selectProjectByCodeSeq(d.getProjectCodeSeq());
            }else{
                Project project = projectService.selectByPk(d.getProjectId());
                pList.add(project);
                List<DrawingDetailed>details = drawingDetailedService.selectByEquipmentId(d.getDrawingDetailedId());
                for(DrawingDetailed detail:details){
                    detail.setProjectId(project.getProjectId());
                    detail.setProjectCodeSeq(project.getProjectCodeSeq());
                    detail.setDrawingNumberCode(d.getDrawingNumberCode());
                    detail.setDrawingNumberId(d.getDrawingNumberId());
                }
                this.variousUpdateList(details);
            }
            if(pList.size()>0){
                p = pList.get(0);
            }
            if(p==null || !p.getProjectState().equals("wbspass")){//校验wbs，因为无影响所以并入错误类型3
                hasError = true;
                d.setErrorMessage("wbs输入有误  " + d.getErrorMessage());
                d.setErrorType("3");
            }else{
                d.setErrorMessage(d.getErrorMessage());
                d.setProjectId(p.getProjectId());
                d.setProjectCodeSeq(p.getProjectCodeSeq());
            }
            if(errorRow.length()>0){
                hasError=true;
                d.setErrorType("3");
                d.setErrorMessage("数据存在完整性错误  " + d.getErrorMessage());
            }
            if(d.getErrorMessage()!=null && d.getErrorMessage().equals("")){
                d.setErrorMessage(null);
                d.setErrorType(null);
            }
            d.setUnitWeight(new BigDecimal("-1"));
            d.setTotalWeight(new BigDecimal("-1"));
            allDetailList.add(d);
            if(NeedToDo.equals("Upload")){
                for(DrawingDetailed ddb: (List<DrawingDetailed>)allDetailMap.get(cn).get("DDb")){
                    ddb.setParentId(d.getDrawingDetailedId());
                    ddb.setDrawingNumberVersion(d.getDrawingNumberVersion());
                    ddb.setProjectId(d.getProjectId());
                    ddb.setTotalCount(ddb.getDesignCount().multiply(d.getTotalCount()));
                    allDetailList.add(ddb);
                }
            }
        }
        if(!hasError){
            mt.setMaterialsTableState("checkPass");
        }
        if(NeedToDo.equals("Upload")){
            materialsTableService.insert(mt);
        }else{
            materialsTableService.update(mt);
        }

        if(NeedToDo.equals("Upload")){
            this.variousInsertList(allDrawingList, allDetailList);
        }else{
            this.variousUpdateList(allDetailList);
        }
        return mt.getMaterialsTableId();
    }

    //检验上传的文件是否使用给定 设备导入 模板填写
    public void checkoutTemplateS(XSSFSheet sheet){
        //确认模板行数与列数
        if(sheet.getLastRowNum()<3 ){
            throw new BusinessException("物资类别和模板不一致，请修改！");
        }else{
            if(sheet.getRow(3).getPhysicalNumberOfCells()<17){
                throw new BusinessException("物资类别和模板不一致，请修改！");
            }
        }
        //检验模板表头内容
        if(sheet.getRow(3) == null ||
                !this.getArrivalDate(sheet.getRow(3).getCell(0)).equals("设计图号") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(1)).equals("版次") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(2)).equals("WBS编码") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(3)).equals("序号") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(4)).equals("设备编号") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(5)).equals("位号") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(6)).equals("设备描述") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(7)).equals("设备数量") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(8)).equals("设备计量单位") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(9)).equals("序号或件号") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(10)).equals("部件编码（图号或标准号）") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(11)).equals("部件描述（规格和材料的合并）") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(12)).equals("部件数量") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(13)).equals("部件计量单位") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(14)).equals("单重") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(15)).equals("总重") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(16)).equals("部件属性") ||
                !this.getArrivalDate(sheet.getRow(3).getCell(17)).equals("备注")){
            throw new BusinessException("物资类别和模板不一致，请修改！");
        }
        //检验表格中是否有数据条目
        boolean isEmpty = true;
        for(int i=4; i<=sheet.getLastRowNum(); i++){
            for(int j=0; j<=17; j++){
                if(sheet.getRow(i)!= null && sheet.getRow(i).getCell(j) != null &&
                        !this.getArrivalDate(sheet.getRow(i).getCell(j)).equals("")){
                    isEmpty = false;
                    break;
                }
            }
            if(!isEmpty){
                break;
            }
        }
        if(isEmpty){
            throw new BusinessException("上传的文件中至少需要存在一条数据！");
        }
    }


}