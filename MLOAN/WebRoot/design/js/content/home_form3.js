var norm_graph =null;
var huffPuff =null;
var ipFlows = null;
var links = null;
var normWidth = null;
var tb1Width = null;
var Width = null;
var linkd = null;
var currentIndex;
var linkWidth = 14;
var currentIndex;
var network = true;
var ipAddrEnd = false;

//生成表格传给后台数据
var pdfData=null;
function chartAjax(data,id_array_id){
		norm_graph =null;
		huffPuff =null;
		ipFlows = null;
		links = null;
		normWidth = null;
		tb1Width = null;
		tb1DetailWidth = null;
		document.getElementById("norm").style.width=null;
		document.getElementById("tb1").style.width=null;
		document.getElementById("table1Info").style.width=null;
		document.getElementById("tb1Detail").style.width=null;
		document.getElementById("right").style.width=null;
		
		/*var fileName;
		var arrNames =new Array(); 
		var aa = data.fileInfos.length;
		for(var i=0;i<data.fileInfos.length;i++){
			arrNames.push(data.fileInfos[i].fileName.split("_"));
		}
		var mark=0;
		for(var i=0;i<arrNames.length;i++){
			mark=0;
			fileName=arrNames[i][0];
			for(var j=0;j<arrNames.length;j++){
				if(mark>1)
					break;
				if(fileName==arrNames[j][0])
					++mark;
			}
			if(mark==1){
				if(data.fileInfos[i].fileName==jingpinFileName)
					jingpinFileName=fileName;
				data.fileInfos[i].fileName=fileName;
			}	
		}*/
		
		getIpAddress();
		getsysteminf(data);
		toggleshow();
		IPtoggleshow();
		
		pdfData=data;
//     滚动条高度
		//scorllTop();
		resetScrollLeftAndTop();
		deletetd();//暂时不知道干嘛的
		//gaoji(id_array_id);//旧的判断显示哪些列的方法，我通过修改模板控制by heshiyuan
		
		
		MainTop(3);
		//alert("offsetWidth="+$("#div_basicConclusion")[0].offsetWidth);
		//alert("offsetHeight="+$("#div_basicConclusion")[0].offsetHeight);
		$("#ul_li_summarize").addClass('hit').siblings().removeClass('hit');//显示"数据明细"选项卡
		$('#home_form3>div:eq('+$("#ul_li_summarize").index()+')').show().siblings().hide();
		
		
	/*	
		if($('#checkbox_compareConclusion')[0].checked){//比较时,two pcap,one pdf
				html2canvas($("#messageSummarize"), {
				    	width:1050,
				    	height:$("#messageSummarize")[0].offsetHeight,
				    	onrendered: function (canvas) {
					    		$.ajax({type:'post',
					    				async: false,
					    				data: ("imgdata=" + encodeURIComponent(canvas.toDataURL())), 
					    				dataType:'xml',
					    				url : "/MLOAN/img/uploadImg.req?imgName=gaishu"});
				    	}
			    });
			
			    html2canvas($("#div_basicConclusion"), {
			    	 	width:$("#div_basicConclusion")[0].offsetWidth,
				        height:$("#div_basicConclusion")[0].offsetHeight,
				        onrendered: function (canvas) {
		 			            $.ajax({type:'post',
		 			            		async: false,
		 			            		data: ("imgdata=" + encodeURIComponent(canvas.toDataURL())), 
		 			            		dataType:'xml',
		 			            		url : "/MLOAN/img/uploadImg.req?imgName=jielun"});
				        }
			    });
		}else{
				document.getElementById("over_home_form3").style.display = "block";
			    document.getElementById("layout_home_form3").style.display = "block";
			    document.onmousewheel=function(){
			    	return false;
			    }//禁止拖动鼠标
			    
				$("#information").append($('<div id="gaishu" class="messageSummarize" style="position: absolute;left: 50px;top: 10px;z-index:-1"></div>'));
				$("#information").append($('<div id="jielun" class="bw" style="position: absolute;left: 50px;top: 10px;z-index:-1"></div>'));
				
				var pcapCount=baowen.length;
				uploadImg_sync(pcapCount,0,0);
		}
		
		
	*/
		
		
		
		delAllTr("tb1");
		delAllTr("tb1Detail");
		var pathName = window.document.location.pathname;
		$("#tag1").empty();
		$("#charts").empty();
		
		$("#tag1").append(data.norm_edit);
		$("#tb1").append(data.norm_table);
		$("#tb1Detail").append(data.norm_norm);
		
		normWidth = $("#norm").width();
		tb1Width = $("#tb1").width();
		tb1DetailWidth = $("#tb1Detail").width();
		rightWidth = $("#right").width(); //home.jsp  全局变量
		
		document.getElementById("tb1").style.width=(normWidth-10)+"px";
		document.getElementById("tb1De").style.width=(normWidth-10)+"px";
		
		document.getElementById("contr1").style.display="none";
		document.getElementById("tb1Detail").style.display="none";
		document.getElementById("tag1").style.display="none";
		if(data.norm_table){
			document.getElementById("tb1").style.border='solid 1px #cccccc';
		}
		if(data.norm_norm!=null){
			document.getElementById("tb1De").style.display='block';
		}
		var norm_minSumList = data.norm_minSumList;
		if(norm_minSumList!=null){
			var minSumList = JSON.parse(norm_minSumList);
			 $.each(minSumList,function(key,value) {
				 document.getElementById(value).style.color="green";
		       });
		}
		
		normData();
		
		norm_graph = data.norm_graph;
		if(norm_graph!=null){
				var graph=JSON.parse(norm_graph);
				ipFlows = graph.IP流量分布图;
				if(ipFlows!=null){
					$("#charts").append("<div style='width: 550px'>IP流量分布图</div>");
					var ipFlowJs = JSON.parse(ipFlows);
					var length = ipFlowJs.length;
					for(var i in ipFlowJs){
						var info = JSON.parse(ipFlowJs[i]);
						var id = info.id;
						$("#charts").append("<div id='"+id+"' class='use' STYLE='border-width:1pt; border-color:#DDDDDD' ></div>");
						ipFlow(info.title,id,info.x,info.list_y);
					}
				}
				
				var spread = graph.包长分布图;
				if(spread!=null){
						$("#charts").append("<div style='width: 550px'>包长分布图</div>");
						var spreadObj = JSON.parse(spread);
						var length = spreadObj.length;
						for(var i in spreadObj){
							var spreadJs = JSON.parse(spreadObj[i]);
							var id = spreadJs.id;
							if(eval(i)%2==0)
								$("#charts").append("<div id='"+id+"' class='use1' STYLE='border-width:1pt; border-color:#DDDDDD' ></div>");
							else
								$("#charts").append("<div id='"+id+"' class='use2' STYLE='border-width:1pt; border-color:#DDDDDD' ></div>");
							packetLen(spreadJs.title,id,spreadJs.x,spreadJs.list_y,spreadJs.subtext);
						}
						if (length%2 != 0) {
							var id = JSON.parse(spreadObj[length-1]).id;
							$("#charts").append("<div id='"+id+"' class='use2' STYLE='border-width:1pt; border-color:#FFFFFF'></div>");
						}
				}
				huffPuff = graph.吞吐曲线图;
				if(huffPuff!=null){
					$("#charts").append("<div style='width: 550px'>吞吐曲线图</div>");
					var huffPuffJs = JSON.parse(huffPuff);
					var id = huffPuffJs.id;
					$("#charts").append("<div id='"+id+"' class='use' STYLE='border-width:1pt; border-color:#DDDDDD' ></div>");
					if(huffPuffJs.unit == "s")
						use2(huffPuffJs.title,id,huffPuffJs.list,huffPuffJs.x_str,huffPuffJs.list_y);
					else
						use22(huffPuffJs.title,id,huffPuffJs.list,huffPuffJs.x_str,huffPuffJs.list_y);
				}
				
				huffPuffDetail = graph.吞吐曲线图detail;
				if(huffPuffDetail!=null){
						var huffPuffDetailObj = JSON.parse(huffPuffDetail);
						for(var i in huffPuffDetailObj){
							var huffPuffDetailJs = JSON.parse(huffPuffDetailObj[i]);
							var id = huffPuffDetailJs.id;
							$("#charts").append("<div id='"+id+"' class='use' STYLE='border-width:1pt; border-color:#DDDDDD;display: none;' ></div>");
							use2(huffPuffDetailJs.title,id,huffPuffDetailJs.list,huffPuffDetailJs.x_str,huffPuffDetailJs.list_y);
							if(huffPuffDetailJs.unit == "s")
								use2(huffPuffDetailJs.title,id,huffPuffDetailJs.list,huffPuffDetailJs.x_str,huffPuffDetailJs.list_y);
							else
								use22(huffPuffDetailJs.title,id,huffPuffDetailJs.list,huffPuffDetailJs.x_str,huffPuffDetailJs.list_y);
						}
				}
				links = graph.链路图;
				if(links!=null){
						$("#charts").append("<div style='width: 550px'>链路图</div>");
						var linksObj = JSON.parse(links);
						for(var i in linksObj){
								var info = JSON.parse(linksObj[i]);
								var id = info.id;
								var height1 = info.height
								var topHeight = 0;
								var leftHeiht = 45;
								if (eval(height1)+95 < 200) {
									topHeight = (200 - (eval(height1)+95))/2;
								}
								leftHeiht = leftHeiht + topHeight;
								$("#charts").append(
										"<div id='change"+id+"' style='height:"+(eval(height1)+95)+"px; overflow-x: auto;position:relative;width:915px;float:left; border: solid 1px #ccc; min-height: 200px;'>" +
											"<div   STYLE='border-width:1pt; width:50px; height:"+topHeight+"px; border-color:#FFFFFF'></div>" +
											"<div  id='"+id+"' style='height:"+(eval(height1)+65)+"px;width:915px;border-right: solid 1px #ccc;border-bottom: solid 1px #ccc;'></div>" +
										"</div>" +
										"<div  style='border-width:1pt; width:50px; height:"+leftHeiht+"px; border-color:#FFFFFF'  id='title"+id+"'></div>" +
										"<div  id='remain"+id+"' style='height:"+(eval(height1))+"px;width:130px;overflow:hidden;position:absolute;font-size:10px;border-left: solid 1px #DDDDDD;border-top: solid 1px #FFFFFF;line-height:12px;background:white;text-align:right;'>"+info.ipPort+"</div>" +
										"<div style='text-align:center; width:915px;'>" +
											"<input type='submit' value='扩大' onclick='magnify(\""+id+"\")'/>" +
											"<input type='submit' value='缩小'  onclick='shrink(\""+id+"\")' />" +
										"</div>");
								if(info.height < 259){
									linkChart1(info.title,id,info.y,info.list_y,info.topLeg,info.topGr);
								}else{
									linkChart2(info.title,id,info.y,info.list_y,info.topLeg,info.topGr);
								}
						}
				}
				currentIndex = 0;
				network = true;
				ipAddrEnd = false;
	//			getIpAddr();
		}
}


