/* * Copyright (C) 2015 G-Wearable Inc. * All rights reserved. */package com.children.littlewalter.model;import java.io.Serializable;import java.util.List;import com.lidroid.xutils.db.annotation.Id;/** * Created by peter on 3/3/15. */public class CardItem implements Serializable {	private static final long serialVersionUID = 3388701081007512693L;	@Id	private int _id;	//id	private int mid;	//正常模式下的item的Drawable Id	private int img_normal;	//按下模式下的item的Drawable Id	private int img_pressed;	//item的排序字段	private int orderId;    private String mName;    private List<String> mImages;    private String[] mAudios;	public int get_id() {		return _id;	}	public void set_id(int _id) {		this._id = _id;	}	public int getMid() {		return mid;	}	public void setMid(int mid) {		this.mid = mid;	}	public int getImgurl() {		return img_normal;	}	public void setImgurl(int imgurl) {		this.img_normal = imgurl;	}	public int getImgdown() {		return img_pressed;	}	public void setImgdown(int imgdown) {		this.img_pressed = imgdown;	}	public int getOrderId() {		return orderId;	}	public void setOrderId(int orderId) {		this.orderId = orderId;	}    public List<String> getImages() {        return this.mImages;    }    public void setImages(List<String> images) {        this.mImages = images;    }    public String[] getAudios() {        return this.mAudios;    }    public void setAudios(String[] audios) {        this.mAudios = audios;    }    public void setName(String name) {        this.mName = name;    }    public String getName() {        return this.mName;    }}