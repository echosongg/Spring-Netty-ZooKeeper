package com.xtwy.client.param;

public class Response {
	private Long id;
	private Object result;
	private String code = "00000";//00000表示成功，其他表示失败
	private String msg;//失败的原因
	public void setId(Long id) {
		this.id = id;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public Long getId() {
		// TODO Auto-generated method stub
		return id;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
		
	}
	
}