var basicConclusions;

//加载模板数据（系统信息+报文信息）
var baowen;
function getsysteminf(inf){//即data
		baowen=[];
		$.each(inf.fileInfos,function(key,value){
			baowen.push(value);
		})
		
		if(inf.sysInfo.userAgents.length==1){
			 var userAgent=inf.sysInfo.userAgents[0];
			 inf.sysInfo.userAgents[0]=userAgent.substring(userAgent.indexOf('：')+1);
		}
		
		var html=template("information_system",inf.sysInfo);
		//alert(html);
		
		$("#information").html("").append(html);
		
		
		//*************************"报文处理概述/基本结论"数据封装开始*********************************************
		basicConclusions={};//基本结论(比较时有有2个，第一个是基础的，第二个是竞品)，非比较时，个数不确定
		basicConclusions.data=[];
		norm_graph = inf.norm_graph;
		var spreadObj;
		if(norm_graph!=null){
				var graph=JSON.parse(norm_graph);
				var spread = graph.包长分布图;
					if(spread!=null){
						spreadObj = JSON.parse(spread);
					}
		}
		var crossProvice;
		var mark=true;
		for(var i=0;i<baowen.length;i++){//比较时长度为2
				var currBaowen=baowen[i];
				var basicConclusion={};
				basicConclusion.adviceNum=0;//建议个数(也即问题个数)
				
				var spreadJs = JSON.parse(spreadObj[i]);//图形信息
				var list_y=eval('('+spreadJs.list_y+')');//list_y="[{value:972,name:'0-100Byte'},{value:86,name:'101-1000Byte'},{value:1717,name:'1001-1514Byte'}]"
				var smallPacketCount=list_y[0].value;
				var mediumPacketCount=list_y[1].value;
				var largePacketCount=list_y[2].value;
				var pcaketCount=smallPacketCount+mediumPacketCount+largePacketCount;
				
				var smallPacketPercent=smallPacketCount/pcaketCount*100;
				smallPacketPercent=smallPacketPercent.toFixed(2)+"%";
				
				var mediumPacketPercent=mediumPacketCount/pcaketCount*100;
				mediumPacketPercent=mediumPacketPercent.toFixed(2)+"%";
				
				var largePacketPercent=largePacketCount/pcaketCount*100;
				largePacketPercent=largePacketPercent.toFixed(2)+"%";
				
				basicConclusion.fileName=currBaowen.fileName;//业务名称
				basicConclusion.dnsTimeDelayedcount=currBaowen.dnsTimeDelayedcount;//dns时延
				basicConclusion.packetCount=currBaowen.packetCount;//包数,
				//basicConclusion.ipNum=currBaowen.serviceIP.length;//ip数
				basicConclusion.tcpCount=currBaowen.tcpCount;//链路数
				basicConclusion.exchangeTimeCount=currBaowen.exchangeTimeCount;//交互时间
				
				
				
				//报文处理概述的信息
				basicConclusion.ipCount=currBaowen.ipCount;
				basicConclusion.othersIpCount=currBaowen.othersIpCount;
				basicConclusion.othersTcpCount=currBaowen.othersTcpCount;
				basicConclusion.permissableParameterCount=currBaowen.permissableParameterCount;
				basicConclusion.noPermissablePara=currBaowen.noPermissablePara;
				basicConclusion.noPermissableParaNum=currBaowen.noPermissableParaNum;

				if(currBaowen.proviceIpFlag == "0" ){
					basicConclusion.crossProviceNum=currBaowen.proviceIpaddrNum;//获取跨区域个数
					basicConclusion.crossProvice=currBaowen.proviceIpAddr;//获取跨区域省份
				}				
				if(basicConclusion.crossProviceNum>1){//存在"访问服务器跨区域"问题
						basicConclusion.adviceNum++;
				}
				
				basicConclusion.downloadRate=currBaowen.downloadRate;//TCP windos问题
				if(basicConclusion.downloadRate!='N/A'){//存在"TCP windos问题"问题
						basicConclusion.adviceNum++;
				}
				
				Res="";
				if(basicConclusion.crossProviceNum > 1)
					Res += "、访问服务器跨区域 ";
				if(currBaowen.sumOftenOffVerdict>0)
					Res += "、频繁拆建链 ";
				if(basicConclusion.downloadRate!='N/A')
					Res += "、链路不支持 TCP WINDOW SCALE OPTION参数";
				basicConclusion.conclusionRes = Res.substring(1);
				if(mark && Res.length>0)
					mark=false;
				
				basicConclusion.sumOftenOffVerdict=currBaowen.sumOftenOffVerdict;//频繁拆建链共涉及多少次
				if(basicConclusion.sumOftenOffVerdict!=0){//存在"频繁拆建链"问题
						basicConclusion.adviceNum++;
				}
				//还有后面几个问题的判断????
				
				basicConclusion.smallPacketPercent=smallPacketPercent;
				basicConclusion.mediumPacketPercent=mediumPacketPercent;
				basicConclusion.largePacketPercent=largePacketPercent;//担心fileinfo和graph的顺序是不是一致的
				
				//DNS时延(ms)	建链时延(ms)	建链后首包时延(ms)	TCP键连时延(ms)	平均RTT时间(ms)	交互时间(ms)
				basicConclusion.avgDnsDelsyTs=currBaowen.avgDnsDelsyTs;
				basicConclusion.avgOffTimeDelayed=currBaowen.avgOffTimeDelayed;
				basicConclusion.avgTimeToFirstByte=currBaowen.avgTimeToFirstByte;
				basicConclusion.avgTcpTimeDelayed=currBaowen.avgTcpTimeDelayed;
				basicConclusion.avgRttTime=currBaowen.avgRttTime;
				basicConclusion.avgExchangeTime=currBaowen.avgExchangeTime;
				basicConclusion.timeEfficiencyAVG=currBaowen.timeEfficiencyAVG;
				basicConclusion.lowestEffLink=currBaowen.lowestEffLink;
				
				
				basicConclusion.exchangeFlowCount=currBaowen.exchangeFlowCount;//交互流量
				basicConclusion.flowBigIp=currBaowen.flowBigIp;
				basicConclusion.flowBigPort=currBaowen.flowBigPort;
				basicConclusion.flow=currBaowen.flow;
				
				
				basicConclusion.dnsBigIp=currBaowen.dnsBigIp;
				basicConclusion.dnsBig=currBaowen.dnsBig;
				
				basicConclusion.offTimeBigIp=currBaowen.offTimeBigIp;
				basicConclusion.offTimeBigPort=currBaowen.offTimeBigPort;
				basicConclusion.offTimeBig=currBaowen.offTimeBig;
				
				basicConclusion.timeToFirstBigIp=currBaowen.timeToFirstBigIp;
				basicConclusion.timeToFirstBigPort=currBaowen.timeToFirstBigPort;
				basicConclusion.timeToFirstBig=currBaowen.timeToFirstBig;
				
				basicConclusion.tcpTimeBigIp=currBaowen.tcpTimeBigIp;
				basicConclusion.tcpTimeBigPort=currBaowen.tcpTimeBigPort;
				basicConclusion.tcpTimeBig=currBaowen.tcpTimeBig;
				
				basicConclusion.rttTimeBigIp=currBaowen.rttTimeBigIp;
				basicConclusion.rttTimeBigPort=currBaowen.rttTimeBigPort;
				basicConclusion.rttTimeBig=currBaowen.rttTimeBig;
				
				basicConclusion.timeEfficiencyLowest=currBaowen.timeEfficiencyLowest;
				basicConclusion.maxFlowIP=currBaowen.maxFlowIP;
				basicConclusion.maxFlowPort=currBaowen.maxFlowPort;
				
				basicConclusion.allFileDataTimes=currBaowen.allFileDataTimes;
				basicConclusion.exchangeTimeBigIp=currBaowen.exchangeTimeBigIp;
				basicConclusion.exchangeTimeBigPort=currBaowen.exchangeTimeBigPort;
				basicConclusion.exchangeTimeBig=currBaowen.exchangeTimeBig;
				if($('#checkbox_compareConclusion')[0].checked)
					basicConclusion.checkbox_compareConclusion="OK";
				basicConclusion.checkbox_compareConclusion;
				
				basicConclusions.data.push(basicConclusion);
		}
		if(!mark)
			basicConclusions.str1 = "基本结论";
		//if($('#a19')[0].checked||$('#a20')[0].checked){//此时basicConclusions长度为2
		if($('#checkbox_compareConclusion')[0].checked){//此时basicConclusions长度为2
			//alert("勾选了基本信息比较 in  getsysteminf()");
			
				var array=new Array(); 
				var isExchange = false;
				if(basicConclusions.data[0].fileName==jingpinFileName){//交换顺序使得第一个是基础的，第二个是竞品
					  var basicConclusion_temp=basicConclusions.data[0];
					  basicConclusions.data[0]=basicConclusions.data[1];
					  basicConclusions.data[1]=basicConclusion_temp;
					  isExchange = true;
				}
				
				var compareInfo={};//比较的信息(对比时才显示)
				//交互流量的对比信息
				putCompareResult(compareInfo,'smallPacketPercent',basicConclusions.data[0].smallPacketPercent,basicConclusions.data[1].smallPacketPercent,-1);
				putCompareResult(compareInfo,'mediumPacketPercent',basicConclusions.data[0].mediumPacketPercent,basicConclusions.data[1].mediumPacketPercent,-1);
				putCompareResult(compareInfo,'largePacketPercent',basicConclusions.data[0].largePacketPercent,basicConclusions.data[1].largePacketPercent,-1);
				putCompareResult(compareInfo,'packetCount',basicConclusions.data[0].packetCount,basicConclusions.data[1].packetCount,-1);
				
			    var pcakageCompareMsg=null;
			    if(compareInfo.smallPacketPercent=='劣于'){//小包低
				  	pcakageCompareMsg="小包 比例多于竞品";
			  	  	if(compareInfo.mediumPacketPercent=='劣于')
			  	  		pcakageCompareMsg+="、中包 比例多于竞品";
			    }
			    if(pcakageCompareMsg){
				  compareInfo.pcakageCompareMsg=pcakageCompareMsg;
			    }
			  
			    //比较“包数(个)	IP数(个)	链路数(个)	交互流量(byte)”
				putCompareResult(compareInfo,'packetCount',basicConclusions.data[0].packetCount,basicConclusions.data[1].packetCount,-1);
				putCompareResult(compareInfo,'ipCountRes',basicConclusions.data[0].ipCount,basicConclusions.data[1].ipCount,-1);
				putCompareResult(compareInfo,'exchangeFlowCount',basicConclusions.data[0].exchangeFlowCount,basicConclusions.data[1].exchangeFlowCount,-1);
				
				compareInfo.percentDiff_avgExchangeTime= calcuPercentDiff(basicConclusions.data[0].avgExchangeTime,basicConclusions.data[1].avgExchangeTime);
				
			    putCompareResult(compareInfo,'tcpCountRes',basicConclusions.data[0].tcpCount,basicConclusions.data[1].tcpCount,-1);
			    var tcpCountCompareMsg=null;
			    if(compareInfo.tcpCountRes=='劣于' || compareInfo.ipCountRes=='劣于')
			    	tcpCountCompareMsg="链路数多：";
			    if(compareInfo.tcpCountRes=='劣于'){//链路数(tcpCount)多
				  	compareInfo.tcpCount=basicConclusions.data[0].tcpCount;
				  	compareInfo.fileName=basicConclusions.data[0].fileName;
				  	compareInfo["tcpCountPercent"]=((basicConclusions.data[0].tcpCount/basicConclusions.data[1].tcpCount-1)*100).toFixed(2);
			    }else{
				  	compareInfo.tcpCount=basicConclusions.data[0].tcpCount;
				  	compareInfo.fileName=basicConclusions.data[0].fileName;
				  	compareInfo["tcpCountPercent"]=((1-basicConclusions.data[0].tcpCount/basicConclusions.data[1].tcpCount)*100).toFixed(2);
			    }
			    if(tcpCountCompareMsg)
				  	compareInfo.tcpCountCompareMsg=tcpCountCompareMsg;
			    
				//“交互时间”下面的对比信息
				putCompareResult(compareInfo,'timeEfficiencyAVG',basicConclusions.data[0].timeEfficiencyAVG,basicConclusions.data[1].timeEfficiencyAVG,1);
				//列出效率最低的链路端口号
				if(compareInfo.timeEfficiencyAVG=='劣于'){
						compareInfo.lowestEffLink=basicConclusions.data[0].lowestEffLink;
				}
				
				putCompareResult(compareInfo,'avgDnsDelsyTs',basicConclusions.data[0].avgDnsDelsyTs,basicConclusions.data[1].avgDnsDelsyTs,-1);
				putCompareResult(compareInfo,'avgTcpTimeDelayed',basicConclusions.data[0].avgTcpTimeDelayed,basicConclusions.data[1].avgTcpTimeDelayed,-1);
				putCompareResult(compareInfo,'avgTimeToFirstByte',basicConclusions.data[0].avgTimeToFirstByte,basicConclusions.data[1].avgTimeToFirstByte,-1);
				putCompareResult(compareInfo,'avgOffTimeDelayed',basicConclusions.data[0].avgOffTimeDelayed,basicConclusions.data[1].avgOffTimeDelayed,-1);
				putCompareResult(compareInfo,'avgRttTime',basicConclusions.data[0].avgRttTime,basicConclusions.data[1].avgRttTime,-1);
				putCompareResult(compareInfo,'avgExchangeTime',basicConclusions.data[0].avgExchangeTime,basicConclusions.data[1].avgExchangeTime,-1);//-1表示越小越优，显然‘延迟’是越小越优
				putCompareResult(compareInfo,'exchangeTimeCount',basicConclusions.data[0].exchangeTimeCount,basicConclusions.data[1].exchangeTimeCount,-1);
				
				array=new Array();
				if(compareInfo.timeEfficiencyAVG=='劣于'){
					basicConclusions.data[0].adviceNum++;
					compareInfo["timeEfficiencyAVGNum"]=((basicConclusions.data[0].timeEfficiencyAVG)/(basicConclusions.data[1].timeEfficiencyAVG)*100).toFixed(2);				
					array.push("链路效率");
				}
				if(compareInfo.exchangeTimeCount=='劣于')
					basicConclusions.data[0].adviceNum++;
				
				var a0 = basicConclusions.data[0].exchangeTimeCount;
				var a1 = basicConclusions.data[1].exchangeTimeCount;
				var a0_a1 = Math.abs(a0/a1);
				var res;
				if(compareInfo.exchangeTimeCount=='劣于')
					res = Math.abs(a0_a1-1);
				else
					res = Math.abs(1-a0_a1);
				compareInfo["exchangeTimeCountNum"]=(res*100).toFixed(2);
				array.push("时间消耗");
				
				if(compareInfo.exchangeFlowCount=='劣于'){
					basicConclusions.data[0].adviceNum++;
					compareInfo["exchangeFlowCountNum"]=((basicConclusions.data[0].exchangeFlowCount/basicConclusions.data[1].exchangeFlowCount-1)*100).toFixed(2);				
					array.push("流量消耗");				
				}
				if(pcakageCompareMsg){
					basicConclusions.data[0].adviceNum++;
					array.push("报文结构");
				}
				if(compareInfo.tcpCountRes=='劣于'){
					basicConclusions.data[0].adviceNum++;
					array.push("链路多");
				}
					
				compareInfo.wholeRes = array;
				
				//列出平均值比较结果，效率的指标
				Res="";
				if(compareInfo.avgDnsDelsyTs=='劣于'){
					compareInfo["avgDnsDelsyTsNum"]=((basicConclusions.data[0].avgDnsDelsyTs/basicConclusions.data[1].avgDnsDelsyTs-1)*100).toFixed(2);				
					Res += "、DNS时延";
				}
				if(compareInfo.avgTcpTimeDelayed=='劣于'){
					compareInfo["avgTcpTimeDelayedNum"]=((basicConclusions.data[0].avgTcpTimeDelayed/basicConclusions.data[1].avgTcpTimeDelayed-1)*100).toFixed(2);				
					Res += "、TCP建链时延";
				}
				if(compareInfo.avgTimeToFirstByte=='劣于'){
					compareInfo["avgTimeToFirstByteNum"]=((basicConclusions.data[0].avgTimeToFirstByte/basicConclusions.data[1].avgTimeToFirstByte-1)*100).toFixed(2);				
					Res += "、建链后首包时延";
				}
				if(compareInfo.avgOffTimeDelayed=='劣于'){
					compareInfo["avgOffTimeDelayedNum"]=((basicConclusions.data[0].avgOffTimeDelayed/basicConclusions.data[1].avgOffTimeDelayed-1)*100).toFixed(2);				
					Res += "、断链时延";
				}
				if(compareInfo.avgRttTime=='劣于'){
					compareInfo["avgRttTimeNum"]=((basicConclusions.data[0].avgRttTime/basicConclusions.data[1].avgRttTime-1)*100).toFixed(2);				
					Res += "、RTT时间";
				}
				if(compareInfo.avgExchangeTime=='劣于'){
					compareInfo["avgExchangeTimeNum"]=((basicConclusions.data[0].avgExchangeTime/basicConclusions.data[1].avgExchangeTime-1)*100).toFixed(2);				
					Res += "、交互时间";
				}
				var aa =  Res.substring(1);
				compareInfo.compareRes = Res.substring(1);
				
				//比较时计算“基础”的比较时候的问题(建议个数)（比较时"竞品"的这4个问题不算(因为问题是针对'基础'的，是来诊断‘基础’的问题的!!!)）
//				if(compareInfo.timeEfficiencyAVG=='劣于')//效率(平均链路时间有效率)
//					  	basicConclusions.data[0].adviceNum++;
				  
//				if(compareInfo.avgExchangeTime=='劣于')//交互时间
//					  	basicConclusions.data[0].adviceNum++;
				
//				if(compareInfo.pcakageCompareMsg)//'（大/中）包比例偏低'问题
//						basicConclusions.data[0].adviceNum++;
				
//				if(compareInfo.tcpCountCompareMsg)//'业务链路数多'问题
//						basicConclusions.data[0].adviceNum++;
				
				
				basicConclusions.compareInfo=compareInfo;
		}
		//*************************"报文处理概述/基本结论"数据封装结束*********************************************
		
		//渲染"报文处理概述"
		html=template("summarize",{'basicConclusions':basicConclusions});
		$("#information").append(html);
		
		//渲染"基本结论"
		html=template("basicConclusion",{'basicConclusions':basicConclusions});
		$("#information").append(html);
		
		
	/*	
		var avgt={};
		var aa = $('#a19')[0].checked;
		
		if($('#a19')[0].checked){//判断是否显示"视频业务对比"信息（fileInfos[0存储的是基础的，[1]是竞品的]）
			var avgT1_1 = parseFloat(inf.fileInfos[0].avgT1);  // 1fr
			var avgT2_1 = parseFloat(inf.fileInfos[0].avgT2);  // 1fs
			var avgT1_2 = parseFloat(inf.fileInfos[1].avgT1);  // 2fr
			var avgT2_2 = parseFloat(inf.fileInfos[1].avgT2);  // 2fs
			
			var avgT1_s = 0;
			var avgT2_s = 0;
			if(!isNaN(avgT1_1) && !isNaN(avgT1_2)){
				if(avgT1_1>avgT1_2){
					avgT1_s = avgT1_2/avgT1_1*100;
				}else
					avgT1_s = avgT1_1/avgT1_2*100;
			}else{
				if(isNaN(avgT1_1))
					avgT1_1 = "N/A";
				if(isNaN(avgT1_2))
					avgT1_2 = "N/A";
				avgT1_s = "N/A";
			}
			if(!isNaN(avgT2_1) && !isNaN(avgT2_2)){
				if(avgT2_1>avgT2_2){
					avgT2_s = avgT2_2/avgT2_1*100;
				}else
					avgT2_s = avgT2_1/avgT2_2*100;
			}else{
				if(isNaN(avgT2_1))
					avgT2_1 = "N/A";
				if(isNaN(avgT2_2))
					avgT2_2 = "N/A";
				avgT2_s = "N/A";
			}
			if(isNaN(avgT1_1) && isNaN(avgT1_2) && isNaN(avgT2_1) && isNaN(avgT2_2)){
				avgt['all'] = "N/A";
			}else{
				avgt['all'] = "";
			}
				
			avgt['file1'] = inf.fileInfos[0].fileName;
			avgt['file2'] = inf.fileInfos[1].fileName;
			avgt['avgT1_1'] = avgT1_1;
			avgt['avgT2_1'] = avgT2_1;
			avgt['avgT1_2'] = avgT1_2;
			avgt['avgT2_2'] = avgT2_2;
			avgt['avgT1_s'] = avgT1_s;
			avgt['avgT2_s'] = avgT2_s;
			if(avgT1_s != "N/A") {
				if(avgT1_1 < avgT1_2) {
					avgt['avgT1_s'] = inf.fileInfos[0].fileName+"比"+inf.fileInfos[1].fileName+"高效,时间相差百分比："+avgT1_s.toFixed(2)+"%";
				} else {
					avgt['avgT1_s'] = inf.fileInfos[1].fileName+"比"+inf.fileInfos[0].fileName+"高效,时间相差百分比："+avgT1_s.toFixed(2)+"%";
				}
			}
			if(avgT2_s != "N/A"){
				if(avgT2_1 < avgT2_2) {
					avgt['avgT2_s'] = inf.fileInfos[0].fileName+"比"+inf.fileInfos[1].fileName+"高效,时间相差百分比："+avgT2_s.toFixed(2)+"%";
				} else {
					avgt['avgT2_s'] = inf.fileInfos[1].fileName+"比"+inf.fileInfos[0].fileName+"高效,时间相差百分比："+avgT2_s.toFixed(2)+"%";
				}
			}
			
			avgt['baowen'] = baowen;
			var avgtobj={list:avgt};
			var html2=template("ratio_log",avgtobj);//为"视频业务对比"模板赋值
			
			$("#information").append(html2);
		}
	*/
	
		/*if($('#a19')[0].checked){//判断是否显示"基本信息比较"信息(此时baowen长度必为2)
					//alert("基础"+baseFileName+",竞品："+jingpinFileName);
					//在此判断谁是基础，谁是竞品，然后赋值
					var base={"fileName":baseFileName};
					var jingpin={"fileName":jingpinFileName};
					var  compareResult={};//比较结果:(优于/劣于/相似于)
			
					for(var i=0;i<2;i++){
							var currBaowen=baowen[i];
							if(currBaowen.fileName==baseFileName){//当前报文是"基础数据"
									base.commuTime=currBaowen.exchangeTimeCount;
									base.commuFlow=currBaowen.exchangeFlowCount;
									base.ipNum=currBaowen.serviceIP.length;//不知道是字符串还是集合(集合就是其长度，字符串得先split再求其长度)
									base.linkNum=currBaowen.tcpCount;
									base.timeEfficiencyAVG=currBaowen.timeEfficiencyAVG;
									base.frequRebuild="否";//只要有一个拆建链即为true
									
									var len_serviceIP=currBaowen.serviceIP.length;
									for(var i1=0;i1<len_serviceIP;i1++){
										    var  requests=currBaowen.serviceIP[i1].serviceIPInfo;
										    var len_serviceIPInfo=requests.length;
										    for(var j=0;j<len_serviceIPInfo;j++){
										    	  var  serviceIPInfo=requests[j];
										    	  if(serviceIPInfo.oftenOffVerdict=='Y'){
										    		  base.frequRebuild="是";
										    			break;
										    	  }
										    }
									}
							}else if(currBaowen.fileName==jingpinFileName){//当前报文是"竞品数据"
									jingpin.commuTime=currBaowen.exchangeTimeCount;
									jingpin.commuFlow=currBaowen.exchangeFlowCount;
									jingpin.ipNum=currBaowen.serviceIP.length;//不知道是字符串还是集合(集合就是其长度，字符串得先split再求其长度)
									jingpin.linkNum=currBaowen.tcpCount;
									jingpin.timeEfficiencyAVG=currBaowen.timeEfficiencyAVG;
									jingpin.frequRebuild="否";//只要有一个拆建链即为true
									
									var len_serviceIP=currBaowen.serviceIP.length;
									for(var i1=0;i1<len_serviceIP;i1++){
										    var  requests=currBaowen.serviceIP[i1].serviceIPInfo;
										    var len_serviceIPInfo=requests.length;
										    for(var j=0;j<len_serviceIPInfo;j++){
										    	  var  serviceIPInfo=requests[j];
										    	  if(serviceIPInfo.oftenOffVerdict=='Y'){
										    		    jingpin.frequRebuild="是";
										    			break;
										    	  }
										    }
									}
							}
					}
					putCompareResult(compareResult,'commuTime',base.commuTime,jingpin.commuTime,-1);//1表示越大越优，-1表示越小越优
					putCompareResult(compareResult,'commuFlow',base.commuFlow,jingpin.commuFlow,-1);
					putCompareResult(compareResult,'ipNum',base.ipNum,jingpin.ipNum,-1);
					putCompareResult(compareResult,'linkNum',base.linkNum,jingpin.linkNum,-1);
					putCompareResult(compareResult,'timeEfficiencyAVG',base.timeEfficiencyAVG,jingpin.timeEfficiencyAVG,1);
			
			
					baseInfoCompareObj={"base":base,"jingpin":jingpin,"compareResult":compareResult};
					
					var html=template("template_baseInfoCompare",baseInfoCompareObj);
					$("#information").append(html);
		}*/
				
		var newobj={
					lists:baowen,
					whoShow:{
						  a1:$('#a1')[0].checked,
						  a2:$('#a2')[0].checked,
						  a3:$('#a3')[0].checked,
						  a4:$('#a4')[0].checked,
						  a5:$('#a5')[0].checked,
						  a6:$('#a6')[0].checked,
						  a7:$('#a7')[0].checked,
						  a8:$('#a8')[0].checked,
						  a9:$('#a9')[0].checked,
						  a10:$('#a10')[0].checked,
						  a11:$('#a11')[0].checked,
						  a12:$('#a12')[0].checked,
						  a13:$('#a13')[0].checked,
						  a14:$('#a14')[0].checked,
						  a15:$('#a15')[0].checked,
						  a16:$('#a16')[0].checked,
						  a17:$('#a17')[0].checked,
						  a18:$('#a18')[0].checked,
						//  a19:$('#a19')[0].checked,
						  //a20:$('#a20')[0].checked,
						  a21:$('#a21')[0].checked,
						  a22:$('#a22')[0].checked,
						  a23:$('#a23')[0].checked,
						  a24:$('#a24')[0].checked
					}};
		var bwhtml=template("information_baowen",newobj);
		$("#information").append(bwhtml);
}

