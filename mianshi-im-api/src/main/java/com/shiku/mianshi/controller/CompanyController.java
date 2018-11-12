package com.shiku.mianshi.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.CompanyManager;
import cn.xyz.mianshi.vo.CompanyVO;
import cn.xyz.mianshi.vo.DepartmentVO;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;

/**
 * 用于组织架构功能的相关接口
 * @author hsg
 *
 */
@RestController
@RequestMapping("/org")
public class CompanyController extends AbstractController {
	
	@Resource
	private CompanyManager companyManager;
	
	//创建公司
	@RequestMapping(value = "/company/create")
	public JSONMessage createCompany(@RequestParam String companyName, @RequestParam int createUserId){
		
		try {
			if(companyName != null && !"".equals(companyName) && createUserId > 0){
				 CompanyVO company = companyManager.createCompany(companyName, createUserId);
				 company.setDepartments(companyManager.departmentList(company.getId()) ); //将部门及员工数据封装进公司
				 Object data = company;
				return JSONMessage.success(null, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
			
		}
		return JSONMessage.failure("创建失败");
	
	}

	
	//根据userId查找是否存在其所属的公司
	@RequestMapping("/company/getByUserId")
	public JSONMessage getCompanyByUserId(@RequestParam int userId){
		List<CompanyVO> companys = companyManager.findCompanyByUserId(userId);
		if (companys == null || companys.isEmpty()){  //判断是否存在公司
			//companys=new ArrayList<CompanyVO>();
			//companys.add(companyManager.autoJoinCompany(userId));
			return JSONMessage.success();
		}
		for(Iterator<CompanyVO> iter = companys.iterator(); iter.hasNext(); ){   //遍历公司
			CompanyVO company = iter.next();
			company.setDepartments(companyManager.departmentList(company.getId()) );  //将部门及员工数据封装进公司
		}
		Object data = companys;
		return JSONMessage.success(null, data);
		
	}
	
	//指定管理员
	@RequestMapping("/company/setManager")
	public JSONMessage setCompanyManager(@RequestParam String companyId, @RequestParam String managerId){
		
		//以字符串的形式接收managerId，然后解析转换为int 
		List<Integer> userIds= new ArrayList<Integer>();
		char first = managerId.charAt(0); 
		char last = managerId.charAt(managerId.length() - 1); 
		if(first=='[' && last==']'){ 
			userIds = JSON.parseArray(managerId, Integer.class);
		}
		ObjectId compId = new ObjectId(companyId);
		companyManager.setManager(compId, userIds);
		return JSONMessage.success();	
	}
	
	//管理员列表
	@RequestMapping("/company/managerList")
	public JSONMessage ManagerList(@RequestParam String companyId){
		ObjectId compId = new ObjectId(companyId);
		Object data = companyManager.managerList(compId);
		return JSONMessage.success(null, data);
	}
	
	//修改公司名称、公告
	@RequestMapping("/company/modify")
	public JSONMessage modifyCompany(@RequestParam String companyId, String companyName,@RequestParam(defaultValue = "") String noticeContent){
		ObjectId compId = new ObjectId(companyId);
		CompanyVO company = new CompanyVO();
		company.setId(compId);
		if(companyName != null){
			company.setCompanyName(companyName);
		}
		if(noticeContent != null &&  !"".equals(noticeContent)){ //判断是否存在公告
			company.setNoticeContent(noticeContent);
			company.setNoticeTime(DateUtil.currentTimeSeconds());
		}
		Object data = companyManager.modifyCompanyInfo(company);
		return JSONMessage.success(null,data);
		
	}
	
	
	//查找公司:（通过公司名称的关键字查找）
//	@RequestMapping("/company/search")
//	public JSONMessage changeNotice(@RequestParam String keyworld){
//		Object data = companyManager.findCompanyByKeyworld(keyworld);
//		return JSONMessage.success(null,data);
//	}
	
	
	//删除公司(即：记录删除者id,将公司信息隐藏)
	@RequestMapping("/company/delete")
	public JSONMessage deleteCompany(@RequestParam String companyId, int userId){
		ObjectId compId = new ObjectId(companyId);
		companyManager.deleteCompany(compId, userId);
		return JSONMessage.success();
	}
	

	//创建部门
	@RequestMapping("/department/create")
	public JSONMessage createDepartment(@RequestParam String companyId, @RequestParam String parentId, @RequestParam String departName,@RequestParam int createUserId){
		ObjectId compId = new ObjectId(companyId);
		ObjectId parentID = new ObjectId();
		if(parentId.trim() != null){
			parentID = new ObjectId(parentId);
		}
		Object data = companyManager.createDepartment(compId, parentID, departName, createUserId);
		return JSONMessage.success(null,data);
	}
	
	//修改部门名称
	@RequestMapping("/department/modify")
	public JSONMessage modifyDepartment (@RequestParam String departmentId,@RequestParam  String dpartmentName){
		ObjectId departId = new ObjectId(departmentId);
		DepartmentVO department = new DepartmentVO();
		department.setId(departId);
		department.setDepartName(dpartmentName);
		Object data = companyManager.modifyDepartmentInfo(department);
		return JSONMessage.success(null,data);
	}
	
	
	//删除部门
	@RequestMapping("/department/delete")
	public JSONMessage modifyDepartment (@RequestParam String departmentId){
		ObjectId departId = new ObjectId(departmentId);
		companyManager.deleteDepartment(departId);
		return JSONMessage.success();
	}
	
	//添加员工
	@RequestMapping("/employee/add")
	public JSONMessage addEmployee (@RequestParam String userId, @RequestParam String companyId,
			              @RequestParam String departmentId, @RequestParam(defaultValue = "0") int role){
		//以字符串的形式接收userId，然后解析转换为int
		List<Integer> userIds= new ArrayList<Integer>();
		char first = userId.charAt(0); 
		char last = userId.charAt(userId.length() - 1); 
		if(first=='[' && last==']'){  //用于解析web端
			userIds = JSON.parseArray(userId, Integer.class);
		}else{ //用于解析Android和IOS端
			String[] strs = userId.split(",");
			for(String str : strs){
				if(str != null && !"".equals(str)){
					userIds.add(Integer.parseInt(str));
				}
			}
		}
		ObjectId compId = new ObjectId(companyId);
		ObjectId departId = new ObjectId(departmentId);
		Object data = companyManager.addEmployee(compId, departId, userIds, role);
		return JSONMessage.success(null,data);	
	}
	
	
	//删除员工
	@RequestMapping("/employee/delete")
	public JSONMessage addEmployee (@RequestParam String userIds, @RequestParam String departmentId){
		
		//以字符串的形式接收userId，然后解析转换为int 
		List<Integer> uIds= new ArrayList<Integer>();
		char first = userIds.charAt(0); 
		char last = userIds.charAt(userIds.length() - 1); 
		if(first=='[' && last==']'){ //用于解析web端
			uIds = JSON.parseArray(userIds, Integer.class);
		}else{ //用于解析Android和IOS端
			uIds.add(Integer.parseInt(userIds));
		}
		ObjectId departId = new ObjectId(departmentId);
		companyManager.deleteEmployee(uIds, departId);
		return JSONMessage.success();
	}
	
	//更改员工部门
	@RequestMapping("/employee/modifyDpart")
	public JSONMessage addEmployee (@RequestParam int userId, @RequestParam String companyId, @RequestParam String newDepartmentId){
		ObjectId compId = new ObjectId(companyId);
		ObjectId departId = new ObjectId(newDepartmentId);
		Employee employee = new Employee();
		employee.setCompanyId(compId);
		employee.setUserId(userId);
		employee.setDepartmentId(departId);
		Object data = companyManager.modifyEmpInfo(employee);  //更改该员工的信息
		
		return JSONMessage.success(null,data);
	}
	
	/**
	* @Title: updateEmployee
	* @Description: 更改员工信息
	* @param @param employee
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/employee/updateEmployee")
	public JSONMessage updateEmployee(@Valid Employee employee){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			Employee employeeInfo =companyManager.changeEmployeeInfo(employee);
			if (!StringUtil.isEmpty(employeeInfo.toString())) {
				return JSONMessage.success("", employeeInfo);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	//部门员工列表
	@RequestMapping("/departmemt/empList")
	public JSONMessage departEmpList (@RequestParam String departmentId){
		ObjectId departId = new ObjectId(departmentId);
		Object data = companyManager.departEmployeeList(departId);
		return JSONMessage.success(null,data);
	}
	//公司员工列表
	@RequestMapping("/company/employees")
	public JSONMessage companyEmpList(@RequestParam String companyId){
		ObjectId compId = new ObjectId(companyId);
		Object data = companyManager.employeeList(compId);
		return JSONMessage.success(null,data);
	}
	
	//更改员工角色
	@RequestMapping("/employee/modifyRole")
	public JSONMessage addEmployee (@RequestParam int userId, @RequestParam String companyId, @RequestParam int role){
		ObjectId compId = new ObjectId(companyId);
		Employee employee = new Employee();
		employee.setCompanyId(compId);
		employee.setUserId(userId);
		employee.setRole(role);
		Object data = companyManager.modifyEmpInfo(employee);
		
		return JSONMessage.success(null,data);
	}
	
	//更改员工职位(头衔)
	@RequestMapping("/employee/modifyPosition")
	public JSONMessage modifyPosition (@RequestParam int userId, @RequestParam String companyId, @RequestParam String position){
		ObjectId compId = new ObjectId(companyId);
		Employee employee = new Employee();
		employee.setCompanyId(compId);
		employee.setUserId(userId);
		employee.setPosition(position);
		Object data = companyManager.modifyEmpInfo(employee);
		return JSONMessage.success(null,data);
	}
	
	
	//公司列表
	@RequestMapping("/company/list")
	public JSONMessage companyList (@RequestParam(defaultValue = "0") int pageIndex,@RequestParam(defaultValue = "30") int pageSize){
		Object data = companyManager.companyList(pageSize, pageIndex);
		return JSONMessage.success(null, data);
	}
		
	//部门列表
	@RequestMapping("/department/list")
	public JSONMessage departmentList (@RequestParam String companyId){
		ObjectId compId = new ObjectId(companyId);
		Object data = companyManager.departmentList(compId);
		return JSONMessage.success(null,data);
	}

	//获取公司详情
	@RequestMapping("/company/get")
	public JSONMessage getCompany (@RequestParam String companyId){
		ObjectId compId = new ObjectId(companyId);
		Object data = companyManager.getCompany(compId);
		return JSONMessage.success(null,data);
	}
	
	//获取员工详情
	@RequestMapping("/employee/get")
	public JSONMessage getEmployee (@RequestParam String employeeId){
		ObjectId empId = new ObjectId(employeeId);
		Object data = companyManager.getEmployee(empId);
		return JSONMessage.success(null,data);
	}
	
	//获取部门详情
	@RequestMapping("/department/get")
	public JSONMessage getDpartment(@RequestParam String departmentId){
		ObjectId departId = new ObjectId(departmentId);
		Object data = companyManager.getDepartmentVO(departId);
		return JSONMessage.success(null,data);
	}
	
	//员工退出公司
	@RequestMapping("/company/quit")
	public JSONMessage quitCompany(@RequestParam String companyId, @RequestParam int userId){
		ObjectId compId = new ObjectId(companyId);
		companyManager.empQuitCompany(compId, userId);
		return JSONMessage.success();	
	}
	
	//获取公司中某个员工角色值
	@RequestMapping("/employee/role")
	public JSONMessage getEmployRole(@RequestParam String companyId, @RequestParam int userId){
		ObjectId compId = new ObjectId(companyId);
		Object data = companyManager.getEmpRole(compId, userId);
		return JSONMessage.success(null,data);	
	}
	
	/**
	* @Title: findEmployee
	* @Description: 判断是否为查询员工是否为客服
	* @param @param employee
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/employee/findEmployee")
	public JSONMessage findEmployee(@Valid Employee employee,User.UserSettings userSettings){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			if (!StringUtil.isEmpty(employee.toString())) {
				employee = companyManager.findEmployee(employee,userSettings);
				return JSONMessage.success("", employee);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
}
