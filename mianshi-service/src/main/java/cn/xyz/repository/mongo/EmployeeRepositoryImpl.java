package cn.xyz.repository.mongo;

import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.EmployeeRepository;
import cn.xyz.repository.MongoRepository;
/**
 * 
 * 组织架构功能员工数据操纵接口的实现
 * @author hsg
 *
 */
@Service
public class EmployeeRepositoryImpl extends MongoRepository implements EmployeeRepository{
	
	//添加员工（单个）
	@Override
	public ObjectId addEmployee(Employee employee) {
		
		employee.setId(new ObjectId());
		dsForRW.save(employee);
		return employee.getId();
	}
	
	
	//添加员工（多个）
	@Override
	public List<Employee> addEmployees(List<Integer> userId, ObjectId companyId, ObjectId departmentId, int role) {
		
		
		for(Iterator<Integer> iter = userId.iterator(); iter.hasNext();){
			Integer uId = iter.next();
			Employee emp = new Employee();
			emp.setDepartmentId(departmentId);
			emp.setCompanyId(companyId);
			emp.setRole(role);
			emp.setUserId(uId);
			dsForRW.save(emp);//存入员工数据
		}
	    //将整个部门的员工数据封装返回
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("departmentId").equal(departmentId);
		
		return query.asList();
	}
	
	
	//修改员工信息
	@Override
	public Employee modifyEmployees(Employee employee) {
		int userId = employee.getUserId();
		ObjectId companyId = employee.getCompanyId();
		
		if(userId == 0 || companyId == null){
			return null;
		}
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("userId").equal(userId).field("companyId").equal(companyId);
		UpdateOperations<Employee> ops = dsForRW.createUpdateOperations(Employee.class);
		if(null != employee.getDepartmentId())
			ops.set("departmentId", employee.getDepartmentId());
		if(0 <= employee.getRole() && employee.getRole() <= 3)
			ops.set("role", employee.getRole());
		if(null != employee.getPosition() && employee.getPosition() != "")
			ops.set("position", employee.getPosition());
		//当前回话人数
		if (0<= employee.getChatNum() && employee.getChatNum() <= 5) {
			ops.set("chatNum", employee.getChatNum());
		}
		//会话状态（是否暂停，开启）
		if(!StringUtil.isEmpty(String.valueOf(employee.getIsPause()))){
			ops.set("isPause",employee.getIsPause());
		}
		Employee emp = dsForRW.findAndModify(query, ops);
		return emp;
	}
	
	
	//通过userId查找员工
	@Override
	public List<Employee> findByUserId(int userId){
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("userId").equal(userId);
		if(query == null){
			return null;
		}
		List<Employee> employees = query.asList();
		return employees;
		
	}


	
	//查找公司中某个角色的所有员工
	@Override
	public List<Employee> findByRole(ObjectId companyId, int role) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("companyId").equal(companyId).filter("role", role);
		if(query == null){
			return null;
		}
		return query.asList();
	}


	
	//删除整个部门的员工
	@Override
	public void delEmpByDeptId(ObjectId departmentId) {
		//根据部门id找到员工
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("departmentId").equal(departmentId);
		//删除记录
		if(query != null)
	    dsForRW.delete(query);
	}
	

	//删除员工
	@Override
	public void deleteEmployee(List<Integer> userIds, ObjectId departmentId) {
		for(Iterator<Integer> iter = userIds.iterator(); iter.hasNext();){
			Integer userId = iter.next();
			Query<Employee> query = dsForRW.createQuery(Employee.class).field("userId").equal(userId).field("departmentId").equal(departmentId);
			if(query != null){
				dsForRW.delete(query);
			}
		}
		
		
	}
	

	//根据公司ID查询员工(员工列表)
	@Override
	public List<Employee> compEmployeeList(ObjectId companyId) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("companyId").equal(companyId);
		if(query == null)
			return null;
		List<Employee> employees = query.asList(); 
		return employees;
	}


	//根据部门ID查询员工(部门员工列表)
	@Override
	public List<Employee> departEmployeeList(ObjectId departmentId) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("departmentId").equal(departmentId);
		if(query == null)
			return null;
		List<Employee> emps= query.asList();
		return emps;
	}

	//根据id查找员工
	@Override
	public Employee findById(ObjectId employeeId) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("_id").equal(employeeId);
		return query.get();
	}

	
	//查找某个员工的角色
	@Override
	public int findRole(ObjectId companyId, int userId) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("companyId").equal(companyId).field("userId").equal(userId);
		return query.get().getRole();
	}


	//删除员工(根据公司id）
	@Override
	public void delEmpByCompId(ObjectId companyId, int userId) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("companyId").equal(companyId).field("userId").equal(userId);
		if(query != null)
			dsForRW.delete(query);
	}

	/**
	 * 根据用户id来修改员工信息
	 */
	@Override
	public Employee modifyEmployeesByuserId(int userId) {
		Employee employeeInfo = new Employee();
		if (!StringUtil.isEmpty(String.valueOf(userId))) {
			// 根据用户id来查询员工信息
			Query<Employee> query = dsForRW.createQuery(Employee.class).field("userId").equal(userId);
			// 修改
			UpdateOperations<Employee> uo = dsForRW.createUpdateOperations(Employee.class);
			// 赋值
			List<Employee> employeeList = query.asList();
			for (Employee employee2 : employeeList) {
				if (null != employee2 && !"".equals(employee2)) {
					// 会话人数
					if (!StringUtil.isEmpty(String.valueOf(employee2.getChatNum()))) {
						uo.set("chatNum", 0);
					}
					// 会话状态
					if (!StringUtil.isEmpty(String.valueOf(employee2.getIsPause()))) {
						uo.set("isPause", 0);
					}
				}
				// employeeInfo = dsForRW.findAndModify(query,uo);
				dsForRW.update(employee2, uo);
			}
		}
		return employeeInfo;
	}

	/**
	 * 查询员工是否为客服
	 */
	@Override
	public Employee findEmployee(Employee employee,User.UserSettings userSettings) {
			Query<Employee> query = dsForRW.createQuery(Employee.class).field("userId").equal(employee.getUserId())
					.field("departmentId").equal(employee.getDepartmentId()).field("companyId").equal(employee.getCompanyId());
			Employee employeeInfo = query.get();
		return employeeInfo;
	}

	
	/**
	 *根据公司，部门，用户id来查询出员工信息 
	 */
	@Override
	public Employee findEmployee(Employee employee) {
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("userId").equal(employee.getUserId())
				.field("departmentId").equal(employee.getDepartmentId()).field("companyId").equal(employee.getCompanyId());
		Employee employeeInfo = query.get();
	return employeeInfo;
	}
	
	
	
	
}