//var baseInfoCompareObj;


function putCompareResult(compareResult,compareItem,data_base,data_jingpin,compareRule){//1和-1
		var item_base = parseFloat(data_base);  
		var item_jingpin = parseFloat(data_jingpin); //只要有一方没有数据，则此项就比较项目
		if(isNaN(item_base) || isNaN(item_jingpin)){
				compareResult[compareItem]="N/A";//交互流量越大越好，其他越小越好
		}else{
			    if((item_base*compareRule)>(item_jingpin*compareRule)){//基础"优于"竞品
						compareResult[compareItem]="优于";
				}else if((item_base*compareRule)<(item_jingpin*compareRule)){
						compareResult[compareItem]="劣于";
				}else if(item_base==item_jingpin){
						compareResult[compareItem]="相似于";
				}
		}
}



//合并相关表格行
function rowspan(){//“<td class="a2" onload="rowspan">IP归属地(rowspan)</td>” 此处调用的
	var num=$(".detailed_information").length;
	for(var i=0;i<num;i++){
		var length=$(".detailed_information").eq(i).find("tr").length;
		$(".detailed_information").eq(i).find("tr").eq(1).children().last().attr({"rowspan":length+1});
	}
}
//合并serviceIp、dns时延、IP归属地
function IPspan(){
	$(".rowspan").each(function(){
		if($(this).text()==""){
			$(this).css({"border":"none"});
		}else{
			$(this).css({"border-bottom":"none"})
		}
	})
	$(".a2").each(function(){
		if($(this).text()==""){
			$(this).css({"border":"none"});
		}else{
			$(this).css({"border-bottom":"none"})
		}
	})
	$(".a5").each(function(){
		if($(this).text()==""){
			$(this).css({"border":"none"});
		}else{
			$(this).css({"border-bottom":"none"})
		}
	})
}
//详细信息隐藏
function toggleshow(){
	$(".detailed_information").css("display","none");
	$(".detailed_click").each(function(){	
		$(this).click(function(){
			$(this).next().stop(true,true).fadeToggle("slow",function(){
				if($(this).prev().find("i").text()=="-"){
					$(this).prev().find("i").text("+");
				}else{
					$(this).prev().find("i").text("-");
				}				
				if($(this).prev().find("em").text()=="展开详细信息"){
					$(this).prev().find("em").text("详细信息").css("font-style","normal");
				}else{
					$(this).prev().find("em").text("展开详细信息").css("font-style","italic");
				}				
			});
		})
	})
}
//无效IP隐藏
function IPtoggleshow(){
	$(".wuxiaoIP").css("display","none");
	$(".wuxiaoclick").each(function(){	
		$(this).click(function(){
			$(this).parent().find("span").stop(true,true).fadeToggle("slow",function(){
				if($(this).parent().find("em").text()=="-"){
					$(this).parent().find("em").text("+");
				}else{
					$(this).parent().find("em").text("-");
				}				
							
			});
		})
	})
}
//删除多余版本version表格
function deletetd(){
	$(".detailed_information").each(function(){
		$(this).find(".version").each(function(){
			var length=$(this).parent().parent().find(".version").length;
			for(var i=1;i<length;i++){
				$(this).parent().parent().find(".version").eq(i).remove();
			}
		})		
	})	
}
//过去IP归属地
function getIpAddress(){
	
	var crossProvice=null;
	$.ajax({
			type : "GET",
			dataType : "json",
			url : "/MLOAN/reportOut/ipAddr.do",
			error: function() {},
			cache:false,
			success : function(data) {
				crossProvice = data;
				$("td[name='getip']").each(function(){
					var that=$(this);
					$.each(data,function(key,value){
						if(that.attr("id")==key){
						   if(value.length==0){
							   value="N/A";
						   }
						   that.html(value);
						}	
					});

				})	
//				合并serviceIp、dns时延、IP归属地
				IPspan();
			}
		})
		return crossProvice;
}
//跨区
function ipProvide(){
	
	var crossProvice=null;
	$.ajax({
			type : "GET",
			dataType : "json",
			url : "/MLOAN/reportOut/ipAddr.do",
			error: function() {},
			cache:false,
			async: false,
			success : function(data) {
				crossProvice = data;
				$("td[name='getip']").each(function(){
					var that=$(this);
					$.each(data,function(key,value){
						if(that.attr("id")==key){
						   if(value.length==0){
							   value="N/A";
						   }
						   that.html(value);
						}	
					});
				})	
//				合并serviceIp、dns时延、IP归属地
				IPspan();
			}
		})
		return crossProvice;
}
//div切换时滚动条高度
	var divflag=1;
	var divtop=[0,0,0];
