package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainSetting;
import com.qinfei.qferp.entity.study.TrainTeacher;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.TrainSettingMapper;
import com.qinfei.qferp.service.study.ITrainSettingService;
import com.qinfei.qferp.service.study.ITrainTeacherService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nfunk.jep.JEP;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @CalssName: TrainSettingService
 * @Description: 培训设置接口
 * @Author: Xuxiong
 * @Date: 2020/3/31 0031 14:02
 * @Version: 1.0
 */
@Service
public class TrainSettingService implements ITrainSettingService {
    @Autowired
    private TrainSettingMapper trainSettingMapper;
//    private ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");//使用JEP代替
    @Autowired
    private ITrainTeacherService trainTeacherService;

    @Override
    public List<TrainSetting> listTrainSetting(Map<String, Object> param) {
        List<TrainSetting> result = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            if(param.get("settingModuleList") != null){
                param.put("settingModuleList", Arrays.asList(String.valueOf(param.get("settingModuleList")).split(",")));
            }
            result = trainSettingMapper.listTrainSettingByParam(param);
        }
        return result;
    }

    @Transactional
    @Override
    public void save(TrainSetting trainSetting) {
        try{
            User user = AppUtil.getUser();
            validTrainSetting(trainSetting, user);//数据校验
            //获取当前模块最后的排序，进行排序计算
            TrainSetting temp = trainSettingMapper.getMaxSeqTrainSetting(trainSetting.getSettingModule());
            //TEACHER_INTEGRAL_RULE-积分规则、UP_RULE-升级规则，数学公式直接放在settingValue属性中，否则都放在settingValueList
            if("TEACHER_INTEGRAL_RULE".equals(trainSetting.getSettingModule()) || "STUDENT_INTEGRAL_RULE".equals(trainSetting.getSettingModule()) || "UP_RULE".equals(trainSetting.getSettingModule()) || "PAPER_GRADE".equals(trainSetting.getSettingModule())){
                //判断是否存在规则设置，如果存在则删除
                if(temp != null){
                    trainSettingMapper.updateStateByModule((byte) -9, user.getId(), trainSetting.getSettingModule());
                    temp.setSettingLevel(-1);//由于后面需要用到这个字段计算层级会加1，所以设置-1
                }
                trainSetting.setSettingLevel(CollectionUtils.isNotEmpty(trainSetting.getSettingValueList()) ? trainSetting.getSettingValueList().size() : 0);

                trainSettingMapper.save(trainSetting);
            }
            List<TrainSetting> trainSettingList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(trainSetting.getSettingValueList())){
                int seq = temp != null ? (temp.getSettingLevel() != null ? (temp.getSettingLevel() + 1) : 0) : 0;
                trainSetting.getSettingValueList().forEach(val -> {
                    TrainSetting setting = new TrainSetting();
                    setting.setParentId(trainSetting.getId());//如果是规则，则有该值
                    setting.setSettingValue(val);
                    setting.setSettingLevel(trainSettingList.size() + seq);//层级
                    setting.setSettingModule(trainSetting.getSettingModule());
                    setting.setCreateId(trainSetting.getCreateId());
                    setting.setUpdateId(trainSetting.getUpdateId());
                    setting.setCompanyCode(trainSetting.getCompanyCode());
                    trainSettingList.add(setting);
                });

                //如果是讲师等级，则需要计算对应积分
                updateRuleValue(trainSetting.getSettingModule(), trainSettingList);

                trainSettingMapper.saveBatch(trainSettingList);
            }

            //如果是评级规则，则需要更新等级对应的积分数
            if("UP_RULE".equals(trainSetting.getSettingModule())){
                //公式列表，添加当前公式
                trainSettingList.add(trainSetting);
                //查询讲师等级选项
                Map<String, Object> param = new HashMap<>();
                param.put("settingModule", "TEACHER_LEVEL");
                param.put("orderFlag", true);
                List<TrainSetting> teacherLevelList = trainSettingMapper.listTrainSettingByParam(param);
                if(CollectionUtils.isNotEmpty(teacherLevelList)){
                    //更新等级积分
                    updateRuleValue(trainSettingList, teacherLevelList);
                    trainSettingMapper.batchUpdateTrainSetting(user.getId(), teacherLevelList);
                }
            }



        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "配置培训设置异常！");
        }
    }

    @Transactional
    @Override
    public void updateStateById(byte state, int id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //如果是删除操作，则需要将后面的元素依次减一
            if(-9 == state){
                TrainSetting trainSetting = trainSettingMapper.get(TrainSetting.class, id);
                if(trainSetting != null){
                    trainSettingMapper.updateStateById(state, user.getId(), id);
                    Map<String, Object> param = new HashMap<>();
                    param.put("settingModule", trainSetting.getSettingModule());
                    param.put("orderFlag", true);
                    param.put("moreThanSettingLevel", trainSetting.getSettingLevel()+1);
                    List<TrainSetting> trainSettingList = trainSettingMapper.listTrainSettingByParam(param);
                    //如果有两条记录，则说明可移动操作，否则不操作
                    if(CollectionUtils.isNotEmpty(trainSettingList) && trainSettingList.size() > 0){
                        for(TrainSetting currentTrain : trainSettingList){
                            currentTrain.setSettingLevel(currentTrain.getSettingLevel() - 1); //层级往上移
                        }
                        //如果移除的是讲师等级
                        updateRuleValue(trainSetting.getSettingModule(), trainSettingList);

                        trainSettingMapper.batchUpdateTrainSetting(user.getId(), trainSettingList);
                    }
                }
            }else {
                trainSettingMapper.updateStateById(state, user.getId(), id);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改培训设置状态异常！");
        }
    }

    @Transactional
    @Override
    public void update(TrainSetting trainSetting) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            trainSettingMapper.updateById(trainSetting);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改培训设置异常！");
        }
    }

    @Transactional
    @Override
    public void top(TrainSetting trainSetting) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //获取所有选项列表，按照顺序排序
            Map<String, Object> param = new HashMap<>();
            param.put("settingModule", trainSetting.getSettingModule());
            param.put("lessThanSettingLevel", trainSetting.getSettingLevel());
            param.put("orderFlag", true);
            List<TrainSetting> trainSettingList = trainSettingMapper.listTrainSettingByParam(param);
            //如果当前配置已经是置顶元素，则不作操作，否则操作
            if(CollectionUtils.isNotEmpty(trainSettingList) && trainSettingList.get(0).getId() != trainSetting.getId()){
                for(TrainSetting setting : trainSettingList){
                    //如果是当前元素，则设置层级为顶级
                    if(setting.getId() == trainSetting.getId()){
                        setting.setSettingLevel(0);
                    }else {
                        setting.setSettingLevel(setting.getSettingLevel()+1);//其他选项往后推
                    }
                }
                //如果移除的是讲师等级
                updateRuleValue(trainSetting.getSettingModule(), trainSettingList);
                trainSettingMapper.batchUpdateTrainSetting(user.getId(), trainSettingList);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "置顶异常！");
        }
    }

    @Transactional
    @Override
    public void up(TrainSetting trainSetting) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //获取当前记录 和  上一条记录
            Map<String, Object> param = new HashMap<>();
            param.put("lessThanSettingLevel", trainSetting.getSettingLevel());
            param.put("moreThanSettingLevel", trainSetting.getSettingLevel() - 1);
            param.put("settingModule", trainSetting.getSettingModule());
            param.put("orderFlag", true);
            List<TrainSetting> trainSettingList = trainSettingMapper.listTrainSettingByParam(param);
            //如果有两条记录，则说明可移动操作，否则不操作
            if(CollectionUtils.isNotEmpty(trainSettingList) && trainSettingList.size() >= 2){
                //和前一位对调位置
                int currentLevel = trainSettingList.get(trainSettingList.size() - 1).getSettingLevel();
                int beforeLevel = trainSettingList.get(trainSettingList.size() - 2).getSettingLevel();
                trainSettingList.get(trainSettingList.size() - 1).setSettingLevel(beforeLevel);
                trainSettingList.get(trainSettingList.size() - 2).setSettingLevel(currentLevel);

                //如果移除的是讲师等级
                updateRuleValue(trainSetting.getSettingModule(), trainSettingList);

                trainSettingMapper.batchUpdateTrainSetting(user.getId(), trainSettingList);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "上移异常！");
        }
    }

    @Transactional
    @Override
    public void down(TrainSetting trainSetting) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //获取当前记录 和  下一条记录
            Map<String, Object> param = new HashMap<>();
            param.put("lessThanSettingLevel", trainSetting.getSettingLevel() + 1);
            param.put("settingModule", trainSetting.getSettingModule());
            param.put("orderFlag", true);
            param.put("moreThanSettingLevel", trainSetting.getSettingLevel());
            List<TrainSetting> trainSettingList = trainSettingMapper.listTrainSettingByParam(param);
            //如果有两条记录，则说明可移动操作，否则不操作
            if(CollectionUtils.isNotEmpty(trainSettingList) && trainSettingList.size() >= 2){
                //和前一位对调位置
                int currentLevel = trainSettingList.get(0).getSettingLevel();
                int afterLevel = trainSettingList.get(1).getSettingLevel();
                trainSettingList.get(0).setSettingLevel(afterLevel);
                trainSettingList.get(1).setSettingLevel(currentLevel);

                //如果移除的是讲师等级
                updateRuleValue(trainSetting.getSettingModule(), trainSettingList);

                trainSettingMapper.batchUpdateTrainSetting(user.getId(), trainSettingList);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "下移异常！");
        }
    }

    @Transactional
    @Override
    public void bottom(TrainSetting trainSetting) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //获取所有选项列表，按照顺序排序
            Map<String, Object> param = new HashMap<>();
            param.put("settingModule", trainSetting.getSettingModule());
            param.put("orderFlag", true);
            param.put("moreThanSettingLevel", trainSetting.getSettingLevel());
            List<TrainSetting> trainSettingList = trainSettingMapper.listTrainSettingByParam(param);
            //如果当前配置已经是底部元素，则不作操作，否则操作
            if(CollectionUtils.isNotEmpty(trainSettingList) && trainSettingList.get(trainSettingList.size()-1).getId() != trainSetting.getId()){
                for(TrainSetting setting : trainSettingList){
                    //如果是当前元素，则设置层级为底级
                    if(setting.getId() == trainSetting.getId()){
                        setting.setSettingLevel(trainSettingList.size()-1);
                    }else {
                        setting.setSettingLevel(setting.getSettingLevel() - 1);//其他选项往前推
                    }
                }

                //如果移除的是讲师等级
                updateRuleValue(trainSetting.getSettingModule(), trainSettingList);

                trainSettingMapper.batchUpdateTrainSetting(user.getId(), trainSettingList);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "置底异常！");
        }
    }

    @Override
    public float calFormula(List<TrainSetting> trainSettingList, Map<String, String> param) {
        float result = 0;
        try{
            if(CollectionUtils.isNotEmpty(trainSettingList)){
                //拷贝原来公式列表，防止除数为0情况，直接被修改原公式变量
                List<TrainSetting> targetTrainList = new ArrayList<>();
                trainSettingList.forEach(trainSetting -> {
                    TrainSetting train = new TrainSetting();
                    BeanUtils.copyProperties(trainSetting, train);
                    targetTrainList.add(train);
                });

                for(int i = 0; i < targetTrainList.size(); i++){
                    TrainSetting trainSetting = targetTrainList.get(i);
                    //1、判断列表是否只有一条记录，如果只有一条记录则说明没有变量除数，直接进行计算，否则得先进行除数计算并判断值是否为0
                    if(i == targetTrainList.size() - 1){
                        result = calFormulaValue(trainSetting.getSettingValue(), param);
                    }else {
                        float tmp = calFormulaValue(trainSetting.getSettingValue(), param);
                        //如果除数为,则把后面带有该除数的表达式该除数替换成 *0，进行0处理
                        if(tmp == 0){
                            for(int j = i+1; j < targetTrainList.size(); j++){
                                targetTrainList.get(j).setSettingValue(replaceAllChar(targetTrainList.get(j).getSettingValue(),"/"+trainSetting.getSettingValue(),"*0"));
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public TrainSetting getMaxSeqTrainSetting(String settingModule) {
        TrainSetting result = null;
        User user = AppUtil.getUser();
        if(user != null){
            result = trainSettingMapper.getMaxSeqTrainSetting(settingModule);
        }
        return result;
    }

    @Override
    public Map<String, Object> getTrainPermission(HttpServletRequest request) {
        Map<String, Object> permissionMap = new HashMap<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                //判断是否有讲师权限
                TrainTeacher trainTeacher = trainTeacherService.getTrainTeacherByUserId(user.getId());
                if(trainTeacher != null){
                    permissionMap.put("teacher", true);//有讲师权限
                }
                HttpSession session = request.getSession();
                //获取当前用户权限资源，判断是否有培训设置权限，有的话则代表有管理员权限
                List<Resource> resources = (List<Resource>) session.getAttribute(IConst.USER_RESOURCE);
                if(CollectionUtils.isNotEmpty(resources)){
                    for(Resource resource : resources){
                        if(StringUtils.isNotEmpty(resource.getUrl()) && resource.getUrl().contains("study/trainSetting")){
                            permissionMap.put("admin", true);//有管理员权限
                        }
                        if (StringUtils.isNotEmpty(resource.getUrl()) && resource.getUrl().contains("study/courseSignList")) {
                            permissionMap.put("viewAllCourse", true);//有查看报名名单权限
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return permissionMap;
    }

    //校验数据
    private void validTrainSetting(TrainSetting trainSetting, User user){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(StringUtils.isEmpty(trainSetting.getSettingModule())){
            throw new QinFeiException(1002, "配置模块不能为空！");
        }
        //TEACHER_INTEGRAL_RULE-积分规则、UP_RULE-升级规则，数学公式直接放在settingValue属性中，否则都放在settingValueList
        if("TEACHER_INTEGRAL_RULE".equals(trainSetting.getSettingModule()) || "STUDENT_INTEGRAL_RULE".equals(trainSetting.getSettingModule()) || "UP_RULE".equals(trainSetting.getSettingModule()) || "PAPER_GRADE".equals(trainSetting.getSettingModule())){
            if(StringUtils.isEmpty(trainSetting.getSettingValue())){
                throw new QinFeiException(1002, "配置数学公式不能为空！");
            }
        }else {
            if(CollectionUtils.isEmpty(trainSetting.getSettingValueList())){
                throw new QinFeiException(1002, "配置值不能为空！");
            }
        }
        trainSetting.setCompanyCode(user.getCompanyCode());
        trainSetting.setCreateId(user.getId());
        trainSetting.setUpdateId(user.getId());
    }

    //除数替换，使用replaceAll替换除数遇到特殊字符()不会进行替换
    private String replaceAllChar(String source, String regex, String replace){
        int index = source.indexOf(regex);
        if(index != -1){
            int regexLen = regex.length();
            source = source.substring(0,index) + replace + source.substring(index+regexLen);
            source = replaceAllChar(source, regex, replace);//递归
        }
        return source;
    }

    //替换字符串，计算公式值
    public float calFormulaValue(String formula, Map<String, String> param){
        float result = 0;
        try{
            JEP jep = new JEP();
            for(String variable : param.keySet()){
//                formula = formula.replaceAll(variable, param.get(variable));//使用JEP代替
                jep.addVariable(variable, Double.parseDouble(param.get(variable)));
            }
//            result = Float.parseFloat(String.valueOf(scriptEngine.eval("eval("+formula+").toFixed(2)")));//使用JEP代替
            jep.parseExpression(formula);
            result = (float) jep.getValue();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    //更新等级积分
    private void updateRuleValue(List<TrainSetting> trainSettingList, List<TrainSetting> teacherLevelList){
        for(TrainSetting setting : teacherLevelList){
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("x", (setting.getSettingLevel() != null ? setting.getSettingLevel().toString() : "0"));
            setting.setRuleValue(calFormula(trainSettingList, variableMap));//设置等级积分
        }
    }

    //如果操作的是讲师等级，更新积分
    private void updateRuleValue(String settingModule, List<TrainSetting> trainSettingList){
        if("TEACHER_LEVEL".equals(settingModule)){
            //获取公式列表
            Map<String, Object> param = new HashMap<>();
            param.put("settingModule", "UP_RULE");
            param.put("orderFlag", true);
            List<TrainSetting> formulaList = trainSettingMapper.listTrainSettingByParam(param);
            if(CollectionUtils.isNotEmpty(formulaList)){
                //更新等级积分
                updateRuleValue(formulaList, trainSettingList);
            }
        }
    }


}
