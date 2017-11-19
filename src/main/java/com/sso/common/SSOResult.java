package com.sso.common;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SSOResult {
	private static final ObjectMapper MAPPER=new  ObjectMapper();
	//״̬��
	private Integer status;
	//������Ϣ
	private String meg;
	//��������
	private Object data;
	
	public static SSOResult build(Integer status, String msg, Object data) {
        return new SSOResult(status, msg, data);
    }
	public static SSOResult ok(Object data) {
        return new SSOResult(data);
    }

    public static SSOResult ok() {
        return new SSOResult(null);
    }

    public SSOResult() {

    }

    public static SSOResult build(Integer status, String msg) {
        return new SSOResult(status, msg, null);
    }
	//���캯��
	public SSOResult(Integer status,String meg,Object data){
		this.status=status;
		this.meg=meg;
		this.data=data;
	}
	//�ɹ�200״̬
	public SSOResult(Object data){
		this.status=200;
		this.meg="OK";
		this.data=data;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getMeg() {
		return meg;
	}
	public void setMeg(String meg) {
		this.meg = meg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	/**
     * ��json�����ת��ΪSSOResult����
     * 
     * @param jsonData json����
     * @param clazz SSOResult�е�object����
     * @return
     */
    public static SSOResult formatToPojo(String jsonData, Class<?> clazz) {
        try {
            if (clazz == null) {
                return MAPPER.readValue(jsonData, SSOResult.class);
            }
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (clazz != null) {
                if (data.isObject()) {
                    obj = MAPPER.readValue(data.traverse(), clazz);
                } else if (data.isTextual()) {
                    obj = MAPPER.readValue(data.asText(), clazz);
                }
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * û��object�����ת��
     * 
     * @param json
     * @return
     */
    public static SSOResult format(String json) {
        try {
            return MAPPER.readValue(json, SSOResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Object�Ǽ���ת��
     * 
     * @param jsonData json����
     * @param clazz �����е�����
     * @return
     */
    public static SSOResult formatToList(String jsonData, Class<?> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (data.isArray() && data.size() > 0) {
                obj = MAPPER.readValue(data.traverse(),
                        MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }
}
