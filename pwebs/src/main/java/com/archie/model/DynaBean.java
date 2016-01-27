package com.archie.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.archie.util.BeanUtils;
import com.archie.util.DateUtils;

/**
 * 自定义动态类
 *
 * 手工设定属性的内容，系统保留以下属性：
 * $TABLE_CODE$ :   表的名称
 * $ROWS$       :   结果记录集
 * $P_COUNT$      :   当前返回记录数
 * $A_COUNT$  :   总的查询记录数
 * $SQL$        :   查询的SQL语句
 * .....  (所有参数信息见@see BeanUtils.java)
 */
public class DynaBean implements Serializable {
    /** 序列号  */
    private static final long serialVersionUID = -2563215753689331432L;
    
    /** 存放属性的值集 */
    private Map values = Collections.synchronizedMap(new HashMap<String,Object>());

    /**
     * 初始化空的动态类
     */
    public DynaBean() {
    }
    
    /**
     * 初始化带表编码信息的动态类
     * @param tableCode 表的编码（即数据库表名）
     */
    public DynaBean(String tableCode) {
        values.put(BeanUtils.KEY_TABLE_CODE, tableCode);
    }
    /**
     * 初始化带表编码信息的动态类
     * @param tableCode 表的编码（即数据库表名）
     * @param sqlWhere 表的查询条件
     */
    public DynaBean(String tableCode,String sqlWhere) {
    	values.put(BeanUtils.KEY_TABLE_CODE, tableCode);
        values.put(BeanUtils.KEY_WHERE, sqlWhere);
    }
    /**
     * 初始化带表编码信息的动态类
     * @param tableCode 表的编码（即数据库表名）
     * @param sqlWhere 表的查询条件
     * @param orderby 排序
     */
    public DynaBean(String tableCode,String sqlWhere,String orderBy) {
    	values.put(BeanUtils.KEY_TABLE_CODE, tableCode);
        values.put(BeanUtils.KEY_WHERE, sqlWhere);
        values.put(BeanUtils.KEY_ORDER, orderBy);
    }
    /**
     * 设置属性值，如果有则直接覆盖，如果没有则添加一个
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public void set(String key, Object value) {
        values.put(key, value);
    }
    
    /**
     * 设置String类型的属性值，如果有则直接覆盖，如果没有则添加一个
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public void setStr(String key, String value) {
        values.put(key, value);
    }

    /**
     * 设置int类型的属性值，如果有则直接覆盖，如果没有则添加一个
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public void setInt(String key, int value) {
        values.put(key, String.valueOf(value));
    }

    /**
     * 设置long类型的属性值，如果有则直接覆盖，如果没有则添加一个
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public void setLong(String key, long value) {
        values.put(key, String.valueOf(value));
    }
    
    /**
     * 设置float类型的属性值，如果有则直接覆盖，如果没有则添加一个
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public void setFloat(String key, float value) {
        values.put(key, String.valueOf(value));
    }
    
    /**
     * 设置double类型的属性值，如果有则直接覆盖，如果没有则添加一个
     *
     * @param key 属性名称
     * @param value 属性值
     */
    public void setDouble(String key, double value) {
        values.put(key, String.valueOf(value));
    }
    
    /**
     * 得到属性对应的值
     *
     * @param key 属性名称
     *
     * @return 对应的值
     */
    public Object get(String key) {
        return values.get(key);
    }
    
    /**
     * 得到属性对应的值
     *
     * @param key 属性名称
     * @param defValue  缺省值，如果没有就返回缺省值
     *
     * @return 对应的值
     */
    public Object get(String key, Object defValue) {
        Object value = get(key);
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }
    
    /**
     * 直接得到字符串类型的返回值
     * @param key   属性名称
     * @return      字符串类型的返回值
     */
    public String getStr(String key) {
    	Object value = values.get(key);
        if (value == null) {
            return null;
        } else if(value instanceof java.sql.Timestamp){
        	return (String) DateUtils.getStringFromDate((java.sql.Timestamp)value, "yyyy-MM-dd HH:mm:ss");
        }else if (value instanceof java.util.Date){
            return (String) DateUtils.getStringFromDate((java.util.Date)value, "yyyy-MM-dd HH:mm:ss");
        }else {
        	return value.toString();
		}
         
    }
    
