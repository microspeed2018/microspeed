package com.zjzmjr.admin.web.audit.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.zjzmjr.admin.web.audit.form.AuditForm;
import com.zjzmjr.core.api.audit.IGardenProjectAuditFacade;
import com.zjzmjr.core.base.page.PageBean;
import com.zjzmjr.core.base.result.ResultEntry;
import com.zjzmjr.core.base.result.ResultPage;
import com.zjzmjr.core.base.util.Util;
import com.zjzmjr.core.common.message.MessageHandlerUtil;
import com.zjzmjr.core.common.transaction.AdminTransactionUtil;
import com.zjzmjr.core.enums.admin.AdminBusinessTypeEnum;
import com.zjzmjr.core.enums.admin.HandleResultEnum;
import com.zjzmjr.core.enums.project.ProjectTableStatusEnum;
import com.zjzmjr.core.model.admin.AdminBusiness;
import com.zjzmjr.core.model.audit.GardenProjectAuditQuery;
import com.zjzmjr.core.model.audit.GardenProjectFinancialAudit;
import com.zjzmjr.security.web.util.SpringSecurityUtil;
import com.zjzmjr.web.mvc.controller.BaseController;

/**
 * 财务审核控制层
 * 
 * @author lenovo
 * @version $Id: FinancialAuditController.java, v 0.1 2017-9-1 下午3:20:53 lenovo Exp $
 */
