package com.qinfei.qferp.service.impl.media;

import com.qinfei.qferp.mapper.media.MediaInfoMapper;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.service.biz.IArticleService;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.media.*;
import com.qinfei.qferp.service.workbench.IMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 媒体服务类
 *
 * @author GZW
 */
@Slf4j
@Service
public class MediaInfoService implements IMediaInfoService {
    @Autowired
    private MediaInfoMapper mediaInfoMapper;
    @Autowired
    private IMediaTermService mediaTermService;
    @Autowired
    private UserService userService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IArticleService articleService;
    // 消息推送接口；
    @Autowired
    private IMessageService messageService;
    @Autowired
    private IMediaTypeService mediaTypeService;
    @Autowired
    private IMediaFormService mediaFormService;
    @Autowired
    private MediaPlateMapper mediaPlateMapper;

    /**
     * 查询媒体列表
     *
     * @param mediaInfo 媒体查询条件
     * @param pageable  分页对象
     * @return
     */
   /* @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = CACHE_KEY, key = "#mediaInfo.id")
    public PageInfo<MediaInfo> list(MediaInfo mediaInfo, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        mediaInfo.setState(1);
        List<MediaInfo> list = mediaInfoMapper.listByOrder(mediaInfo, "id desc");
        Integer mType = mediaInfo.getmType();
        List<MediaTerm> mts = mediaTermService.list(mType);
        if (list != null && !list.isEmpty() && mts != null && !mts.isEmpty()) {
            Class<? extends MediaInfo> cls = mediaInfo.getClass();
            Map<Integer, User> userMap = userService.listAllUserMap();
            Map<Integer, Supplier> supplierMap = supplierService.listAllSupplier();
            for (MediaInfo m : list) {
                fillMedia(mts, cls, m, userMap, supplierMap);
            }
        }
        return new PageInfo(list);
    }*/

    /**
     * 根据页面传递的集合信息查询；
     *
     * @param map：查询条件集合；
     * @param pageable：分页对象；
     * @return ：分页完成的数据集合；
     */
   /* @Override
    @Transactional(readOnly = true)
    public PageInfo<MediaInfo> list(Map<String, Object> map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        map.put("state", 1);
        Object object = map.get("sidx");
        if (object == null || StringUtils.isEmpty(object)) {
            map.put("sidx", "id");
        }
        List<MediaInfo> list = mediaInfoMapper.listPage(map);
        Integer mType = map.get("mType") == null ? 1 : Integer.parseInt(map.get("mType").toString());
        MediaInfo mediaInfo = new MediaInfo();
        List<MediaTerm> mts = mediaTermService.list(mType);
        if (list != null && !list.isEmpty() && mts != null && !mts.isEmpty()) {
            Class<? extends MediaInfo> cls = mediaInfo.getClass();
            Map<Integer, User> userMap = userService.listAllUserMap();
            Map<Integer, Supplier> supplierMap = supplierService.listAllSupplier();
            for (MediaInfo m : list) {
                fillMedia(mts, cls, m, userMap, supplierMap);
            }
        }
        return new PageInfo(list);
    }*/

