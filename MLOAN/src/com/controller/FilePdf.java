package com.controller;

import com.constant.RedisStorageConfig;
import com.service.IIpAddService;
import com.util.StringUtil;
import com.util.base.PDFUti;
import com.util.base.ZipUtil;
import com.vo.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author lipengfei
 *
 */
@Controller
@RequestMapping("FilePdf")
public class FilePdf{

	private static final Logger log = Logger.getLogger(FilePdf.class);

	@Autowired
	private ShardedJedisPool shardedJedisPool;

	@Autowired
	IIpAddService ipAddService;


	/**未选择“视频比较”时走这个方法:one pcap,one pdf
	 */
	private void createPdfEachPcapEachFile(HttpServletRequest request,
										   HttpServletResponse response,
										   String data1,
										   String watermarker//水印信息
	) throws Exception{
		watermarker=decodeAndSetDefaultValue(watermarker,"CMLAB-测试所-无线室","utf-8");//参数(水印)判断

		DecimalFormat df= new DecimalFormat("#");
		List<String> filePathInZips = new ArrayList<String>();//生成多个pdf文件打包zip
		if (StringUtil.isEmpty(data1)) {
			log.error("生成pdf报告内容参数data1为空*****************");
		}

		JSONObject jObj =decodeAndTransfer2JsonObj(data1,"utf-8");//代表所有前台传递的参数转换的大对象(包含几乎所有"明细数据",pdf中要生成的图表信息不是数据而是截取的页面现成的图片)
		List<FileInfo> fileInfoList = generateFileInfoList(jObj);//"图表信息"是tcpGraph(这里没用上，因为图表现在是以文件形式上传到服务器),"明细信息"是FileInfo,因为这里要生成多个pcap文件的pdf，所以就要提供fileInfoList了
		HashMap<String, Boolean> whoshowMap = generateWhoshowMap(jObj);//应显示的"通用指标"

		Map<String, String> ipAddrMap=null;
		if(whoshowMap.get("a2")){//需要显示"ip归属地"
			ipAddrMap = getIpAddrMap(request);//ip地址, session中取出ip地址, 字符串处理之后存map
		}

		FileInfo fileInfo;

		PDFUti fPdfUti_hsy=new PDFUti();
		Map<String, Object> basicConclusion;
		Map<String, Object> fileDispose;
		String username = (String) request.getSession().getAttribute("username");

		for(int k=0; k<fileInfoList.size(); k++){ //依次处理每一个FileInfo(一个循环对应生成一个pdf报告)
			fileInfo = fileInfoList.get(k);

			basicConclusion = basicConclusionInfo(username, fileInfo);//基本结论
			fileDispose = fileDisposeInfo(fileInfo,basicConclusion.size()-1);//报文处理概述
			basicConclusion.put("username", username);
			List<RebuildInfo> rebuildInfoList=generateRebuildInfos(fileInfo);
			String title = URLDecoder.decode(request.getParameter("Title").trim(), "utf-8");

			List<List<String>> baseInfoTable =generateBaseInfoTable(fileInfo,df);//基本信息

			List<String> kindInfo = new ArrayList<String>();//全局
			StringBuffer serviceIP = new StringBuffer();//全局

			List<List<String>> detailsInfoTable =new ArrayList<>();//详细信息
			detailsInfoTable.add(generateDetailsInfoTitle(whoshowMap));
			loadDetailsInfoData(request,fileInfo, kindInfo, serviceIP, ipAddrMap, detailsInfoTable,whoshowMap);

			List<List<String>> popularInfoTable =generatePopularInfoTable(serviceIP,fileInfo,df,whoshowMap);//通用信息

			List<List<String>> detailsInfoOthers=null;
			if(whoshowMap.get("a1")){
				detailsInfoOthers= generateDetailsInfoOthers(fileInfo);//(其他)详细信息(即"无效ip集合")
			}

			String pcapShortName= fileInfo.getFileName();//detailsInfoOthers.get(1).get(0);

			String filePath = fPdfUti_hsy.pdfMethod(request,
					baseInfoTable,
					response,
					popularInfoTable,
					detailsInfoTable,
					watermarker,
					title,
					pcapShortName,
					username,
					detailsInfoOthers,
					rebuildInfoList
				/*	kindInfo,
					null,
					basicConclusion,
					fileDispose*/);//timeToFirstTible
			filePathInZips.add(filePath);
		}

		if (filePathInZips.size() > 1) {
			ZipUtil.downloadZip(response, filePathInZips);
		} else if (filePathInZips.size() == 1) {
			PDFUti.download(response, filePathInZips.get(0));
		}
	}

