package com.archie.model.user;

import java.io.Serializable;

/**
 * @ClassName:UserInfo
 * @Description:TODO
 *
 * @author 潘立海
 * @date 2015年12月10日 @
 */
public class UserInfo implements Serializable {
	/**
	 * 用户ID
	 */
	private int userId;

	/**
	 * 注册密文
	 */
	private String registerCiphertext;

	/**
	 * 全局ID
	 * 
	 * @return
	 */
	private String globalId;

	/**
	 * 验证码
	 */
	private int authCode;
	/**
	 * 手机
	 */
	private String mobilePhone;

	/**
	 * 会话ID
	 * 
	 * @return
	 */
	private int authId;

	/**
	 * 昵称
	 * 
	 * @return
	 */
	private String nickName;

	/**
	 * 目的（0.注册/1.重置密码）
	 * 
	 * @return
	 */
	private int destination;

	/**
	 * 头像路径
	 */
	private String avatarPath;

	/**
	 * 支付方式
	 */
	private int payType;

	private String pwd;
	
	/**
	 * 用户类型  1 系统用户，0 普通用户
	 * */
	private Integer memberType; 
   
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getRegisterCiphertext() {
		return registerCiphertext;
	}

	public void setRegisterCiphertext(String registerCiphertext) {
		this.registerCiphertext = registerCiphertext;
	}

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public int getAuthCode() {
		return authCode;
	}

	public void setAuthCode(int authCode) {
		this.authCode = authCode;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public int getAuthId() {
		return authId;
	}

	public void setAuthId(int authId) {
		this.authId = authId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public String getAvatarPath() {
		return avatarPath;
	}

	public void setAvatarPath(String avatarPath) {
		this.avatarPath = avatarPath;
	}

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public Integer getMemberType() {
		return memberType;
	}

	public void setMemberType(Integer memberType) {
		this.memberType = (memberType!=null?memberType:0);
	}
	
}