    /**
     * 填充Media
     *
     * @param mts
     * @param cls
     * @param m
     */
   /* private void fillMedia(List<MediaTerm> mts, Class<? extends MediaInfo> cls, MediaInfo m, Map<Integer, User> userMap, Map<Integer, Supplier> supplierMap) {
        Integer supplierId = m.getSupplierId();
        if (supplierId != null && supplierId != 0) {
            m.setSupplier(supplierMap.get(supplierId));
        }
        Cache cache = cacheManager.getCache(CACHE_KEY);
        m.setCreator(userMap.get(m.getCreatorId()));
        m.setUser(userMap.get(m.getUserId()));
        for (MediaTerm mt : mts) {
            String fieldName = mt.getField();
            if (!StringUtils.isEmpty(fieldName)) {
                String fieldName1 = StrUtil.camelCaseName(fieldName);
                try {
                    Field field = cls.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object val = field.get(m);
                    if (field != null && val != null) {
                        String sql = mt.getSql();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(fieldName, val);
                        if (!StringUtils.isEmpty(sql)) {
                            String sql1 = StrUtil.parse(sql, map);
                            List<Map<String, Object>> datas = (List<Map<String, Object>>) cache.get(sql1, new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    return mediaInfoMapper.dictSQL(sql1);
                                }
                            });
                            try {
                                if (datas != null && !datas.isEmpty()) {// 如果有值则放到对应的字段中
                                    Field dataField = cls.getDeclaredField(fieldName1 + "Data");
                                    dataField.setAccessible(true);
                                    dataField.set(m, datas.get(0));
                                }
                            } catch (Exception e) {
                                log.error("没有这个类型" + e.getMessage());
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
*/
   /* @Override
    @Transactional
    @CachePut(value = CACHE_KEY)
    public void save(MediaInfo mediaInfo) {
        mediaInfoMapper.insert(mediaInfo);
//        Map<String, Object> map = new HashMap<>();
//        map.put("taskUser", "1001");
//        map.put("url", "1001");
//        map.put("type", "1001");
//        map.put("title", "1001");
//        map.put("createDate", new Date());
//        map.put("creator", AppUtil.getUser().getName());
//        map.put("creatorId", AppUtil.getUser().getId());
    }*/

   /* @Override
    @Transactional
    @CacheEvict(value = CACHE_KEY, key = "'id='+#mediaId")
    public void selectToSave(Integer mediaId) {
        mediaInfoMapper.delById(MediaInfo.class, mediaId);
        mediaInfoMapper.selectToSave(mediaId);
        // return mediaInfoMapper.get(MediaInfo.class, mediaId);
    }*/

    /**
     * 批量复制媒体数据到下单媒体表；
     *
     * @param mediaIds：要复制的媒体ID；
     */
  /*  @Override
    @Transactional
    public void saveBatch(List<Integer> mediaIds) {
        mediaInfoMapper.saveBatch(mediaIds);
    }*/

    /**
     * 批量复制媒体数据到下单媒体表；
     *
     * @param mediaIds：要复制的媒体ID；
     */
 /*   @Override
    @Transactional
    public void deleteBatch(List<Integer> mediaIds) {
        mediaInfoMapper.deleteBatch(mediaIds);
    }*/

  /*  @Override
    @Transactional
    @CacheEvict(value = CACHE_KEY, key = "'id='+#mediaId")
    public void selectToUpdate(Integer mediaId) {
        mediaInfoMapper.selectToUpdate(mediaId);
    }*/

   /* @Override
    @Transactional
    @CachePut(value = CACHE_KEY, key = "'id='+#mediaInfo.id")
    public MediaInfo update(MediaInfo mediaInfo) {
        mediaInfoMapper.update(mediaInfo);
        return mediaInfo;
    }*/

//    @Override
//    @Transactional(readOnly = true)
//    @Cacheable(value = CACHE_KEY, key = "'id='+#mType+',mediaName'+#mediaName")
//    public boolean getByName(int mType, String mediaName) {
//        return mediaInfoMapper.getIdByName(mType, mediaName) > 0;
//    }

 /*   @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_KEY, key = "'id='+#id")
    public MediaInfo getById(Integer id) {
        MediaInfo mediaInfo = mediaInfoMapper.get(MediaInfo.class, id);
        if (mediaInfo == null)
            return null;
        mediaInfo.setSupplier(supplierService.getById(mediaInfo.getSupplierId()));
        List<MediaTerm> mts = mediaTermService.list(mediaInfo.getmType());
        Map<Integer, User> userMap = userService.listAllUserMap();
        Map<Integer, Supplier> supplierMap = supplierService.listAllSupplier();
        this.fillMedia(mts, MediaInfo.class, mediaInfo, userMap, supplierMap);
        return mediaInfo;
    }*/