function scorllTop(){
	$("#home_maintop_div3").find("li").eq(0).click(function(){
		divflag=1;
		window.scrollTo(0,divtop[0]);		
	});
	$("#home_maintop_div3").find("li").eq(1).click(function(){
		divflag=2;
		window.scrollTo(0,divtop[1]);	
	});
	$("#home_maintop_div3").find("li").eq(2).click(function(){
		divflag=3;
		window.scrollTo(0,divtop[2]);	
	});
	$(window).scroll(function(){
		if(divflag==1){
			divtop[0]=document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
		}
		if(divflag==2){
			divtop[1]=document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;		
		}
		if(divflag==3){
			divtop[2]=document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
		}
	})
}

//非高级设置选项内容不显示(有问题，我不用此方法，直接修改模板)
function gaoji(id_array_id){
	var an=[];
	
	$(".detailed").first().find(".name").find("td").each(function(){
		an.push($(this).attr("class"));	
	})
	 var result = [];
	
	for(var i = 0; i < an.length; i++){
	    var num = an[i];
	 
	    var isExist = false;
	    for(var j = 0; j < id_array_id.length; j++){
	        var aj = id_array_id[j];
	      
	        if(aj == num){
	            isExist = true;
	            break;
	        }
	    }
	    if(!isExist){
	        result.push(num);
	    }
	}
	for(var k=0;k<result.length;k++){
		if(result[k]==""){
			result.splice(result.indexOf(result[k]),1);	
			k--;
		}
	}
	
	for(var l=0;l<result.length;l++){
		$(".detailed_information").each(function(){
			$(this).find("."+result[l]).remove();
		})
	}
	
}


