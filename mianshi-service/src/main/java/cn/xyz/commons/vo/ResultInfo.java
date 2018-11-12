package cn.xyz.commons.vo;

import java.io.Serializable;

/**
 * 
 * @ClassName: ResultInfo 
 * @Description: 用于传递结果参数
 * @author muGua 
 * @date 2017年9月22日 下午5:49:36 
 * @param <T>
 */
public class ResultInfo<T extends Object> implements Serializable {

	/** 
	* @Fields serialVersionUID : TODO 
	*/
	private static final long serialVersionUID = 1L;

	private String code;// 状态码

	private String msg;// 消息

	private T data;// 数据

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ResultInfo [code=" + code + ", msg=" + msg + ", data=" + data + "]";
	}
	
}