    /**
     * 审核通过
     *
     * @param id
     * @return
     */
/*    @Override
    @CacheEvict(value = CACHE_KEY, key = "'id='+#id")
    @Transactional
    public boolean pass(Integer id) {
        try {
            mediaInfoMapper.modifyStateById(2, id);
            sendMessage(this.getById(id), true);
            return true;
        } catch (QinFeiException e) {
            e.printStackTrace();
            return false;
        }
    }*/

    /**
     * 审核驳回
     *
     * @param id
     * @return
     */
    /*@Override
    @CacheEvict(value = CACHE_KEY, key = "'id='+#id")
    @Transactional
    public boolean reject(Integer id) {
        try {
            mediaInfoMapper.modifyStateById(3, id);
            sendMessage(this.getById(id), false);
            return true;
        } catch (QinFeiException e) {
            e.printStackTrace();
            return false;
        }
    }*/

    /**
     * 审核删除
     *
     * @param id
     * @return
     */
  /*  @Override
    @CacheEvict(value = CACHE_KEY, key = "'id='+#id")
    @Transactional
    public boolean del(Integer id) {
        try {
            mediaInfoMapper.modifyStateById(-1, id);
            return true;
        } catch (QinFeiException e) {
            e.printStackTrace();
            return false;
        }
    }*/

   /* @Override
    @Transactional
    public void modifyStateById(int i, Integer id) {
        mediaInfoMapper.modifyStateById(i, id);
    }*/

  /*  @Override
    @Transactional
    public void updateInfo(Map map){
        mediaInfoMapper.updateInfo(map);
    }*/

    /**
     * 批量删除；
     *
     * @param ids：媒体ID数组；
     */
  /*  @Override
    public void deleteBatch(Integer[] ids) {
        Map<String, Object> map = new HashMap<>();
        map.put("state", IConst.STATE_DELETE);
        map.put("ids", Arrays.asList(ids));
        mediaInfoMapper.stateBatchUpdate(map);
    }*/

 /*   @Cacheable(value = CACHE_KEYS, key = "'supplierId='+#supplierId")
    public List<String> queryBySupplierId(int supplierId) {
        return mediaInfoMapper.queryBySupplierId(supplierId);
    }*/