//function getIpAddr(){
//	var ips="";
//	$("td[name='ipAddr']").each(function(){  
//		if(this.innerHTML=="N/A")
//			ips += ","+$(this).attr('id');
//	});  
//	if(ips.length>1 && currentIndex++<3 && network){
//		$.ajax( {
//			data : {'ips':ips},
//			type : "GET",
//			dataType : "json",
//			url : "/MLOAN/reportOut/ipAddr.do",
//			error: function() {ipAddrEnd = true;},
//			success : function(data) {
//				for(var ip in data){
//					document.getElementById(ip).innerHTML = data[ip];
//				}
//				normIpRefresh();
//				getIpAddr();
//			}
//		})
//	}else
//		ipAddrEnd = true;
//}
function shrink(linkId){
	var width = document.getElementById(linkId).style.width;
	var height = document.getElementById(linkId).style.height;
	var widthCurr = width.substr(0, width.length-2);
	var heightCurr = height.substr(0, width.length-2);
	if(widthCurr>915){
		var linksObj = JSON.parse(links);
		for(var i in linksObj){
			var info = JSON.parse(linksObj[i]);
			var id = info.id;
			if(id==linkId){
				$("#change"+id+"").empty();
				document.getElementById("change"+id).innerHTML = "<div id='"+id+"' class='use' style='height:"+(eval(info.height)+65)+"px;width:"+(eval(widthCurr)-400)+"px;'></div>";
				if(info.height < 259)
					linkChart1(info.title,id,info.y,info.list_y,info.topLeg,info.topGr);
				else
					linkChart2(info.title,id,info.y,info.list_y,info.topLeg,info.topGr);
			}
		}
	}else{
		alert("已经是最小了")
	}
}
function magnify(linkId){
	 
	document.getElementById("title"+linkId).style.height="51.5px";
	var width = document.getElementById(linkId).style.width;
	var widthCurr = width.substr(0, width.length-2)
	if(widthCurr<3150){
		var linksObj = JSON.parse(links);
		for(var i in linksObj){
			var info = JSON.parse(linksObj[i]);
			var id = info.id;
			if(id==linkId){
				$("#change"+id+"").empty();
				document.getElementById("change"+id).innerHTML = "<div id='"+id+"' class='use' style='height:"+(eval(info.height)+65)+"px;width:"+(eval(widthCurr)+400)+"px;'></div>";
				if(info.height < 259)
					linkChart1(info.title,id,info.y,info.list_y,info.topLeg,info.topGr);
				else
					linkChart2(info.title,id,info.y,info.list_y,info.topLeg,info.topGr);
			}
		}
	}else{
		alert("已经是最大了")
	}
}

