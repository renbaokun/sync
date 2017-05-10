package com.util.base;

import com.constant.SysInfoConfig;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.util.StringUtil;
import com.vo.CellData;
import com.vo.ContrastConclusion;
import com.vo.RebuildInfo;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @author 作者 E-mail:lipengfei0716@163.com
 * @date 创建时间：2016年11月29日 下午3:59:48
 * @version 1.0
 * @类的说明:
 *
 *
 */
public class PDFUti {


    private static final Logger log = Logger.getLogger(PDFUti.class);

    public static  BaseFont FONT_SIMYOU = null;
    static{
       try {
            FONT_SIMYOU = BaseFont.createFont("//config/SIMYOU.TTF",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }




    // 总方法
    @SuppressWarnings("resource")
    public String pdfMethod(HttpServletRequest request,
                            List<List<String>> baseInfoTable,//baseInfoTable
                            HttpServletResponse response,
                            List<List<String>> popularInfoTable, //popularInfoTable
                            List<List<String>> detailsInfoTable,// detailsInfoTable
                            String Svalue,//水印信息
                            String pdfTitle,//title   移动互联网业务品质智能测评系统 (V_0.8.5_alpha)
                            String pcapFileName,//[aqydelay.pcap]
                            String username,//username
                            List<List<String>> invalidIpTable,//无效ip集合(invalidIpTable)
                            List<RebuildInfo> rebuildInfos //频繁拆建链信息集合
                          ) throws Exception {
        String template = new File(this.getClass().getClassLoader().getResource("").getPath()).toString();
        String templatePath = template + "//config//word1.pdf";

        @SuppressWarnings("deprecation")
        String webpath = request.getRealPath("/");
        String yemei = webpath + "design//img//yemei.png";
        String yejiao = webpath + "design//img//yejiao.png";
        String echartsPath = webpath + "Echarts/"+username;
        String errorImg = webpath + "design//img//home_form3//";
        String disposeImg = webpath + "design//img//";

        String currUserPdfDir=webpath+ "tempPdf//"+username+"//";
        File pdfDir = new File(currUserPdfDir);
        if(pdfDir.exists()){
            pdfDir.delete();
        }
        pdfDir.mkdirs();

        String tempPDFPath =currUserPdfDir+"tempPdf.pdf";// 生成的新文件路径转换的;
        String finalPdfPath = currUserPdfDir+pcapFileName + "的测试报告.pdf";// 最后的文件名字;

        PdfReader templateReader = new PdfReader(templatePath);// 读取模板
        ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();// 预存（后面合并pdf需要使用）
        PdfStamper stamp = new PdfStamper(templateReader, tempBaos);// 读取到的模板放入预存中
        AcroFields fields = stamp.getAcroFields();
        Map<String, String> data = data(pdfTitle, pcapFileName, createDate(), username,null);
        fillPdfTemplateForm(fields, data,FONT_SIMYOU);//填充顶部版本等原信息
        stamp.setFormFlattening(true);
        templateReader.close();
        stamp.close();

        Document doc = new Document(templateReader.getPageSize(1));
        ByteArrayOutputStream detaialBaos = new ByteArrayOutputStream();
        PdfWriter baseWriter = PdfWriter.getInstance(doc, detaialBaos);

        setHeaderAndFooter(yemei,yejiao,doc);//设置页眉页脚图片

        // 图形(表格table)绘制
        newgenOtherTable(request,username,pcapFileName, baseInfoTable, response, popularInfoTable, detailsInfoTable, doc, errorImg, invalidIpTable, rebuildInfos);

        //图表(图片)添加
        ByteArrayOutputStream[] baos_linkImgs = addEchartsImg(pcapFileName, echartsPath, doc, yemei, yejiao, baseWriter);

        doc.close();

        OutputStream outputStream = new FileOutputStream(tempPDFPath);// 合并模板与其它页，并生成最后报表

        List<ByteArrayOutputStream> list = new ArrayList<ByteArrayOutputStream>();
        list.add(tempBaos);
        list.add(detaialBaos);

        if(baos_linkImgs!=null&&baos_linkImgs.length>0){
            for(int i=0;i<baos_linkImgs.length;i++){
                list.add(baos_linkImgs[i]);
            }
        }

        mergePdfFiles(list,outputStream);
        addShYin(tempPDFPath, Svalue, finalPdfPath,request);//加入水印信息

        File file = new File(tempPDFPath);
        file.delete();

        return finalPdfPath;
    }


    //"视频比较"或"基本信息比较"
    public String pdfMethod_Compare(HttpServletRequest request,
                                    List<List<List<String>>> dataListList,//baseInfoTable
                                    HttpServletResponse response,
                                    List<List<List<String>>> dataList1List, //popularInfoTable
                                    List<List<List<String>>> dataList2List,// detailsInfoTable
                                    String Svalue,//水印信息
                                    List<String> pdfTitleList,//title
                                    String[] pcapFileNames,
                                    String username,//username
                                    List<List<List<String>>> dataList3List,//detailsInfoOthersList(无效ip列表)
                                    List<List<RebuildInfo>> rebuildInfosList/*,
                                    List<List<String>> kindInfoList11,
                                    List<Map<String, Object>> basicConclusionList111,//基础结论比较
                                    List<Map<String, Object>> fileDisposeList111,//报文处理概述比较
                                    ContrastConclusion contrastConclusion111*/) throws Exception {
        String template = new File(this.getClass().getClassLoader().getResource("").getPath()).toString();
        String templatePath = template + "//config//word1.pdf";

        @SuppressWarnings("deprecation")
        String webpath = request.getRealPath("/");
        String yemei = webpath + "design//img//yemei.png";
        String yejiao = webpath + "design//img//yejiao.png";
        String echartPath = webpath + "Echarts/"+username;
        String errorImg = webpath + "design//img//home_form3//";
        String disposeImg = webpath + "design//img//";

        String currUserPdfDir=webpath+ "tempPdf//"+username+"//";
        File pdfDir = new File(currUserPdfDir);
        if(pdfDir.exists()){
            pdfDir.delete();
        }
        pdfDir.mkdirs();

        String tempPDFPath = currUserPdfDir+"tempPdf.pdf";// 生成的新文件路径转换的;
        String finalPdfPath = currUserPdfDir+pcapFileNames[0]+"和" +pcapFileNames[1]+ "的对比测试报告.pdf";// 最后的文件名字;

        // 读取模板
        PdfReader templateReader = new PdfReader(templatePath);
        // 预存
        ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
        // 读取到的模板放入预存中
        PdfStamper stamp = new PdfStamper(templateReader, tempBaos);

        @SuppressWarnings("unused")
        PdfContentByte under = stamp.getUnderContent(1);


        //填充顶部模板版本等字段信息
        AcroFields fields = stamp.getAcroFields();
        Map<String, String> data = data(pdfTitleList.get(0),pcapFileNames[0]+" & "+pcapFileNames[1],createDate(),username,null);
        fillPdfTemplateForm(fields, data,FONT_SIMYOU);

        stamp.setFormFlattening(true);
        templateReader.close();
        stamp.close();

        @SuppressWarnings("unused")
        int totalPage = 0;

        Document doc = new Document(templateReader.getPageSize(1));
        ByteArrayOutputStream detaialBaos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, detaialBaos);

        doc.setPageCount(2);
        setHeaderAndFooter(yemei,yejiao,doc);//设置页眉页脚图片

        Color color = new Color(165, 42, 42);

        Paragraph para=null;
        
       /* 报文处理概述----对比
        doc.newPage();
        renderFileDisposeInfo(doc,disposeImg, fileDisposeList);//"报文处理概述"渲染
        	基本结论----对比
        doc.newPage();
        renderBasicConclusionInfo(doc, basicConclusionList, errorImg);//"基本结论"渲染
        renderContrastConclusionInfo_hsy(doc,contrastConclusion,errorImg);//"对比结论"渲染*/        
        
        String imgPath_gaishu = webpath + "xxxx/"+username+"/gaishu.png";
        String imgPath_jielun = webpath + "xxxx/"+username+"/jielun.png";
        ByteArrayOutputStream baos_gaishu = renderGaishu_newDoc(doc, imgPath_gaishu, yemei, yejiao);
        ByteArrayOutputStream baos_conclusionInfo = renderConclusionInfo_newdoc(doc, imgPath_jielun, yemei, yejiao);
        
        //*******************************************视频业务对比开始*******************************************
      /*  if(timeToFirstTable!=null&&timeToFirstTable.size()>1){
            doc.newPage();
            para = new Paragraph(new Chunk("视频业务对比",new Font(FONT_SIMYOU,16f)));
            doc.add(para);

            renderTtFTable(doc, timeToFirstTable);//绘制"TtFS"和"TtFR"的表格
            renderTtFComment(doc,timeToFirstTable.get(1).get(1),
                    timeToFirstTable.get(2).get(1),
                    timeToFirstTable.get(1).get(2),
                    timeToFirstTable.get(2).get(2),
                    timeToFirstTable.get(1).get(0),
                    timeToFirstTable.get(2).get(0));//绘制绘制"TtFS"和"TtFR"表格下面的说明(包括：TtFs和TtFr相差百分比)

            //绘制各自的kindinfo信息
            Paragraph paragraph1=null;
            for(int i=0;i<2;i++){
                paragraph1= new Paragraph(new Chunk(pcapFileNames[i],new Font(FONT_SIMYOU,16f)));
                paragraph1.setIndentationLeft(80f);
                doc.add(paragraph1);
                //分隔线(有时间再实现)
                List<String> kindInfo = kindInfoList.get(i);
                renderKindInfoMsg(doc, kindInfo, errorImg);//绘制"kindInfo"信息
            }
            paragraph1 = new Paragraph(new Chunk("服务器不支持TCP  WINDOW  SCALE  OPTION参数，会影响下载速率。",new Font(FONT_SIMYOU,10,Font.NORMAL, new Color(165, 42, 42))));
            paragraph1.setIndentationLeft(90f);
            doc.add(paragraph1);
        }
        log.info("\"视频业务比较\"绘制完毕!");*/
        //*******************************************视频业务对比结束*******************************************


        //*******************************************基本信息比较开始*******************************************
       /* if(baseInfoCompareResult!=null&&baseInfoCompareResult.size()>0){//"基本信息比较"表格上面的"比较结果"信息说明
            doc.newPage();
            para = new Paragraph(new Chunk("基本信息对比",new Font(FONT_SIMYOU,16f)));
            doc.add(para);
            para = new Paragraph(new Chunk("对比结果",new Font(FONT_SIMYOU,13f)));
            para.setIndentationLeft(80f);
            doc.add(para);

            int fsize=10;

            para = new Paragraph(new Chunk("1 ) 交互时间",new Font(FONT_SIMYOU,fsize)));
            para.setIndentationLeft(85f);
            doc.add(para);
            if(baseInfoCompareResult.get("commuTime").equals("N/A")){
                para = new Paragraph(new Chunk("N/A",new Font(FONT_SIMYOU,fsize)));
            }else{
                para = new Paragraph(new Chunk("基础数据 "+baseInfoCompareResult.get("commuTime")+" 竞品数据",new Font(FONT_SIMYOU,fsize)));
            }
            para.setIndentationLeft(85f);
            doc.add(para);

            para = new Paragraph(new Chunk("2 ) 交互流量",new Font(FONT_SIMYOU,fsize)));
            para.setIndentationLeft(85f);
            doc.add(para);
            if(baseInfoCompareResult.get("commuFlow").equals("N/A")){
                para = new Paragraph(new Chunk("N/A",new Font(FONT_SIMYOU,fsize)));
            }else{
                para = new Paragraph(new Chunk("基础数据 "+baseInfoCompareResult.get("commuFlow")+" 竞品数据",new Font(FONT_SIMYOU,fsize)));
            }
            para.setIndentationLeft(85f);
            doc.add(para);

            para = new Paragraph(new Chunk("3 ) ip数",new Font(FONT_SIMYOU,fsize)));
            para.setIndentationLeft(85f);
            doc.add(para);
            if(baseInfoCompareResult.get("ipNum").equals("N/A")){
                para = new Paragraph(new Chunk("N/A",new Font(FONT_SIMYOU,fsize)));
            }else{
                para = new Paragraph(new Chunk("基础数据 "+baseInfoCompareResult.get("ipNum")+" 竞品数据",new Font(FONT_SIMYOU,fsize)));
            }
            para.setIndentationLeft(85f);
            doc.add(para);

            para = new Paragraph(new Chunk("4 ) 链路个数",new Font(FONT_SIMYOU,fsize)));
            para.setIndentationLeft(85f);
            doc.add(para);
            if(baseInfoCompareResult.get("linkNum").equals("N/A")){
                para = new Paragraph(new Chunk("N/A",new Font(FONT_SIMYOU,fsize)));
            }else{
                para = new Paragraph(new Chunk("基础数据 "+baseInfoCompareResult.get("linkNum")+" 竞品数据",new Font(FONT_SIMYOU,fsize)));
            }
            para.setIndentationLeft(85f);
            doc.add(para);

            para = new Paragraph(new Chunk("5 ) 链路效率",new Font(FONT_SIMYOU,fsize)));
            para.setIndentationLeft(85f);
            doc.add(para);
            if(baseInfoCompareResult.get("timeEfficiencyAVG").equals("N/A")){
                para = new Paragraph(new Chunk("N/A",new Font(FONT_SIMYOU,fsize)));
            }else{
                para = new Paragraph(new Chunk("基础数据 "+baseInfoCompareResult.get("timeEfficiencyAVG")+" 竞品数据",new Font(FONT_SIMYOU,fsize)));
            }
            para.setIndentationLeft(85f);
            doc.add(para);
        }

        if(baseInfoCompareList!=null&&baseInfoCompareList.size()>1){//'基本信息比较'表格
            renderBaseInfoCompareTable(doc,baseInfoCompareList);
        }
        log.info("\"基本信息比较\"绘制完毕!");*/
        //*******************************************基本信息比较结束*******************************************
        //然后是2个pcap文件各自的"明细信息"的绘制
        for(int i=0;i<2;i++){
            doc.newPage();
            String pcapFileName=pcapFileNames[i];//显示当前pcap测试文件的name
            Paragraph paragraph = new Paragraph();
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(new Phrase(new Chunk(pcapFileName,new Font(FONT_SIMYOU,20f))));
            doc.add(paragraph);

            paragraph=new Paragraph();
            paragraph.add(new Phrase(new Chunk("明细数据:",new Font(FONT_SIMYOU, 16f))));
            doc.add(paragraph);
            doc.add(new Chunk("\n\n"));

            renderBaseInfoTable(doc, dataListList.get(i));//绘制“基本信息”表格
            renderRebuildInfoMsg(doc, rebuildInfosList.get(i), errorImg);//绘制"频繁拆建链"信息
            renderPopularInfoTable(doc,dataList1List.get(i));//绘制"通用信息"表格
            renderDetailsInfoTable(doc,dataList2List.get(i));//绘制"详细信息"表格
            renderInvalidIPTable(doc,dataList3List.get(i));//绘制"无效IP"表格
            log.info("文件："+pcapFileName+"\"明细信息\"绘制完毕!");
        }

        //图表(图片)添加
        ByteArrayOutputStream[] baos_linkImgs = addEchartsImg_compare(pcapFileNames[0], pcapFileNames[1], echartPath, doc, yemei, yejiao, writer);

        doc.close();
        totalPage = writer.getPageNumber();

        OutputStream outputStream = new FileOutputStream(tempPDFPath);

        List<ByteArrayOutputStream> list = new ArrayList<ByteArrayOutputStream>();
        list.add(tempBaos);
        if(baos_gaishu!=null){
        	list.add(baos_gaishu);
        }
        if(baos_conclusionInfo!=null){
        	list.add(baos_conclusionInfo);
        }
        list.add(detaialBaos);

        if(baos_linkImgs!=null&&baos_linkImgs.length>0){
            for(int i=0;i<baos_linkImgs.length;i++){
                list.add(baos_linkImgs[i]);
            }
        }

        mergePdfFiles(list,outputStream);

        addShYin(tempPDFPath, Svalue, finalPdfPath,request);//加入水印信息

        File file = new File(tempPDFPath);
        file.delete();

        return finalPdfPath;
    }
    
    public String pdfMethod_whenCompare_byFtl(HttpServletRequest request,
									    		HttpServletResponse response,
									    		String Svalue,//水印信息
									    		String title,//title
									    		String baseFileName,
									    		String jingpineFileName,
									    		String username,
									    		String userAgent_baseFile,
									    		String userAgent_jingpinFile) throws Exception {
		    	String template = new File(this.getClass().getClassLoader().getResource("").getPath()).toString();
		    	//String templatePath = template + "//config//word2.pdf";
		    	String templatePath = template + "//config//word3.pdf";
		    	
		    	@SuppressWarnings("deprecation")
		    	String webpath = request.getRealPath("/");
		    	String yemei = webpath + "design//img//yemei.png";
		    	String yejiao = webpath + "design//img//yejiao.png";
		    	String echartPath = webpath + "Echarts/"+username;
		    	String errorImg = webpath + "design//img//home_form3//";
		    	String disposeImg = webpath + "design//img//";
		    	String currUserPdfDir=webpath+ "tempPdf//"+username+"//";
		    	
		    	String tempPDFPath = currUserPdfDir+"tempPdf.pdf";// 生成的新文件路径转换的;
		    	String finalPdfPath = currUserPdfDir+baseFileName+"和" +jingpineFileName+ "的对比测试报告.pdf";// 最后的文件名字;
		    	
		    	
		    	//****************************模板读入内存begin****************************
		    	PdfReader reader_template = new PdfReader(templatePath);//模板读取器
		    	Rectangle pageSize = reader_template.getPageSize(1);
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("模板：getWidth="+pageSize.getWidth());
		    	System.out.println("模板：getHeight="+pageSize.getHeight());
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	
		    	ByteArrayOutputStream baos_template = new ByteArrayOutputStream();// 将模板读取到内存中
		    	PdfStamper stamper_template = new PdfStamper(reader_template, baos_template);
		    	
		    	@SuppressWarnings("unused")
		    	PdfContentByte under = stamper_template.getUnderContent(1);
		    	//填充顶部模板版本等字段信息
		    	AcroFields fields = stamper_template.getAcroFields();
		    	Map<String, String> data = data(title,baseFileName+" & "+jingpineFileName,createDate(),username,userAgent_baseFile+" & "+userAgent_jingpinFile);
		    	fillPdfTemplateForm(fields, data,FONT_SIMYOU);
		    	
		    	stamper_template.setFormFlattening(true);
		    	reader_template.close();
		    	stamper_template.close();
		    	//****************************模板读入内存end****************************
		    	
		    	
		    	
		    	//****************************导出的ftl(详细信息)读入内存begin****************************
		    	int pageNum = addFooterImgAndPagination(currUserPdfDir+"details_whenCompare.pdf", currUserPdfDir+"details_whenCompare_.pdf", 3,yejiao);
				
				
		    	PdfReader reader_ftl = new PdfReader(currUserPdfDir+"details_whenCompare_.pdf");
		    	
		    	
		    	Rectangle pageSize2 = reader_ftl.getPageSize(1);
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("我的：getWidth="+pageSize2.getWidth());
		    	System.out.println("我的：getHeight="+pageSize2.getHeight());
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	System.out.println("*******************************");
		    	
		    	
		    	ByteArrayOutputStream baos_ftl = new ByteArrayOutputStream();
		    	PdfStamper stamper_ftl = new PdfStamper(reader_ftl, baos_ftl);
		    	reader_ftl.close();
		    	stamper_ftl.close();
		    	File file = new File(currUserPdfDir+"details_whenCompare_.pdf");
		    	if(file!=null&&file.exists()){
		    			file.delete();
		    	}
		    	//****************************导出的ftl(详细信息)读入内存end****************************
		    	
		    	
		    	
		    	//****************************echarts读入内存begin****************************
		    	Document doc_echarts = new Document(reader_template.getPageSize(1));
		    	ByteArrayOutputStream baos_echarts = new ByteArrayOutputStream();
		    	PdfWriter writer_echarts = PdfWriter.getInstance(doc_echarts, baos_echarts);
		    	doc_echarts.setPageCount(pageNum-1);
		    	setHeaderAndFooter(yemei,yejiao,doc_echarts);//设置页眉页脚图片
		    	//图表(图片)添加
		    	ByteArrayOutputStream[] baos_linkImgs = addEchartsImg_compare(baseFileName, jingpineFileName, echartPath, doc_echarts, yemei, yejiao, writer_echarts);
		    	doc_echarts.close();
		    	//****************************echarts读入内存end****************************
		    	
		    	
		    	
		    	List<ByteArrayOutputStream> list = new ArrayList<ByteArrayOutputStream>();
		    	list.add(baos_template);
		    	list.add(baos_ftl);
		    	list.add(baos_echarts);
		    	
		    	if(baos_linkImgs!=null&&baos_linkImgs.length>0){
			    		for(int i=0;i<baos_linkImgs.length;i++){
			    				list.add(baos_linkImgs[i]);
			    		}
		    	}
		    	mergePdfFiles(list,new FileOutputStream(tempPDFPath));
		    	addShYin(tempPDFPath, Svalue, finalPdfPath,request);//加入水印信息
		    	return finalPdfPath;
    }
    
    
    public String pdfMethod_single_byFtl(HttpServletRequest request,
								    		HttpServletResponse response,
								    		String Svalue,//水印信息
								    		String title,//title
								    		String fileName,
								    		String username,
								    		String userAgent) throws Exception {
			String template = new File(this.getClass().getClassLoader().getResource("").getPath()).toString();
//			String templatePath = template + "//config//word2.pdf";
			String templatePath = template + "//config//word3.pdf";
			
			@SuppressWarnings("deprecation")
			String webpath = request.getRealPath("/");
			String yemei = webpath + "design//img//yemei.png";
			String yejiao = webpath + "design//img//yejiao.png";
			String echartPath = webpath + "Echarts/"+username;
			String errorImg = webpath + "design//img//home_form3//";
			String disposeImg = webpath + "design//img//";
			String currUserPdfDir=webpath+ "tempPdf//"+username+"//";
			
			String tempPDFPath = currUserPdfDir+"tempPdf.pdf";// 生成的新文件路径转换的;
			String finalPdfPath = currUserPdfDir+fileName+ "的测试报告.pdf";// 最后的文件名字;
			
			
			//****************************模板读入内存begin****************************
			PdfReader reader_template = new PdfReader(templatePath);//模板读取器
			ByteArrayOutputStream baos_template = new ByteArrayOutputStream();// 将模板读取到内存中
			PdfStamper stamper_template = new PdfStamper(reader_template, baos_template);
			
			@SuppressWarnings("unused")
			PdfContentByte under = stamper_template.getUnderContent(1);
			//填充顶部模板版本等字段信息
			AcroFields fields = stamper_template.getAcroFields();
			Map<String, String> data = data(title,fileName,createDate(),username,userAgent);
			fillPdfTemplateForm(fields, data,FONT_SIMYOU);
			
			stamper_template.setFormFlattening(true);
			reader_template.close();
			stamper_template.close();
			//****************************模板读入内存end****************************
			
			//****************************导出的ftl(详细信息)读入内存begin****************************
			int pageNum = addFooterImgAndPagination(currUserPdfDir+"details_single_"+fileName+".pdf", currUserPdfDir+"details_single_"+fileName+"_.pdf", 3,yejiao);
			
			PdfReader reader_ftl = new PdfReader(currUserPdfDir+"details_single_"+fileName+"_.pdf");
			ByteArrayOutputStream baos_ftl = new ByteArrayOutputStream();
			PdfStamper stamper_ftl = new PdfStamper(reader_ftl, baos_ftl);
			reader_ftl.close();
			stamper_ftl.close();
			File file = new File(currUserPdfDir+"details_single_"+fileName+"_.pdf");//读入内存后将此临时文件删除
			if(file!=null&&file.exists()){
				file.delete();
			}
			//****************************导出的ftl(详细信息)读入内存end****************************
			
			
			
			//****************************echarts读入内存begin****************************
			Document doc_echarts = new Document(reader_template.getPageSize(1));
			ByteArrayOutputStream baos_echarts = new ByteArrayOutputStream();
			PdfWriter writer_echarts = PdfWriter.getInstance(doc_echarts, baos_echarts);
			doc_echarts.setPageCount(pageNum-1);
			setHeaderAndFooter(yemei,yejiao,doc_echarts);//设置页眉页脚图片
			ByteArrayOutputStream[] baos_linkImgs = addEchartsImg(fileName, echartPath, doc_echarts, yemei, yejiao, writer_echarts);
			doc_echarts.close();
			//****************************echarts读入内存end****************************
			
			
			
			List<ByteArrayOutputStream> list = new ArrayList<ByteArrayOutputStream>();
			list.add(baos_template);
			list.add(baos_ftl);
			list.add(baos_echarts);
			
			if(baos_linkImgs!=null&&baos_linkImgs.length>0){
				for(int i=0;i<baos_linkImgs.length;i++){
					list.add(baos_linkImgs[i]);
				}
			}
			mergePdfFiles(list,new FileOutputStream(tempPDFPath));
			addShYin(tempPDFPath, Svalue, finalPdfPath,request);//加入水印信息
			return finalPdfPath;
}

    

    //"结论概述"渲染	
    private void renderConclusionInfo(Document doc,String imgPath) throws MalformedURLException, IOException, DocumentException {
	    	Image img_conclusionInfo = getImage(imgPath);
	    	doc.newPage();
	        Paragraph paragraph = new Paragraph();
	        paragraph.add(img_conclusionInfo);
	        doc.add(paragraph);
	}
    private ByteArrayOutputStream renderConclusionInfo_newdoc(Document doc,String imgPath,String yemei,String yejiao) throws MalformedURLException, IOException, DocumentException {
	      Image img_conclusionInfo = getImage(imgPath);
	      float currPageHeight=img_conclusionInfo.getHeight()*0.69f+150f;
	      float currPageWidth=img_conclusionInfo.getWidth()*0.69f+150f;
	    //  img_conclusionInfo.scaleToFit(1000f, 1000f);
	      img_conclusionInfo.scalePercent(69f);
	      
         // Document newDoc = new Document(new Rectangle(doc.getPageSize().getWidth(),currPageHeight));
          Document newDoc = new Document(new Rectangle(currPageWidth,currPageHeight));
          ByteArrayOutputStream newBaos = new ByteArrayOutputStream();
          PdfWriter.getInstance(newDoc, newBaos);
        //  System.out.println(baseWriter.getPageNumber());
          newDoc.setPageCount(1);
          setHeaderAndFooter(yemei,yejiao,newDoc);//设置页眉页脚图片//发现页号是此行代码加上的(注释掉就没有了)
          newDoc.newPage();
          newDoc.add(new Paragraph());
          newDoc.add(img_conclusionInfo);
          newDoc.close();
          return newBaos;
    }
    //"报文处理概述"渲染	
    private void renderGaishu(Document doc,String imgPath) throws MalformedURLException, IOException, DocumentException {
    	Image img_gaishu = getImage(imgPath);
    	doc.newPage();
    	Paragraph paragraph = new Paragraph();
    	paragraph.add(img_gaishu);
    	doc.add(paragraph);
    }
    
    private ByteArrayOutputStream renderGaishu_newDoc(Document doc,String imgPath,String yemei,String yejiao) throws MalformedURLException, IOException, DocumentException {
	        Image img_gaishu = getImage(imgPath);
	        float currPageHeight=img_gaishu.getHeight()*0.69f+150f;
	       // img_gaishu.scaleToFit(1000f, 1000f);
	        img_gaishu.scalePercent(69f);
	        
	        Document newDoc = new Document(new Rectangle(doc.getPageSize().getWidth(),currPageHeight));
	        ByteArrayOutputStream newBaos = new ByteArrayOutputStream();
	        PdfWriter.getInstance(newDoc, newBaos);
	     //   int pageNumber = baseWriter.getPageNumber();
	        newDoc.setPageCount(0);
	        setHeaderAndFooter(yemei,yejiao,newDoc);//设置页眉页脚图片//发现页号是此行代码加上的(注释掉就没有了)
	        newDoc.newPage();
	        newDoc.add(new Paragraph());
	        newDoc.add(img_gaishu);
	        newDoc.close();
	        return newBaos;
  }


	private void renderContrastConclusionInfo(Document doc, ContrastConclusion contrastConclusion, String errorImg) throws DocumentException {
        // TODO Auto-generated method stub
        Font font = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.black);
        Paragraph paragraph;
        paragraph = new Paragraph();
        paragraph.setIndentationLeft(80f);

        if (!StringUtil.isEmpty(contrastConclusion.getTitle())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getTitle(), new Font(FONT_SIMYOU, 12f))));
            doc.add(paragraph);
            doc.add(new Chunk("\n", new Font( FONT_SIMYOU, 16f)));
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getEffLinkTitle())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getEffLinkTitle(), new Font(FONT_SIMYOU, 12f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getLowestEffLink())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getLowestEffLink(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getExchangeTimeTitle())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getExchangeTimeTitle(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getExchangeTimeSecondTitle())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getExchangeTimeSecondTitle(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getDnsBigIp())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getDnsBigIp(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getTcpTimeDelayed())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getTcpTimeDelayed(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getTimeToFirst())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getTimeToFirst(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getOffTime())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getOffTime(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }
        if (!StringUtil.isEmpty(contrastConclusion.getRttTime())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getRttTime(), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);
            paragraph.clear();
        }

        List<List<String>> dataList = contrastConclusion.getTable1();
        List<List<String>> dataList2 = contrastConclusion.getTable2();
        List<List<String>> dataList3 = contrastConclusion.getTable3();
        Table table= new Table(dataList.get(0).size());//列数
        // 创建一个表格
        table.setPadding(2f);
        Color bgcolor_title = new Color(161, 219, 246, 1);
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = 0; j < dataList.get(i).size(); j++) {
                Cell cell = new Cell(new Chunk(dataList.get(i).get(j).toString(),font));
                if (i==0) {
                    cell.setBackgroundColor(bgcolor_title);
                }
                table.addCell(cell);
            }
        }
        table.setBorderWidth(1f);
        doc.add(table);
        if (!StringUtil.isEmpty(contrastConclusion.getPacketLenData())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getPacketLenData(), new Font(FONT_SIMYOU, 12f))));
            doc.add(paragraph);
            paragraph.clear();
            doc.add(new Chunk("\n", new Font( FONT_SIMYOU, 16f)));
            table= new Table(dataList2.get(0).size());//列数
            // 创建一个表格
            table.setPadding(2f);
            for (int i = 0; i < dataList2.size(); i++) {
                for (int j = 0; j < dataList2.get(i).size(); j++) {
                    Cell cell = new Cell(new Chunk(dataList2.get(i).get(j).toString(),font));
                    if (i==0) {
                        cell.setBackgroundColor(bgcolor_title);
                    }
                    table.addCell(cell);
                }
            }
            table.setBorderWidth(1f);
            doc.add(table);
        }
        if (!StringUtil.isEmpty(contrastConclusion.getTcpCount())) {
            paragraph.add(new Phrase(new Chunk(contrastConclusion.getTcpCount(), font)));
            doc.add(paragraph);
            doc.add(new Chunk("\n", new Font( FONT_SIMYOU, 16f)));
            table= new Table(dataList3.get(0).size());//列数
            // 创建一个表格
            table.setPadding(2f);
            for (int i = 0; i < dataList3.size(); i++) {
                for (int j = 0; j < dataList3.get(i).size(); j++) {
                    Cell cell = new Cell(new Chunk(dataList3.get(i).get(j).toString(),font));
                    if (i==0) {
                        cell.setBackgroundColor(bgcolor_title);
                    }
                    table.addCell(cell);
                }
            }
            table.setBorderWidth(1f);
            doc.add(table);
        }
    }
    
    //“对比结论”绘制
    private void renderContrastConclusionInfo_hsy(Document doc, ContrastConclusion contrastConclusion, String errorImg) throws DocumentException, MalformedURLException, IOException {
    		Font font = new Font(FONT_SIMYOU,10,Font.NORMAL, Color.black);
    		Image img_cuo = Image.getInstance(errorImg+"cuo.jpg");
    		img_cuo.scaleToFit(1000f, 1000f);
    		img_cuo.scalePercent(55f);
    		img_cuo.setAlignment(Element.ALIGN_LEFT);
    		img_cuo.setIndentationLeft(80f);
            
    		Paragraph paragraph=null;
    		doc.newPage();
    		doc.add(new Chunk("对比结论:", new Font(FONT_SIMYOU, 14f)));
    		String title1 = contrastConclusion.getTitle1();
    		if(title1!=null&&title1.length()>0){
    				paragraph=new Paragraph(title1,font);
    				paragraph.setIndentationLeft(80f);
    				doc.add(paragraph);
    				
    				paragraph=new Paragraph(contrastConclusion.getTitle2()==null?"":contrastConclusion.getTitle2(),font);
    				paragraph.setIndentationLeft(80f);
    				doc.add(paragraph);
    				
    				paragraph=new Paragraph(contrastConclusion.getTitle3()==null?"":contrastConclusion.getTitle3(),font);
    				paragraph.setIndentationLeft(80f);
    				doc.add(paragraph);
    				
    				String title3_1 = contrastConclusion.getTitle3_1();
    				if(title3_1!=null&&title3_1.length()>0){
    						paragraph=new Paragraph();
    						paragraph.setIndentationLeft(120f);
	    					Table table = new Table(20);
	    					table.setBorder(0);
	    					table.setAlignment(Element.ALIGN_LEFT);
	    					Cell cell=new Cell(img_cuo);
	    					cell.setColspan(1);
	    					cell.setBorder(0);
	        				table.addCell(cell);
	        				
	        				cell=new Cell(new Chunk(title3_1,font));
	        				cell.setColspan(19);
	        				cell.setBorder(0);
	        				table.addCell(cell);
	        				paragraph.add(table);
	        				doc.add(paragraph);
    				}
    				
    				String title3_2 = contrastConclusion.getTitle3_2();
    				if(title3_2!=null&&title3_2.length()>0){
	    					paragraph=new Paragraph();
							paragraph.setIndentationLeft(120f);
    						Table table = new Table(20);
    						table.setBorder(0);
    						table.setAlignment(Element.ALIGN_LEFT);
    						Cell cell=new Cell(img_cuo);
    						cell.setBorder(0);
    						cell.setColspan(1);
	        				table.addCell(cell);
	        				
	        				cell=new Cell(new Chunk(title3_2,font));
	        				cell.setColspan(19);
	        				cell.setBorder(0);
	        				table.addCell(cell);
	        				paragraph.add(table);
	        				doc.add(paragraph);
	        				
	        				List<String> list = contrastConclusion.getList_title3_2();
	        				if(list!=null&&list.size()>0){
	        							for (String string : list) {
	        									paragraph= new Paragraph(string,font);
	        									paragraph.setIndentationLeft(160f);
	        									doc.add(paragraph);
	    								}
	        				}
    				}
    				//有一个table忘了添加
    				if(!contrastConclusion.getTable_title3().isEmpty())
    					renderSimple2DimensionTable_cellData(doc, contrastConclusion.getTable_title3(),120f);
    				
    				String title3_3 = contrastConclusion.getTitle3_3();
    				if(title3_3!=null&&title3_3.length()>0){
	    					paragraph=new Paragraph();
							paragraph.setIndentationLeft(120f);
	    					Table table = new Table(20);
	    					table.setBorder(0);
	    					table.setAlignment(Element.ALIGN_LEFT);
	    					Cell cell = new Cell(img_cuo);
	    					cell.setBorder(0);
	    					cell.setColspan(1);
	        				table.addCell(cell);
	        				
	        				cell=new Cell(new Chunk(title3_3,font));
	        				cell.setColspan(19);
	        				cell.setBorder(0);
	        				table.addCell(cell);
	        				paragraph.add(table);
	        				doc.add(paragraph);
	        				
	        				renderSimple2DimensionTable(doc, contrastConclusion.getTable_title3_3(),120f);
    				}
    				
    				String title3_4 = contrastConclusion.getTitle3_4();
    				if(title3_4!=null&&title3_4.length()>0){
	    					paragraph=new Paragraph();
							paragraph.setIndentationLeft(120f);
	    					Table table = new Table(20);
	    					table.setBorder(0);
	    					table.setAlignment(Element.ALIGN_LEFT);
	    					Cell cell=new Cell(img_cuo);
	    					cell.setBorder(0);
	    					cell.setColspan(1);
	        				table.addCell(cell);
	        				
	        				cell=new Cell(new Chunk(title3_4,font));
	        				cell.setColspan(19);
	        				cell.setBorder(0);
	        				table.addCell(cell);
	        				paragraph.add(table);
	        				doc.add(paragraph);
	        				if(!contrastConclusion.getTable_title3_4().isEmpty())
	        					renderSimple2DimensionTable_cellData(doc, contrastConclusion.getTable_title3_4(),120f);
    				}
    		}
    }


    private void renderBaseInfoCompareTable(Document doc, List<List<String>> baseInfoCompareList) throws DocumentException, IOException {
        renderSimple2DimensionTable(doc, baseInfoCompareList);
    }


    /**
     *
     四张图片时
     */
    private ByteArrayOutputStream[] addEchartsImg(String pcapFileName,String echartsFolderPath,Document doc, String yemei, String yejiao,PdfWriter baseWriter) throws MalformedURLException, IOException, DocumentException {//写入4个图表图片
        Image image_aipFlow = getImage(echartsFolderPath+"/aipFlow/"+pcapFileName+".png");
        Image image_bpacketLen =getImage(echartsFolderPath+"/bpacketLen/"+pcapFileName+".png");;
        Image image_cuseDetail= getImage(echartsFolderPath+"/cuse2Detail/"+pcapFileName+".png");;
        Image image_zlink =getImage(echartsFolderPath+"/zlink/"+pcapFileName+".png");;
        if (image_aipFlow != null) {
            doc.newPage();
            doc.add(new Paragraph());
            doc.add(new Chunk("图表展示:", new Font( FONT_SIMYOU, 16f)));
            doc.add(new Chunk("\n\n\n"));
            doc.add(new Chunk("IP流量分布图", new Font(FONT_SIMYOU, 14f)));
            doc.add(new Chunk("\n"));
            doc.add(image_aipFlow);
        }
        if (image_bpacketLen != null) {
            doc.newPage();
            doc.add(new Chunk("包长分布图", new Font(FONT_SIMYOU, 14f)));
            doc.add(new Chunk("\n"));
            doc.add(image_bpacketLen);
        }
        if (image_cuseDetail != null) {
            doc.newPage();
            doc.add(new Chunk("吞吐曲线图", new Font(FONT_SIMYOU, 14f)));
            doc.add(new Chunk("\n"));
            doc.add(image_cuseDetail);
        }
        if (image_zlink != null) {//因为"链路图"肯能很高，所以要单独生成一个pdf(合并),设置pageHeight,
            if((image_zlink.getHeight()*0.69f+150)>doc.getPageSize().getHeight()){//链路图过高
                image_zlink.scalePercent(69f);
                Document doc_linkImg = new Document(new Rectangle(doc.getPageSize().getWidth(),image_zlink.getHeight()*0.69f+150f));
                ByteArrayOutputStream baos_linkImg = new ByteArrayOutputStream();
                PdfWriter.getInstance(doc_linkImg, baos_linkImg);
                doc_linkImg.setPageCount(baseWriter.getPageNumber());
                setHeaderAndFooter(yemei,yejiao,doc_linkImg);//设置页眉页脚图片//发现页号是此行代码加上的(注释掉就没有了)
                doc_linkImg.newPage();
                doc_linkImg.add(new Paragraph());
                doc_linkImg.add(new Phrase(new Chunk("链路图",new Font(FONT_SIMYOU, 14f))));//不知道为什么加此行可以实现每页top padding效果
                doc_linkImg.add(image_zlink);
                doc_linkImg.close();
                return new ByteArrayOutputStream[]{baos_linkImg};
            }else{
                doc.newPage();
                doc.add(new Chunk("链路图", new Font(FONT_SIMYOU, 14f)));
                doc.add(image_zlink);
                return null;
            }
        }
        return null;
    }

    /**
     *两个pcap比较时一共7张图表图片(其中"吞吐曲线图"是叠加图)
     */
    private ByteArrayOutputStream[] addEchartsImg_compare(String pcapFileName1,String pcapFileName2,String echartsFolderPath,Document doc, String yemei, String yejiao,PdfWriter baseWriter) throws MalformedURLException, IOException, DocumentException {//写入4个图表图片
        Image image_aipFlow1 = getImage(echartsFolderPath+"/aipFlow/"+pcapFileName1+".png");
        Image image_aipFlow2 = getImage(echartsFolderPath+"/aipFlow/"+pcapFileName2+".png");
        Image image_bpacketLen1 =getImage(echartsFolderPath+"/bpacketLen/"+pcapFileName1+".png");;
        Image image_bpacketLen2 =getImage(echartsFolderPath+"/bpacketLen/"+pcapFileName2+".png");;
        Image image_cuseDetail= getImage(echartsFolderPath+"/cuse2Detail/diejiatu.png");;
        Image image_zlink1 =getImage(echartsFolderPath+"/zlink/"+pcapFileName1+".png");;
        Image image_zlink2 =getImage(echartsFolderPath+"/zlink/"+pcapFileName2+".png");;
        if (image_aipFlow1 != null) {
            doc.newPage();
            doc.add(new Paragraph());
            doc.add(new Chunk("图表展示:", new Font( FONT_SIMYOU, 16f)));
            doc.add(new Chunk("\n\n\n"));
            doc.add(new Chunk("IP流量分布图", new Font(FONT_SIMYOU, 14f)));
            Paragraph paragraph = new Paragraph();
            Table table = new Table(2, 1);
            table.setPadding(2);
            table.setSpacing(4);
            table.setWidth(100f);
            table.setBorderWidth(0);
            Cell cell1 = new Cell(image_aipFlow1);
            Cell cell2 = new Cell(image_aipFlow2);
            table.addCell(cell1);
            table.addCell(cell2);
            paragraph.add(table);
            doc.add(paragraph);
        }
        if (image_bpacketLen1 != null) {
            doc.newPage();
            doc.add(new Chunk("包长分布图", new Font(FONT_SIMYOU, 14f)));
            Paragraph paragraph = new Paragraph();
            Table table = new Table(2, 1);
            table.setPadding(2);
            table.setSpacing(4);
            table.setWidth(100f);
            table.setBorderWidth(0);
            Cell cell1 = new Cell(image_bpacketLen1);
            Cell cell2 = new Cell(image_bpacketLen2);
            table.addCell(cell1);
            table.addCell(cell2);
            paragraph.add(table);
            doc.add(paragraph);
        }
        if (image_cuseDetail != null) {
            doc.newPage();
            doc.add(new Chunk("吞吐曲线图", new Font(FONT_SIMYOU, 14f)));
            doc.add(image_cuseDetail);
        }
        if (image_zlink1 != null) {
            if((image_zlink1.getHeight()*0.69f+150)>doc.getPageSize().getHeight()){////如果第一张图片过长，则要单独生成一个doc,则第二长也要生成一个单独的doc(无论是否过长)
                ByteArrayOutputStream[] arr=new  ByteArrayOutputStream[2];
                image_zlink1.scalePercent(69f);
                Document doc_linkImg = new Document(new Rectangle(doc.getPageSize().getWidth(),image_zlink1.getHeight()*0.69f+150f));
                ByteArrayOutputStream baos_linkImg = new ByteArrayOutputStream();
                PdfWriter.getInstance(doc_linkImg, baos_linkImg);
                doc_linkImg.setPageCount(baseWriter.getPageNumber());
                setHeaderAndFooter(yemei,yejiao,doc_linkImg);//设置页眉页脚图片//发现页号是此行代码加上的(注释掉就没有了)
                doc_linkImg.newPage();
                doc_linkImg.add(new Paragraph());
                doc_linkImg.add(new Phrase(new Chunk("链路图",new Font(FONT_SIMYOU, 14f))));//不知道为什么加此行可以实现每页top padding效果
                doc_linkImg.add(image_zlink1);
                doc_linkImg.close();
                arr[0]=baos_linkImg;
                if (image_zlink2 != null) {
                    image_zlink2.scalePercent(69f);
                    doc_linkImg = new Document(new Rectangle(doc.getPageSize().getWidth(),image_zlink2.getHeight()*0.69f+150f));
                    baos_linkImg = new ByteArrayOutputStream();
                    PdfWriter.getInstance(doc_linkImg, baos_linkImg);
                    doc_linkImg.setPageCount(baseWriter.getPageNumber()+1);
                    setHeaderAndFooter(yemei,yejiao,doc_linkImg);//设置页眉页脚图片//发现页号是此行代码加上的(注释掉就没有了)
                    doc_linkImg.newPage();
                    doc_linkImg.add(image_zlink2);
                    doc_linkImg.close();
                    arr[1]=baos_linkImg;
                }
                return arr;
            }else{//如果第一章链路图比较小，则判断第二张大小是否过大
                doc.newPage();
                doc.add(new Chunk("链路图", new Font(FONT_SIMYOU, 14f)));
                doc.add(new Chunk("\n"));
                doc.add(image_zlink1);
                doc.add(new Chunk("\n"));
                if (image_zlink2 != null) {
                    if((image_zlink2.getHeight()*0.69f+150)>doc.getPageSize().getHeight()){
                        ByteArrayOutputStream[] arr=new  ByteArrayOutputStream[1];
                        image_zlink2.scalePercent(69f);
                        Document doc_linkImg = new Document(new Rectangle(doc.getPageSize().getWidth(),image_zlink2.getHeight()*0.69f+150f));
                        ByteArrayOutputStream baos_linkImg = new ByteArrayOutputStream();
                        PdfWriter.getInstance(doc_linkImg, baos_linkImg);
                        doc_linkImg.setPageCount(baseWriter.getPageNumber());
                        setHeaderAndFooter(yemei,yejiao,doc_linkImg);//设置页眉页脚图片//发现页号是此行代码加上的(注释掉就没有了)
                        doc_linkImg.newPage();
                        doc_linkImg.add(image_zlink2);
                        doc_linkImg.close();
                        arr[0]=baos_linkImg;
                        return arr;
                    }else{
                        doc.add(new Chunk("\n"));
                        doc.add(image_zlink2);
                        doc.add(new Chunk("\n"));
                        return  null;
                    }
                }
            }
        }
        return null;
    }



    private Image getImage(String imgAbsPath) throws BadElementException, MalformedURLException, IOException {
        Image image = Image.getInstance(imgAbsPath);
        image.scaleToFit(1000f, 1000f);
        image.scalePercent(69f);
        image.setAlignment(Element.ALIGN_CENTER);
        File file = new File(imgAbsPath);
        file.delete();
        return image;
    }


    public static void mergePdfFiles(String[] fileNames, String savepath)  {
        try  {
            Document document = new Document(new PdfReader(fileNames[0]).getPageSize(1));
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(savepath));
            document.open();
            for(int i=0; i<fileNames.length; i++) {
                String filePath = fileNames[i];
                if(filePath==null||filePath.length()==0){
                    break;
                }
                PdfReader reader = new PdfReader(filePath);
                int n = reader.getNumberOfPages();
                for(int j=1; j<=n; j++)  {
                    document.newPage();
                    PdfImportedPage page = copy.getImportedPage(reader, j);
                    copy.addPage(page);
                }
            }
            document.close();




            for(int i=0; i<fileNames.length; i++) {
                String filePath = fileNames[i];
                if(filePath==null||filePath.length()==0){
                    break;
                }
                File file = new File(filePath);
                if(file.exists()){
                    file.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch(DocumentException e) {
            e.printStackTrace();
        }
    }



    private void setHeaderAndFooter(String headerImgPath,String footerImgPath,Document detailDoc) throws BadElementException, MalformedURLException, IOException {//设置页眉页脚
	        // 页眉页脚图片
	        Image headerImage = Image.getInstance(headerImgPath);//局部
	        headerImage.scalePercent(70f);
	        Chunk chunk = new Chunk(headerImage, -330, 0);//局部
	        Image headerImage2 = Image.getInstance(footerImgPath);
	        headerImage2.scalePercent(70f);
	        Chunk chunk2 = new Chunk(headerImage2, 300, 0);
	        // 页眉
	        HeaderFooter header = new HeaderFooter(new Phrase(chunk), false);
	
	        // 设置是否有边框等
	        // header.setBorder(Rectangle.NO_BORDER);
	        header.setBorder(Rectangle.BOTTOM);
	        header.setAlignment(1);
	        header.setBorderColor(Color.black);
	
	        // 页脚
	        HeaderFooter footer = new HeaderFooter(new Phrase(""), new Phrase(chunk2));
	        /**
	         *
	         * 0是靠左 1是居中 2是居右
	         */
	        footer.setAlignment(1);
	        footer.setBorderColor(Color.white);
	        footer.setBorder(Rectangle.BOTTOM);
	
	        detailDoc.setHeader(header);
	        detailDoc.setFooter(footer);
	        detailDoc.open();
	        // "C:/WINDOWS/Fonts/SIMSUN.TTC,1"
	        log.info("设置页眉页脚完毕！");
    }


    /**
     * 字体控制;
     *
     * @return
     */
    public static Font ChineseFont() {

        BaseFont baseFont = null;
        try {
            baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", true);
        } catch (DocumentException e) {
            ActionBase.log.error(e);
        } catch (IOException e) {
            ActionBase.log.error(e);
        } // bfChinese, 10, Font.BOLD
        Font chineseFont = new Font(baseFont, 10f, 0, Color.black);

        return chineseFont;
    }

    /**
     * 下载问题;
     *
     * @param response
     * @param filePath
     */
    public static void download(HttpServletResponse response, String filePath) {
        try {
            //String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            File file = new File(filePath);
            String fileName=file.getName();
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            String len = String.valueOf(file.length());
            response.setHeader("Content-Length", len);
            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(file);
            byte[] b = new byte[1024];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            in.close();
            out.close();
            file.delete();
        } catch (FileNotFoundException e) {
            ActionBase.log.error(e);
        } catch (IOException e) {
            ActionBase.log.error(e);
        }
    }

    /**
     * pdf增加水印;
     *
     * @param value
     * @throws Exception
     * @throws IOException
     */
    public static void addShYin(String srcPdfPath, String value, String targetPdfPath, HttpServletRequest request)
            throws Exception, IOException {
        PdfReader pdfReader = null;
        PdfStamper pdfStamper = null;
        try {
            pdfReader = new PdfReader(srcPdfPath);
            pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(targetPdfPath));
            int total = pdfReader.getNumberOfPages() + 1;
            Rectangle psize = pdfReader.getPageSize(1);

            float width = psize.getWidth();
            float height = psize.getHeight();
            com.lowagie.text.pdf.PdfContentByte content;
            PdfGState gs = new PdfGState();
            for (int i = 1; i < total; i++) {
                content = pdfStamper.getOverContent(i);// 在内容上方加水印
                // content = pdfStamper.GetUnderContent(i);//在内容下方加水印
                // 透明度
                gs.setFillOpacity(0.1f);
                gs.setStrokeOpacity(0.1f);
                content.saveState();
                // set Transparency
                content.setGState(gs);
                content.beginText();

               content.setFontAndSize(FONT_SIMYOU, 60);
                content.setTextMatrix(0, 0);
                // 位置
                content.showTextAligned(Element.ALIGN_CENTER, value, width / 2 - 50, height / 2 - 50, 55);
                content.endText();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {

            if (pdfStamper != null)
                pdfStamper.close();

            if (pdfReader != null)
                pdfReader.close();
            
            File file = new File(srcPdfPath);
            if(file!=null&&file.exists()){
            	file.delete();
            }
        }

        log.info("添加水印完毕!");
    }
    public static int addFooterImgAndPagination(String srcPdfPath, String targetPdfPath,int firstPageNum,String imgPath)
    		throws Exception, IOException {
    	PdfReader pdfReader = null;
    	PdfStamper pdfStamper = null;
    	try {
    		pdfReader = new PdfReader(srcPdfPath);
    		pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(targetPdfPath));
    		int total = pdfReader.getNumberOfPages() + 1;
    		Rectangle psize = pdfReader.getPageSize(1);
    		
    		float width = psize.getWidth();
    		float height = psize.getHeight();
    		com.lowagie.text.pdf.PdfContentByte content;
    		PdfGState gs = new PdfGState();
    		gs.setFillOpacity(1);
            gs.setStrokeOpacity(1);
    		for (int i = 1; i < total; i++) {
	    			 content = pdfStamper.getOverContent(i);// 在内容上方加水印
	                 content.saveState();
	                 content.setGState(gs);
	                 content.beginText();
	                 content.setFontAndSize(BaseFont.createFont(), 12);
	                 content.setTextMatrix(0, 0);
	                 content.showTextAligned(Element.ALIGN_CENTER,(firstPageNum++)+"" , width / 2 , 10, 0);
	                 content.endText();
	                 
	                 Image yejiaoimg = Image.getInstance(imgPath);
	                 yejiaoimg.scalePercent(70f);
	                 yejiaoimg.setAbsolutePosition(850, 40);
		    		 content.addImage(yejiaoimg);
    		}
    	} catch (Exception ex) {
    		throw ex;
    	} finally {
    		
    		if (pdfStamper != null)
    			pdfStamper.close();
    		
    		if (pdfReader != null)
    			pdfReader.close();
    		
    		File file = new File(srcPdfPath);
    		if(file!=null&&file.exists()){
    			file.delete();
    		}
    		
    		return firstPageNum;
    	}
    }

    /**
     * 设置文本域;
     *
     * @param fields
     * @param data
     * @throws IOException
     * @throws DocumentException
     */
    public static void fillPdfTemplateForm(AcroFields fields, Map<String, String> data,BaseFont baseFont) throws IOException, DocumentException {

        for (String key : data.keySet()) {
            String value = data.get(key);
            fields.setFieldProperty(key, "textfont", baseFont	,null);
            fields.setField(key, value); // 为字段赋值,注意字段名称是区分大小写的

        }
        log.info("填充模板表单完毕！");
    }

    /**
     * 获取数据;
     *
     * @return
     */
    private static Map<String, String> data(String value, String string, String date, String object,String userAgent) {//pdf模板文件最上边的版本等信息
        Map<String, String> data = new HashMap<String, String>();
        data.put("Text1", SysInfoConfig.VERSION);
        data.put("Text2", object);
        data.put("Text3", date);
        data.put("Text4", string);
        data.put("Text5", SysInfoConfig.BUSINESS_TYPE_01);
        data.put("Text6", userAgent);
        return data;
    }

    /**
     * 合并pdf流;移动互联网业务品质智能测评系统
     *
     * @param osList
     * @param os
     */
    public static void mergePdfFiles(List<ByteArrayOutputStream> osList, OutputStream os) {
        try {
            // 页码+1
            Document document = new Document(new PdfReader(osList.get(0).toByteArray()).getPageSize(1));
            PdfCopy pdfCopy = new PdfCopy(document, os);
            document.open();
            for (int i = 0; i < osList.size(); i++) {
                PdfReader reader = new PdfReader(osList.get(i).toByteArray());
                // 当前页
                int n = reader.getNumberOfPages();
                for (int j = 1; j <= n; j++) {
                    document.newPage();
                    PdfImportedPage page = pdfCopy.getImportedPage(reader, j);
                    pdfCopy.addPage(page);
                }
            }
            document.close();
        } catch (IOException e) {
            ActionBase.log.error(e);
        } catch (DocumentException e) {
            ActionBase.log.error(e);
        }

        log.info("pdf合并完毕!");
    }




    /**
     * 创建当前时间;
     *
     * @return
     */
    private static String createDate() {
        long l = System.currentTimeMillis();
        Date date = new Date(l);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    // 表格(非"比较"时走该方法，绘制表单table)
    public void newgenOtherTable(HttpServletRequest request,
    							 String username,
    							 String pcapFileName,
                                 List<List<String>> dataList,//baseInfoTable
                                 HttpServletResponse response,
                                 List<List<String>> dataList1, // popularInfoTable
                                 List<List<String>> dataList2, //detailsInfoTable
                                 Document doc,
                                 String imgPath, //errorImg
                                 List<List<String>> dataList3, //detailsInfoOthers
                                 List<RebuildInfo> rebuildInfos) throws Exception {
        log.info("这是概述---------------------------------");
        String webpath = request.getRealPath("/");
        String imgPath_gaishu = webpath + "xxxx/"+username+"/gaishu_"+pcapFileName+".png";
        String imgPath_jielun = webpath + "xxxx/"+username+"/jielun_"+pcapFileName+".png";
        
        /* renderFileDisposeInfo(doc,disposeImg, fileDispose);//"报文处理概述"渲染
         renderBasicConclusionInfo(doc, basicConclusion, imgPath);//"结论概述"渲染*/ 
         renderGaishuAndJielun(doc,imgPath_gaishu,imgPath_jielun);//"报文处理概述"和"结论概述"渲染
      
        doc.newPage();
        Paragraph paragraph = new Paragraph();

        paragraph.add(new Phrase(new Chunk("明细数据:",new Font(FONT_SIMYOU, 16f))));
        doc.add(paragraph);
        doc.add(new Chunk("\n\n"));
        renderBaseInfoTable(doc, dataList);//绘制“基本信息”表格
        renderRebuildInfoMsg(doc, rebuildInfos, imgPath);//绘制"频繁拆建链"信息
        renderPopularInfoTable(doc,dataList1);//绘制"通用信息"表格
        renderDetailsInfoTable(doc,dataList2);//绘制"详细信息"表格
        renderInvalidIPTable(doc,dataList3);//绘制"无效IP"表格

        log.info(" 图形(表格table)绘制完毕！");
    }

    private void renderGaishuAndJielun(Document doc, String imgPath_gaishu, String imgPath_jielun) throws MalformedURLException, IOException, DocumentException {
    	Image img_gaishu = getImage(imgPath_gaishu);
    	Image img_jielun = getImage(imgPath_jielun);
    	img_jielun.setAlignment(Element.ALIGN_LEFT);
    	doc.newPage();
    	Paragraph paragraph = new Paragraph();
    	paragraph.add(img_gaishu);
    	doc.add(paragraph);
    	paragraph = new Paragraph();
    	paragraph.add(img_jielun);
    	doc.add(paragraph);
    	
	}


	private void renderFileDisposeInfo(Document doc, String disposeImg, Map<String, Object> fileDispose) throws DocumentException {
        log.info("这是盖伦---------------------------------");
        Font font = new Font(FONT_SIMYOU,10,Font.NORMAL, Color.black);
        doc.newPage();
        doc.add(new Chunk("报文处理概述:", new Font(FONT_SIMYOU, 14f)));
        doc.add(new Chunk("\n"));
        Table table = new Table(5);
        Cell cell;

        cell = new Cell(new Chunk("",font));
        cell.setBorder(0);
        table.addCell(cell);
        table = addImg(table, disposeImg + "pcapjiexi.jpg");
        table = addImg(table, disposeImg + "pcapqingxi.jpg");
        table = addImg(table, disposeImg + "canshujisuan.jpg");
        table = addImg(table, disposeImg + "zhuanjiaxitong.jpg");

        cell = new Cell(new Chunk("",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("报文解析",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("报文清洗",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("参数计算",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("专家系统",font));
        cell.setBorder(0);
        table.addCell(cell);

        cell = new Cell(new Chunk((String) fileDispose.get("fileName"),font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("1."+fileDispose.get("packetCount")+"条报文",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("1."+fileDispose.get("ipCount")+"条清洗后有效ip,清洗掉"+fileDispose.get("othersIpCount")+"条无效ip \n" +
                "2."+fileDispose.get("tcpCount")+"条有效链路,清洗掉"+fileDispose.get("othersTcpCount")+"条无效链路",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("1.可计算"+fileDispose.get("permissableParameterCount")+"个参数 \n" +
                "2."+fileDispose.get("noPermissablePara"),font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("1."+fileDispose.get("adviceNum")+"条建议",font));
        cell.setBorder(0);
        table.addCell(cell);


        table.setBorderWidth(0f);
        table.setAlignment(Element.ALIGN_CENTER);
        doc.add(table);
        log.info("这是盖伦---------------------------------");

    }

    private void renderFileDisposeInfo(Document doc, String disposeImg, List<Map<String, Object>> fileDisposeList) throws DocumentException {
        log.info("报文处理概述---------------------------------");
        Font font = new Font(FONT_SIMYOU,10,Font.NORMAL, Color.black);
        doc.newPage();
        doc.add(new Paragraph());
        doc.add(new Chunk("报文处理概述:", new Font(FONT_SIMYOU, 14f)));
        doc.add(new Chunk("\n\n\n\n\n\n"));
        Table table = new Table(5);
        Cell cell;

        cell = new Cell(new Chunk("",font));
        cell.setBorder(0);
        table.addCell(cell);
        table = addImg(table, disposeImg + "pcapjiexi.jpg");
        table = addImg(table, disposeImg + "pcapqingxi.jpg");
        table = addImg(table, disposeImg + "canshujisuan.jpg");
        table = addImg(table, disposeImg + "zhuanjiaxitong.jpg");

        cell = new Cell(new Chunk("",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("报文解析",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("报文清洗",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("参数计算",font));
        cell.setBorder(0);
        table.addCell(cell);
        cell = new Cell(new Chunk("专家系统",font));
        cell.setBorder(0);
        table.addCell(cell);

        Map<String, Object> fileDispose;
        for (int i=0; i<fileDisposeList.size(); i++) {
            fileDispose = fileDisposeList.get(i);
            cell = new Cell(new Chunk((String) fileDispose.get("fileName")+"  业务",font));
            cell.setBorder(0);
            table.addCell(cell);
            cell = new Cell(new Chunk("1."+fileDispose.get("packetCount")+"条报文",font));
            cell.setBorder(0);
            table.addCell(cell);
            cell = new Cell(new Chunk("1."+fileDispose.get("ipCount")+"条有效ip,清洗掉"+fileDispose.get("othersIpCount")+"条无效ip \n" +
                    "2."+fileDispose.get("tcpCount")+"条有效链路,清洗掉"+fileDispose.get("othersTcpCount")+"条无效链路",font));
            cell.setBorder(0);
            table.addCell(cell);
            cell = new Cell(new Chunk("1.可计算"+fileDispose.get("permissableParameterCount")+"个参数 \n" +
                    "2."+fileDispose.get("noPermissablePara"),font));
            cell.setBorder(0);
            table.addCell(cell);
            cell = new Cell(new Chunk("1."+fileDispose.get("adviceNum")+"条建议",font));
            cell.setBorder(0);
            table.addCell(cell);
        }

        table.setBorderWidth(0f);
        table.setAlignment(Element.ALIGN_CENTER);
        doc.add(table);
        log.info("这是盖伦---------------------------------");

    }

    private Table addImg(Table table, String disposeImg) {
        File mFile;
        mFile = new File(disposeImg);
        String imgName = (mFile.getAbsolutePath());
        // 图片长度
        Image image = null;
        try {
            image = Image.getInstance(imgName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadElementException e) {
            e.printStackTrace();
        }
        Cell cell = null;
        try {
            cell = new Cell(image);
        } catch (BadElementException e) {
            e.printStackTrace();
        }
        cell.setBorder(0);
        table.addCell(cell);
        return table;
    }

    private void renderBasicConclusionInfo(Document doc, Map<String, Object> basicConclusion, String imgPath) throws DocumentException {
        // TODO Auto-generated method stub
        Font font = new Font(FONT_SIMYOU,10,Font.NORMAL, Color.black);
        doc.newPage();
        doc.add(new Chunk("基本结论:", new Font(FONT_SIMYOU, 14f)));
        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(60f);
        paragraph.add(new Phrase(new Chunk((String) basicConclusion.get("title"), new Font(FONT_SIMYOU, 10f))));
        doc.add(paragraph);

        String ipProvince = (String) basicConclusion.get("ipProvince");
        String oftenOffVerdict = (String) basicConclusion.get("oftenOffVerdict");
        String downloadRate = (String) basicConclusion.get("downloadRate");
        File mFile;
        if (basicConclusion.size() > 1) {
            mFile = new File(imgPath + "cuo.jpg");
            String imgName = (mFile.getAbsolutePath());
            // 图片长度
            Image image = null;
            try {
                image = Image.getInstance(imgName);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.scaleToFit(1000f, 1000f);
            image.scalePercent(50f);
            image.setAlignment(Element.ALIGN_LEFT);
            image.setIndentationLeft(80f);
            image.setRotationDegrees(30);
            Table table = new Table(20);
            Cell cell = new Cell(image);
            cell.setWidth("20");
            cell.setBorder(0);
            Cell cell1;
            if (!StringUtil.isEmpty(ipProvince)) {
                table.addCell(cell);
                cell1 = new Cell(new Chunk(ipProvince,font));
                cell1.setColspan(19);
                cell1.setBorder(0);
                table.addCell(cell1);
            }
            if (!StringUtil.isEmpty(oftenOffVerdict)) {
                table.addCell(cell);
                cell1 = new Cell(new Chunk(oftenOffVerdict,font));
                cell1.setColspan(19);
                cell1.setBorder(0);
                table.addCell(cell1);
            }
            if (!StringUtil.isEmpty(downloadRate)) {
                table.addCell(cell);
                cell1 = new Cell(new Chunk(downloadRate,font));
                cell1.setColspan(19);
                cell1.setBorder(0);
                table.addCell(cell1);
            }
            table.setBorderWidth(0f);
            table.setAlignment(1);
            doc.add(table);
        }
        log.info("\"基本结论\"表格绘制完毕");

    }

    private void renderBasicConclusionInfo(Document doc, List<Map<String, Object>> basicConclusionList, String imgPath) throws DocumentException {
        // TODO Auto-generated method stub
        Font font = new Font(FONT_SIMYOU,10,Font.NORMAL, Color.black);
        doc.newPage();
        doc.add(new Chunk("基本结论:", new Font(FONT_SIMYOU, 14f)));
        Paragraph paragraph;
        String ipProvince;
        String oftenOffVerdict;
        String downloadRate;
        String imgName;
        Map<String,Object> basicConclusion;
        File mFile;
        Table table;
        Cell cell;
        Cell cell1;
        for (int i=0; i<basicConclusionList.size(); i++) {
            basicConclusion = basicConclusionList.get(i);

            paragraph = new Paragraph();
            paragraph.setIndentationLeft(60f);
            paragraph.add(new Phrase(new Chunk((String) basicConclusion.get("title"), new Font(FONT_SIMYOU, 10f))));
            doc.add(paragraph);

            ipProvince = (String) basicConclusion.get("ipProvince");
            oftenOffVerdict = (String) basicConclusion.get("oftenOffVerdict");
            downloadRate = (String) basicConclusion.get("downloadRate");

            if (basicConclusion.size() > 1) {
                mFile = new File(imgPath + "cuo.jpg");
                imgName = (mFile.getAbsolutePath());
                // 图片长度
                Image image = null;
                try {
                    image = Image.getInstance(imgName);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.scaleToFit(1000f, 1000f);
                image.scalePercent(55f);
                image.setAlignment(Element.ALIGN_LEFT);
                image.setIndentationLeft(80f);
              //image.setRotationDegrees(30);
                table = new Table(20);
                cell = new Cell(image);
                cell.setWidth("20");
                cell.setBorder(0);

                if (!StringUtil.isEmpty(ipProvince)) {
                    table.addCell(cell);
                    cell1 = new Cell(new Chunk(ipProvince, font));
                    cell1.setColspan(19);
                    cell1.setBorder(0);
                    table.addCell(cell1);
                }
                if (!StringUtil.isEmpty(oftenOffVerdict)) {
                    table.addCell(cell);
                    cell1 = new Cell(new Chunk(oftenOffVerdict, font));
                    cell1.setColspan(19);
                    cell1.setBorder(0);
                    table.addCell(cell1);
                }
                if (!StringUtil.isEmpty(downloadRate)) {
                    table.addCell(cell);
                    cell1 = new Cell(new Chunk(downloadRate, font));
                    cell1.setColspan(19);
                    cell1.setBorder(0);
                    table.addCell(cell1);
                }
                table.setBorderWidth(0f);
                table.setAlignment(1);
                doc.add(table);
            }
        }
        log.info("\"基本结论\"表格绘制完毕");

    }

    private void renderInvalidIPTable(Document doc, List<List<String>> dataList) throws DocumentException, IOException {//绘制"无效IP"表格
        if(dataList==null||dataList.size()==0){
            return;
        }

        int fsize_title=10;
        int fsize_data=9;
        doc.add(new Chunk("\n"));

        Table table= new Table(2);
        table.setPadding(2f);
        Cell cell2 = new Cell();

        cell2.add(new Chunk("无效ip",new Font(FONT_SIMYOU,fsize_title,Font.NORMAL, Color.black)));

        table.addCell(cell2);

        Cell cell3 = new Cell();

        if (dataList.get(1).get(1) != null && !dataList.get(1).get(1).equals("")) {
            cell3.add(new Chunk(dataList.get(1).get(1),new Font(FONT_SIMYOU,fsize_data,Font.NORMAL, Color.black)));
        } else {
            cell3.add(new Chunk("无",new Font(FONT_SIMYOU,fsize_data,Font.NORMAL, Color.black)));
        }

        table.addCell(cell3);
        table.setBorderWidth(1f);
        table.setAlignment(Element.ALIGN_CENTER);
        doc.add(table);
        log.info("\"无效IP\"表格绘制完毕!");
    }

    private void renderDetailsInfoTable(Document doc,List<List<String>> dataList) throws DocumentException {
        //搞一个记录延时顺序的集合serviceIp顺序
        //显示顺序要和dataList一直
        //重行组织详细dataList
        Font font_title = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.black);
        Font font_data = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.black);
        Color bgcolor_title = new Color(161, 219, 246, 1);
        int colNum_serviceIp=-1;
        int colNum_dnsTimeDelay=-1;
        int colNum_ipAddr=-1;

        ArrayList<String> uniqueServiceIpList = new ArrayList<String>();//按原始顺序唯一的保存了serviceIp

        HashMap<String,LinkedList<Integer>> map = new HashMap<String,LinkedList<Integer>>();//key=serviceIp,value=dataList的row List
        for(int rowNum=1;rowNum<dataList.size();rowNum++){//serviceIp字段必传(且是第一个)
            String serviceIp = dataList.get(rowNum).get(0);
            LinkedList<Integer> rowNumList =map.get(serviceIp);
            if(rowNumList==null){
                rowNumList=new LinkedList<Integer>();
                map.put(serviceIp, rowNumList);

                uniqueServiceIpList.add(serviceIp);
            }
            rowNumList.add(rowNum);
        }

        doc.add(new Chunk("详细信息:", new Font(FONT_SIMYOU, 14f)));

        int colSize=dataList.get(0).size();
        int preColSize=colSize;
        if(preColSize>9){
            preColSize=9;
        }

        for(int colNum=0;colNum<colSize;colNum++){
            String currTitle=dataList.get(0).get(colNum);
            if("server IP".equals(currTitle)){
                colNum_serviceIp=colNum;
                continue;
            }else if("DNS时延(ms)".equals(currTitle)){
                colNum_dnsTimeDelay=colNum;
                continue;
            }else if ("服务器IP归属".equals(currTitle)) {
                colNum_ipAddr=colNum;
                continue;
            }
        }
        //最多显示9列(serviceIp,dns时延，IP归属地的rowspan=同一个serviceIp的个数)
        Table table_preCols = new Table(preColSize);
        table_preCols.setPadding(2f);
        table_preCols.setBorderWidth(1f);

        for(int colNum=0;colNum<preColSize;colNum++){//前9列的标题
            String title=dataList.get(0).get(colNum);
            if(title==null){
                title="";
            }
            Cell cell = new Cell(new Chunk(title,font_title));
            cell.setBackgroundColor(bgcolor_title);
            table_preCols.addCell(cell);
        }

        for (String serviceIp: uniqueServiceIpList) { //处理当前serviceIp对应的dataList的前preColSize个列
            LinkedList<Integer> rowNumList = map.get(serviceIp);
            int rowspan=rowNumList.size();

            int firstRowNum=rowNumList.get(0);
            for(int colNum=0;colNum<preColSize;colNum++){//前9列的第一行(因为要单独设置rowspan)
                String data=dataList.get(firstRowNum).get(colNum);
                if(data==null){
                    data="";
                }
                Cell cell = new Cell(new Chunk(data,font_data));
                if(colNum==colNum_serviceIp||colNum==colNum_dnsTimeDelay||colNum==colNum_ipAddr){
                    cell.setRowspan(rowspan);
                }
                table_preCols.addCell(cell);
            }

            for (int  i=1;i<rowNumList.size();i++  ) {//前9列的剩余row
                int rowNum=rowNumList.get(i);
                for(int colNum=0;colNum<preColSize;colNum++){
                    if(colNum!=colNum_serviceIp&&colNum!=colNum_dnsTimeDelay&&colNum!=colNum_ipAddr){
                        String data=dataList.get(rowNum).get(colNum);
                        if(data==null){
                            data="";
                        }
                        Cell cell = new Cell(new Chunk(data,font_data));
                        table_preCols.addCell(cell);
                    }
                }
            }
        }
        doc.add(table_preCols);
        doc.add(new Chunk("\n\n"));

        if(colSize>9){//最麻烦
            Table table_postCols = new Table(colSize-preColSize);
            table_postCols.setPadding(2f);
            table_postCols.setBorderWidth(1f);

            for(int colNum=preColSize;colNum<colSize;colNum++){//后9列的标题
                String title=dataList.get(0).get(colNum);
                if(title==null){
                    title="";
                }
                Cell cell = new Cell(new Chunk(title,font_title));
                cell.setBackgroundColor(bgcolor_title);
                table_postCols.addCell(cell);
            }

            for (String serviceIp: uniqueServiceIpList) { //后9列的数据列
                LinkedList<Integer> rowNumList = map.get(serviceIp);
                int rowspan=rowNumList.size();

                int firstRowNum=rowNumList.get(0);
                for(int colNum=preColSize;colNum<colSize;colNum++){//后9列的第一行(要设置rowspan)
                    String data=dataList.get(firstRowNum).get(colNum);
                    if(data==null){
                        data="";
                    }
                    Cell cell = new Cell(new Chunk(data,font_data));
                    if(colNum==colNum_serviceIp||colNum==colNum_dnsTimeDelay||colNum==colNum_ipAddr){
                        cell.setRowspan(rowspan);
                    }
                    table_postCols.addCell(cell);
                }

                for (int  i=1;i<rowNumList.size();i++  ) {//后9列的剩余行
                    int rowNum=rowNumList.get(i);
                    for(int colNum=preColSize;colNum<colSize;colNum++){
                        if(colNum!=colNum_serviceIp&&colNum!=colNum_dnsTimeDelay&&colNum!=colNum_ipAddr){
                            String data=dataList.get(rowNum).get(colNum);
                            if(data==null){
                                data="";
                            }
                            Cell cell = new Cell(new Chunk(data,font_data));
                            table_postCols.addCell(cell);
                        }
                    }
                }
            }
            doc.add(table_postCols);
            doc.add(new Chunk("\n\n"));
        }
        log.info("\"详细信息\"表格绘制完毕!");
    }

    private void renderPopularInfoTable(Document doc, List<List<String>> dataList) throws DocumentException, IOException {//绘制"通用信息"表格
        doc.add(new Chunk("通用信息", new Font(FONT_SIMYOU, 14f)));
        renderSimple2DimensionTable(doc, dataList);
        doc.add(new Chunk("\n"));
        log.info("\"通用信息\"表格绘制完毕");
    }


    //绘制二维表格
    private void renderSimple2DimensionTable(Document doc, List<List<String>> dataList) throws DocumentException, IOException{
        Font font = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.black);
        Color bgcolor_title = new Color(161, 219, 246, 1);
        Table table= new Table(dataList.get(0).size());//列数
        // 创建一个表格
        table.setPadding(2f);
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = 0; j < dataList.get(i).size(); j++) {
                Cell cell = new Cell(new Chunk(dataList.get(i).get(j).toString(),font));
                if (i==0) {
                    cell.setBackgroundColor(bgcolor_title);
                }
                table.addCell(cell);
            }
        }
        table.setBorderWidth(1f);
        doc.add(table);
    }
    private void renderSimple2DimensionTable(Document doc, List<List<String>> dataList,float marginLeft) throws DocumentException, IOException{
    	Font font = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.black);
    	Color bgcolor_title = new Color(161, 219, 246, 1);
    	Table table= new Table(dataList.get(0).size());//列数
    	Paragraph paragraph=new Paragraph();
		paragraph.setIndentationLeft(marginLeft);
    	// 创建一个表格
    	table.setPadding(2f);
    	table.setAlignment(Element.ALIGN_LEFT);
    	for (int i = 0; i < dataList.size(); i++) {
    		for (int j = 0; j < dataList.get(i).size(); j++) {
    			Cell cell = new Cell(new Chunk(dataList.get(i).get(j).toString(),font));
    			if (i==0) {
    				cell.setBackgroundColor(bgcolor_title);
    			}
    			table.addCell(cell);
    		}
    	}
    	table.setBorderWidth(1f);
    	paragraph.add(table);
    	doc.add(paragraph);
    }
    
    
    private void renderSimple2DimensionTable_cellData(Document doc, List<List<CellData>> dataList,float marginLeft) throws DocumentException, IOException{
    	Font font_blcak = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.black);
    	Font font_red = new Font(FONT_SIMYOU,9,Font.NORMAL, Color.red);
    	Color bgcolor_title = new Color(161, 219, 246, 1);
    	Table table= new Table(dataList.get(0).size());//列数
    	Paragraph paragraph=new Paragraph();
    	paragraph.setIndentationLeft(marginLeft);
    	// 创建一个表格
    	table.setPadding(2f);
    	table.setAlignment(Element.ALIGN_LEFT);
    	for (int i = 0; i < dataList.size(); i++) {
	    		if(i==0){
		    			for (int j = 0; j < dataList.get(i).size(); j++) {
			        			Cell cell = new Cell(new Chunk(dataList.get(i).get(j).getData(),font_blcak));
			        			cell.setBackgroundColor(bgcolor_title);
			        			table.addCell(cell);
		        		}
	    		}else if(i==1){
		    			for (int j = 0; j < dataList.get(i).size(); j++) {
		    					CellData data = dataList.get(i).get(j);
		    					Cell cell =null;
		    					if(data.getColor()!=null){//加红
		    						cell=new Cell(new Chunk(data.getData(),font_red));
		    					}else{
		    						cell=new Cell(new Chunk(data.getData(),font_blcak));
		    					}
			        			table.addCell(cell);
		        		}
	    		}else{
		    			for (int j = 0; j < dataList.get(i).size(); j++) {
			        			Cell cell = new Cell(new Chunk(dataList.get(i).get(j).getData(),font_blcak));
			        			table.addCell(cell);
		        		}
	    		}
    	}
    	table.setBorderWidth(1f);
    	paragraph.add(table);
    	doc.add(paragraph);
    }


    private void renderTtFComment(Document doc,String TtFS1,String TtFS2,String TfFR1,String TtFR2,String fileName1,String fileName2) throws DocumentException {//绘制绘制"TtFS"和"TtFR"表格下面的说明
        Color color = new Color(165, 42, 42);
        Font font = new Font(FONT_SIMYOU, 10f);
        font.setColor(color);

        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(90f);//80f

        String msg_ttfs=null;
        String msg_ttfr=null;

        Table table = new Table(1);//默认是在paragraph中居中显示的
        table.setAlignment(Element.ALIGN_LEFT);
        table.setBorderWidth(0f);

        if(TtFS1==null||TtFS1.equals("N/A")||TtFS2==null||TtFS2.equals("N/A")){
            msg_ttfs="TtFS相差百分比: N/A;";
        }
        if(TfFR1==null||TfFR1.equals("N/A")||TtFR2==null||TtFR2.equals("N/A")){
            msg_ttfr="TtFR相差百分比: N/A;";
        }

        if(msg_ttfs==null){
            float TtFS1_f = Float.parseFloat(TtFS1);
            float TtFS2_f = Float.parseFloat(TtFS2);
            if(TtFS1_f<TtFS2_f){
                msg_ttfs="TtFS相差百分比: "+fileName1+"比"+fileName2+"高效,时间相差百分比: "+new BigDecimal(TtFS1_f*100).divide(new BigDecimal(TtFS2_f), 2, RoundingMode.HALF_UP).toString()+"%;";
            }else{
                msg_ttfs="TtFS相差百分比: "+fileName2+"比"+fileName1+"高效,时间相差百分比: "+new BigDecimal(TtFS2_f*100).divide(new BigDecimal(TtFS1_f), 2, RoundingMode.HALF_UP).toString()+"%;";
            }
        }

        if(msg_ttfr==null){
            float TtFR1_f = Float.parseFloat(TfFR1);
            float TtFR2_f = Float.parseFloat(TtFR2);
            if(TtFR1_f<TtFR2_f){
                msg_ttfr="TtFR相差百分比: "+fileName1+"比"+fileName2+"高效,时间相差百分比: "+new BigDecimal(TtFR1_f*100).divide(new BigDecimal(TtFR2_f), 2, RoundingMode.HALF_UP).toString()+"%";
            }else{
                msg_ttfr="TtFR相差百分比: "+fileName2+"比"+fileName1+"高效,时间相差百分比: "+new BigDecimal(TtFR2_f*100).divide(new BigDecimal(TtFR1_f), 2, RoundingMode.HALF_UP).toString()+"%";
            }
        }

        Cell cell = new Cell(new Chunk(msg_ttfs,new Font(FONT_SIMYOU,10,Font.NORMAL, color)));
        cell.setBorderWidth(0f);
        table.addCell(cell);

        cell = new Cell(new Chunk(msg_ttfr,new Font(FONT_SIMYOU,10,Font.NORMAL, color)));
        cell.setBorderWidth(0f);
        table.addCell(cell);

        cell = new Cell(new Chunk("TtFS：建立连接后，客户端发送第一个请求的时间，时间越短越高效;",new Font(FONT_SIMYOU,10,Font.NORMAL, color)));
        cell.setBorderWidth(0f);
        table.addCell(cell);

        cell = new Cell(new Chunk("TtFR：客户端发送请求后，服务器响应时间，时间越短越高效;",new Font(FONT_SIMYOU,10,Font.NORMAL, color)));
        cell.setBorderWidth(0f);
        table.addCell(cell);
        paragraph.add(table);

        doc.add(paragraph);
    }


    private void renderTtFTable(Document doc,List<List<String>> timeToFirstTible) throws DocumentException, IOException {//绘制"TtFS"和"TtFR"的表格
        renderSimple2DimensionTable(doc, timeToFirstTible);
    }




    //没有要显示"N/A"
    private void renderKindInfoMsg(Document doc,List<String> kindInfo,String imgPath) throws MalformedURLException, IOException, DocumentException {//绘制"KindInfo"信息
        StringBuffer kindIP = new StringBuffer();
        File mFile = new File(imgPath + "kind.jpg");
        String imgName = mFile.getAbsolutePath();
        // 图片长度
        Image image = Image.getInstance(imgName);
        image.scaleToFit(1000f, 1000f);
        image.scalePercent(69f);

        String msg_kindips="        N/A\n";

        if(kindInfo!=null&&kindInfo.size()>0){
            for(int i=0; i<kindInfo.size(); i++) {
                kindIP.append(kindInfo.get(i));
                kindIP.append(",");
            }

            kindIP.deleteCharAt(kindIP.length()-1);
            kindIP.append("\n");
            msg_kindips="        "+kindIP.toString();
        }

        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(90f);

        Table table = new Table(1);
        table.setBorderWidth(0f);
        table.setAlignment(Element.ALIGN_LEFT);
        Cell cell = new Cell(image);
        cell.setBorderWidth(0f);
        table.addCell(cell);

        cell=new Cell(new Chunk(msg_kindips,new Font(FONT_SIMYOU,10,Font.NORMAL, Color.black)));
        cell.setBorderWidth(0f);
        table.addCell(cell);

        paragraph.add(table);
        doc.add(paragraph);
        doc.add(new Chunk("\n"));
    }

    private void renderRebuildInfoMsg(Document doc,List<RebuildInfo> rebuildInfos,String imgPath) throws MalformedURLException, IOException, DocumentException {//渲染"频繁拆建链"信息
        Paragraph paragraph;
        File mFile;
        RebuildInfo rebuildInfo = new RebuildInfo();
        if (rebuildInfos != null && rebuildInfos.size()>0) {
            for(int i=0; i<rebuildInfos.size(); i++) {
                rebuildInfo = rebuildInfos.get(i);
                mFile = new File(imgPath + "rebuild.jpg");
                String imgName = null;
                imgName = (mFile.getAbsolutePath());
                // 图片长度
                Image image = Image.getInstance(imgName);
                image.scaleToFit(1000f, 1000f);
                image.scalePercent(69f);
                image.setAlignment(Element.ALIGN_LEFT);
                image.setIndentationLeft(80f);
                doc.add(image);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(80f);
                paragraph.add(new Phrase(new Chunk("IP: "+rebuildInfo.getServiceIp()+",   拆建链的数目: "+rebuildInfo.getRepeatCount()+
                        "个,   最短键连间隔: " + rebuildInfo.getMinLastTimeDelayed() + "ms,   平均建立时间: "+rebuildInfo.getAvgLastTimeDelayed() + "ms", new Font(FONT_SIMYOU, 10f))));
                doc.add(paragraph);
                doc.add(new Chunk("\n"));
            }
        }

        log.info("\"频繁拆建链\"信息绘制完毕!");
    }

    private void renderBaseInfoTable(Document doc,List<List<String>> dataList) throws DocumentException, IOException {//绘制“基本信息”表格
        doc.add(new Chunk("基本信息:", new Font(FONT_SIMYOU, 14f)));
        renderSimple2DimensionTable(doc, dataList);
        doc.add(new Chunk("\n"));
        log.info("\"基本信息 \"表格绘制完毕");
    }

}