    /**
     * 消息推送；
     *
     * @param mediaInfo：媒体信息；
     * @param agree：是否同意；
     */
    /*private void sendMessage(MediaInfo mediaInfo, boolean agree) {
        String subject;
        String content;
        if (agree) {
            subject = "媒体订单审核通过。";
            content = "恭喜你，你提交的媒体订单已经审核通过。";
        } else {
            subject = "媒体信息已被驳回。";
            content = "很遗憾，你提交的媒体订单审核未通过。";
        }
        User user = AppUtil.getUser();
        Integer userId = user.getId();
        User creator = mediaInfo.getCreator();
        Integer acceptId = creator.getId();

        // 推送WebSocket消息；
        WSMessage message = new WSMessage();
        message.setReceiveUserId(acceptId + "");
        message.setReceiveName(creator.getName());
        message.setSendName(user.getName());
        message.setSendUserId(userId + "");
        message.setSendUserImage(user.getImage());
        message.setContent(content);
        message.setSubject(subject);
        WebSocketServer.sendMessage(message);

        // 推送系统的消息；
        Message newMessage = new Message();
        String userImage = user.getImage();
        // 获取消息显示的图片；
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
        newMessage.setPic(pictureAddress);
        newMessage.setContent(content);
        newMessage.setInitiatorDept(user.getDeptId());
        newMessage.setInitiatorWorker(userId);
        newMessage.setAcceptDept(creator.getDeptId());
        newMessage.setAcceptWorker(acceptId);
        messageService.addMessage(newMessage);
    }
*/
  /*  @Override
    @Transactional
    public void exportTemplate(Map map, OutputStream outputStream) {
        // 查询所有的业务员
//        List<User> YWList = userService.listByTypeAndCode(IConst.ROLE_TYPE_YW, null);
        User user = AppUtil.getUser() ;
        Map<String, List<String>> siteMap = new HashMap<>();
        List<MediaForm> allFormList = mediaFormService.queryAllPriceColumns() ;
//        List<MediaType> mediaTypeList = mediaTypeService.listByParentId(0,user) ;
//        String[] provNameList = new String[mediaTypeList.size()] ;
        List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(user.getId());
        String[] provNameList = new String[mediaPlateList.size()] ;

        for (int i = 0, len = mediaPlateList.size(); i < len; ++i) {
//            MediaType mediaType = mediaTypeList.get(i);
            MediaPlate mediaPlate = mediaPlateList.get(i);
            provNameList[i] = mediaPlate.getName();
            List<String> formList = new ArrayList<>() ;
            if(allFormList!=null&&allFormList.size()>0){
                for(MediaForm mediaForm:allFormList){
                    if(mediaForm.getMediaTypeId().equals(mediaPlate.getId())){
                        //报纸把底价和标题费用去掉
                        if(mediaForm.getMediaTypeId()==3 && (mediaForm.getCode().equals("f3")||mediaForm.getCode().equals("f1"))){
                            continue ;
                        }
                        formList.add(mediaForm.getName()) ;
                    }
                }
            }
            siteMap.put(mediaPlate.getName(), formList);
        }

        // 创建一个excel
        XSSFWorkbook book = new XSSFWorkbook();
        // 创建需要用户填写的数据页
        // 设计表头
        XSSFSheet sheet1 = book.createSheet("sheet1");
        XSSFCellStyle cellStyle = book.createCellStyle();// 创建提示样式

        XSSFRow row0 = sheet1.createRow(0);
        row0.setHeightInPoints((6 * sheet1.getDefaultRowHeightInPoints()));
        cellStyle.setWrapText(true);
        String info = "提示：1、有*号的列，必填，有*号的列为空时不会导入，导入不成功的会有弹框提示。\n"
                + "2、媒体板块、价格类型建议通过下拉框选择；复制进去的内容必须是下拉框中已有的内容。\n"
                + "3、发布日期格式为“yyyy/MM/dd”,例如：2018/01/12。\n" +
                "4、链接修改为必填。\n" +
                "5、去除单价列,单价由公式自动计算：单价=（支出金额-其他费用） ÷ 数量。\n" +
                "6、数量改为非必填，为空时导入后为默认值1" ;
        row0.createCell(0).setCellValue(info);
        row0.getCell(0).setCellStyle(cellStyle);

        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 17);
        sheet1.addMergedRegion(region);

        XSSFCellStyle headerStyle = book.createCellStyle();// 创建标题样式
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFRow row1 = sheet1.createRow(1);
        row1.setHeightInPoints(30);// 单元格高度
        sheet1.setColumnWidth(0, 3000);
        sheet1.setColumnWidth(1, 3000);
        sheet1.setColumnWidth(2, 4000);
        sheet1.setColumnWidth(3, 3000);
        sheet1.setColumnWidth(4, 3000);
        sheet1.setColumnWidth(5, 3000);
        sheet1.setColumnWidth(6, 4000);
        sheet1.setColumnWidth(7, 5000);
        sheet1.setColumnWidth(8, 5000);
        sheet1.setColumnWidth(9, 3000);
        sheet1.setColumnWidth(10, 4000);
        sheet1.setColumnWidth(11, 2000);
        sheet1.setColumnWidth(12, 3000);
        sheet1.setColumnWidth(13, 3000);
        sheet1.setColumnWidth(14, 5000);
        sheet1.setColumnWidth(15, 4000);
        sheet1.setColumnWidth(16, 3000);

        row1.createCell(0).setCellValue("*媒体板块");
        row1.createCell(1).setCellValue("*媒介");
        row1.createCell(2).setCellValue("*供应商名称");
        row1.createCell(3).setCellValue("*供应商联系人");
        row1.createCell(4).setCellValue("*业务员");
        row1.createCell(5).setCellValue("*发布日期");
        row1.createCell(6).setCellValue("*媒体名称");
        row1.createCell(7).setCellValue("*标题");
        row1.createCell(8).setCellValue("*链接");
        row1.createCell(9).setCellValue("*支付金额");
        row1.createCell(10).setCellValue("*价格类型");
        row1.createCell(11).setCellValue("数量");
        row1.createCell(12).setCellValue("其他费用");
        row1.createCell(13).setCellValue("备注");
        row1.createCell(14).setCellValue("电商商家");
        row1.createCell(15).setCellValue("内外部(内部/外部)");
        row1.createCell(16).setCellValue("频道");

        for (int i = 0; i < 17; i++) {
            row1.getCell(i).setCellStyle(headerStyle);
        }

        // 创建一个专门用来存放板块和报价信息的隐藏sheet页
        // 因此也不能在现实页之前创建，否则无法隐藏。
        XSSFSheet hideSheet = book.createSheet("site");
        book.setSheetHidden(book.getSheetIndex(hideSheet), true);

        int rowId = 1;
        // 设置第一行，存媒体类型的信息
        XSSFRow proviRow = hideSheet.createRow(rowId++);
        proviRow.createCell(0).setCellValue("媒体板块");

        String mediaTypeName = null ;
        String mediaFormName = null ;
        Boolean mediaTypeFlag = false ;
        Boolean mediaFormFlag = false ;
        if(mediaPlateList.size()==1){
            mediaTypeName = mediaPlateList.get(0).getName() ;
            mediaTypeFlag = true ;
            int mediaTypeId = mediaPlateList.get(0).getId() ;
            List<MediaForm> mediaFormList = mediaFormService.queryPriceColumnsByTypeId(mediaTypeId);
            if(mediaFormList != null && mediaFormList.size() == 1){
                mediaFormName = mediaFormList.get(0).getName() ;
                mediaFormFlag = true ;
            }
        }
        if(mediaTypeFlag){
            for(int i=2;i<1002;i++){
                Row row = sheet1.createRow(i) ;
                row.createCell(0).setCellValue(mediaTypeName);
                row.createCell(1).setCellValue(user.getName());
                if(mediaFormFlag){
                    row.createCell(10).setCellValue(mediaFormName);
                }
            }
        }

                // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
        for (Map.Entry<String, List<String>> entry : siteMap.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isEmpty(key) && !CodeUtil.isStartWithNumber(key)) {
                List<String> son = entry.getValue();
                Row row = hideSheet.createRow(rowId++);
                row.createCell(0).setCellValue(key);
                for (int i = 0; i < son.size(); i++) {
                    row.createCell(i+1).setCellValue(son.get(i));
                }
                // 添加名称管理器
                String range = getRange(1, rowId, son.size());
                Name name = book.createName();
                try {
                    name.setNameName(key);
                    String formula = "site!" + range;
                    name.setRefersToFormula(formula);
                } catch (Throwable t) {
//                    book.removeName(name);
                }
            }
        }

//        // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
//        for (Map.Entry<String, List<String>> entry : siteMap.entrySet()) {
//            String key = entry.getKey();
//            if (!StringUtils.isEmpty(key) && !CodeUtil.isStartWithNumber(key)) {
//                List<String> son = entry.getValue();
//                Row row = hideSheet.createRow(rowId++);
//                row.createCell(0).setCellValue(key);
//                for (int i = 1; i < son.size(); i++) {
//                    row.createCell(i).setCellValue(son.get(i));
//                }
//                // 添加名称管理器
//                String range = getRange(1, rowId, son.size());
//                Name name = book.createName();
//                try {
//                    name.setNameName(key);
//                    String formula = "site!" + range;
//                    name.setRefersToFormula(formula);
//                } catch (Throwable t) {
////                    book.removeName(name);
//                }
//            }
//        }
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet1);

//        参照 省-->市 的两级联动校验，此处是 媒体类型-->价格类型
        // 省规则 (媒体类型)
        DataValidationConstraint provConstraint = dvHelper.createExplicitListConstraint(provNameList);
        CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, 1001, 0, 0);
        DataValidation mediaTypeValid = dvHelper.createValidation(provConstraint, provRangeAddressList);
        mediaTypeValid.createErrorBox("error", "请选择正确的媒体类型");
        mediaTypeValid.setShowErrorBox(true);
        mediaTypeValid.setSuppressDropDownArrow(true);
        sheet1.addValidationData(mediaTypeValid);

        for (int i = 3; i < 1003; i++) {
    //        Row row2 = sheet1.createRow(i);
            // 市规则，（价格类型）
            // "INDIRECT($A$" + 2 + ")" 表示规则数据会从名称管理器中获取key与单元格 A2 值相同的数据，如果A2是浙江省，那么此处就是
            DataValidationConstraint formula = dvHelper.createFormulaListConstraint("INDIRECT($A$" + i + ")");
            CellRangeAddressList rangeAddressList = new CellRangeAddressList(i - 1, i - 1, 10, 10);
            DataValidation cacse = dvHelper.createValidation(formula, rangeAddressList);
            cacse.createErrorBox("error", "请选择正确的价格类型");
            cacse.setShowErrorBox(true);
            cacse.setSuppressDropDownArrow(true);
            sheet1.addValidationData(cacse);
        }
        try {
            book.write(outputStream);
        } catch (
                Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

    }*/

