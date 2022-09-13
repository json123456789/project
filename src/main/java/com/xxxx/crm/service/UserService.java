package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.dao.UserMapper;
import com.xxxx.crm.query.UserModel;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.Md5Util;
import com.xxxx.crm.utils.UserIDBase64;
import com.xxxx.crm.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserService extends BaseService<User,Integer> {

    @Resource
    private UserMapper userMapper;

    /** 用户登录
             2.校验参数是否为空
                如果为空，抛异常
             3.调用dao层查询通过用户名查询数据库数据
                如果未查到，抛异常(用户不存在)
             4.校验前台传来的密码和数据库中的密码是否一致 (前台密码加密后再校验)
                如果不一致，抛异常(密码错误)
             5.封装ResultInfo对象给前台（根据前台需求：usermodel对象封装后传到前台使用）
     */
    public ResultInfo loginCheck(String userName ,String userPwd){
        //校验参数是否为空
        checkLoginData(userName,userPwd);
        //调用dao层查询通过用户名查询数据库数据，判断账号是否存在
        User user = userMapper.queryUserByName(userName);
        AssertUtil.isTrue(user == null,"账号不存在");

        //校验前台传来的密码和数据库中的密码是否一致 (前台密码加密后再校验)
        checkLoginPwd(user.getUserPwd(),userPwd);

        //封装ResultInfo对象给前台（根据前台需求：usermodel对象封装后传到前台使用）
        ResultInfo resultInfo = buildResultInfo(user);

        return resultInfo;
    }


    /** 修改密码
     */
    public void userUpdate(Integer userId,String oldPassword,String newPassword,String confirmPassword){
        //确保用户是否是登录状态获取cookie中的id 非空 查询数据库
        AssertUtil.isTrue(userId == null,"用户未登录");
        User user = userMapper.selectByPrimaryKey(userId);
        AssertUtil.isTrue(user == null,"用户状态异常");

        //校验密码数据
        checkUpdateData(oldPassword,newPassword,confirmPassword,user.getUserPwd());
        // 执行修改操作，返回ResultInfo
        user.setUserPwd(Md5Util.encode(newPassword));
        user.setUpdateDate(new Date());
        //判断是否修改成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) < 1,"密码修改失败");
    }


    /**密码校验
     *          1.确保用户是否是登录状态获取cookie中的id 非空 查询数据库
     *          2.校验老密码 非空  老密码必须要跟数据库中密码一致
     *          3.新密码    非空  新密码不能和原密码一致
     *          4.确认密码  非空  确认必须和新密码一致
     *          5.执行修改操作，返回ResultInfo
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @param
     */
    private void checkUpdateData(String oldPassword, String newPassword, String confirmPassword, String dbPassword) {
        //校验老密码  非空  老密码必须要跟数据库中密码一致
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"原始密码不存在");
        AssertUtil.isTrue(!dbPassword.equals(Md5Util.encode(oldPassword)),"原始密码错误");

        //新密码    非空  新密码不能和原密码一致
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        AssertUtil.isTrue(oldPassword.equals(newPassword),"新密码不能和原密码一致");

        //确认密码  非空  确认必须和新密码一致
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword),"确认密码不能为空");
        AssertUtil.isTrue(!confirmPassword.equals(newPassword),"确认密码必须和新密码一致");

    }


    /**
     * 准备前台cookie需要的数  usermodel
     * @param user
     */
    private ResultInfo buildResultInfo(User user) {
        ResultInfo resultInfo = new ResultInfo();

        //封装userMdel  cookie需要的数据
        UserModel userModel = new UserModel();
        //将userid加密
        String id = UserIDBase64.encoderUserID(user.getId());
        userModel.setUserId(id);
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());

        resultInfo.setResult(userModel);
        return resultInfo;
    }


    /**
     * 校验前台传来的密码和数据库中的密码是否一致 (前台密码加密后再校验)
     */
    private void checkLoginPwd(String dbPwd, String userPwd) {
        //将传来的密码加密再校验
        String encodePwd = Md5Util.encode(userPwd);
        //校验
        AssertUtil.isTrue(!encodePwd.equals(dbPwd),"用户密码错误");
    }

    /**
     * 用户登录参数非空校验
     * @param userName
     * @param userPwd
     */
    private void checkLoginData(String userName, String userPwd) {
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"用户密码不能为空");
    }
}
