package com.dkd.emms.core.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dkd.emms.core.dao.BaseDao;
import com.dkd.emms.core.util.bean.BeanUtils;
import com.dkd.emms.core.util.page.Page;
import com.dkd.emms.core.util.uuid.UUIDGenerator;


/**
 * 泛型Service实现
 * @author SY
 * @param <T> 实体
 * 
 */

@Service
@Transactional
public abstract class BaseService<T>  {
	
	public Log log = LogFactory.getLog(this.getClass());
	
	public abstract BaseDao<T> getDao();
	
	public String getDefaultSqlNamespace() {
		Class<?> genericClass = BeanUtils.getGenericClass(this.getClass());
		return genericClass == null ? null : genericClass.getSimpleName();
	}
    
	/**
	 * 通用插入实体对象
	 * @param t
	 * @return
	 */
	
    public int insert(T t) {  
    	return getDao().insert(t);
    }  
    
    /**
     * 通用修改实体
     * @param t
     * @return
     */
    
    public int update(T t) {  
        return getDao().update(t);  
    }  
    
    /**
     * 通用编辑实体
     * @param t
     * @param primaryKey
     */
    
    public void edit(T t, String primaryKey) {
    	if(StringUtils.isEmpty(primaryKey)){
    		try {
    			BeanUtils.invokeSet(t, getDefaultSqlNamespace().toLowerCase()+"Id", UUIDGenerator.getUUID());
    		} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString());
			}
    		getDao().insert(t);
		}else{
			getDao().update(t);
		}
    }
    
  
    
    /**
     * 通用删除实体 物理删除
     * @param primaryKey
     * @return
     */
    
    public int delete(String primaryKey) {  
        return getDao().delete(primaryKey);  
    } 
    
    /**
     * 分页查询
     * @param object 
     * @return
     */
    
    public List<T> selectByCondition(Object object,Integer total,Integer start,Integer length) {
    		Page page = new Page(total,start,length);	
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(object.getClass().getSimpleName(), object);
			map.put("page", page);
			return getDao().selectByCondition(map);
	}
	/**
	 * 分页查询
	 * @param object
	 * @return
	 */

	public List<T> selectByCondition(Object object,Integer total) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(object.getClass().getSimpleName(), object);
		return getDao().selectByCondition(map);
	}
    /**
     * 通用的id查询实体
     * @param primaryKey
     * @return
     */
    
	public T selectByPk(String primaryKey) {  
        return getDao().selectByPk(primaryKey);
    }
	
	/**
	 * 通用查询全部
	 * @return
	 */
	
    public List<T> selectAll() {
    	return getDao().selectAll();
	}
	
    /**
     * 通用统计全部
     * @return
     */
    
	public int countAll() { 
		 return getDao().countAll();
	}
	
	/**
	 * 通用条件统计
	 * @param object
	 * @return
	 */
	
	public int countByCondition(Object object) { 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(object.getClass().getSimpleName(), object);
		return getDao().countByCondition(map);
	} 
    
	/**
	 * 打印预览
	 * @param fileName
	 * @param outPutFile
	 * @param map
	 * @throws TransformerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 *//*
	
	public void convert2Html(String fileName, String outPutFile,Map<String, String> map) {
		HWPFDocument wordDocument=replaceDoc(fileName, map);
		WordToHtmlConverter wordToHtmlConverter;
		try {
			wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			wordToHtmlConverter.processDocument(wordDocument);
			Document htmlDocument = wordToHtmlConverter.getDocument();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.toString("UTF-8");
			DOMSource domSource = new DOMSource(htmlDocument);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, streamResult);
			out.close();
			writeFile(new String(out.toByteArray(),"UTF-8"), outPutFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}*/
	
	/**
	 * 替换文本
	 * @param srcPath
	 * @param map
	 * @return
	 *//*
	
	public HWPFDocument replaceDoc(String srcPath, Map<String, String> map) {
		try {
			FileInputStream fis = new FileInputStream(new File(srcPath));// 读取word模板
			HWPFDocument doc = new HWPFDocument(fis);
			Range bodyRange = doc.getRange();// 读取word文本内容
			for (Map.Entry<String, String> entry : map.entrySet()) {// 替换文本内容
				if(StringUtils.equals(entry.getValue(), null)){
					bodyRange.replaceText("${" + entry.getKey() + "}", "");
				}else{
					bodyRange.replaceText("${" + entry.getKey() + "}", entry.getValue().toString());
				}
			}
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}*/
	
	/**
	 * word转换jsp
	 * @param content
	 * @param path
	 */
	
	public void writeFile(String content, String path) {
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			File file = new File(path);
			fos = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));
			bw.write("<%@ page language=\"java\" import=\"java.util.*\" pageEncoding=\"UTF-8\"%>");
			bw.write(content);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fos != null)
					fos.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

}