	private Map<String, Object> fileDisposeInfo(FileInfo fileInfo, int adviceNum) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("packetCount",fileInfo.getPacketCount());
		resultMap.put("ipCount",fileInfo.getIpCount());
		resultMap.put("othersIpCount",fileInfo.getOthersIpCount());
		resultMap.put("tcpCount",fileInfo.getTcpCount());
		resultMap.put("othersTcpCount",fileInfo.getOthersTcpCount());
		resultMap.put("noPermissablePara",fileInfo.getNoPermissablePara());
		resultMap.put("permissableParameterCount",fileInfo.getPermissableParameterCount());
		resultMap.put("adviceNum",adviceNum);
		resultMap.put("fileName",fileInfo.getFileName());
		return resultMap;
	}

	
	
	/**基本结论：
	 * 		 yknorm 业务涉及 访问服务器跨区域 、链路不支持 TCP WINDOW SCALE OPTION 问题情况：
 			  服务器涉及9个地区： 浙江省、北京市、江苏省、山东省、局域网、上海市、江西省、广东省、河南省
 			  链路不支持 TCP WINDOW SCALE OPTION 问题涉及：39.161.149.186(47636)、39.164.108.181(37099)、39.186.136.238(46953)、112.0.210.110(54569)、117.149.34.114(37667)、223.99.221.18(35722)、223.99.221.59(60048)、223.99.221.50(43304)、223.99.221.66(32803)、120.203.68.50(56622)、140.205.160.76(48820)、140.205.36.25(48918)、106.39.203.108(50900)
	 */
	private Map<String, Object> basicConclusionInfo(String username, FileInfo fileInfo) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String proviceCount;
		String provice;
		int sumOftenOffVerdict = 0;
		String downloadRate;
		StringBuffer titleBuffer = new StringBuffer();
		String title;
		//标题
		titleBuffer.append(fileInfo.getFileName());
		titleBuffer.append("业务涉及 ");

		ShardedJedis jedis = shardedJedisPool.getResource();
		proviceCount = jedis.get(username + "_" + fileInfo.getFileName() + RedisStorageConfig.CROSS_PROVICE_IPADDRNUM);
		provice = jedis.get(username + "_" + fileInfo.getFileName() + RedisStorageConfig.CROSS_PROVICE_IPADDR);
		sumOftenOffVerdict = fileInfo.getSumOftenOffVerdict();
		downloadRate = fileInfo.getDownloadRate();

		if (provice !=null && !provice.equals("") && !proviceCount.equals("0")) {
			titleBuffer.append("访问服务器跨区域 ");
			resultMap.put("ipProvince", "服务器涉及"+proviceCount+"个地区" + provice);
		}
		if (sumOftenOffVerdict > 0) {
			titleBuffer.append("频繁拆建链 ");
			resultMap.put("oftenOffVerdict", "频繁拆建链共涉及"+sumOftenOffVerdict+"次");
		}
		if (!StringUtil.isEmpty(downloadRate)) {
			titleBuffer.append("TCP WINDOW SCALE OPTION");
			resultMap.put("downloadRate", "TCP WINDOW SCALE OPTION 问题涉及:"+downloadRate);
		}
		titleBuffer.append("问题情况:");
		title = titleBuffer.toString();
		if (!title.contains("访问服务器跨区域") && !title.contains("频繁拆建链") && !title.contains("TCP WINDOW SCALE OPTION")) {
			title = fileInfo.getFileName() + "无 访问服务器跨区域 频繁拆建链 TCP WINDOW SCALE OPTION 问题";
		}
		resultMap.put("title", title);
		log.info("这是title---------------------"+title);
		return resultMap;
	}

	/**
	 * 选中"视频比较"或者"基本信息比较"时走这个方法:two pcap,one pdf
	 * @throws Exception
	 */
	private  void create2pcapIntoOnePDF(HttpServletRequest request,
										HttpServletResponse response,
										String data1,
										String watermarker,//水印信息
										String baseFileName,
										String jingpinFileName) throws Exception{
		watermarker=decodeAndSetDefaultValue(watermarker,"CMLAB-测试所-无线室","utf-8");//参数(水印)判断

		jingpinFileName = URLDecoder.decode(jingpinFileName, "utf-8");
		DecimalFormat df= new DecimalFormat("#");
		List<String> filePathInZips = new ArrayList<String>();//生成多个pdf文件打包zip
		if (StringUtil.isEmpty(data1)) {
			log.error("生成pdf报告内容参数data1为空*****************");
		}

		JSONObject jObj =decodeAndTransfer2JsonObj(data1,"utf-8");//代表所有前台传递的参数转换的大对象(包含几乎所有"明细数据",pdf中要生成的图表信息不是数据而是截取的页面现成的图片)
		List<FileInfo> fileInfoList = generateFileInfoList(jObj);//"图表信息"是tcpGraph,"明细信息"是FileInfo,因为这里要生成多个pcap文件的pdf，所以就要提供fileInfoList了
		HashMap<String, Boolean> whoshowMap = generateWhoshowMap(jObj);//应显示的"通用指标"

		Map<String, String> ipAddrMap=null;
		if(whoshowMap.get("a2")){//需要显示"ip归属地"
			ipAddrMap = getIpAddrMap(request);//ip地址, session中取出ip地址, 字符串处理之后存map
		}

		FileInfo fileInfo;

		List<List<List<String>>> baseInfoTableList=new ArrayList<>();
		List<List<List<String>>> popularInfoTableList=new ArrayList<>();
		List<List<List<String>>> detailsInfoTableList =new ArrayList<>();
		List<List<List<String>>> detailsInfoOthersList=new ArrayList<>();//无效ip列表
		List<List<RebuildInfo>> rebuildInfoListList=new ArrayList<>();
		//List<List<String>> kindInfoList=new ArrayList<>();

		//HashMap<String, String> baseInfoCompareResult =null;
		//List<List<String>>  baseInfoCompareTable=null;
		/*if(baseInfoCompare){
			baseInfoCompareResult = new HashMap<String,String>();//"基本信息比较"表格上面的比较结果信息
			baseInfoCompareTable=initBaseInfoCompareTable(jObj,baseInfoCompareResult);//"基本信息比较"表格
		}*/
		List<String> titleList=new ArrayList<>();
		String[]  pcapFileNames=new String[2];

		PDFUti fPdfUti_hsy=new PDFUti();
		String username = (String) request.getSession().getAttribute("username");


		String pcapShortName_a=null;//aqynorm.pcap
		String pcapShortName_b= null;//dqydelay.pcap

		Map<String, Object> basicConclusion;
		Map<String, Object> fileDispose;
		//List<Map<String, Object>> basicConclusionList = new ArrayList<Map<String, Object>>();
		//List<Map<String, Object>> fileDisposeList = new ArrayList<Map<String, Object>>();
		if(!fileInfoList.get(1).getFileName().equals(jingpinFileName)) {
				FileInfo fileInfo1 = fileInfoList.get(1);
				FileInfo fileInfo2 = fileInfoList.get(0);
				fileInfoList.clear();
				fileInfoList.add(fileInfo1);
				fileInfoList.add(fileInfo2);
		}
		
		for(int k=0; k<2; k++){ //依次处理每一个FileInfo(一个循环对应生成一个pdf报告)
				fileInfo = fileInfoList.get(k);
				if(k==0){
					pcapShortName_a=fileInfo.getFileName();
				}else if(k==1){
					pcapShortName_b=fileInfo.getFileName();
				}
	
				//basicConclusion = basicConclusionInfo(username, fileInfo);//基本结论
				//basicConclusionList.add(basicConclusion);
				//fileDispose = fileDisposeInfo(fileInfo,basicConclusion.size()-1);//报文处理概述
				//fileDisposeList.add(fileDispose);
	
				List<RebuildInfo> rebuildInfoList=generateRebuildInfos(fileInfo);
				String title = URLDecoder.decode(request.getParameter("Title").trim(), "utf-8");
	
				List<List<String>> baseInfoTable =generateBaseInfoTable(fileInfo,df);//基本信息
	
				List<String> kindInfo = new ArrayList<String>();//全局
				StringBuffer serviceIP = new StringBuffer();//全局
	
				List<List<String>> detailsInfoTable =new ArrayList<>();//详细信息
				detailsInfoTable.add(generateDetailsInfoTitle(whoshowMap));//详细信息title
				loadDetailsInfoData(request,fileInfo, kindInfo, serviceIP, ipAddrMap, detailsInfoTable,whoshowMap);//详细信息data
	
				List<List<String>> popularInfoTable =generatePopularInfoTable(serviceIP,fileInfo,df,whoshowMap);//通用信息
	
	
				List<List<String>> detailsInfoOthers=null;
	
				if(whoshowMap.get("a1")){//是否显示"无效ip"
					detailsInfoOthers= generateDetailsInfoOthers(fileInfo);//(其他)详细信息(即无效ip列表)
				}
				pcapFileNames[k]=fileInfo.getFileName();//不带pcap
	
				baseInfoTableList.add(baseInfoTable);
				popularInfoTableList.add(popularInfoTable);//通用信息
				detailsInfoTableList.add(detailsInfoTable);
				detailsInfoOthersList.add(detailsInfoOthers);//无效ip
				rebuildInfoListList.add(rebuildInfoList);
	
				/*if(videoCompare){
					kindInfoList.add(kindInfo);
				}*/
	
				titleList.add(title);
		}

		FileInfo fileInfo1=fileInfoList.get(0);
		FileInfo fileInfo2=fileInfoList.get(1);

		//List<List<String>> timeToFirstTable = null;
		/*if(videoCompare){//勾选了"视频比较"
			timeToFirstTable = generateTimeToFirstTible_videoCompare(fileInfo1, fileInfo2);
		}*/


		//ContrastConclusion contrastConclusion =  contrastConclusionInfo_hsy(fileInfo1, fileInfo2, username);//对比结论(此时“基础数据”会产生最多3个错误！)
		
		//Integer adviceNum = (Integer) fileDisposeList.get(0).get("adviceNum");//“基础的一方在“基本结论”项中的问题数(建议数)
		//fileDisposeList.get(0).put("adviceNum",adviceNum+contrastConclusion.getAdvivcNum());//比较时“基础”的一方还会在"对比结论"中产生最多4个问题(建议)
		
		

		String filePath = fPdfUti_hsy.pdfMethod_Compare(request,
				baseInfoTableList,
				response,
				popularInfoTableList,//通用信息表格
				detailsInfoTableList,
				watermarker,
				titleList,
				pcapFileNames,
				username,
				detailsInfoOthersList,//无效ip列表
				rebuildInfoListList/*,
				kindInfoList,
				basicConclusionList,
				fileDisposeList,
				contrastConclusion*/);
		
		PDFUti.download(response, filePath);
	}

	private ContrastConclusion contrastConclusionInfo(FileInfo fileInfo1, FileInfo fileInfo2, String username) {
			//TODO
			ContrastConclusion contrastConclusion = new ContrastConclusion();
			
			StringBuffer title = new StringBuffer();
			StringBuffer exchangeTimeSecondTitle = new StringBuffer();
			String filename = fileInfo1.getFileName();
			List<List<String>> table1 = new ArrayList<List<String>>();
			List<List<String>> table2 = new ArrayList<List<String>>();
			List<List<String>> table3 = new ArrayList<List<String>>();
			List<String> tableTitle1 = new ArrayList<String>();
			List<String> tableTitle2 = new ArrayList<String>();
			List<String> tableTitle3 = new ArrayList<String>();
			List<String> fileinfolist11 = new ArrayList<String>();
			List<String> fileinfolist12 = new ArrayList<String>();
			List<String> fileinfolist21 = new ArrayList<String>();
			List<String> fileinfolist22 = new ArrayList<String>();
			List<String> fileinfolist31 = new ArrayList<String>();
			List<String> fileinfolist32 = new ArrayList<String>();
			boolean flag = true;//只要有一项业务差 为false
			title.append(filename);
			title.append("业务 可能存在 ");//yknorm 业务 可能存在  链路效率低 、 耗时长 的问题。（一级标题结论）
			//业务效率
			if (!compare(fileInfo1.getTimeEfficiencyAVG(), fileInfo2.getTimeEfficiencyAVG())) {
					contrastConclusion.setEffLinkTitle(filename + " 业务 效率 劣于 " + fileInfo2.getFileName());
					title.append("链路效率低/");
					flag = false;
					//效率最差链路
					contrastConclusion.setLowestEffLink("链路 "+fileInfo1.getLowestEffLink()+" 可能存在问题，请深度分析。 ");
			}
			//平均交互时间
			if (compare(fileInfo1.getAvgExchangeTime(), fileInfo2.getAvgExchangeTime())) {
				title.append("耗时长/");
				flag = false;
				exchangeTimeSecondTitle.append(filename);
				exchangeTimeSecondTitle.append(" 业务在 ");
				if (compare(fileInfo1.getAvgDnsDelsyTs(), fileInfo2.getAvgDnsDelsyTs())) {
					exchangeTimeSecondTitle.append("平均DNS时延(ms)");
					contrastConclusion.setDnsBigIp("链路IP" +fileInfo1.getDnsBigIp()+ "的 平均DNS时延 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgTcpTimeDelayed(),fileInfo2.getAvgTcpTimeDelayed())) {
					exchangeTimeSecondTitle.append("平均TCP建链时延(ms)");
					contrastConclusion.setTcpTimeDelayed("链路IP" + fileInfo1.getTcpTimeBigIp() + ",端口号" + fileInfo1.getTcpTimeBigPort() + " 的 TCP建链时延 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgTimeToFirstByte(),fileInfo2.getAvgTimeToFirstByte())) {
					exchangeTimeSecondTitle.append("平均建链后首包时延(ms)");
					contrastConclusion.setTimeToFirst("链路IP" + fileInfo1.getTimeToFirstBigIp() + ",端口号" + fileInfo1.getTimeToFirstBigPort() + " 的 平均建链后首包时延 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgOffTimeDelayed(),fileInfo2.getAvgOffTimeDelayed())) {
					exchangeTimeSecondTitle.append("平均断链时延(ms)");
					contrastConclusion.setOffTime("链路IP" + fileInfo1.getOffTimeBigIp() + ",端口号" + fileInfo1.getOffTimeBigPort() + " 的 平均断链时延(ms)	 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgRttTime(),fileInfo2.getAvgRttTime())) {
					exchangeTimeSecondTitle.append("平均RTT时间(ms)");
					contrastConclusion.setRttTime("链路IP" + fileInfo1.getRttTimeBigIp() + ",端口号" + fileInfo1.getRttTimeBigPort() + " 的 平均平均RTT时间(ms)	 指标差，建议深度分析。");
				}
				exchangeTimeSecondTitle.append("方面可能存在问题，请深度分析。");
			}
			List<List<Integer>> packetLenData1 = fileInfo1.getPacketLenData();
			List<List<Integer>> packetLenData2 = fileInfo2.getPacketLenData();
			int small1 = 0;
			int big1 = 0;
			int middle1 = 0;
			int small2 = 0;
			int big2 = 0;
			int middle2 = 0;
			int all1 = 0;
			int all2 = 0;
			List<Integer> list;
			int packetLen;
			for(int i=0; i<packetLenData1.size(); i++) {
				list = packetLenData1.get(i);
				all1 += list.size();
				for(int j=0; j<list.size(); j++) {
					packetLen = list.get(j);
					if (packetLen < 100) {
						small1 ++;
					} else if(packetLen < 1000 && packetLen > 100){
						middle1 ++;
					} else {
						big1 ++;
					}
				}
			}
			for(int i=0; i<packetLenData2.size(); i++) {
				list = packetLenData2.get(i);
				all2 += list.size();
				for(int j=0; j<list.size(); j++) {
					packetLen = list.get(j);
					if (packetLen < 100) {
						small2 ++;
					} else if(packetLen < 1000 && packetLen > 100){
						middle2 ++;
					} else {
						big2 ++;
					}
				}
			}
	
			if (small1/all1 > small2/all2) {
				title.append("小包多/");
				flag = false;
				//效率最差链路
				contrastConclusion.setPacketLenData("小包多："+filename+"业务 （大/中）包比例偏低，建议深度分析");
				fileinfolist21.add(filename);
				fileinfolist21.add(String.valueOf(small1));
				fileinfolist21.add(String.valueOf(middle1));
				fileinfolist21.add(String.valueOf(big1));
				fileinfolist22.add(fileInfo2.getFileName());
				fileinfolist22.add(String.valueOf(small2));
				fileinfolist22.add(String.valueOf(middle2));
				fileinfolist22.add(String.valueOf(big2));
			}
	
			if (compare(fileInfo1.getTcpCount(),fileInfo2.getTcpCount())) {
				title.append("链路多/");
				flag = false;
				contrastConclusion.setTcpCount("链路数多，"+filename+" 业务IP链路"+fileInfo1.getTcpCount()+"条，高于竞品，建议深度分析。");
				fileinfolist31.add(filename);
				fileinfolist31.add(String.valueOf(fileInfo1.getPacketCount()));
				fileinfolist31.add(String.valueOf(fileInfo1.getIpCount()));
				fileinfolist31.add(String.valueOf(fileInfo1.getTcpCount()));
				fileinfolist31.add(String.valueOf(fileInfo1.getExchangeFlowCount()));
				fileinfolist32.add(fileInfo2.getFileName());
				fileinfolist32.add(String.valueOf(fileInfo2.getPacketCount()));
				fileinfolist32.add(String.valueOf(fileInfo2.getIpCount()));
				fileinfolist32.add(String.valueOf(fileInfo2.getTcpCount()));
				fileinfolist32.add(String.valueOf(fileInfo2.getExchangeFlowCount()));
			}
	
	
			contrastConclusion.setExchangeTimeSecondTitle(exchangeTimeSecondTitle.toString());
			title.append("的问题");
			if (flag) {
				title.delete(0, title.length());
			}
			contrastConclusion.setTitle(title.toString());
			tableTitle1.add("业务名");//
			tableTitle1.add("平均DNS时延(ms)");
			tableTitle1.add("平均TCP建链时延(ms)");
			tableTitle1.add("平均建链后首包时延(ms)");
			tableTitle1.add("平均断链时延(ms)");
			tableTitle1.add("平均RTT时间(ms)");
			tableTitle1.add("平均交互时间(ms)");
			tableTitle2.add("业务名");//
			tableTitle2.add("小包");
			tableTitle2.add("中包");
			tableTitle2.add("大包");
			tableTitle3.add("业务名");//
			tableTitle3.add("包数");
			tableTitle3.add("IP数");
			tableTitle3.add("链路数");
			tableTitle3.add("交互流量");
			fileinfolist11.add(filename);
			fileinfolist11.add(fileInfo1.getAvgDnsDelsyTs());
			fileinfolist11.add(fileInfo1.getAvgTcpTimeDelayed());
			fileinfolist11.add(fileInfo1.getAvgTimeToFirstByte());
			fileinfolist11.add(fileInfo1.getAvgOffTimeDelayed());
			fileinfolist11.add(fileInfo1.getAvgRttTime());
			fileinfolist11.add(fileInfo1.getAvgExchangeTime());
			fileinfolist12.add(fileInfo2.getFileName());
			fileinfolist12.add(fileInfo2.getAvgDnsDelsyTs());
			fileinfolist12.add(fileInfo2.getAvgTcpTimeDelayed());
			fileinfolist12.add(fileInfo2.getAvgTimeToFirstByte());
			fileinfolist12.add(fileInfo2.getAvgOffTimeDelayed());
			fileinfolist12.add(fileInfo2.getAvgRttTime());
			fileinfolist12.add(fileInfo2.getAvgExchangeTime());
			table1.add(tableTitle1);
			table1.add(fileinfolist11);
			table1.add(fileinfolist12);
			table2.add(tableTitle2);
			table2.add(fileinfolist21);
			table2.add(fileinfolist22);
			table3.add(tableTitle3);
			table3.add(fileinfolist31);
			table3.add(fileinfolist32);
			contrastConclusion.setTable1(table1);
			contrastConclusion.setTable2(table2);
			contrastConclusion.setTable3(table3);
			return contrastConclusion;
	}
	
	
	private ContrastConclusion contrastConclusionInfo_hsy(FileInfo fileInfo1, FileInfo fileInfo2, String username) {
		//TODO
		ContrastConclusion contrastConclusion = new ContrastConclusion();
		int advivcNum=0;
		StringBuffer title1 = new StringBuffer();
		StringBuffer title3_2 = new StringBuffer();
		String filename = fileInfo1.getFileName();
		boolean flag = true;//只要有一项业务差 为false
		title1.append(filename);
		title1.append("业务 可能存在 ");//yknorm 业务 可能存在  链路效率低 、 耗时长 的问题。（一级标题结论）
		
		//业务效率
		if (!compare(fileInfo1.getTimeEfficiencyAVG(), fileInfo2.getTimeEfficiencyAVG())) {
				contrastConclusion.setTitle3(filename + " 业务 效率 劣于 " + fileInfo2.getFileName()+"。");
				title1.append("链路效率低/");
				flag = false;
				contrastConclusion.setTitle3_1("链路效率低:"+fileInfo1.getLowestEffLink()+" 可能存在问题，请深度分析。 ");
				advivcNum++;
		}
		
		//平均交互时间
		if (compare(fileInfo1.getAvgExchangeTime(), fileInfo2.getAvgExchangeTime())) {
				title1.append("耗时长/");
				title3_2.append("耗时长:"+filename+" 业务在");
				flag = false;
				
				if (compare(fileInfo1.getAvgDnsDelsyTs(), fileInfo2.getAvgDnsDelsyTs())) {
						title3_2.append("DNS时延、");					
						contrastConclusion.getList_title3_2().add("链路IP" +fileInfo1.getDnsBigIp()+ "的 DNS时延 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgTcpTimeDelayed(),fileInfo2.getAvgTcpTimeDelayed())) {
						title3_2.append("TCP建链时延、");					
						contrastConclusion.getList_title3_2().add("链路IP" + fileInfo1.getTcpTimeBigIp() + ",端口号" + fileInfo1.getTcpTimeBigPort() + " 的 TCP建链时延 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgTimeToFirstByte(),fileInfo2.getAvgTimeToFirstByte())) {
						title3_2.append("建链后首包时延、");					
						contrastConclusion.getList_title3_2().add("链路IP" + fileInfo1.getTimeToFirstBigIp() + ",端口号" + fileInfo1.getTimeToFirstBigPort() + " 的 平均建链后首包时延 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgOffTimeDelayed(),fileInfo2.getAvgOffTimeDelayed())) {
						title3_2.append("断链时延、");					
						contrastConclusion.getList_title3_2().add("链路IP" + fileInfo1.getOffTimeBigIp() + ",端口号" + fileInfo1.getOffTimeBigPort() + " 的 平均断链时延(ms)	 指标差，建议深度分析。");
				}
				if (compare(fileInfo1.getAvgRttTime(),fileInfo2.getAvgRttTime())) {
						title3_2.append("RTT时间、");					
						contrastConclusion.getList_title3_2().add("链路IP" + fileInfo1.getRttTimeBigIp() + ",端口号" + fileInfo1.getRttTimeBigPort() + " 的 平均平均RTT时间(ms)	 指标差，建议深度分析。");
				}
				title3_2.append("方面可能存在问题，请深度分析。");
				advivcNum++;
		}
		
		List<List<Integer>> packetLenData1 = fileInfo1.getPacketLenData();
		List<List<Integer>> packetLenData2 = fileInfo2.getPacketLenData();
		int small1 = 0;
		int big1 = 0;
		int middle1 = 0;
		int small2 = 0;
		int big2 = 0;
		int middle2 = 0;
		int all1 = 0;
		int all2 = 0;
		List<Integer> list;
		int packetLen;
		for(int i=0; i<packetLenData1.size(); i++) {
				list = packetLenData1.get(i);
				all1 += list.size();
				for(int j=0; j<list.size(); j++) {
						packetLen = list.get(j);
						if (packetLen < 100) {
							small1 ++;
						} else if(packetLen < 1000 && packetLen > 100){
							middle1 ++;
						} else {
							big1 ++;
						}
				}
		}
		for(int i=0; i<packetLenData2.size(); i++) {
				list = packetLenData2.get(i);
				all2 += list.size();
				for(int j=0; j<list.size(); j++) {
						packetLen = list.get(j);
						if (packetLen < 100) {
							small2 ++;
						} else if(packetLen < 1000 && packetLen > 100){
							middle2 ++;
						} else {
							big2 ++;
						}
				}
		}

		if (small1/all1 > small2/all2) {
				title1.append("小包多/");
				flag = false;
				//效率最差链路
				contrastConclusion.setTitle3_3("小包多："+filename+"业务 （大/中）包比例偏低，建议深度分析");
				
				ArrayList<String> listxxx = new ArrayList<String>();
				listxxx.add("业务名");
				listxxx.add("小包");
				listxxx.add("中包");
				listxxx.add("大包");
				contrastConclusion.getTable_title3_3().add(listxxx);
				
				listxxx = new ArrayList<String>();
				listxxx.add(filename);
				listxxx.add(String.valueOf(small1));
				listxxx.add(String.valueOf(middle1));
				listxxx.add(String.valueOf(big1));
				contrastConclusion.getTable_title3_3().add(listxxx);
				
				listxxx = new ArrayList<String>();
				listxxx.add(fileInfo2.getFileName());
				listxxx.add(String.valueOf(small2));
				listxxx.add(String.valueOf(middle2));
				listxxx.add(String.valueOf(big2));
				contrastConclusion.getTable_title3_3().add(listxxx);
				advivcNum++;
		}

		if (compare(fileInfo1.getTcpCount(),fileInfo2.getTcpCount())) {
				contrastConclusion.setTitle3_4("链路数多，"+filename+" 业务IP链路"+fileInfo1.getTcpCount()+"条，高于竞品，建议深度分析。");
				title1.append("链路多/");
				flag = false;
				
				ArrayList<CellData> listxxx = new ArrayList<CellData>();
				listxxx.add(new CellData("业务名"));
				listxxx.add(new CellData("包数(个)"));
				listxxx.add(new CellData("IP数(个)"));
				listxxx.add(new CellData("链路数(个)"));
				listxxx.add(new CellData("交互流量(byte)"));
				contrastConclusion.getTable_title3_4().add(listxxx);
				
				listxxx = new ArrayList<CellData>();//差的要加颜色
				listxxx.add(new CellData(filename));
				
				if(isBad(fileInfo1.getPacketCount(), fileInfo2.getPacketCount(), -1)){
						listxxx.add(new CellData(String.valueOf(fileInfo1.getPacketCount()), Color.red));
				}else{
						listxxx.add(new CellData(String.valueOf(fileInfo1.getPacketCount())));
				}
				
				
				if(isBad(fileInfo1.getIpCount(), fileInfo2.getIpCount(), -1)){
						listxxx.add(new CellData(String.valueOf(fileInfo1.getIpCount()),Color.red));
				}else{
						listxxx.add(new CellData(String.valueOf(fileInfo1.getIpCount())));
				}
				
				if(isBad(fileInfo1.getTcpCount(), fileInfo2.getTcpCount(), 1)){
						listxxx.add(new CellData(String.valueOf(fileInfo1.getTcpCount()),Color.red));
				}else{
						listxxx.add(new CellData(String.valueOf(fileInfo1.getTcpCount())));
				}
				
				
				if(isBad(fileInfo1.getExchangeFlowCount(), fileInfo2.getExchangeFlowCount(), -1)){
						listxxx.add(new CellData(String.valueOf(fileInfo1.getExchangeFlowCount()),Color.red));
				}else{
						listxxx.add(new CellData(String.valueOf(fileInfo1.getExchangeFlowCount())));
				}
				
				
				contrastConclusion.getTable_title3_4().add(listxxx);
				
				listxxx = new ArrayList<CellData>();
				listxxx.add(new CellData(fileInfo2.getFileName()));
				listxxx.add(new CellData(String.valueOf(fileInfo2.getPacketCount())));
				listxxx.add(new CellData(String.valueOf(fileInfo2.getIpCount())));
				listxxx.add(new CellData(String.valueOf(fileInfo2.getTcpCount())));
				listxxx.add(new CellData(String.valueOf(fileInfo2.getExchangeFlowCount())));
				contrastConclusion.getTable_title3_4().add(listxxx);
				advivcNum++;
		}
		title1.append("的问题。");
		if (flag) {//不存在 链路效率低 、耗时长、小包多、链路多4个问题中的任何一个 
			title1.delete(0, title1.length());
		}
		
		if(contrastConclusion.getTitle3()!=null&&contrastConclusion.getTitle3().length()>0){
				ArrayList<CellData> list2 = new ArrayList<CellData>();
				list2.add(new CellData("业务名"));//
				list2.add(new CellData("平均DNS时延(ms)"));
				list2.add(new CellData("平均TCP建链时延(ms)"));
				list2.add(new CellData("平均建链后首包时延(ms)"));
				list2.add(new CellData("平均断链时延(ms)"));
				list2.add(new CellData("平均RTT时间(ms)"));
				list2.add(new CellData("平均交互时间(ms)"));
				contrastConclusion.getTable_title3().add(list2);
				
				list2 = new ArrayList<CellData>();
				list2.add(new CellData(filename));
				
				if(isBad(Double.valueOf(fileInfo1.getAvgDnsDelsyTs()), Double.valueOf(fileInfo2.getAvgDnsDelsyTs()), -1)){
						list2.add(new CellData(fileInfo1.getAvgDnsDelsyTs(),Color.red));
				}else{
						list2.add(new CellData(fileInfo1.getAvgDnsDelsyTs()));
				}
				
				if(isBad(Double.valueOf(fileInfo1.getAvgTcpTimeDelayed()), Double.valueOf(fileInfo2.getAvgTcpTimeDelayed()), -1)){
						list2.add(new CellData(fileInfo1.getAvgTcpTimeDelayed(),Color.red));
				}else{
						list2.add(new CellData(fileInfo1.getAvgTcpTimeDelayed()));
				}
				
				
				if(isBad(Double.valueOf(fileInfo1.getAvgTimeToFirstByte()), Double.valueOf(fileInfo2.getAvgTimeToFirstByte()), -1)){
						list2.add(new CellData(fileInfo1.getAvgTimeToFirstByte(),Color.red));
				}else{
						list2.add(new CellData(fileInfo1.getAvgTimeToFirstByte()));
				}	
				
				
				if(isBad(Double.parseDouble(fileInfo1.getAvgOffTimeDelayed()), Double.parseDouble(fileInfo2.getAvgOffTimeDelayed()), -1)){
						list2.add(new CellData(fileInfo1.getAvgOffTimeDelayed(),Color.red));
				}else{
						list2.add(new CellData(fileInfo1.getAvgOffTimeDelayed()));
				}
				
				if(isBad(Double.parseDouble(fileInfo1.getAvgRttTime()), Double.parseDouble(fileInfo2.getAvgRttTime()), -1)){
						list2.add(new CellData(fileInfo1.getAvgRttTime(),Color.red));
				}else{
						list2.add(new CellData(fileInfo1.getAvgRttTime()));
				}
				
				if(isBad(Double.parseDouble(fileInfo1.getAvgExchangeTime()), Double.parseDouble(fileInfo2.getAvgExchangeTime()), -1)){
					list2.add(new CellData(fileInfo1.getAvgExchangeTime(),Color.red));
				}else{
					list2.add(new CellData(fileInfo1.getAvgExchangeTime()));
				}
				contrastConclusion.getTable_title3().add(list2);
				
				list2 = new ArrayList<CellData>();
				list2.add(new CellData(fileInfo2.getFileName()));
				list2.add(new CellData(fileInfo2.getAvgDnsDelsyTs()));
				list2.add(new CellData(fileInfo2.getAvgTcpTimeDelayed()));
				list2.add(new CellData(fileInfo2.getAvgTimeToFirstByte()));
				list2.add(new CellData(fileInfo2.getAvgOffTimeDelayed()));
				list2.add(new CellData(fileInfo2.getAvgRttTime()));
				list2.add(new CellData(fileInfo2.getAvgExchangeTime()));
				contrastConclusion.getTable_title3().add(list2);
		}
		
		
		contrastConclusion.setTitle1(title1.toString());
		contrastConclusion.setTitle3_2(title3_2.toString());
		if(contrastConclusion.getTitle1()!=null&&contrastConclusion.getTitle1().length()>0){
			  contrastConclusion.setTitle2("这些问题导致交互时间长。");
		}
		contrastConclusion.setAdvivcNum(advivcNum);
		return contrastConclusion;
}
	
	
	
	private boolean isBad(double data_base,double data_jingpin,int compareRule){//1和-1()判断是否“差”，1表示越大越好，-1表示越小越好
			    if((data_base*compareRule)>(data_jingpin*compareRule)){//基础"优于"竞品
						return false;//基础不差
				}else if((data_base*compareRule)<(data_jingpin*compareRule)){
						return true;//基础差
				}else if(data_base==data_base){
						return false;//一样(基础不差)
				}
			    return false;
			   
}

	
	
	

	private Boolean compare(int para1, int para2) {
		boolean reslut = false;
		if (para1 > 0 && para2 > 0 && para1 > para2) {
			reslut = true;
		}
		return reslut;
	}

	private Boolean compare(String para1, String para2) {
		boolean reslut = false;
		if (!isNa(para1) && !isNa(para2)) {
			log.info("zheshi 1--" + para1 + "---zheshi2----" + para2 + "----");
			if (Double.valueOf(para1) > Double.valueOf(para2)) {
				reslut = true;
			}
		}
		return reslut;
	}

	private Boolean isNa(String para) {
		boolean reslut;
		if (para == null || para.equals("") || para.contains("N/A")) {
			reslut = true;
		} else {
			reslut = false;
		}
		return reslut;
	}

	private List<List<String>> initBaseInfoCompareTable(JSONObject jObj,HashMap<String, String> baseInfoComparResul) {
		List<List<String>> baseInfoComparList= new ArrayList<>();

		List<String> titleList=  new ArrayList<>();
		List<String> dataList= null;

		titleList.add("名称");
		titleList.add("交互时间(ms)");
		titleList.add("交互流量(byte)");
		titleList.add("ip数(个)");
		titleList.add("链路个数");
		titleList.add("链路效率(%)");
		titleList.add("是否频繁拆建链");
		baseInfoComparList.add(titleList);

		try {
			JSONArray xxx = JSONArray.fromObject(jObj.get("baseInfoCompareObj"));//[{"base":{"fileName":"aqynorm.pcap","commuTime":"99266.69","commuFlow":2645131,"ipNum":8,"linkNum":17,"frequRebuild":"否"},"jingpin":{"fileName":"aqydelay.pcap","commuTime":"37389.82","commuFlow":1055595,"ipNum":6,"linkNum":14,"frequRebuild":"否"},"compareResult":{"commuTime":"优于","commuFlow":"优于","ipNum":"优于","linkNum":"优于","frequRebuild":"不存在"}}]
			List<HashMap<String, Object>> list =  (List<HashMap<String, Object>>) JSONArray.toCollection(xxx, HashMap.class);

			HashMap<String, Object> jsonObj = list.get(0);
			if (jsonObj == null || jsonObj.size() == 0) {
				baseInfoComparList.clear();
				return baseInfoComparList;
			}
			Set<Entry<String, Object>> entrySet = jsonObj.entrySet();
			HashMap<String, String> baseMap = new HashMap<String,String>();
			HashMap<String, String> jingpinMap = new HashMap<String,String>();
			for (Entry<String, Object> entry : entrySet) {
				String key=entry.getKey();
				JSONArray jsonArray = JSONArray.fromObject( entry.getValue());
				List<HashMap<String, String>> list1 =  (List<HashMap<String, String>>) JSONArray.toCollection(jsonArray, HashMap.class);

				if("base".equals(key)){
					baseMap = list1.get(0);
				}else if("jingpin".equals(key)){
					jingpinMap = list1.get(0);
				}else if("compareResult".equals(key)){
					HashMap<String, String> map = list1.get(0);
					baseInfoComparResul.put("commuTime", map.get("commuTime"));
					baseInfoComparResul.put("commuFlow", map.get("commuFlow"));
					baseInfoComparResul.put("ipNum", map.get("ipNum"));
					baseInfoComparResul.put("linkNum", map.get("linkNum"));
					baseInfoComparResul.put("linkNum", map.get("linkNum"));
					baseInfoComparResul.put("timeEfficiencyAVG", map.get("timeEfficiencyAVG"));
					baseInfoComparResul.put("frequRebuild", map.get("frequRebuild"));
				}
			}

			dataList=new ArrayList<>();
			dataList.add(baseMap.get("fileName")+"(基础数据)");
			dataList.add(String.valueOf(baseMap.get("commuTime")));
			dataList.add(String.valueOf(baseMap.get("commuFlow")));
			dataList.add(String.valueOf(baseMap.get("ipNum")));
			dataList.add(String.valueOf(baseMap.get("linkNum")));
			dataList.add(String.valueOf(baseMap.get("timeEfficiencyAVG")));
			dataList.add(baseMap.get("frequRebuild"));
			baseInfoComparList.add(dataList);

			dataList=new ArrayList<>();
			dataList.add(jingpinMap.get("fileName")+"(竞品数据)");
			dataList.add(String.valueOf(jingpinMap.get("commuTime")));
			dataList.add(String.valueOf(jingpinMap.get("commuFlow")));
			dataList.add(String.valueOf(jingpinMap.get("ipNum")));
			dataList.add(String.valueOf(jingpinMap.get("linkNum")));
			dataList.add(String.valueOf(jingpinMap.get("timeEfficiencyAVG")));
			dataList.add(jingpinMap.get("frequRebuild"));
			baseInfoComparList.add(dataList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baseInfoComparList;
	}

	@SuppressWarnings({ "unchecked"})
	@RequestMapping(value="/CreatePDF.do", method=RequestMethod.POST)//与上面的CreatePDF不同的是，两个pcap信息生成到一个pdf文件中，并且最顶部要包含比较的信息(即页面中top处的比较信息)
	public void CreatePDF(HttpServletRequest request,
						  HttpServletResponse response,
						  String[] data1,//前台传递的可能是是超长字段(所以要分多个字段)
						  String watermarker,//水印信息
						  boolean showCompareConclusion,
						  String baseFileName,
						  String jingpinFileName) throws Exception{
		log.info("生成pdf(视频比较生成到单个文件)报告开始----------------");
		StringBuilder data1Str=new StringBuilder("");
		if(data1!=null&&data1.length>0){
			for (int i = 0; i < data1.length; i++) {
				data1Str.append(data1[i]);
			}
		}

		if(showCompareConclusion){
			create2pcapIntoOnePDF(request, response, data1Str.toString(), watermarker,baseFileName, jingpinFileName);//jingpinFileName带.pcap
		}else{
			createPdfEachPcapEachFile(request, response, data1Str.toString(), watermarker);
		}
		log.info("生成pdf报告(视频比较)结束------------");
	}
	
	

	private List<List<String>> generateDetailsInfoOthers(FileInfo fileInfo) {
		List<List<String>> detailsInfoOthers = new ArrayList<>();//(其他)详细信息
		List<String> othersTitle = new ArrayList<String>();
		othersTitle.add("业务名");
		othersTitle.add("无效ip");
		List<String> othersInfo = new ArrayList<String>();
		othersInfo.add(fileInfo.getFileName());

		StringBuffer othersIPs = new StringBuffer();
		List<String> othersIP = fileInfo.getOthers();
		for(int i=0; i<othersIP.size(); i++) {
			othersIPs.append(othersIP.get(i));
			othersIPs.append(",");
		}

		if (othersIPs.length() > 1) {
			othersIPs.deleteCharAt(othersIPs.length()-1);
		}
		othersInfo.add(othersIPs.toString());
		detailsInfoOthers.add(othersTitle);
		detailsInfoOthers.add(othersInfo);

		return detailsInfoOthers;
	}




	private void loadDetailsInfoData(HttpServletRequest request,FileInfo fileInfo,List<String> kindInfo,StringBuffer serviceIP,Map<String, String> ipAddrMap,List<List<String>> detailsInfoTable,HashMap<String,Boolean> whoShowMap) throws InterruptedException, ExecutionException, Exception{


		List<String> detailsInfoData;//局部
		JSONArray serviceIPJSON = JSONArray.fromObject(fileInfo.getServiceIP());//局部
		List<ServiceIP> serviceIPs = (List<ServiceIP>) JSONArray.toCollection(serviceIPJSON, ServiceIP.class);//局部
		JSONArray ipInfoJSON;//局部
		List<ServiceIPInfo> ipInfoList = new ArrayList<ServiceIPInfo>();//局部




		for(int i=0; i<serviceIPs.size(); i++) {
			serviceIP.append(" ");
			serviceIP.append(serviceIPs.get(i).getServiceIP());
			serviceIP.append(",");

			ipInfoJSON = JSONArray.fromObject(serviceIPs.get(i).getServiceIPInfo());
			ipInfoList = (List<ServiceIPInfo>) JSONArray.toCollection(ipInfoJSON, ServiceIPInfo.class);
			for(int j=0; j<ipInfoList.size(); j++) {
				if (ipInfoList.get(j).getKind() < 0) {
					kindInfo.add(serviceIPs.get(i).getServiceIP() + "(" + ipInfoList.get(j).getPorts() + ")");
				}
				detailsInfoData = new ArrayList<String>();
				detailsInfoData.add(serviceIPs.get(i).getServiceIP());//标题：serviceIP(一显示)
				if(whoShowMap.get("a3")){//标题：测试地归属(a3)
					detailsInfoData.add(ipInfoList.get(j).getTestAttribution());
				}

				if(whoShowMap.get("a5")){//标题:dns时延(a5)
					if (ipInfoList.get(j).getDnsDelsyTs() == null || ipInfoList.get(j).getDnsDelsyTs().equals("")) {
						detailsInfoData.add("N/A");
					} else {
						detailsInfoData.add(ipInfoList.get(j).getDnsDelsyTs());
					}
				}

				detailsInfoData.add(String.valueOf(ipInfoList.get(j).getPorts()));//标题:端口号(一定显示)

				if(whoShowMap.get("a6")){//标题：tcp建链时延(a6)
					detailsInfoData.add(ipInfoList.get(j).getTcpTimeDelayed());
				}

				if(whoShowMap.get("a4")){//标题：请求类别(a4)
					detailsInfoData.add(ipInfoList.get(j).getRequestCategoryBreviary());
				}

				if(whoShowMap.get("a8")){//标题：Time to First Byte(ms)(a8)
					if (ipInfoList.get(j).getTimeToFirstByte().equals("N/A")) {
						detailsInfoData.add("N/A");
					} else {
						detailsInfoData.add(ipInfoList.get(j).getTimeToFirstByte());
					}
				}

				if(whoShowMap.get("a7")){//标题:包数(个)(a7)
					detailsInfoData.add(String.valueOf(ipInfoList.get(j).getPacketCount()));
				}

				if(whoShowMap.get("a9")){//标题:交互流量（a9）
					if (ipInfoList.get(j).getExchangeFlow() != null && !ipInfoList.get(j).getExchangeFlow().equals("")) {
						if (ipInfoList.get(j).getExchangeFlow().contains(".")) {
							detailsInfoData.add(ipInfoList.get(j).getExchangeFlow().substring(0,ipInfoList.get(j).getExchangeFlow().indexOf(".")));
						} else {
							detailsInfoData.add(ipInfoList.get(j).getExchangeFlow());
						}
					} else {
						detailsInfoData.add("0");
					}
				}

				if(whoShowMap.get("a10")){//标题：交互时间(a10)
					detailsInfoData.add(ipInfoList.get(j).getExchangeTime());
				}


				if(whoShowMap.get("a11")){//标题：链路时间有效率(%)(a11)
					if (ipInfoList.get(j).getTimeEfficiency().equals("0.00")) {
						detailsInfoData.add("N/A");
					} else {
						detailsInfoData.add(ipInfoList.get(j).getTimeEfficiency());
					}
				}

				if(whoShowMap.get("a12")){//标题：重传识别（次）(a12)
					detailsInfoData.add(String.valueOf(ipInfoList.get(j).getAgainRequestCount()));
				}

				if(whoShowMap.get("a14")){//标题：平均RTT时间(ms)(a14)
					detailsInfoData.add(ipInfoList.get(j).getRttTime());
				}

				if(whoShowMap.get("a15")){//标题：断链时延(ms)(a15)
					detailsInfoData.add(ipInfoList.get(j).getOffTimeDelayed());
				}

				if(whoShowMap.get("a16")){//标题：与上条链路串并行关系(a16)
					detailsInfoData.add(ipInfoList.get(j).getLastRelationship());
				}

				if(whoShowMap.get("a17")){//标题:与上条链路间隔时延(a17)
					detailsInfoData.add(ipInfoList.get(j).getLastTimeDelayed());
				}

				if(whoShowMap.get("a18")){//标题：串行链路频繁拆建链判断(a18)
					detailsInfoData.add(String.valueOf(ipInfoList.get(j).getOftenOffVerdict()));
				}

				if(whoShowMap.get("a2")&&ipAddrMap!=null){//标题：IP归属地(a2)(目前这个有问题，为Null,前台展示时通过额外发送ajax访问数据库查询的，所以前台传递的pdfData暂不包含该信息)
					String ipAddr=ipAddrMap.get(serviceIPs.get(i).getServiceIP());
					if(ipAddr==null||ipAddr.length()==0){
						ipAddr="N/A";
					}
					detailsInfoData.add(ipAddr);
				}
				detailsInfoTable.add(detailsInfoData);
			}
		}
	}


	private List<String> generateDetailsInfoTitle(HashMap<String, Boolean> whoShowMap) {
		List<String> detailsInfoTitle = new ArrayList<String>();
		detailsInfoTitle.add("server IP");//必须显示
		if(whoShowMap.get("a3")){
			detailsInfoTitle.add("测试地归属");
		}

		if(whoShowMap.get("a5")){
			detailsInfoTitle.add("DNS时延(ms)");
		}

		detailsInfoTitle.add("端口号");//必须显示

		if(whoShowMap.get("a6")){
			detailsInfoTitle.add("TCP建链时延(ms)");
		}

		if(whoShowMap.get("a4")){
			detailsInfoTitle.add("请求类别");
		}

		if(whoShowMap.get("a8")){
			detailsInfoTitle.add("建链后首包延时(ms)");
		}

		if(whoShowMap.get("a7")){
			detailsInfoTitle.add("包数(个)");
		}

		if(whoShowMap.get("a9")){
			detailsInfoTitle.add("交互流量(byte)");
		}

		if(whoShowMap.get("a10")){
			detailsInfoTitle.add("交互时间(ms)");
		}

		if(whoShowMap.get("a11")){
			detailsInfoTitle.add("链路时间有效率(%)");
		}

		if(whoShowMap.get("a12")){
			detailsInfoTitle.add("重传识别(次)");
		}

		if(whoShowMap.get("a14")){
			detailsInfoTitle.add("平均RTT时间(ms)");
		}

		if(whoShowMap.get("a15")){
			detailsInfoTitle.add("断链时延(ms)");
		}

		if(whoShowMap.get("a16")){
			detailsInfoTitle.add("与上条链路串并行关系");
		}

		if(whoShowMap.get("a17")){
			detailsInfoTitle.add("与上条链路间隔时延(ms)");
		}

		if(whoShowMap.get("a18")){
			detailsInfoTitle.add("串行链路频繁拆建链判断(0~2s)");
		}

		if(whoShowMap.get("a2")){
			detailsInfoTitle.add("服务器IP归属");
		}
		return detailsInfoTitle;
	}



	private List<List<String>> generatePopularInfoTable(StringBuffer serviceIP,FileInfo fileInfo,DecimalFormat df,HashMap<String, Boolean> whoShowMap) {
		List<List<String>> popularInfoTable =new ArrayList<>();//通用信息（目测是对统计信息统计得来的）

		List<String> popularInfoTitle = new ArrayList<String>();
		List<String> popularInfoData = new ArrayList<String>();
		popularInfoTitle.add("Server IP");
		popularInfoTitle.add("DNS时延(ms)");

		if(whoShowMap.get("a8")){
			popularInfoTitle.add("链路数");
		}

		popularInfoTitle.add("包数(个)");
		popularInfoTitle.add("交互流量(byte)");
		popularInfoTitle.add("交互时间(ms)");

		if(whoShowMap.get("a13")){
			popularInfoTitle.add("平均链路时间有效率(%)");
		}


		popularInfoData.add(serviceIP.deleteCharAt(serviceIP.length()-1).toString());
		popularInfoData.add(String.valueOf(fileInfo.getDnsTimeDelayedcount()));

		if(whoShowMap.get("a8")){
			popularInfoData.add(String.valueOf(fileInfo.getTcpCount()));
		}

		popularInfoData.add(String.valueOf(fileInfo.getPacketCount()));
		popularInfoData.add(df.format(fileInfo.getExchangeFlowCount()));
		popularInfoData.add(String.valueOf(fileInfo.getExchangeTimeCount()));

		if(whoShowMap.get("a13")){
			popularInfoData.add(fileInfo.getTimeEfficiencyAVG());
		}

		popularInfoTable.add(popularInfoTitle);
		popularInfoTable.add(popularInfoData);

		return popularInfoTable;
	}



	private List<List<String>> generateBaseInfoTable(FileInfo fileInfo,DecimalFormat df) {
		List<List<String>> baseInfoTable =new ArrayList<>();//基本信息
		List<String> baseInfoTitle = new ArrayList<String>();
		List<String> baseInfoData = new ArrayList<String>();
		baseInfoTitle.add("持续时间(ms)");
		baseInfoTitle.add("数据传输时间(ms)");
		baseInfoTitle.add("传输总包数(个)");
		baseInfoTitle.add("传输量最大IP及端口");
		baseInfoTitle.add("传输总字节(byte)");
		baseInfoTitle.add("总链路数(个)");
		baseInfoTitle.add("链路最多IP");
		baseInfoTitle.add("链路最多IP(链路数)");


		baseInfoData.add(fileInfo.getAllKeepTimes());
		baseInfoData.add(fileInfo.getAllFileDataTimes());
		baseInfoData.add(String.valueOf(fileInfo.getPacketCount()));
		baseInfoData.add(fileInfo.getMaxFlowIP() + ": " + fileInfo.getMaxFlowPort());
		baseInfoData.add(df.format(fileInfo.getExchangeFlowCount()));
//		    baseInfoData.add(String.valueOf(fileInfo.getExchangeFlowCount()));
		baseInfoData.add(String.valueOf(fileInfo.getTcpCount()));
		baseInfoData.add(fileInfo.getMaxTCPip());
		baseInfoData.add(String.valueOf(fileInfo.getIpTCPCounts()));
		baseInfoTable.add(baseInfoTitle);
		baseInfoTable.add(baseInfoData);

		return baseInfoTable;
	}



	private List<RebuildInfo> generateRebuildInfos(FileInfo fileInfo) {
		JSONArray rebuild = JSONArray.fromObject(fileInfo.getRebuildInfo());//这一句不是废话吗？
		List<RebuildInfo> rebuildInfoList= (List<RebuildInfo>) JSONArray.toCollection(rebuild, RebuildInfo.class);
		return rebuildInfoList;
	}



	/*private List<String> generateTimeToFirst(FileInfo fileInfo) {
			List<String> timeToFirst =new ArrayList<String>();
		
			StringBuffer msg_TtFS = new StringBuffer();
			StringBuffer msg_TtFR = new StringBuffer();
			
			if(fileInfo.getAvgT1Rs() == 0) {
					timeToFirst.add("false");
					msg_TtFS.append("耗时多于");
			} else if(fileInfo.getAvgT1Rs() == 1) {
					timeToFirst.add("true");
					msg_TtFS.append("耗时等于");
			} else if(fileInfo.getAvgT1Rs() == 2) {
					timeToFirst.add("true");
					msg_TtFS.append("耗时低于");
			}
			msg_TtFS.append("竞品相关参数,");
			msg_TtFS.append("高于腾讯");
			msg_TtFS.append(fileInfo.getAvgTxT1Rs());
			msg_TtFS.append(",");
			msg_TtFS.append("高于爱奇艺");
			msg_TtFS.append(fileInfo.getAvgAqyT1Rs());
			msg_TtFS.append(".");
			timeToFirst.add(msg_TtFS.toString());
			
			if(fileInfo.getAvgT2Rs() == 0) {
					timeToFirst.add("false");
					msg_TtFR.append("耗时多于");
			} else if(fileInfo.getAvgT2Rs() == 1) {
					timeToFirst.add("true");
					msg_TtFR.append("耗时等于");
			} else if(fileInfo.getAvgT2Rs() == 2) {
					timeToFirst.add("true");
					msg_TtFR.append("耗时低于");
			}
			msg_TtFR.append("竞品相关参数.");
			
			if (fileInfo.getAvgTxT2Rs() != null && !fileInfo.getAvgTxT1Rs().equals("")) {
					msg_TtFR.append("高于腾讯");
					msg_TtFR.append(fileInfo.getAvgTxT2Rs());
			}
			if (fileInfo.getAvgAqyT2Rs() != null && !fileInfo.getAvgAqyT2Rs().equals("")) {
					msg_TtFR.append(",");
					msg_TtFR.append("高于爱奇艺");
					msg_TtFR.append(fileInfo.getAvgAqyT2Rs());
					msg_TtFR.append(".");
			}
			
			timeToFirst.add(msg_TtFR.toString());
			
			return timeToFirst;
	}*/


	/**
	 * 视频业务对比:TtF表格信息
	 * @param fileInfo1
	 * @param fileInfo2
	 * @return
	 */
	private List<List<String>> generateTimeToFirstTible_videoCompare(FileInfo fileInfo1,FileInfo fileInfo2) {
		List<List<String>> timeToFirstTible = null;
		List<String> ttftTitle;
		timeToFirstTible = new ArrayList<>();

		ttftTitle = new ArrayList<String>();
		ttftTitle.add("业务名");
		ttftTitle.add("TtFS(ms)");
		ttftTitle.add("TtFR(ms)");
		timeToFirstTible.add(ttftTitle);

		ttftTitle = new ArrayList<String>();//FileInfo1
		ttftTitle.add(fileInfo1.getFileName());
		ttftTitle.add(fileInfo1.getavgT1());
		ttftTitle.add(fileInfo1.getavgT2());
		timeToFirstTible.add(ttftTitle);

		ttftTitle = new ArrayList<String>();//FileInfo2
		ttftTitle.add(fileInfo2.getFileName());
		ttftTitle.add(fileInfo2.getavgT1());
		ttftTitle.add(fileInfo2.getavgT2());
		timeToFirstTible.add(ttftTitle);

		return timeToFirstTible;
	}

	
	

	/*private List<List<String>> generateTimeToFirstTible(FileInfo fileInfo) {
			List<List<String>> timeToFirstTible = null;
			List<String> ttftTitle;
			timeToFirstTible = new ArrayList<>();
			
			ttftTitle = new ArrayList<String>();
			ttftTitle.add("业务名");
			ttftTitle.add("TtFS");
			ttftTitle.add("TtFR");
			timeToFirstTible.add(ttftTitle);
			
			
			ttftTitle = new ArrayList<String>();
			ttftTitle.add("");			
			ttftTitle.add("时间(ms)");
			ttftTitle.add("相差百分比");
			ttftTitle.add("时间(ms)");
			ttftTitle.add("相差百分比");
			timeToFirstTible.add(ttftTitle);
			
			
			ttftTitle = new ArrayList<String>();
			ttftTitle.add("本次测试");			
			ttftTitle.add(fileInfo.getavgT1());
			ttftTitle.add("-");
			ttftTitle.add(fileInfo.getavgT2());
			ttftTitle.add("-");
			timeToFirstTible.add(ttftTitle);
			
			
			ttftTitle = new ArrayList<String>();
			ttftTitle.add("Tencent");			
			ttftTitle.add(String.valueOf(fileInfo.getavgTxT1()));
			if (fileInfo.getAvgTxT1Rs() == null || fileInfo.getAvgTxT1Rs().equals("")) {
					ttftTitle.add("-");
			} else {
					ttftTitle.add(fileInfo.getAvgTxT1Rs());
			}
			ttftTitle.add(String.valueOf(fileInfo.getavgTxT2()));
			if (fileInfo.getAvgTxT2Rs() == null || fileInfo.getAvgTxT2Rs().equals("")) {
					ttftTitle.add("-");
			} else {
					ttftTitle.add(fileInfo.getAvgTxT2Rs());
			}
			timeToFirstTible.add(ttftTitle);
			
			
			ttftTitle = new ArrayList<String>();
			ttftTitle.add("IQiyi");			
			ttftTitle.add(String.valueOf(fileInfo.getAvgAqyT1()));
			if (fileInfo.getAvgAqyT1Rs() == null || fileInfo.getAvgAqyT1Rs().equals("")) {
					ttftTitle.add("-");
			} else {
					ttftTitle.add(fileInfo.getAvgAqyT1Rs());
			}
			ttftTitle.add(String.valueOf(fileInfo.getavgAqyT2()));
			if (fileInfo.getAvgAqyT2Rs() == null || fileInfo.getAvgAqyT2Rs().equals("")) {
					ttftTitle.add("-");
			} else {
					ttftTitle.add(fileInfo.getAvgAqyT2Rs());
			}
			timeToFirstTible.add(ttftTitle);
			
			return timeToFirstTible;
	}
*/


	private List<FileInfo> generateFileInfoList(JSONObject jObj) {
		JSONArray fileInfosJSON = null;
		List<FileInfo> fileInfoList = null;
		try {
			fileInfosJSON = JSONArray.fromObject(jObj.get("fileInfos"));
			fileInfoList = (List<FileInfo>) JSONArray.toCollection(fileInfosJSON, FileInfo.class);
		} catch (Exception e) {
			log.error("json转json数组错误--json数组转list错误*******************:" + jObj.get("fileInfos"));
			log.error(e);
		}
		return fileInfoList;
	}
	private HashMap<String, Boolean> generateWhoshowMap(JSONObject jObj) {
		HashMap<String, Boolean> map=null;
		try {
			JSONArray xxx = JSONArray.fromObject(jObj.get("whoShow"));
			List<HashMap<String, Boolean>> list =  (List<HashMap<String, Boolean>>) JSONArray.toCollection(xxx, HashMap.class);
			map = list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}



	private JSONObject decodeAndTransfer2JsonObj(String data, String charset) {
		String dataDecoder = "";
		JSONObject jObj =null;
		try {
			dataDecoder = URLDecoder.decode(data, charset);//转码
			jObj = JSONObject.fromObject(dataDecoder);
		} catch (Exception e) {
			log.error("生成pdf报告内容参数data1格式化为json错误*****************入参数据-----转码后:" + dataDecoder);
			log.error(e);
		}
		return jObj;
	}




	private Map<String, String> getIpAddrMap(HttpServletRequest request) throws InterruptedException, ExecutionException, Exception {
			/*HttpSession session = request.getSession();
			Map<String, String> sessionMap = (Map<String, String>) session.getAttribute("ipAddrIds");
			Map<String, String> ipAddr = new HashMap<String, String>();
			if (sessionMap != null && sessionMap.size() > 0) {
				for (String ipAdd : sessionMap.keySet()) {//distinctByIpNotIpid
					ipAddr.put(ipAdd.substring(ipAdd.indexOf("-")+1), sessionMap.get(ipAdd));
				}
			}
			return ipAddr;*/
		ShardedJedis jedis = null;
		jedis = shardedJedisPool.getResource();

		HttpSession session = request.getSession();
		String[] filecheck = (String[]) session.getAttribute("filecheck");
		String userName = (String)session.getAttribute("username");

		HashMap<String, String> map_ip_ipAddr = new HashMap<String, String>();
		HashMap<String, String> tempMap = new HashMap<String, String>();

		String fileIpAddr = null;
		List<String> ipAddrIds = new ArrayList<String>();
		List<String> redisFileSerIP;

		String ipState;
		//从redis中取出redis中没有ip归属地的ip,进行ip归属地查询
		for(int i=0; i<filecheck.length; i++) {
			ipState = jedis.get(userName + "_" + filecheck[i] + RedisStorageConfig.IP_ADDRESS_ASCRIPTION_STATE);
			if(ipState==null || ipState.equals("1")){
				redisFileSerIP = jedis.lrange(userName + "_" + filecheck[i] + RedisStorageConfig.IP_ADDRESS, 0, -1);
				ipAddrIds.addAll(redisFileSerIP);
				ipAddService.ipAscription(ipAddrIds, filecheck[i], userName);
				//取出相应ip以及归属地
				fileIpAddr = jedis.get(userName + "_" + filecheck[i] + RedisStorageConfig.IP_ADDRESS_ASCRIPTION);
				if(fileIpAddr != null){
					tempMap.putAll(com.alibaba.fastjson.JSONObject.parseObject(fileIpAddr, Map.class));
				}
			}else{
				fileIpAddr = jedis.get(userName + "_" + filecheck[i] + RedisStorageConfig.IP_ADDRESS_ASCRIPTION);
				if(fileIpAddr != null){
					tempMap.putAll(com.alibaba.fastjson.JSONObject.parseObject(fileIpAddr, Map.class));
				}
			}
		}

		Set<String> keySet = tempMap.keySet();
		for (String key : keySet) {
			String newKey = key.substring(key.indexOf("-")+1);
			map_ip_ipAddr.put(newKey, tempMap.get(key));
		}
		return map_ip_ipAddr;
	}



	private String decodeAndSetDefaultValue(String svalue,String defV,String charset) throws UnsupportedEncodingException {
		if (StringUtil.isEmpty(svalue)) {
			log.error(svalue + "svalue参数为空(pdf报告水印)----------------");
			return  defV;
		} else {
			return URLDecoder.decode(svalue, charset);
		}
	}


}