function changeTd(tagId,tableId,col,no){
	var tags = document.all(tagId).getElementsByTagName("input");
	var status = tags[no].checked; 
	if(status){
		showHiddenTd(tableId,col,"");
	}else{
		showHiddenTd(tableId,col,"none");
	}
}
function draw(){
    var bg = document.getElementById("draw").style.background;
    switch (bg) {
	case "": document.getElementById("norm").style.width='inherit';
			 document.getElementById("ico1").style.width=$("#norm").width+"px";
			 document.getElementById("ico1").style.width=$("#norm").height+"px";
			 document.getElementById("draw").style.background="#0093D3";
			 document.getElementById("draw").innerText= "数据";
			 chart();
			 break;
	default: if(normWidth1>normWidth){
			 	document.getElementById("norm").style.width=normWidth1+"px";
			 }else
				document.getElementById("norm").style.width='inherit';
			 document.getElementById("draw").style.background="";
			 document.getElementById("draw").innerText= "图表";
			 data();
	}
}
var normWidth1 = 0;
function normIpRefresh(){
	tb1DetailWidth = $("#tb1Detail").width();
	var state = document.getElementById("contr1").style.display;
	if(state==""){
		var tb1Height = $("#tb1").height();
		document.getElementById("tag1").style.top=(tb1Height+200)+"px";			
		var currMax = normWidth-30;
		if(currMax>tb1DetailWidth){
			document.getElementById("tb1").style.width=currMax+"px";
			document.getElementById("tb1De").style.width=currMax+"px";
			document.getElementById("tb1Detail").style.width=currMax+"px";
			document.getElementById("table1Info").style.width=normWidth+"px";
			document.getElementById("tag1").style.left=(currMax-25)+"px";	
		}else{
			document.getElementById("norm").style.width=(tb1DetailWidth+25)+"px";
			document.getElementById("comm").style.width=(tb1DetailWidth+30)+"px";
			document.getElementById("indiv").style.width=(tb1DetailWidth+30)+"px";
			document.getElementById("tb1").style.width=tb1DetailWidth+"px";	
			document.getElementById("tb1De").style.width=tb1DetailWidth+"px";
			document.getElementById("table1Info").style.width=(tb1DetailWidth+35)+"px";	
			document.getElementById("tag1").style.left=(tb1DetailWidth-25)+"px";	
			normWidth1 = tb1DetailWidth+35;
		}
	}
}
function normData(){
	var state = document.getElementById("contr1").style.display;
	if(state=="none"){
		
		document.getElementById("tb1DeTen").innerHTML="-详细信息";	
		var tb1Height = $("#tb1").height();
		document.getElementById("tag1").style.top=(tb1Height+200)+"px";	
		
		document.getElementById("contr1").style.display="";
		document.getElementById("tb1Detail").style.display="";
		var currMax = normWidth-30;
		if(currMax>tb1DetailWidth){
			document.getElementById("tb1").style.width=currMax+"px";
			document.getElementById("tb1De").style.width=currMax+"px";
			document.getElementById("tb1Detail").style.width=currMax+"px";
			document.getElementById("table1Info").style.width=normWidth+"px";
			document.getElementById("tag1").style.left=(currMax-25)+"px";	
		}else{
			document.getElementById("norm").style.width=(tb1DetailWidth+25)+"px";
			document.getElementById("comm").style.width=(tb1DetailWidth+30)+"px";
			document.getElementById("indiv").style.width=(tb1DetailWidth+30)+"px";
			document.getElementById("tb1").style.width=tb1DetailWidth+"px";	
			document.getElementById("tb1De").style.width=tb1DetailWidth+"px";
			document.getElementById("table1Info").style.width=(tb1DetailWidth+35)+"px";	
			document.getElementById("tag1").style.left=(tb1DetailWidth-25)+"px";	
			normWidth1 = tb1DetailWidth+35;
		}
	}else{
		document.getElementById("tb1DeTen").innerHTML="+详细信息";	
		document.getElementById("contr1").style.display="none";
		document.getElementById("tb1Detail").style.display="none";
		document.getElementById("tag1").style.display="none";
		
		if(tb1Width>normWidth){
		 	document.getElementById("norm").style.width='inherit';
			document.getElementById("comm").style.width='1110px';
			document.getElementById("indiv").style.width='1110px';
		 }else{
			document.getElementById("comm").style.width=(normWidth-10)+"px";
			document.getElementById("norm").style.width='inherit';
			document.getElementById("indiv").style.width='inherit';
		 }
		document.getElementById("tb1").style.width=(normWidth-10)+"px";
		document.getElementById("tb1De").style.width=(normWidth-10)+"px";
		normWidth1 = tb1Width;
	}
}
function nullIP(){
	var state = document.getElementById("nullIp").style.display;
	if(state=="none"){
		document.getElementById("tb1DetailTen").innerHTML="&nbsp;&nbsp;&nbsp;-&nbsp;无效IP";
		document.getElementById("nullIp").style.display="";
	}else{
		document.getElementById("tb1DetailTen").innerHTML="&nbsp;&nbsp;&nbsp;+&nbsp;无效IP";
		document.getElementById("nullIp").style.display="none";
	}
}
/**导出方法
 */
