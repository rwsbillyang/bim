package cn.niukid.bim.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BucketItemBean {

	String id;
	String text;
	String type;
	boolean children;
	public BucketItemBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BucketItemBean(String id, String text, String type, boolean children) {
		super();
		this.id = id;
		this.text = text;
		this.type = type;
		this.children = children;
	}
	
	
}
