package com.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.util.base.ActionBase;
import com.util.base.LogThreads;
import com.vo.FileInfo;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

@Service
public class LogsAnalyzeServiceImpl extends ActionBase implements ILogsAnalyzeService{

	private static final Logger log = Logger.getLogger(LogsAnalyzeServiceImpl.class);
	private TaskExecutorUtil taskExecutorUtil;
	@Autowired
	private ShardedJedisPool shardedJedisPool; 
	public TaskExecutorUtil getTaskExecutorUtil() {
		return taskExecutorUtil;
	}

	public void setTaskExecutorUtil(TaskExecutorUtil taskExecutorUtil) {
		this.taskExecutorUtil = taskExecutorUtil;
	}

	@SuppressWarnings({ "rawtypes"})
	@Override
	public Map<String, Object> logAscription(String normData, List<String> redisNoFile,
			IGraphDateService service) throws InterruptedException, ExecutionException {
    	
		log.info("log文件分析线程池启动");
		LogThreads.list_maps0.clear();
		LogThreads.ipAddrIds.clear();
		LogThreads.ipId0 = 0;
		LogThreads.service0 = service;
		Iterator<String> loglist = redisNoFile.iterator();
		LogThreads.normData0 = normData;
		
		try {
			LogThreads.serverpath = config("serverpath");
		} catch (IOException e) {
			log.error(e);
		}
		
		
		// 创建一个线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        
        //获取Future对象
        Future f1 = null;

        List<FileInfo> fileInfos = new ArrayList<FileInfo>();
        
        // 创建多个有返回值的任务
        List<Future> list = new ArrayList<Future>();
        ShardedJedis jedis = null;
        while (loglist.hasNext()) {
        	jedis = shardedJedisPool.getResource();
             // 执行任务
       		 f1 = pool.submit(new LogThreads(loglist.next(), jedis));
       		 list.add(f1); 
   		}
    	 // 获取所有并发任务的运行结果
        for (Future f : list) {
           // 从Future对象上获取任务的返回值，并输出到控制台
      	  	fileInfos.add((FileInfo) f.get());
        }
        
        // 关闭线程池
        pool.shutdown();
		
    	log.info("文件 分析完毕*************主线程执行");
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("fileInfos", fileInfos);
    	map.put("list_maps", LogThreads.list_maps0);
    	map.put("ipAddrIds", LogThreads.ipAddrIds);
    	return map;
		
	}

}