function ExportExcel(data){
	myFunction();
	var  iName=""
	if(norm_graph!=null){
		var graph=JSON.parse(norm_graph);
		ipFlows = graph.IP流量分布图;
		iName="aIP流量分布图";
		if(ipFlows!=null){
				var ipFlowJs = JSON.parse(ipFlows);
				for(var i in ipFlowJs){
						var info = JSON.parse(ipFlowJs[i]);
						var id = info.id;
						var title = info.title;//title:文件名，id:aipflow01,id:aipflow02,...
					
						var myChart = ipFlow(title,id,info.x,info.list_y);
						//imageEx(id+title, myChart,iName);//ps:调用的是utilAfter.js中的imageEx(imgname,myChart);而不是本文件中的imageEx(imgname,myChart,iName)；
						imageEx(title, myChart,"aipFlow");
				}
		}
		
			//if($('#a19')[0].checked||$('#a20')[0].checked){//上传"叠加图"
			if($('#checkbox_compareConclusion')[0].checked){//上传"叠加图"
				huffPuff = graph.吞吐曲线图;
				if(huffPuff!=null){
						iName="c吞吐曲线图";
						var huffPuffJs = JSON.parse(huffPuff);
						var id=huffPuffJs.id;
						var myChart;
						if(huffPuffJs.unit == "s")
							myChart=use2(huffPuffJs.title,id,huffPuffJs.list,huffPuffJs.x_str,huffPuffJs.list_y);
						else
							myChart=use22(huffPuffJs.title,id,huffPuffJs.list,huffPuffJs.x_str,huffPuffJs.list_y);
						//imageEx(id, myChart,iName);
						imageEx("diejiatu", myChart,"cuse2Detail");
				}
		}else{
				huffPuffDetail = graph.吞吐曲线图detail;
				if(huffPuffDetail!=null){
						iName="c吞吐曲线图";
						var huffPuffDetailObj = JSON.parse(huffPuffDetail);
						for(var i in huffPuffDetailObj){
								var huffPuffDetailJs = JSON.parse(huffPuffDetailObj[i]);
								var id = huffPuffDetailJs.id;
								var title = huffPuffDetailJs.title;
								var myChart;
								if(huffPuffDetailJs.unit == "s")
									myChart = use2(title,id,huffPuffDetailJs.list,huffPuffDetailJs.x_str,huffPuffDetailJs.list_y);
								else
									myChart = use2(title,id,huffPuffDetailJs.list,huffPuffDetailJs.x_str,huffPuffDetailJs.list_y);
								//imageEx(id+title, myChart,iName);
								imageEx(title, myChart,"cuse2Detail");
						}
				}
		}
		
		var spread = graph.包长分布图;
		if(spread!=null){
				iName="b包长分布图";
				var spreadObj = JSON.parse(spread);
				for(var i in spreadObj){
						var spreadJs = JSON.parse(spreadObj[i]);
						var id = spreadJs.id;
						var title = spreadJs.title;
						var myChart = packetLen(title,id,spreadJs.x,spreadJs.list_y,spreadJs.subtext);
						//imageEx(id+title, myChart,iName);
						imageEx(title, myChart,"bpacketLen");
				}
			
				links = graph.链路图;
				if(links!=null){
						iName="d链路图";
						var linksObj = JSON.parse(links);
						for(var i in linksObj){
								var info = JSON.parse(linksObj[i]);
								var id = info.id;
								var title = info.title;
								var myChart = null;
								if(info.height < 259){
										myChart = linkChart3(title,id,info.y,info.list_y,info.topLeg,info.topGr);
								}else{
										myChart = linkChart4(title,id,info.y,info.list_y,info.topLeg,info.topGr);
								}
								//imageEx(id+title, myChart,iName);
								imageEx(title, myChart,"zlink");
						}
				}
	}
}
	
	
/*	var dataStr="";
    var tableObj = document.getElementById("tb1");
    var tableObj1 = document.getElementById("tb1Detail");
    var te1 = "";
    var te1= document.getElementById("tb1Detail").style.display;*/
    
    // 通用指标遍历
  /*  for (var i = 0; i < tableObj.rows.length; i++) { //遍历Table的所有Row
    	var tableInfo = "";
    	if(te1!="none"){
    		for (var j = 0; j < tableObj.rows[i].cells.length; j++) {   //遍历Row中的每一列
   			 var te = "";
   			 te = document.getElementById("tb1").rows[i].cells[j].style.display;
   			 if(te!="none"){
   					 tableInfo+= ",,"+tableObj.rows[i].cells[j].innerText;  //获取Table中单元格的内容
   			 	}
   			 }
   		 tableInfo=tableInfo.substring(2);		 
   	     dataStr+=("~~"+tableInfo);
	          }else {
    		for (var j = 0; j < tableObj.rows[i].cells.length-1; j++) {   //遍历Row中的每一列
      			 var te = "";
      			 te = document.getElementById("tb1").rows[i].cells[j].style.display;
      			 if(te!="none"){
      					 tableInfo+= ",,"+tableObj.rows[i].cells[j].innerText;  //获取Table中单元格的内容
      			 	}
      			 }
      		 tableInfo=tableInfo.substring(2);		 
      	     dataStr+=("~~"+tableInfo);
   	          }	
    	}
    		 */
    //详细信息遍历
  /*  dataStr=dataStr.substring(2);
    var t1= document.getElementById("tb1Detail").style.display;
    	var dataStr1="";
    	for (var x = 0; x < tableObj1.rows.length-2; x++) { //遍历Table的所有Row
        	var tableInfo = "";
        		 for (var y = 0; y < tableObj1.rows[x].cells.length; y++) {   //遍历Row中的每一列
        			 var te = "";
        			 var te1 = "";
          			 te = document.getElementById("tb1Detail").rows[x].cells[y].getAttribute("title");
          			 te1 = document.getElementById("tb1Detail").rows[x].cells[y].style.display;
        			 if(te1!="none"){
        				 tableInfo+= ",,"+tableObj1.rows[x].cells[y].innerText; //获取Table中单元格的内容
        			 	}
        			 }
        		 tableInfo=tableInfo.substring(2);		 
        		 dataStr1+=("~~"+tableInfo);
     	          }
        dataStr1=dataStr1.substring(2);*/
        
        	var Testvalue = document.getElementById('watermark').value;
        	var  TitleText =document.getElementById('title').innerText;
 
        	var isAutoSend = document.getElementsByName('FileType');
  
			//勾选了"视频比较"和"基本信息比较"中任何一个，后台导出时都要生成一个pdf,同时顶部显示"相应"比较信息
			for (var i = 0; i < isAutoSend.length; i++) {
					if (isAutoSend[i].checked) {
							var whoShowMapStr="{'a1':"+$('#a1')[0].checked+","
												  +"'a2':"+$('#a2')[0].checked+","
												  +"'a3':"+$('#a3')[0].checked+","
												  +"'a4':"+$('#a4')[0].checked+","
												  +"'a5':"+$('#a5')[0].checked+","
												  +"'a6':"+$('#a6')[0].checked+","
												  +"'a7':"+$('#a7')[0].checked+","
												  +"'a8':"+$('#a8')[0].checked+","
												  +"'a9':"+$('#a9')[0].checked+","
												  +"'a10':"+$('#a10')[0].checked+","
												  +"'a11':"+$('#a11')[0].checked+","
												  +"'a12':"+$('#a12')[0].checked+","
												  +"'a13':"+$('#a13')[0].checked+","
												  +"'a14':"+$('#a14')[0].checked+","
												  +"'a15':"+$('#a15')[0].checked+","
												  +"'a16':"+$('#a16')[0].checked+","
												  +"'a17':"+$('#a17')[0].checked+","
												  +"'a18':"+$('#a18')[0].checked+","
												  +"'a21':"+$('#a21')[0].checked+","
												  +"'a22':"+$('#a22')[0].checked+","
												  +"'a23':"+$('#a23')[0].checked+","
												  +"'a24':"+$('#a24')[0].checked+"}";
				    		if(isAutoSend[i].value==1){
					    			if($('#checkbox_compareConclusion')[0].checked){//比较时,two pcap,one pdf
						    				export2ftl(whoShowMapStr,true);
					    				/*
						        		    DownLoadFile({   
				    			                url:'/MLOAN/FilePdf/CreatePDF.do', //请求的url  
				    			                data:{data1:str_pdfData,//将对象序列化成字符串传递
				    			                	  watermarker:Testvalue,
				    			                	  Title:TitleText,
				    			                	  Chenck:filecheck,
				    			                	  showCompareConclusion:true,
				    			                	  baseFileName:baseFileName,
				    			                	  jingpinFileName:jingpinFileName
				    			                	  }//要发送的数据  
				    			            });  */
					    			}else{//非比较时，one pcap,one pdf,将“报文处理概述”表格的每一行都clone成一个单独的表格，生成图片，上传到服务器
					    	    			/*var norm_graph_temp=pdfData.norm_graph;
					    	    			var sysInfo_temp=pdfData.sysInfo;
					    	    			
					    	    			pdfData.norm_graph=null;
					    	    			pdfData.sysInfo=null;
					    	    			
					    	    			var str_pdfData=JSON.stringify(pdfData);
					    	    			
					    	    			pdfData.norm_graph=norm_graph_temp;
					    	    			pdfData.sysInfo=sysInfo_temp;
					    				
						    				DownLoadFile({   
					    		                url:'/MLOAN/FilePdf/CreatePDF.do', //请求的url  
					    		                data:{data1:str_pdfData,//将对象序列化成字符串传递
					    		                	  watermarker:Testvalue,
					    		                	  Title:TitleText,
					    		                	  Chenck:filecheck,
					    		                	  showCompareConclusion:false
					    		                	  }//要发送的数据  
					    					});  */
					    				export2ftl(whoShowMapStr,false);
					    			}
							}else if(isAutoSend[i].value==3){
									DownLoadFile({   
											url:'/MLOAN/file/CreatePPT.do', //请求的url  
											data:{data1:dataStr,data2:dataStr1}//要发送的数据  
						            });  
							}else if(isAutoSend[i].value==2){
									/*alert("dataStr="+dataStr);
									alert("dataStr1="+dataStr1);
									DownLoadFile({   
						                	url:'/MLOAN/reportOut/ExportExcel.do', //请求的url  
						                	data:{data1:dataStr,data2:dataStr1,data3:null}//要发送的数据  
						            }); */
									
									if($('#checkbox_compareConclusion')[0].checked){
											export2excel(whoShowMapStr, true);
									}else{//one file,one sheet
											export2excel(whoShowMapStr, false);
									}
									
							}
				    		break;//radio只能选中一个，所以不用遍历了
					}
			}
}




function uploadImg_sync(pcapCount,currNum0,currNum1){
		if(currNum0<pcapCount){
				var row_aready=3;
				var table=$("#messageSummarize").find("table").clone();
				var trs=table.find("tr");
				var tr_img=$(trs[0]);
				var tr_title=$(trs[1]);
				var tr_line=$(trs[2]);
				
				var trArr=[];
				for(var i=0;i<pcapCount;i++){
					  var tr=trs[row_aready+i*2];//3+0
					  trArr.push($(tr));
				}
			
			//	alert("fileName="+baowen[currNum].fileName);
				var pcapName=baowen[currNum0].fileName;
				
				var dom_gaishu=$("#messageSummarize").clone();
				var span=dom_gaishu.
				table=dom_gaishu.find("table");
				table.empty();
				table.append(tr_img);
				table.append(tr_title);
				table.append(tr_line);
				table.append(trArr[currNum0]);
				
				$("#gaishu").empty();
				$("#gaishu").append($(dom_gaishu.find("span")[0]));
				$("#gaishu").append(table);
				
				html2canvas($("#gaishu"), {
				        width:1050,
				    	height:$("#gaishu")[0].offsetHeight,
				        onrendered: function (canvas) {
				        		$.ajax({type:'post',
				        				async: true,
				        				data: ("imgdata=" + encodeURIComponent(canvas.toDataURL())), 
				        				dataType:'xml',
				        				url : "/MLOAN/img/uploadImg.req?imgName=gaishu_"+pcapName});
				        		
				        		uploadImg_sync(pcapCount,currNum0+1,currNum1);
				        }
			    });
		}else{
				$("#gaishu").empty();
				if(currNum1<pcapCount){
						var div_basicConc=$("#div_basicConclusion").clone();
						var div_jibenjielun=div_basicConc.find("#div_jibenjielun").clone();
						var divs=div_jibenjielun.find(".div_each");
						
						var pcapName=baowen[currNum1].fileName;
						
						$("#jielun").empty();
						var dom_jielun=$("#div_basicConclusion").clone();
						dom_jielun.find("#div_jibenjielun").empty();
						dom_jielun.find("#div_jibenjielun").append($(divs[currNum1]));
						$("#jielun").append(dom_jielun);
						
						html2canvas($("#jielun"), {
						        height:$("#jielun")[0].offsetHeight+20,
						        onrendered: function (canvas) {
						        		$.ajax({type:'post',
						        				async: true,
						        				data: ("imgdata=" + encodeURIComponent(canvas.toDataURL())), 
						        				dataType:'xml',
						        				url : "/MLOAN/img/uploadImg.req?imgName=jielun_"+pcapName});
						        		
						        		uploadImg_sync(pcapCount,currNum0,currNum1+1);
						        }
					    });
				}else{
						$("#jielun").empty();
						document.getElementById("over_home_form3").style.display = "none";
					    document.getElementById("layout_home_form3").style.display = "none";
					    document.onmousewheel=function(){
					    	return true;
					    }//释放"禁止拖动鼠标"
						return;
				}
		}
}





function  deepCopy(sourceObj) { //对象的"深度拷贝"
	var targetObj={};
	for (var attr in sourceObj) {
		if((typeof sourceObj[attr])==='object'){
				targetObj[attr] =deepCopy(sourceObj[attr]);
		}else{
				targetObj[attr] =sourceObj[attr];
		}
	} 
	return targetObj; 
}


