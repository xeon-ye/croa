//package com.qinfei.qferp.flow.custom;
//
//import com.qinfei.qferp.entity.sys.Role;
//import com.qinfei.qferp.entity.sys.User;
//import com.qinfei.qferp.flow.custom.entity.Task;
//import com.qinfei.qferp.utils.AppUtil;
//import org.apache.commons.collections4.MapUtils;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Map;
//
//public class TaskHandler {
//
//    public void call(Task task) {
//        String clsName = task.getClsName();
//        try {
//            Class<?> cls = Class.forName(clsName);
//            Object object = cls.newInstance();
//            Method method = cls.getMethod(task.getMethodName(), Map.class);
//            method.invoke(object, new HashMap<>());
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void handle(Task task, Boolean flag, Map<String, Object> map) {
//        String desc = MapUtils.getString(map, "desc");
//        String type = MapUtils.getString(map, "type");
//        User user = (User) MapUtils.getObject(map, "user");
//        Role role = (Role) MapUtils.getObject(map, "role");
//        if (flag) {//通过
//            Task nextTask = task.getNextTask();
//            this.call(nextTask);
//        } else {//驳回
//            Task prevTask = task.getPrevTask();
//            User creator = task.getCreator();
//            User prevUser = prevTask.getUser();
//            String msg = String.format("由[%s]进行驳回，驳回原因:[%s]", AppUtil.getUser().getName(), desc);
//            prevTask.setDesc(msg);
//            this.call(prevTask);
//        }
//    }
//}
