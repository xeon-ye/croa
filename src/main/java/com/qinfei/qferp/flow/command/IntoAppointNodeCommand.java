package com.qinfei.qferp.flow.command;

import com.qinfei.core.utils.SpringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flowable流程开始到指定流程节点；
 * 
 * @Author ：Yuan；
 * @Date ：2018/12/13 0013 17:09；
 */
public class IntoAppointNodeCommand implements Command {
	// 流程实例ID
    private final String procInstanceId;
    // 指定将要跳转的节点
	private final String taskDefKey;
    // 已完成流程节点(必须是执行顺序排序的列表)
	private List<Map<String, String>> allActList;
	//已完成流程网关参数
    private Map<String, Object> gatewayMap;

	public IntoAppointNodeCommand(String procInstanceId, String taskDefKey, List<Map<String, String>> allActList, Map<String, Object> gatewayMap) {
		this.procInstanceId = procInstanceId;
		this.taskDefKey = taskDefKey;
		this.allActList = allActList;
		this.gatewayMap = gatewayMap;
	}

	@Override
	public Void execute(CommandContext commandContext) {
        RuntimeService runtimeService = SpringUtils.getBean(RuntimeService.class);

        if(CollectionUtils.isEmpty(allActList) || StringUtils.isEmpty(taskDefKey)){ //获取原流程节点为空，则不需要进行跳转
            return null;
        }

        String currentName = ""; //获取当前任务的key（流程启动的第一个userTask）
        boolean currentFlag = true; //是否有设置
        Map<String, Integer> nodeIndexMap = new HashMap<>(); //流程节点对应位置
        for(int i = 0; i < allActList.size(); i++){
            String taskKey = allActList.get(i).get("taskDefKey");
            String taskKeyType = allActList.get(i).get("type");
            nodeIndexMap.put(taskKey, i);
            if("userTask".equalsIgnoreCase(taskKeyType) && currentFlag){
                currentName = taskKey;
                currentFlag = false;
            }
        }

        if(StringUtils.isEmpty(currentName)){ //如果当前流程任务为空，则不进行跳转节点
            return null;
        }

        int startIndex = nodeIndexMap.get(currentName) + 1; //开始节点，第一个用户节点后一个节点
        int endIndex = nodeIndexMap.get(taskDefKey); //结束节点
        if(startIndex <= endIndex){
            Map executionMap = (Map) commandContext.getAttribute("ctx.attribute.involvedExecutions");
            if(executionMap != null && CollectionUtils.isNotEmpty(executionMap.values())){
                currentName = ((Execution)executionMap.values().toArray()[0]).getActivityId(); //获取当前正在执行的任务节点
            }
            for(int i = startIndex; i <= endIndex; i++){
                String taskKey = allActList.get(i).get("taskDefKey");
                String taskKeyType = allActList.get(i).get("type");
                if("gateway".equalsIgnoreCase(currentName)){  //如果当前流程正在网关，则网关下面的第一个用户任务，不需要跳转节点
                    if("userTask".equalsIgnoreCase(taskKeyType)){
                        currentName = taskKey;
                    }
                }else if(("userTask".equalsIgnoreCase(taskKeyType) || "exclusiveGateway".equalsIgnoreCase(taskKeyType)) && !currentName.equalsIgnoreCase(taskKey)){
                    runtimeService.createChangeActivityStateBuilder()
                            .processInstanceId(procInstanceId)
                            .processVariables(gatewayMap)
                            .moveActivityIdTo(currentName, taskKey)
                            .changeState();
                    if("userTask".equalsIgnoreCase(taskKeyType)){
                        currentName = taskKey; //设置当前节点为跳转的用户任务Key
                    }else{
                        currentName = "gateway"; //设置当前节点为网关
                    }
                }
            }
        }
		return null;
	}
}