    /**
     * @param offset   偏移量，如果给0，表示从A列开始，1，就是从B列
     * @param rowId    第几行
     * @param colCount 一共多少列
     * @return 如果给入参 1,1,10. 表示从B1-K1。最终返回 $B$1:$K$1
     */
   /* private String getRange(int offset, int rowId, int colCount) {
        char start = (char) ('A' + offset);
        if (colCount <= 25) {
            char end = (char) (start + colCount - 1);
            if (colCount == 0) {
                // 从B列开始，如果colCount=0，会导致出现B2B1的情况，导致本来没有数据的，把自己放进去了。所以colCount=0时加1，确保为空
                end = (char) (end + 1);
            }
            return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
        } else {
            char endPrefix = 'A';
            char endSuffix = 'A';
            if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
                if ((colCount - 25) % 26 == 0) {// 边界值
                    endSuffix = (char) ('A' + 25);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                }
            } else {// 51以上
                if ((colCount - 25) % 26 == 0) {
                    endSuffix = (char) ('A' + 25);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26);
                }
            }
            return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
        }
    }*/


/*    @Override
    @Transactional
    public void importExcel(File file) {
        User user = AppUtil.getUser();
        Workbook workbook = null;
        try {
            ZipSecureFile.setMinInflateRatio(-1.0d);
            FileInputStream fis = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (file.getName().toLowerCase().endsWith("xls")) {
                workbook = new HSSFWorkbook(fis);
            }
            // 得到一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 获得表头
            Row rowHead = sheet.getRow(0);
            // 获得数据的总行数
            int totalRowNum = sheet.getLastRowNum();

            // 插入订单数据
            Row orderRow = sheet.getRow(2);
            // 取出业务员姓名，客户公司，客户联系人
            String userName = orderRow.getCell(1).toString();
            String companyName = orderRow.getCell(1).toString();
            String custName = orderRow.getCell(2).toString();
            if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(companyName) || StringUtils.isEmpty(custName)) {
                throw new Exception("客户公司名称或客户对接人为空！");
            } else {
                List<Map> list = dockingPeopleService.queryByNameAndName(userName, companyName, custName);
                if (list != null && list.size() > 0) {
                    Map map = list.get(0);
                    Order order = new Order();
                    order.setCreator(user.getId());
                    // a.id custId,a.cust_name custName,\n" +
                    // " b.id companyId,b.company_name companyName,\n" +
                    // " c.id createId,c.name createName,\n" +
                    // " d.id auditId,d.name auditName\n" +
                    order.setCompanyId(Integer.parseInt(map.get("companyId").toString()));
                    order.setCompanyName(map.get("companyName").toString());
                    order.setCustId(Integer.parseInt(map.get("custId").toString()));
                    order.setCustName(map.get("custName").toString());
                    Integer createId = Integer.parseInt(map.get("createId").toString());
                    order.setUserId(createId);
                    String createName = map.get("createName").toString();
                    order.setUserName(createName);
                    if (map.get("auditId") != null && map.get("auditName") != null) {
                        Integer auditId = Integer.parseInt(map.get("auditId").toString());
                        String auditName = map.get("auditName").toString();
                        order.setUserId(auditId);
                        order.setUserName(auditName);
                    }

                    order.setCreateDate(new Date());
                    order.setOrderType(1);
                    if (orderRow.getCell(3) != null) {
                        order.setTitle(orderRow.getCell(3).toString());
                    }
                    if (orderRow.getCell(4) != null) {
                        order.setDesc(orderRow.getCell(4).toString());
                    }
                    order.setState(1);
                    order.setDepatId(user.getDeptId());
                    orderService.save(order);

                    // 累加订单金额,废弃
//                    BigDecimal bigDecimal = new BigDecimal(0.0);

                    // 插入稿件数据
                    for (int i = 3; i <= totalRowNum; i++) {
                        // 获得第i行对象
                        Row row = sheet.getRow(i);
                        // System.out.println(row.getCell(1).toString());
                        String mediaName = row.getCell(1).toString();
                        String mediaType = row.getCell(2).toString();
                        if (StringUtils.isEmpty(mediaName) || StringUtils.isEmpty(mediaType)) {
                            throw new Exception("媒体类型和媒体名称不能为空！");
                        } else {
                            List<Map> tempList = mediaInfoMapper.queryMediaByNameAndType(mediaName, mediaType);
                            if (tempList != null && tempList.size() > 0) {
                                Article article = new Article();
                                Map temp = tempList.get(0);
                                Integer mediaId = Integer.parseInt(temp.get("mediaId").toString());
                                String mediaName2 = temp.get("mediaName").toString();
                                Integer supplierId = Integer.parseInt(temp.get("supplierId").toString());
                                String supplierName = temp.get("supplierName").toString();
                                Integer mediaUserId = Integer.parseInt(temp.get("mediaUserId").toString());
                                String mediaUserName = temp.get("mediaUserName").toString();
                                article.setMediaId(mediaId);
                                article.setMediaName(mediaName2);
                                article.setSupplierId(supplierId);
                                article.setSupplierName(supplierName);
                                article.setMediaUserId(mediaUserId);
                                article.setMediaUserName(mediaUserName);
                                article.setOrderId(order.getId());
                                article.setBrand(row.getCell(i).toString());
                                article.setIssuedDate(DateUtils.parse(row.getCell(1).toString(), DateUtils.DATE_SMALL));
                                article.setTitle(row.getCell(1).toString());
                                article.setLink(row.getCell(1).toString());
                                article.setNum(Integer.parseInt(row.getCell(1).toString()));
                                article.setSaleAmount(Double.parseDouble(row.getCell(1).toString()));
                                article.setPriceColumn(row.getCell(1).toString());
                                article.setPriceType(row.getCell(1).toString());
                                article.setPayAmount(Double.parseDouble(row.getCell(1).toString()));
                                article.setPromiseDate(DateUtils.parse(row.getCell(1).toString(), DateUtils.DATE_SMALL));
                                article.setRemarks(row.getCell(1).toString());
                                article.setState(1);
                                articleService.update(article);
//                                bigDecimal.add(new BigDecimal(article.getSaleAmount()));
                            }
                        }
//                        order.setAmount(bigDecimal.floatValue());
//                        orderService.update(order);
                    }
                } else {
                    throw new Exception("未找到相应的客户信息，请核实后重试！");
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }*/

  /*  @Override
    public PageInfo<Map> getMediaInfoByTypeId(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        User user= AppUtil.getUser();
        Dept dept = user.getDept();
        map.put("companyCode",dept.getCompanyCode());
        List<Map> list = mediaInfoMapper.getMediaInfoByTypeId(map);
        return new PageInfo<>(list);
    }*/
}