    /**
     * 直接得到字符串类型的返回值
     * @param key   属性名称
     * @param defValue  如果取不到就返回缺省值
     * @return      字符串类型的返回值
     */
    public String getStr(String key, String defValue) {
        Object value = values.get(key);
        if (value == null) {
            return defValue;
        } else if(value instanceof java.sql.Timestamp){
            return (String) DateUtils.getStringFromDate((java.sql.Timestamp)value, "yyyy-MM-dd HH:mm:ss");
        } else{
        	return value.toString();
		}
    }
    
    /**
     * 得到整型返回值
     * @param key   属性名称
     * @return      整型返回值
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    /**
     * 得到整型返回值
     * @param key       属性名称
     * @param defValue  如果为空则返回缺省值
     * @return          整型返回值
     */
    public int getInt(String key, int defValue) {
        Object value = values.get(key);
        if (value == null || value.toString().length() == 0) {
            return defValue;
        } else {
            return Integer.parseInt(value.toString());
        }
    }

    /**
     * 得到长整型返回值
     * @param key   属性名称
     * @return      整型返回值
     */
    public long getLong(String key) {
        return getLong(key , 0);
    }
    
    /**
     * 得到长整型返回值
     * @param key       属性名称
     * @param defValue  如果为空则返回缺省值
     * @return          整型返回值
     */
    public long getLong(String key, long defValue) {
        Object value = values.get(key);
        if (value == null || value.toString().length() == 0) {
            return defValue;
        } else {
            return Long.parseLong(value.toString());
        }
    }
    
    /**
     * 得到double型返回值
     * @param key   属性名称
     * @return      整型返回值
     */
    public double getDouble(String key) {
        return getDouble(key, 0);
    }
    
    /**
     * 得到double型返回值
     * @param key       属性名称
     * @param defValue  如果为空则返回缺省值
     * @return          整型返回值
     */
    public double getDouble(String key, double defValue) {
        Object value = values.get(key);
        if (value == null || value.toString().length() == 0) {
            return defValue;
        } else {
            return Double.parseDouble(value.toString());
        }
    }

    /**
     * 得到float型返回值
     * @param key   属性名称
     * @return      float返回值
     */
    public float getFloat(String key) {
        return getFloat(key, 0);
    }
    
    /**
     * float
     * @param key       属性名称
     * @param defValue  如果为空则返回缺省值
     * @return          float返回值
     */
    public float getFloat(String key, float defValue) {
        Object value = values.get(key);
        if (value == null || value.toString().length() == 0) {
            return defValue;
        } else {
            return Float.parseFloat(value.toString());
        }
    }
     
    /**
     * 设置值集
     *
     * @param valueMap 值集
     */
    public void setValues(Map valueMap) {
        this.values = valueMap;
    }

    /**
     * 得到值集
     * @return 值集
     */
    public Map getValues() {
        return values;
    }
    /**
     * 清除数据
     */
    public void clear() {
        this.values.clear();
    }
    
    
    /**
     * 比较两个bean的内容，只比较fields中指定的字段
     * @param anotherBean 另一个bean
     * @param fields 字段编码，可以为多个，以","分隔
     * @return boolean型
     */
    public boolean compare(DynaBean anotherBean, String fields) {
        boolean result = true;
        String[] fieldArray = fields.split(",");
        for (int i = 0; i < fieldArray.length; i++) {
            if (!this.getStr(fieldArray[i]).equals(anotherBean.getStr(fieldArray[i]))) {
                result = false;
                break;
            }
        }
        return result;
    }
    
    
    public String toJsonString(){
    	StringBuilder sb = new StringBuilder("{");
    	Iterator iter = this.values.entrySet().iterator();
    	while(iter.hasNext()){
    		Map.Entry entry = (Map.Entry) iter.next();
    		sb.append(entry.getKey()+":\""+entry.getValue()+"\",");
    	}    	
    	sb.setLength(sb.length()-1);
    	sb.append("}");
    	if(sb.length()==1){
    		return "";
    	}
    	return sb.toString();
    }
}