/**图表生成图片 至 服务器*/
function imageEx(imgname,myChart,iName){
	var data = (""+imgname+"=" + encodeURIComponent(myChart.getDataURL({type:'png', pixelRatio: 1,backgroundColor: 'white'})));
    $.ajax({type:'post',data: data, dataType:'xml',url : "/MLOAN/img/chart.req?imgname="+iName}); }

var DownLoadFile = function (options) {  
    var config = $.extend(true, { method: 'post' }, options);  
    var $iframe = $('<iframe id="down-file-iframe" />');  
    var $form = $('<form target="down-file-iframe" method="' + config.method + '" />');  
    $form.attr('action', config.url);  
    
    for (var key in config.data) { //因为google的post对过长字段有限制，最大500000个字符，所以如果超过500000个字符，则拆分成多个字段提交，服务端接受到后合并 
	    	if(key=="data1"){//可能为过长字段
	    			var encodeValue=encodeURI(config.data[key]);
	    			var len=encodeValue.length;
	    			var maxPostSize=500000;
	    			var value;
	    			if(len>maxPostSize){
		    				var loopTime=len/maxPostSize;
		    				for(var i=0;i<loopTime;i++){
		    							value=encodeValue.substring(i*maxPostSize,(i+1)*maxPostSize);
		    							$form.append('<input id="" class="sss" type="text" name="' + key + '"  value="' + value + '"  />'); 
		    				}
	    			}else{
	    					$form.append('<input id="" class="sss" type="text" name="' + key + '"  value="' + encodeValue + '"  />'); 
	    			}
	    			$form.append('<input id="" class="sss" type="text" name="' + key + '"  value=""  />'); 
	    	}else{//普通字段
	    			$form.append('<input id="" class="sss" type="text" name="' + key + '"  value="' + encodeURI(config.data[key]) + '"  />'); 
	    	}
    }  
    
    $iframe.append($form);  
    $(document.body).append($iframe);  
    $form[0].submit();  
    $iframe.remove();  
}
var DownLoadFile1 = function (url,data,method) {  
	var $iframe = $('<iframe id="down-file-iframe" />');  
	var $form = $('<form action="'+$("#contextPath").val()+url+'"  target="down-file-iframe" method="' + method + '" />');  
	
	for (var key in data) {
			$form.append('<input id="" class="sss" type="text" name="' + key + '"  value="' + data[key] + '"  />'); 
	}  
	
	$iframe.append($form);  
	$(document.body).append($iframe);  
	$form[0].submit();  
	$iframe.remove();  
}

function tag1(){
	var state = document.getElementById('tag1').style.display;
	if(state=='none'){
		document.getElementById('tag1').style.display='';
	}else
		document.getElementById('tag1').style.display='none';}


function tag2(){
	var state = document.getElementById('tag2').style.display;
	if(state=='none'){
		document.getElementById('tag2').style.display='';
	}else
		document.getElementById('tag2').style.display='none';}


//计算相差百分比
function calcuPercentDiff(num1,num2){
		var num1_f = parseFloat(num1);  
		var num2_f = parseFloat(num2); 
		
		var  percentDiff= 0;
		if(!isNaN(num1_f) && !isNaN(num2_f)){
				if(num1_f>num2_f){
						percentDiff = num2_f/num1_f*100;
				}else{
						percentDiff = num1_f/num2_f*100;
				}
				percentDiff=percentDiff.toFixed(2)+"%";
		}else{
				percentDiff="N/A";
		}
		
		return percentDiff;
}


function MouseWheel(e) {
    e = e || window.event;
  
    if (e.stopPropagation) e.stopPropagation();
    else e.cancelBubble = true;
      
    if (e.preventDefault) e.preventDefault();
    else e.returnValue = false;
  
}

function xxx(){
	//判断浏览器
	var isIE=navigator.userAgent.match(/MSIE (\d)/i);
	isIE=isIE?isIE[1]:undefined;
	var isFF=/FireFox/i.test(navigator.userAgent);
	
	//获取元素
	var counter=document.getElementsByName("body");
	//鼠标滚轮事件
	if(isIE<9) //传统浏览器使用MouseWheel事件
	 counter.attachEvent("onmousewheel",function(){
	  //计算鼠标滚轮滚动的距离
	  //一格3行，每行40像素，所以除以120
	  var v=event.wheelDelta/120;
	  counter.innerHTML=counter.innerHTML*1+v;
	  //阻止浏览器默认方法
	  return false;
	 });
	else if(!isFF) //除火狐外的现代浏览器也使用MouseWheel事件
	 counter.addEventListener("mousewheel",function(e){
	  //计算鼠标滚轮滚动的距离
	  var v=e.wheelDelta/120;
	  counter.innerHTML=counter.innerHTML*1+v;
	  //阻止浏览器默认方法
	  e.preventDefault();
	 },false);
	else //奇葩的火狐使用DOMMouseScroll事件
	 counter.addEventListener("DOMMouseScroll",function(e){
	  //计算鼠标滚轮滚动的距离
	  //一格是3行，但是要注意，这里和像素不同的是它是负值
	  var v=-e.detail/3;
	  counter.innerHTML=counter.innerHTML*1+v;
	  //阻止浏览器默认方法
	  e.preventDefault();
	 },false);
	
}



function export2ftl(whoShowMap,compare){//可以理解为："分析+导出到html",而"生成pdf"="分析+导出到pdf"
		var filecheck = null;
		
		var fileNum = $('input[name="filecheck"]:checked').length;
		if(fileNum==0){
			alert("请选择文件");
			return;
		}
		if($('#checkbox_compareConclusion')[0].checked){//"基本信息比较"
			if(fileNum!=2){
				alert("请选择2个文件");
				return;
			}
		}
		var id_array=new Array();  // 文件列表
		$('input[name="filecheck"]:checked').each(function(){  
		    id_array.push($(this).attr('id'));//向数组中添加元素  
		});  
		filecheck = id_array.join(',');//将数组元素连接起来以构建一个字符串  
		
		id_array.length=0; //通用指标
		var id_array_id=new Array();  // 文件列表
		$('input[name="a"]:checked').each(function(){  
			var id = $(this).attr('value');
		    id_array.push(id);
		    id_array_id.push($(this).attr("id"));
		});  
		var norm=id_array.join(',');
		
		id_array.length=0; //业务个性指标
		$('input[name="aa"]:checked').each(function(){  
		    id_array.push($(this).attr('value'));
		});  
		var kidney=id_array.join(',');
			
		if(norm=="" && kidney==""){
			alert("请选择通用指标");
			return;
		}
		
		if(compare){//比较时
			DownLoadFile1("/reportOut/export2ftl.do?math="+Math.random(),
		    		  {'filecheck':filecheck,
					   'norm':norm,
					   'kidney':kidney,
					   'jingpinFileName':jingpinFileName,
					   'watermark':document.getElementById('watermark').value,
					   'title':document.getElementById('title').innerText,
					   'whoShowMap':whoShowMap,
					   'compare':true
					  },
					  'post');  
		}else{
			DownLoadFile1("/reportOut/export2ftl.do?math="+Math.random(),
		    		  {'filecheck':filecheck,
					   'norm':norm,
					   'kidney':kidney,
					   'jingpinFileName':jingpinFileName,
					   'watermark':document.getElementById('watermark').value,
					   'title':document.getElementById('title').innerText,
					   'whoShowMap':whoShowMap,
					   'compare':false
					  },
					  'post');  
		}
}


function export2excel(whoShowMap,compare){//可以理解为："分析+导出到html",而"生成pdf"="分析+导出到pdf"
		var filecheck = null;
		
		var fileNum = $('input[name="filecheck"]:checked').length;
		if(fileNum==0){
			alert("请选择文件");
			return;
		}
		if($('#checkbox_compareConclusion')[0].checked){//"基本信息比较"
			if(fileNum!=2){
				alert("请选择2个文件");
				return;
			}
		}
		var id_array=new Array();  // 文件列表
		$('input[name="filecheck"]:checked').each(function(){  
		    id_array.push($(this).attr('id'));//向数组中添加元素  
		});  
		filecheck = id_array.join(',');//将数组元素连接起来以构建一个字符串  
		
		id_array.length=0; //通用指标
		var id_array_id=new Array();  // 文件列表
		$('input[name="a"]:checked').each(function(){  
			var id = $(this).attr('value');
		    id_array.push(id);
		    id_array_id.push($(this).attr("id"));
		});  
		var norm=id_array.join(',');
		
		id_array.length=0; //业务个性指标
		$('input[name="aa"]:checked').each(function(){  
		    id_array.push($(this).attr('value'));
		});  
		var kidney=id_array.join(',');
			
		if(norm=="" && kidney==""){
			alert("请选择通用指标");
			return;
		}
		
		if(compare){//比较时
			DownLoadFile1("/reportOut/export2ftl.do?math="+Math.random(),
		    		  {'filecheck':filecheck,
					   'norm':norm,
					   'kidney':kidney,
					   'jingpinFileName':jingpinFileName,
					   'watermark':document.getElementById('watermark').value,
					   'title':document.getElementById('title').innerText,
					   'whoShowMap':whoShowMap,
					   'compare':true
					  },
					  'post');  
		}else{//-----todo
			DownLoadFile1("/reportOut/ExportExcel.do?math="+Math.random(),
		    		  {'filecheck':filecheck,
					   'norm':norm,
					   'kidney':kidney,
					   'jingpinFileName':jingpinFileName,
					   'watermark':document.getElementById('watermark').value,
					   'title':document.getElementById('title').innerText,
					   'whoShowMap':whoShowMap,
					   'compare':false
					  },
					  'post');  
		}
}