@Controller
@RequestMapping("/financialAudit/user")
public class FinancialAuditController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(FinancialAuditController.class);
    private final static String index = "/WEB-INF/pages/audit/financia.jsp";

    @RequestMapping(value = "/index.htm", method = RequestMethod.GET)
    public String index(ModelMap model) {
        return index;
    }

    @Resource(name = "gardenProjectAuditFacade")
    private IGardenProjectAuditFacade gardenProjectAuditFacade;
    
    /**
     * 财务审核一览
     * 
     * @param resp
     * @param form
     * @param req
     */
    @RequestMapping(value = "/getProjectFinancialAudit.htm", method = RequestMethod.POST)
    public void getProjectFinancialAudit(HttpServletResponse resp,AuditForm form,HttpServletRequest req){
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            GardenProjectAuditQuery query = new GardenProjectAuditQuery();
            query.setId(form.getId());
            query.setCompanyId(SpringSecurityUtil.getIntCompany());
            if(Util.isNotNull(form.getProjectNo())){
                query.setProjectNo(form.getProjectNo()); 
            }
            //项目名称
            if(Util.isNotNull(form.getName())){
                query.setName(form.getName());  
            }
            if(Util.isNotNull(form.getAdminName())){
                query.setAdminName(form.getAdminName()); 
            }
            if(Util.isNotNull(form.getCreateTimeStart())){
                query.setCreateTimeStart(form.getCreateTimeStart()); 
            }
            if(Util.isNotNull(form.getCreateTimeEnd())){
                query.setCreateTimeEnd(form.getCreateTimeEnd());                
            }
            if(Util.isNotNull(form.getType())){
                query.setType(form.getType());
            }
            if(Util.isNotNull(form.getStatus())){
                query.setStatus(form.getStatus()); 
            }
            if(Util.isNotNull(form.getVerifyTimeStart())){
                query.setVerifyTimeStart(form.getVerifyTimeStart());
            }
            if(Util.isNotNull(form.getVerifyTimeEnd())){
                query.setVerifyTimeEnd(form.getVerifyTimeEnd());
            }   
            query.setUpdateUserId(SpringSecurityUtil.getIntPrincipal());
            query.setPageBean(new PageBean(form.getRows(), form.getPage()));
            ResultPage<GardenProjectFinancialAudit> result = gardenProjectAuditFacade.getProjectFinancialAudit(query);
            if(result.isSuccess()){
                model.put("rows", result.getList());
                model.put("total", result.getPage().getTotalResults());
                putSuccess(model, "");
            } else {
                putError(model, result.getErrorCode(), result.getErrorMsg());
            }
        } catch (Exception ex) {
            logger.error("财务审核信息查询一览出错：", ex);
            putError(model, ex.getMessage());
        }
        responseAsJson(model, resp);
    }
    
    /**
     * 财务审核修改
     * 
     * @param resp
     * @param form
     * @param req
     */
    @RequestMapping(value = "/updateChiefAuditById.htm", method = RequestMethod.POST)
    public void updateChiefAuditById(HttpServletResponse resp,AuditForm form,HttpServletRequest req){
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            GardenProjectAuditQuery financial = new GardenProjectAuditQuery();
            //新增管理员事物
            AdminBusiness adminBusiness=new AdminBusiness(); 
            financial.setId(form.getId());
            financial.setCompanyId(SpringSecurityUtil.getIntCompany());
            financial.setProjectId(form.getProjectId());           
            financial.setStatus(form.getStatus());
            financial.setType(form.getType());
            financial.setStep(form.getStep());
            financial.setSubStep(form.getSubStep());
            financial.setUpdateUserId(SpringSecurityUtil.getIntPrincipal());
            financial.setUpdateTime(new Date());
            financial.setMemo(form.getMemo()); 
            if(Util.isNotNull(form.getStep())){
                adminBusiness.setBusinessType(form.getStep());
                //adminBusiness.setComment(AdminBusinessTypeEnum.getByValue(form.getStep()).getMessage());
            }else if(Util.isNotNull(form.getSubStep())){
                adminBusiness.setBusinessType(form.getSubStep()); 
                //adminBusiness.setComment(AdminBusinessTypeEnum.getByValue(form.getSubStep()).getMessage());
            }
            if(28==form.getType()){
                adminBusiness.setBusinessType(AdminBusinessTypeEnum.B_812.getValue()); 
            }else if(29==form.getType()){
                adminBusiness.setBusinessType(AdminBusinessTypeEnum.B_814.getValue()); 
            }else if(86==form.getType()){
                adminBusiness.setBusinessType(AdminBusinessTypeEnum.B_824.getValue()); 
            }
            if(ProjectTableStatusEnum.VERIFIED.getValue().equals(form.getStatus())){
                adminBusiness.setComment(ProjectTableStatusEnum.VERIFIED.getMessage());
            }else if(ProjectTableStatusEnum.VERIFIED_FAIL.getValue().equals(form.getStatus())){
                if(Util.isNotNull(form.getMemo())){
                    adminBusiness.setComment(ProjectTableStatusEnum.VERIFIED_FAIL.getMessage()+":"+form.getMemo());  
                }else{
                    adminBusiness.setComment(ProjectTableStatusEnum.VERIFIED_FAIL.getMessage());                    
                }                
            }
            adminBusiness.setProjectId(form.getProjectId());
            ResultEntry<AdminBusiness> val=AdminTransactionUtil.insertAdminTransaction(adminBusiness, req);
            ResultEntry<Integer> result = gardenProjectAuditFacade.updateFinancialAudit(financial);
            if(result.isSuccess()){
                putSuccess(model, "");
                adminBusiness.setResult(HandleResultEnum.SUCCESS.getValue());
                // 发送消息
                MessageHandlerUtil.insertMessage(financial);
            } else {
                putError(model, result.getErrorCode(), result.getErrorMsg());
                adminBusiness.setResult(HandleResultEnum.FAIL.getValue());
                adminBusiness.setComment(result.getErrorMsg());
            } 
            //更新管理员事物
            adminBusiness.setId(val.getT().getId());
            AdminTransactionUtil.updateAdminTransaction(adminBusiness);
        } catch (Exception ex) {
            logger.error("财务审核修改出错：", ex);
            putError(model, ex.getMessage());
        }
        responseAsJson(model, resp);
    }
}
