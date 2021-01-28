package com.qinfei.qferp.utils;

import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.employ.*;
import com.qinfei.qferp.entity.fee.Reimbursement_d;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 媒体数据导入工具类；
 *
 * @Author ：Yuan；
 * @Date ：2019/1/10 0010 11:23；
 */
@Slf4j
public class DataImportUtil {
	public static final String [] familyRelation = {"父亲","母亲","丈夫","妻子","儿子","女儿","哥哥","弟弟","姐姐","妹妹","叔叔","阿姨","舅舅","舅妈","姑姑","姑父"};
	/**
	 * 处理上传的文件；
	 *
	 * @param multipartFile：上传的文件对象；
	 * @param uploadDir：服务器保存的目录地址；
	 * @param storeDir：独立保存的目录名称；
	 * @return ：复制完毕的文件对象；
	 */
	public static File copyFile(MultipartFile multipartFile, String uploadDir, String storeDir) {
		String originalFilename = multipartFile.getOriginalFilename();
		originalFilename = originalFilename.replace(".xls", "");
		String fileName = originalFilename + UUIDUtil.get32UUID() + ".xls";
		StringBuilder filePath = new StringBuilder();
		String dateDir = getCurrentDateDir();
		filePath.append(uploadDir).append(dateDir).append(storeDir).append(fileName);
		File uploadFile = new File(filePath.toString());
		if (!uploadFile.getParentFile().exists()) {
			uploadFile.getParentFile().mkdirs();
		}
		try {
			multipartFile.transferTo(uploadFile);
		} catch (IOException e) {
			log.error("数据导入异常", e);
		}
		return uploadFile;
	}

	/**
	 * 创建导出文件；
	 *
	 * @param templateName：文件名称；
	 * @param uploadDir：文件保存的目录；
	 * @param webDir：文件请求的Web路径；
	 * @param singleData：需要导出的数据集合；
	 * @return ：包含下载路径的文件名称；
	 */
	public static String createFile(String templateName, String uploadDir, String webDir, Map<String, Object> singleData) {
		return createFile(templateName, uploadDir, webDir, singleData, null, null);
	}

	/**
	 * 创建导出文件；
	 *
	 * @param templateName：文件名称；
	 * @param uploadDir：文件保存的目录；
	 * @param webDir：文件请求的Web路径；
	 * @param rowTitles：表格列头名称；
	 * @param exportData：需要导出的数据集合；
	 * @return ：包含下载路径的文件名称；
	 */
	public static String createFile(String templateName, String uploadDir, String webDir, List<String> rowTitles, List<Object[]> exportData) {
		return createFile(templateName, uploadDir, webDir, null, rowTitles, exportData);
	}

	/**
	 * 创建导出文件；
	 *
	 * @param templateName：文件名称；
	 * @param uploadDir：文件保存的目录；
	 * @param webDir：文件请求的Web路径；
	 * @param singleData：需要导出的数据集合；
	 * @param rowTitles：表格列头名称；
	 * @param exportData：需要导出的数据集合；
	 * @return ：包含下载路径的文件名称；
	 */
	private static String createFile(String templateName, String uploadDir, String webDir, Map<String, Object> singleData, List<String> rowTitles, List<Object[]> exportData) {
		// 设置文件名称，使用UUID确保唯一；
		String fileName = templateName + PrimaryKeyUtil.getStringUniqueKey();
		StringBuilder filePath = new StringBuilder();
		// 先创建目录；
		String dateDir = getCurrentDateDir();
		File dir = new File(uploadDir + dateDir + "download/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		filePath.append(uploadDir).append(dateDir).append("download/").append(fileName).append(".xls");
		String fullPath = filePath.toString();
		// 获取输出流；
		OutputStream outputStream = null;
		try {
			// 获取输出流；
			outputStream = new FileOutputStream(fullPath);
			if (singleData == null) {
				createExportFile(templateName, rowTitles, outputStream, exportData, null);
			} else {
				createEntryFile(outputStream, singleData);
			}
			fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			return webDir + dateDir + "download/" + fullPath;
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
			return null;
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * 各浏览器的兼容问题，用于对各个类别的浏览器的内核做不同的字符编码，从而可以在下载对话框显示完整正确的文件名；
	 *
	 * @param filename：文件名称；
	 * @param agent：文件头对象；
	 * @return ：处理完毕的文件名称；
	 */
	public static String encodeDownLoadFilename(String filename, String agent) {
		try {
			if (agent.toLowerCase(Locale.US).contains("firefox")) {
				filename = "=?UTF-8?B?" + new BASE64Encoder().encode(filename.getBytes(StandardCharsets.UTF_8)) + "?=";
			} else {
				filename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
			}
		} catch (UnsupportedEncodingException e) {
			log.error("文件处理异常", e);
		}
		return filename;
	}

	/**
	 * 根据提供的表单数据生成数据导入模板；
	 *
	 * @param templateName：文件名称和标题名称；
	 * @param rowTitles：表格列头名称；
	 * @param outputStream：响应的输出流；
	 * @param exportData：需要导出的数据集合；
	 * @param notices：导出模板的提示信息；
	 */
	public static void createExportFile(String templateName, List<String> rowTitles, OutputStream outputStream, List<Object[]> exportData, List<String> notices) {
		// 校验数据；
		int size = rowTitles.size();
		if (size > 0) {
			// 增加一列序号
			rowTitles.add(0, "序号");
			size = rowTitles.size();

			// 创建一个workBook，对应一个Excel文件；
			HSSFWorkbook workBook = new HSSFWorkbook();
			// 在workBook中添加一个sheet，对应Excel文件中的sheet；
			Sheet sheet = workBook.createSheet(templateName);
			// 设置标题样式；
			setTitleStyle(workBook, sheet, size, templateName, 20);

			// 获取列标题样式；
			CellStyle rowStyle = getContentStyle(workBook, true);
			// 填充标题行内容；
			Row titleRow = sheet.createRow(2);
			titleRow.setHeightInPoints(20);
			Cell cell;
			for (int i = 0; i < size; i++) {
				cell = titleRow.createCell((short) (i + 1));
				// 获取表单的显示标题；
				cell.setCellValue(rowTitles.get(i));
				cell.setCellStyle(rowStyle);
			}

			// 获取表格样式；
			CellStyle contentStyle = getContentStyle(workBook, false);
			Row contentRow;
			// 数据正式开始的行；
			int startRow = 3;
			// 判断是否有需要导出的数据；
			boolean dataIsNull = exportData == null;
			// 如果没有要导出的数据则增加20行空白栏；
			int dataSize = dataIsNull ? 20 : exportData.size();
			if (dataIsNull) {
				for (int i = 0; i < dataSize; i++) {
					// 获取当前列；
					contentRow = sheet.createRow(i + startRow);
					contentRow.setHeightInPoints(20);
					for (int j = 0; j < size; j++) {
						// 获取当前行；
						cell = contentRow.createCell((short) (j + 1));
						// 设置行格式；
						cell.setCellStyle(contentStyle);
						// 写入序号；
						if (j == 0) {
							cell.setCellValue((i + 1));
						}
					}
				}
			} else {
				Object[] data;
				int dataLength;
				for (int i = 0; i < dataSize; i++) {
					contentRow = sheet.createRow(i + startRow);
					contentRow.setHeightInPoints(20);
					// 获取导入的数据；
					data = exportData.get(i);
					// 获取数据长度；
					dataLength = data.length;
					for (int j = 0; j < size; j++) {
						cell = contentRow.createCell((short) (j + 1));
						cell.setCellStyle(contentStyle);
						if (j == 0) {
							cell.setCellValue((i + 1));
						} else {
							// 防止出现越界异常；
							if (j <= dataLength) {
								cell.setCellValue(data[j - 1] == null ? "" : data[j - 1].toString());
							}
						}
					}
				}
			}

			// 导出的内容增加提示信息；
			if (notices != null) {
				// 重新设定开始行；
				startRow = startRow + dataSize;
				// 获取表格样式；
				CellStyle noticeStyle = getContentStyle(workBook, null);
				Row noticeRow;
				int currentRow;
				StringBuilder mergedValue;
				// 计算结束列位置；
				String endRowNum = getChar('A', size);
				int noticeSize = notices.size();
				for (int i = 0; i < noticeSize; i++) {
					currentRow = i + startRow;
					noticeRow = sheet.createRow(currentRow);
					noticeRow.setHeightInPoints(20);
					for (int j = 0; j < size; j++) {
						cell = noticeRow.createCell((short) (j + 1));
						cell.setCellStyle(noticeStyle);
						if (j == 0) {
							cell.setCellValue((i + 1) + "、" + notices.get(i));
						}
					}
					// 合并当前行；
					mergedValue = new StringBuilder();
					mergedValue.append("$B$").append(currentRow + 1).append(":$").append(endRowNum).append("$").append(currentRow + 1);
					sheet.addMergedRegion(CellRangeAddress.valueOf(mergedValue.toString()));
				}
			}

			try {
				workBook.write(outputStream);
			} catch (IOException e) {
				log.error("文件处理异常", e);
			} finally {
				IOUtils.closeQuietly(outputStream);
			}
		}
	}

	/**
	 * 根据提供的表单数据生成数据导入模板；
	 *
	 * @param templateName：文件名称和标题名称；
	 * @param rowTitles：表格列头名称；
	 * @param outputStream：响应的输出流；
	 * @param exportData：需要导出的数据集合；
	 * @param notices：导出模板的提示信息；
	 */
	public static void createExportFile(HSSFWorkbook workBook, String templateName, List<String> rowTitles, OutputStream outputStream, List<Object[]> exportData, List<String> notices) {
		// 校验数据；
		int size = rowTitles.size();
		if (size > 0) {
			// 增加一列序号
			rowTitles.add(0, "序号");
			size = rowTitles.size();

			// 创建一个workBook，对应一个Excel文件；
//			HSSFWorkbook workBook = new HSSFWorkbook();
			// 在workBook中添加一个sheet，对应Excel文件中的sheet；
			Sheet sheet = workBook.createSheet(templateName);
			// 设置标题样式；
			setTitleStyle(workBook, sheet, size, templateName, 16);

			// 获取列标题样式；
			CellStyle rowStyle = getContentStyle(workBook, true);
			// 填充标题行内容；
			Row titleRow = sheet.createRow(2);
			titleRow.setHeightInPoints(20);
			Cell cell;
			for (int i = 0; i < size; i++) {
				cell = titleRow.createCell((short) (i + 1));
				// 获取表单的显示标题；
				cell.setCellValue(rowTitles.get(i));
				cell.setCellStyle(rowStyle);
			}

			// 获取表格样式；
			CellStyle contentStyle = getContentStyle(workBook, false);
			Row contentRow;
			// 数据正式开始的行；
			int startRow = 3;
			// 判断是否有需要导出的数据；
			boolean dataIsNull = exportData == null;
			// 如果没有要导出的数据则增加20行空白栏；
			int dataSize = dataIsNull ? 20 : exportData.size();
			if (dataIsNull) {
				for (int i = 0; i < dataSize; i++) {
					// 获取当前列；
					contentRow = sheet.createRow(i + startRow);
					contentRow.setHeightInPoints(20);
					for (int j = 0; j < size; j++) {
						// 获取当前行；
						cell = contentRow.createCell((short) (j + 1));
						// 设置行格式；
						cell.setCellStyle(contentStyle);
						// 写入序号；
						if (j == 0) {
							cell.setCellValue((i + 1));
						}
					}
				}
			} else {
				Object[] data;
				int dataLength;
				for (int i = 0; i < dataSize; i++) {
					contentRow = sheet.createRow(i + startRow);
					contentRow.setHeightInPoints(20);
					// 获取导入的数据；
					data = exportData.get(i);
					// 获取数据长度；
					dataLength = data.length;
					for (int j = 0; j < size; j++) {
						cell = contentRow.createCell((short) (j + 1));
						cell.setCellStyle(contentStyle);
						if (j == 0) {
							cell.setCellValue((i + 1));
						} else {
							// 防止出现越界异常；
							if (j <= dataLength) {
								cell.setCellValue(data[j - 1] == null ? "" : data[j - 1].toString());
							}
						}
					}
				}
			}

			// 导出的内容增加提示信息；
			if (notices != null) {
				// 重新设定开始行；
				startRow = startRow + dataSize;
				// 获取表格样式；
				CellStyle noticeStyle = getContentStyle(workBook, null);
				Row noticeRow;
				int currentRow;
				StringBuilder mergedValue;
				// 计算结束列位置；
				String endRowNum = getChar('A', size);
				int noticeSize = notices.size();
				for (int i = 0; i < noticeSize; i++) {
					currentRow = i + startRow;
					noticeRow = sheet.createRow(currentRow);
					noticeRow.setHeightInPoints(20);
					for (int j = 0; j < size; j++) {
						cell = noticeRow.createCell((short) (j + 1));
						cell.setCellStyle(noticeStyle);
						if (j == 0) {
							cell.setCellValue((i + 1) + "、" + notices.get(i));
						}
					}
					// 合并当前行；
					mergedValue = new StringBuilder();
					mergedValue.append("$B$").append(currentRow + 1).append(":$").append(endRowNum).append("$").append(currentRow + 1);
					sheet.addMergedRegion(CellRangeAddress.valueOf(mergedValue.toString()));
				}
			}
		}
	}

	/**
	 * 创建多表Excel
	 * @param sheetInfo 表信息
	 * @param outputStream 输出流
	 */
	public static void createMoreSheetFile(List<Map<String, Object>> sheetInfo, OutputStream outputStream){
		if(CollectionUtils.isNotEmpty(sheetInfo)){
			HSSFWorkbook workBook = new HSSFWorkbook();
			for(Map sheet : sheetInfo){
				if(sheet != null){
					String templateName = String.valueOf(sheet.get("templateName"));
					List<String> rowTitles = (List<String>) sheet.get("rowTitles");
					List<Object[]> exportData = (List<Object[]>) sheet.get("exportData");
					List<String> notices = (List<String>) sheet.get("notices");
					createExportFile(workBook,templateName,rowTitles,outputStream,exportData,notices);
				}
			}
			try {
				workBook.write(outputStream);
			} catch (IOException e) {
				log.error("文件处理异常", e);
			} finally {
				IOUtils.closeQuietly(outputStream);
			}
		}else{
			log.info("表格没有数据，无法创建文件");
		}
	}

	/**
	 * 创建导出文件；
	 * @param sheetInfo 导出表信息
	 * @param templateName 导出文件名
	 * @param uploadDir：文件保存的目录；
	 * @param webDir：文件请求的Web路径；
	 * @return ：包含下载路径的文件名称；
	 */
	public static String createMoreSheetFile(String templateName, List<Map<String, Object>> sheetInfo, String uploadDir, String webDir) {
		// 设置文件名称，使用UUID确保唯一；
		String fileName = templateName + PrimaryKeyUtil.getStringUniqueKey();
		StringBuilder filePath = new StringBuilder();
		// 先创建目录；
		String dateDir = getCurrentDateDir();
		File dir = new File(uploadDir + dateDir + "download/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		filePath.append(uploadDir).append(dateDir).append("download/").append(fileName).append(".xls");
		String fullPath = filePath.toString();
		// 获取输出流；
		OutputStream outputStream = null;
		try {
			// 获取输出流；
			outputStream = new FileOutputStream(fullPath);
			createMoreSheetFile(sheetInfo, outputStream);
			fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			return webDir + dateDir + "download/" + fullPath;
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
			return null;
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * 读取Excel内容；
	 *
	 * @param file
	 *            ：文件对象；
	 * @param startRow
	 *            ：开始读取的列；
	 * @param startCell
	 *            ：开始读取的行；
	 * @param length
	 *            ：读取的数据长度；
	 * @return 文件的字符串内容集合，集合内部元素为数组；
	 */
	public static List<Object[]> getExcelContent(File file, int startRow, int startCell, int length) {
		List<Object[]> list = new ArrayList<>();
		try {
			ZipSecureFile.setMinInflateRatio(-1.0d);
			// 创建对Excel工作簿文件的引用；
			HSSFWorkbook wookbook = new HSSFWorkbook(new FileInputStream(file));
			// 在Excel文档中，第一张工作表的缺省索引是0，也可以通过工作表的名称来获取对象；
			// 方式1：Sheet sheet = workbook.getSheetAt(0);
			// 方式2：Sheet sheet = wookbook.getSheet("Sheet1");
			Sheet sheet = wookbook.getSheetAt(0);
			// 获取到Excel文件中的所有行数；
			int rows = sheet.getPhysicalNumberOfRows() + 1;
			Cell cell;
			Row row;
			Object[] objects;
			Object cellValue;
			int index;
			// 遍历行，从第三行开始读取，通常Excel表格第一行为空，第二行为标题；
			for (int i = startRow; i < rows; i++) {
				// 读取左上端单元格；
				row = sheet.getRow(i);
				// 行不为空；
				if (row != null) {
					// 获取到Excel文件中的所有的列；
					objects = new Object[length];
					// 遍历列；
					// 获取到列的值，从第二列开始读取，通常第一列为空；
					index = 0;
					for (int j = startCell; j < length + 2; j++) {
						cell = row.getCell((short) j);
						if (cell == null) {
							// 第一列为空则跳过；
							if (j == startCell) {
								break;
							} else {
								objects[index] = "";
								index++;
							}
						} else {
							cellValue = getCellValue(cell);
							// 第一列为空则跳过；
							if (StringUtils.isEmpty(cellValue) && j == startCell) {
								break;
							} else {
								objects[index] = cellValue;
							}
							index++;
						}
					}
					list.add(objects);
				}
			}
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
		} catch (OfficeXmlFileException e) {
			log.error("文件解析异常", e);
		} catch (IOException e) {
			log.error("文件处理异常", e);
		}
		return handleList(list);
	}

	/**
	 * 读取Excel内容；
	 *
	 * @param file  文件对象；
	 * @param sheetIndex 文件sheet位置，下标从0开始
	 * @param startRow 开始读取的列；
	 * @param startCell 开始读取的行；
	 * @param length 读取的数据长度；
	 * @return 文件的字符串内容集合，集合内部元素为数组；
	 */
	public static List<Object[]> getExcelContent(File file, int sheetIndex, int startRow, int startCell, int length) {
		List<Object[]> list = new ArrayList<>();
		try {
			ZipSecureFile.setMinInflateRatio(-1.0d);
			// 创建对Excel工作簿文件的引用；
			HSSFWorkbook wookbook = new HSSFWorkbook(new FileInputStream(file));
			// 在Excel文档中，第一张工作表的缺省索引是0，也可以通过工作表的名称来获取对象；
			// 方式1：Sheet sheet = workbook.getSheetAt(0);
			// 方式2：Sheet sheet = wookbook.getSheet("Sheet1");
			Sheet sheet = wookbook.getSheetAt(sheetIndex);
			// 获取到Excel文件中的所有行数；
			int rows = sheet.getPhysicalNumberOfRows() + 1;
			Cell cell;
			Row row;
			Object[] objects;
			Object cellValue;
			int index;
			// 遍历行，从第三行开始读取，通常Excel表格第一行为空，第二行为标题；
			for (int i = startRow; i < rows; i++) {
				// 读取左上端单元格；
				row = sheet.getRow(i);
				// 行不为空；
				if (row != null) {
					// 获取到Excel文件中的所有的列；
					objects = new Object[length];
					// 遍历列；
					// 获取到列的值，从第二列开始读取，通常第一列为空；
					index = 0;
					for (int j = startCell; j < length + 2; j++) {
						cell = row.getCell((short) j);
						if (cell == null) {
							// 第一列为空则跳过；
							if (j == startCell) {
								break;
							} else {
								objects[index] = "";
								index++;
							}
						} else {
							cellValue = getCellValue(cell);
							// 第一列为空则跳过；
							if (StringUtils.isEmpty(cellValue) && j == startCell) {
								break;
							} else {
								objects[index] = cellValue;
							}
							index++;
						}
					}
					list.add(objects);
				}
			}
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
		} catch (OfficeXmlFileException e) {
			log.error("文件解析异常", e);
		} catch (IOException e) {
			log.error("文件处理异常", e);
		}
		return handleList(list);
	}

	/**
	 * 获取Character类型的字符串，用于指定Excel列位置；
	 *
	 * @param begin
	 *            ：其实列；
	 * @param size
	 *            ：增加数量；
	 * @return 拼接完成的字符串；
	 */
	private static String getChar(int begin, int size) {
		StringBuilder str = new StringBuilder();
		if (begin + size > 'Z') {
			str.append('A');
			str.append(getChar('A' - 1, size - ('Z' - begin)));
		} else {
			str.append((char) (begin + size));
		}
		return str.toString();
	}

	/**
	 * 根据单元格属性返回指定的值；
	 *
	 * @param cell
	 *            ：单元格；
	 * @return 指定格式的值；
	 */
	private static Object getCellValue(Cell cell) {
		Object cellValue = "";
		CellType cellType = cell.getCellType();
		if (CellType.STRING.equals(cellType)) {
			String cellContent = cell.getStringCellValue();
			if (StringUtils.hasLength(cellContent)) {
				cellValue = cellContent.trim();
			}
		} else if (CellType.NUMERIC.equals(cellType)) {
			if (DateUtil.isCellDateFormatted(cell)) {
				cellValue = cell.getDateCellValue();
			} else {
				cellValue = cell.getNumericCellValue();
			}
		} else if (CellType.FORMULA.equals(cellType)) {
			if (DateUtil.isCellDateFormatted(cell)) {
				cellValue = cell.getDateCellValue();
			} else {
				cellValue = cell.getNumericCellValue();
			}
		}
		String dataContent = cellValue.toString();
		// 科学计数正则；
		String scienceRegex = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";
		// 科学计数法的处理；
		if (StringUtils.hasLength(dataContent) && dataContent.matches(scienceRegex)) {
			cellValue = new BigDecimal(dataContent).toPlainString();
		}
		// switch (cellType) {
		// // 字符串类型；
		// case CellType.STRING:
		// String cellContent = cell.getStringCellValue();
		// if (StringUtils.hasLength(cellContent)) {
		// cellValue = cellContent.trim();
		// }
		// break;
		// // 数值类型；
		// case CellType.NUMERIC:
		// if (DateUtil.isCellDateFormatted(cell)) {
		// cellValue = cell.getDateCellValue();
		// } else {
		// cellValue = cell.getNumericCellValue();
		// }
		// break;
		// // 公式；
		// case CellType.FORMULA:
		// cell.setCellType(CellType.NUMERIC);
		// cellValue = cell.getNumericCellValue();
		// break;
		// case CellType.BLANK:
		// break;
		// case CellType.BOOLEAN:
		// break;
		// case CellType.ERROR:
		// break;
		// default:
		// break;
		// }
		return cellValue;
	}

	/**
	 * 对解析的结果进行处理，移除空值；
	 *
	 * @param objects
	 *            ：通过Excel解析的内容；
	 * @return 处理完毕的集合；
	 */
	private static List<Object[]> handleList(List<Object[]> objects) {
		int size = objects.size();
		List<Object[]> newBeans = new ArrayList<Object[]>();
		if (size > 0) {
			Object[] beans;
			for (int i = 0; i < size; i++) {
				beans = objects.get(i);
				if (beans[0] != null) {
					newBeans.add(beans);
				}
			}
		}
		return newBeans;
	}

	/**
	 * 设置标题样式；
	 *
	 * @param workBook：工作簿对象；
	 * @param sheet：当前标签页对象；
	 * @param size：列数量；
	 * @param titleName：标题名字；
	 */
	private static void setTitleStyle(HSSFWorkbook workBook, Sheet sheet, int size, String titleName, int width) {
		// 设置标题宽度；
		for (int i = 0; i < size; i++) {
			sheet.setColumnWidth(i + 1, width * 256);
		}
		// 隐藏Excel网格线；
		sheet.setDisplayGridlines(false);
		// 首行合并，用于显示标题；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$B$2:$" + getChar('A', size) + "$2"));
		// 在sheet中添加表头第0行，即从0行开始，注意老版本poi对Excel的行数列数有限制使用Short；
		Row row = sheet.createRow(1);
		// 创建单元格，并设置值表头 设置表头居中；
		CellStyle style = workBook.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setFillForegroundColor(IndexedColors.TAN.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		Cell cell;
		for (int i = 0; i < size; i++) {
			cell = row.createCell((short) (i + 1));
			cell.setCellStyle(style);
		}
		// 设置字体格式；
		Font titleFont = workBook.createFont();
		// 设置字体加粗；
		// titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleFont.setBold(true);
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setFontName("微软雅黑");
		// 标题颜色设置为红色；
		// titleFont.setColor(IndexedColors.RED.getIndex());
		// 应用字体格式到样式；
		style.setFont(titleFont);
		// 内容显示格式设置为左右居中；
		style.setAlignment(HorizontalAlignment.CENTER);
		// 内容显示格式设置为上下居中；
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.TAN.getIndex());
		// 创建第一行来存放标题内容；
		cell = row.createCell((short) 1);
		row.setHeightInPoints(40);
		cell.setCellValue(titleName);
		cell.setCellStyle(style);
	}

	/**
	 * 获取表格样式；
	 *
	 * @param workBook：工作簿对象；
	 * @param isHead：是否列头；
	 * @return ：样式对象；
	 */
	private static CellStyle getContentStyle(HSSFWorkbook workBook, Boolean isHead) {
		// 设置列标题样式；
		CellStyle rowStyle = workBook.createCellStyle();
		if (isHead == null) {
			// 文字居左；
			rowStyle.setAlignment(HorizontalAlignment.LEFT);
		} else {
			rowStyle.setAlignment(HorizontalAlignment.CENTER);
		}
		rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// 列标题增加背景色；
		if (isHead != null && isHead) {
			rowStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
			rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setWrapText(true);
		// 设置字体格式；
		Font rowTitleFont = workBook.createFont();
		// 标题设置字体加粗；
		if (isHead != null && isHead) {
			// rowTitleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			rowTitleFont.setBold(true);
		}
		// 尾部提示；
		if (isHead == null) {
			// 字体加粗使用红色；
			// rowTitleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			rowTitleFont.setBold(true);
			rowTitleFont.setColor(IndexedColors.RED.getIndex());
			// 文字居左；
			rowStyle.setAlignment(HorizontalAlignment.LEFT);
		}
		rowTitleFont.setFontHeightInPoints((short) 10);
		rowTitleFont.setFontName("微软雅黑");
		rowStyle.setFont(rowTitleFont);

		// 设置格式为文本；
		DataFormat dataFormat = workBook.createDataFormat();
		rowStyle.setDataFormat(dataFormat.getFormat("@"));
		return rowStyle;
	}

	/**
	 * 导出入职申请信息；
	 * 
	 * @param outputStream：输出流；
	 * @param entryInfo：需导出的入职信息；
	 */
	private static void createEntryFile(OutputStream outputStream, Map<String, Object> entryInfo) {
		// 获取入职对象；
		EmployEntry entry = (EmployEntry) EntityUtil.getNewObject(entryInfo, EmployEntry.class);
		// 获取基础信息对象；
		EmployeeBasic basic = (EmployeeBasic) EntityUtil.getNewObject(entryInfo, EmployeeBasic.class);
		// 求职者姓名；
		String entryName = entry.getEntryName();
		// 创建一个workBook，对应一个Excel文件；
		HSSFWorkbook workBook = new HSSFWorkbook();
		// 在workBook中添加一个sheet，对应Excel文件中的sheet；
		Sheet sheet = workBook.createSheet(entryName);
		// 设置标题样式；
		setTitleStyle(workBook, sheet, 10, entryName, 10);

		// 设置单元格列宽；
		sheet.setColumnWidth(0,  10 * 256);
		sheet.setColumnWidth(1,  15 * 256);
		sheet.setColumnWidth(2,  15 * 256);
		sheet.setColumnWidth(3,  15 * 256);
		sheet.setColumnWidth(4,  15 * 256);
		sheet.setColumnWidth(5,  15 * 256);
		sheet.setColumnWidth(6,  15 * 256);
		sheet.setColumnWidth(7,  15 * 256);
		sheet.setColumnWidth(8,  15 * 256);
		sheet.setColumnWidth(9,  15 * 256);
		sheet.setColumnWidth(10, 15 * 256);

		// 获取表格标题样式；
		CellStyle rowStyle = getContentStyle(workBook, true);
		// 获取表格内容样式；
		CellStyle contentStyle = getContentStyle(workBook, false);

		int startRow = 2;
		Row contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);
		Cell cell = contentRow.createCell((short) (1));
		// 获取表单的显示标题；
		cell.setCellValue("基本资料");
		cell.setCellStyle(rowStyle);
		// 列合并，基本资料为长列；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+8)));

		// 申请部门；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("申请部门");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(entryInfo.get("dept") == null ? "" : String.format("%s(%s)",entryInfo.get("dept").toString(),String.valueOf(entryInfo.get("entryCompanyName"))));
		cell.setCellStyle(contentStyle);

		// 申请职位；
		cell = contentRow.createCell((short) (4));
		cell.setCellValue("申请职位");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (5));
		cell.setCellValue(entryInfo.get("post") == null ? "" : entryInfo.get("post").toString());
		cell.setCellStyle(contentStyle);

		// 待遇要求；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("待遇要求");
		cell.setCellStyle(rowStyle);
		// 列合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(startRow+1)+":$H$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 7, 1);

		cell = contentRow.createCell((short) (8));
		cell.setCellValue(entry.getEntryExpectSalary());
		cell.setCellStyle(contentStyle);
		// 列合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$I$"+(startRow+1)+":$J$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 9, 1);

		// 照片；
		cell = contentRow.createCell((short) (10));
		cell.setCellValue("我是照片");
		cell.setCellStyle(contentStyle);
		// 列合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$K$"+(startRow+1)+":$K$"+(startRow+3)));

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(20);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 姓名；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("姓名");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(entryName);
		cell.setCellStyle(contentStyle);

		// 农历生日；
		cell = contentRow.createCell((short) (4));
		cell.setCellValue("农历生日");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (5));
		cell.setCellValue(basic.getEmpBirthday());
		cell.setCellStyle(contentStyle);

		// 性别；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("性别");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (7));
		cell.setCellValue(basic.getEmpGender() == null || basic.getEmpGender() == 0 ? "女" : "男");
		cell.setCellStyle(contentStyle);

		// 民族；
		cell = contentRow.createCell((short) (8));
		cell.setCellValue("民族");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (9));
		cell.setCellValue(entryInfo.get("nation") == null ? "" : entryInfo.get("nation").toString());
		cell.setCellStyle(contentStyle);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 10, 1);

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 身份证号码；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("身份证号码");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(basic.getEmpCode());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(startRow+1)+":$F$"+(startRow+1)));

		// 空白列；
		contentRow = createBlankRow(contentRow, contentStyle, 4, 2);

		// 户口性质；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("户口性质");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (7));
		cell.setCellValue(entry.getEntryResidence() == null || entry.getEntryResidence() == 0 ? "城镇户口" : "农村户口");
		cell.setCellStyle(contentStyle);

		// 婚姻状况；
		String[] marriage = new String[] { "未婚", "已婚", "离婚", "丧偶" };
		cell = contentRow.createCell((short) (8));
		cell.setCellValue("婚姻状况");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (9));
		cell.setCellValue(entry.getEntryMarriage() == null ? "" : marriage[entry.getEntryMarriage()]);
		cell.setCellStyle(contentStyle);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 10, 1);

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 身份证地址；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("身份证地址");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(basic.getEmpCodeAddress());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(startRow+1)+":$F$"+(startRow+1)));

		// 空白列；
		contentRow = createBlankRow(contentRow, contentStyle, 4, 2);

		// 籍贯；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("籍贯");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (7));
		cell.setCellValue(basic.getEmpNative());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$H$"+(startRow+1)+":$K$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 8, 3);

        startRow++; //行+1
        contentRow = sheet.createRow(startRow);
        contentRow.setHeightInPoints(40);

        // 空白列；
        createBlankRow(contentRow, contentStyle, 1, 1);

        // 子女；
        cell = contentRow.createCell((short) (2));
        cell.setCellValue("子女");
        cell.setCellStyle(rowStyle);
        cell = contentRow.createCell((short) (3));
        cell.setCellValue(String.format(" %s 子 %s 女", (basic.getEmpSon() == null ? "" : basic.getEmpSon()),
                (basic.getEmpGirl() == null ? "" : basic.getEmpGirl())));
        cell.setCellStyle(contentStyle);
        // 行合并；
        sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(startRow+1)+":$F$"+(startRow+1)));

        // 空白列；
        contentRow = createBlankRow(contentRow, contentStyle, 4, 2);

        // 兄弟姐妹；
        cell = contentRow.createCell((short) (6));
        cell.setCellValue("兄弟姐妹");
        cell.setCellStyle(rowStyle);
        cell = contentRow.createCell((short) (7));
        cell.setCellValue(String.format(" %s 兄 %s 弟 %s 姐 %s 妹", (basic.getEmpBrother() == null ? "":basic.getEmpBrother()),
                (basic.getEmpYoungerBrother() == null ? "" : basic.getEmpYoungerBrother()),
                (basic.getEmpSister() == null ? "" : basic.getEmpSister()),
                (basic.getEmpYoungerSister() == null ? "" : basic.getEmpYoungerSister())));
        cell.setCellStyle(contentStyle);
        // 行合并；
        sheet.addMergedRegion(CellRangeAddress.valueOf("$H$"+(startRow+1)+":$K$"+(startRow+1)));

        // 空白列；
        createBlankRow(contentRow, contentStyle, 8, 3);

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 身份证地址；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("现住址");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(entry.getEntryLocalAddress());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(startRow+1)+":$F$"+(startRow+1)));

		// 空白列；
		contentRow = createBlankRow(contentRow, contentStyle, 4, 2);

		// 学历；
		String[] educationArray = new String[] { "初中", "高中", "专科", "本科", "硕士", "博士", "博士后","其他","小学" };
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("学历");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (7));
		Integer empEducation = basic.getEmpEducation();
		if (empEducation == null || empEducation < 0 || empEducation > 8) {
			cell.setCellValue("");
		} else {
			// 其他学历；
			if (empEducation == 7) {
				cell.setCellValue(basic.getEmpEducationOther());
			} else {
				cell.setCellValue(educationArray[empEducation]);
			}
		}
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$H$"+(startRow+1)+":$K$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 8, 3);

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 个人邮箱；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("个人邮箱");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(entry.getEntryMail());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$"+(startRow+1)+":$F$"+(startRow+1)));

		// 空白列；
		contentRow = createBlankRow(contentRow, contentStyle, 4, 2);

		// 个人电话；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("个人电话");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (7));
		cell.setCellValue(entry.getEntryPhone());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$H$"+(startRow+1)+":$K$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 8, 3);

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);

		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 紧急事件联络人；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("紧急事件联络人");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(entry.getEntryUrgent());
		cell.setCellStyle(contentStyle);

		// 有效联系电话；
		cell = contentRow.createCell((short) (4));
		cell.setCellValue("有效联系电话");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (5));
		cell.setCellValue(entry.getEntryUrgentPhone());
		cell.setCellStyle(contentStyle);

		// 与联络人的关系；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("关系");
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell((short) (7));
		cell.setCellValue(entry.getEntryUrgentRelation());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$H$"+(startRow+1)+":$K$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 8, 3);

		// 第八行；
		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(20);
		cell = contentRow.createCell((short) (1));
		// 获取表单的显示标题；
		cell.setCellValue("家庭成员");
		cell.setCellStyle(rowStyle);

		// 获取数据集合；
		Object data = entryInfo.get("family");
		// 获取数据长度；
		int dataSize = 0;
		// 获取数据集合；
		List<EmployEntryFamily> familyData = null;
		if (data != null) {
			familyData = (List<EmployEntryFamily>) data;
			dataSize = familyData.size();
			// 列合并,可能有多行，所以合并；
			if(dataSize != 0){
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow + 1)+":$B$" + (startRow + 1 + dataSize)));
			}else {
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow + 1)+":$B$"+(startRow + 2)));
			}
		}

		// 关系；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("关系");
		cell.setCellStyle(rowStyle);

		// 姓名；
		cell = contentRow.createCell((short) (3));
		cell.setCellValue("姓名");
		cell.setCellStyle(rowStyle);

		// 年龄；
		cell = contentRow.createCell((short) (4));
		cell.setCellValue("年龄");
		cell.setCellStyle(rowStyle);

		// 生日；
		cell = contentRow.createCell((short) (5));
		cell.setCellValue("生日");
		cell.setCellStyle(rowStyle);

		// 单位；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("单位");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$G$"+(startRow+1)+":$H$"+(startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 7, 1);

		// 职业；
		cell = contentRow.createCell((short) (8));
		cell.setCellValue("职业");
		cell.setCellStyle(rowStyle);

		// 健康状况；
		cell = contentRow.createCell((short) (9));
		cell.setCellValue("健康状况");
		cell.setCellStyle(rowStyle);

		// 备注；
		cell = contentRow.createCell((short) (10));
		cell.setCellValue("备注");
		cell.setCellStyle(rowStyle);

		// 起始行；
		startRow++; //行+1
		// 家庭成员数据， 如果没有家庭成员数据，则创建一个空行；
		if (dataSize > 0) {
			startRow --;
			EmployEntryFamily entryFamily;
			for (int i = 0; i < dataSize; i++) {
				entryFamily = familyData.get(i);
				// 合并行的行号；
				startRow = startRow + 1;

				contentRow = sheet.createRow(startRow);
				contentRow.setHeightInPoints(40);

				// 空白列；
				createBlankRow(contentRow, contentStyle, 1, 1);

				// 关系；
				cell = contentRow.createCell((short) (2));
				cell.setCellValue((entryFamily.getFamRelation() == null ? "" : familyRelation[entryFamily.getFamRelation()]));
				cell.setCellStyle(contentStyle);

				// 姓名；
				cell = contentRow.createCell((short) (3));
				cell.setCellValue(entryFamily.getFamName());
				cell.setCellStyle(contentStyle);

				// 年龄；
				cell = contentRow.createCell((short) (4));
				cell.setCellValue(entryFamily.getFamAge());
				cell.setCellStyle(contentStyle);

				// 生日；
				cell = contentRow.createCell((short) (5));
				cell.setCellValue(entryFamily.getFamBirthday());
				cell.setCellStyle(contentStyle);

				// 单位；
				cell = contentRow.createCell((short) (6));
				cell.setCellValue(entryFamily.getFamUnit());
				cell.setCellStyle(contentStyle);
				// 行合并；
				sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$H$" + (startRow+1)));

				// 空白列；
				createBlankRow(contentRow, contentStyle, 7, 1);

				//健康状况；
				cell = contentRow.createCell((short) (8));
				cell.setCellValue(entryFamily.getFamProfession());
				cell.setCellStyle(contentStyle);

				// 职业；
				cell = contentRow.createCell((short) (9));
				cell.setCellValue(entryFamily.getFamHealth());
				cell.setCellStyle(contentStyle);

				// 备注；
				cell = contentRow.createCell((short) (10));
				cell.setCellValue(entryFamily.getFamDesc());
				cell.setCellStyle(contentStyle);
			}
		}else {
			contentRow = sheet.createRow(startRow);
			contentRow.setHeightInPoints(40);
			createBlankRow(contentRow, contentStyle,1, 10);
		}

		// 计算开始行；
		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(20);
		cell = contentRow.createCell((short) (1));
		// 获取表单的显示标题；
		cell.setCellValue("教育培训经历");
		cell.setCellStyle(rowStyle);

		// 获取数据集合；
		data = entryInfo.get("education");
		// 获取数据集合；
		List<EmployEntryEducation> educationData = null;
		dataSize = 0;
		// 合并的序号需加1；
		if (data != null) {
			educationData = (List<EmployEntryEducation>) data;
			dataSize = educationData.size();
			// 列合并，可能有多行数据；
			if(dataSize != 0){
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$B$" + (startRow + 1 + dataSize)));
			}else {
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$B$" + (startRow + 2)));
			}
		}

		// 起止年月；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("起止年月");
		cell.setCellStyle(rowStyle);

		// 院校名称；
		cell = contentRow.createCell((short) (3));
		cell.setCellValue("院校名称/培训机构");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 4, 1);

		// 学习所在地；
		cell = contentRow.createCell((short) (5));
		cell.setCellValue("学习所在地");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$F$" + (startRow+1) + ":$G$" + (startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 6, 1);

		// 学制时长（年）；
		cell = contentRow.createCell((short) (7));
		cell.setCellValue("学制时长(年)");
		cell.setCellStyle(rowStyle);

		// 专业；
		cell = contentRow.createCell((short) (8));
		cell.setCellValue("专业/内容");
		cell.setCellStyle(rowStyle);

		// 所获学历；
		cell = contentRow.createCell((short) (9));
		cell.setCellValue("所获学历/资格证书");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$J$" + (startRow+1) + ":$K$" + (startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 10, 1);

		// 教育经历数据；
		startRow++; // 行号加1；
		if (dataSize > 0) {
			startRow --;
			EmployEntryEducation education;
			// 需合并行的行号；
			int columnNum;
			for (int i = 0; i < dataSize; i++) {
				education = educationData.get(i);
				startRow = startRow + 1;

				// 从第九行开始；
				contentRow = sheet.createRow(startRow);
				contentRow.setHeightInPoints(40);

				// 空白列；
				createBlankRow(contentRow, contentStyle, 1, 1);

				// 起止年月；
				cell = contentRow.createCell((short) (2));
				cell.setCellValue((education.getEduStart() != null ? DateUtils.format(education.getEduStart(), "yyyy-MM-dd") : "") + "至"
						+ (education.getEduEnd() != null ? DateUtils.format(education.getEduEnd(), "yyyy-MM-dd") : ""));
				cell.setCellStyle(contentStyle);

				// 院校名称；
				cell = contentRow.createCell((short) (3));
				cell.setCellValue(education.getEduCollege());
				cell.setCellStyle(contentStyle);
				// 行合并；
				sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));

				// 空白列；
				createBlankRow(contentRow, contentStyle, 4, 1);

				// 学习所在地；
				cell = contentRow.createCell((short) (5));
				cell.setCellValue(education.getEduLocation());
				cell.setCellStyle(contentStyle);

				// 行合并；
				sheet.addMergedRegion(CellRangeAddress.valueOf("$F$" + (startRow+1) + ":$G$" + (startRow+1)));

				// 空白列；
				createBlankRow(contentRow, contentStyle, 6, 1);

				// 学制时长（年）；
				cell = contentRow.createCell((short) (7));
				cell.setCellValue((education.getEduDuration() != null ? education.getEduDuration().toString() : ""));
				cell.setCellStyle(contentStyle);

				// 专业；
				cell = contentRow.createCell((short) (8));
				cell.setCellValue(education.getEduMajor());
				cell.setCellStyle(contentStyle);

				// 所获学历；
				cell = contentRow.createCell((short) (9));
				cell.setCellValue(education.getEduRecord());
				cell.setCellStyle(contentStyle);
				// 行合并；
				sheet.addMergedRegion(CellRangeAddress.valueOf("$J$" + (startRow+1) + ":$K$" + (startRow+1)));

				// 空白列；
				createBlankRow(contentRow, contentStyle, 10, 1);
			}
		} else {
			contentRow = sheet.createRow(startRow);
			contentRow.setHeightInPoints(40);
			createBlankRow(contentRow, contentStyle,1, 10);
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$F$" + (startRow+1) + ":$G$" + (startRow+1)));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$J$" + (startRow+1) + ":$K$" + (startRow+1)));
		}

		// 计算起始行；
		startRow++; // 行号加1；
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(20);
		cell = contentRow.createCell((short) (1));
		// 获取表单的显示标题；
		cell.setCellValue("工作经历（请自开始工作写起）");
		cell.setCellStyle(rowStyle);

		// 获取数据集合；
		data = entryInfo.get("experience");
		// 获取数据集合；
		dataSize = 0;
		List<EmployEntryExperience> experienceData = null;
		if (data != null) {
			experienceData = (List<EmployEntryExperience>) data;
			dataSize = experienceData.size();
			if(dataSize != 0){
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$B$" + (startRow + 1 + dataSize)));
			}else {
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$B$" + (startRow + 2)));
			}
		}

		// 起止年月；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("起止年月");
		cell.setCellStyle(rowStyle);

		// 公司名称；
		cell = contentRow.createCell((short) (3));
		cell.setCellValue("公司名称");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 4, 1);

		// 工作地点；
		cell = contentRow.createCell((short) (5));
		cell.setCellValue("工作地点");
		cell.setCellStyle(rowStyle);

		// 职务；
		cell = contentRow.createCell((short) (6));
		cell.setCellValue("职务");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$H$" + (startRow+1)));

		// 空白列；
		createBlankRow(contentRow, contentStyle, 7, 1);

		// 薪资待遇；
		cell = contentRow.createCell((short) (8));
		cell.setCellValue("薪资待遇");
		cell.setCellStyle(rowStyle);

		// 证明人及联系电话；
		cell = contentRow.createCell((short) (9));
		cell.setCellValue("证明人及联系电话");
		cell.setCellStyle(rowStyle);

		// 离职原因；
		cell = contentRow.createCell((short) (10));
		cell.setCellValue("离职原因");
		cell.setCellStyle(rowStyle);

		// 工作经历数据；
		startRow ++;
		if (dataSize > 0) {
			startRow --;
			EmployEntryExperience experience;
			for (int i = 0; i < dataSize; i++) {
				experience = experienceData.get(i);
				//行号；
				startRow = startRow + 1;
				contentRow = sheet.createRow(startRow);
				contentRow.setHeightInPoints(40);

				// 空白列；
				createBlankRow(contentRow, contentStyle, 1, 1);

				// 起止年月；
				cell = contentRow.createCell((short) (2));
				cell.setCellValue((experience.getExpStart() != null ? DateUtils.format(experience.getExpStart(), "yyyy-MM-dd") : "") + "至"
						+ (experience.getExpEnd() != null ? DateUtils.format(experience.getExpEnd(), "yyyy-MM-dd") : ""));
				cell.setCellStyle(contentStyle);

				// 公司名称；
				cell = contentRow.createCell((short) (3));
				cell.setCellValue(experience.getExpCompany());
				cell.setCellStyle(contentStyle);
				// 行合并；
				sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));

				// 空白列；
				createBlankRow(contentRow, contentStyle, 4, 1);

				// 工作地点；
				cell = contentRow.createCell((short) (5));
				cell.setCellValue(experience.getExpLocation());
				cell.setCellStyle(contentStyle);

				// 职务；
				cell = contentRow.createCell((short) (6));
				cell.setCellValue(experience.getExpProfession());
				cell.setCellStyle(contentStyle);
				// 行合并；
				sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$H$" + (startRow+1)));

				// 空白列；
				createBlankRow(contentRow, contentStyle, 7, 1);

				// 薪资待遇；
				cell = contentRow.createCell((short) (8));
				cell.setCellValue(experience.getExpSalary());
				cell.setCellStyle(contentStyle);

				// 证明人及联系电话；
				cell = contentRow.createCell((short) (9));
				cell.setCellValue(experience.getExpContactor());
				cell.setCellStyle(contentStyle);

				// 离职原因；
				cell = contentRow.createCell((short) (10));
				cell.setCellValue(experience.getExpResignReason());
				cell.setCellStyle(contentStyle);
			}
		} else {
			contentRow = sheet.createRow(startRow);
			contentRow.setHeightInPoints(40);
			createBlankRow(contentRow, contentStyle,1, 10);
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$H$" + (startRow+1)));
		}

		startRow++; //行号+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);
		cell = contentRow.createCell((short) (1));
		// 获取表单的显示标题；
		cell.setCellValue("其他");
		cell.setCellStyle(rowStyle);

		// 推荐人；
		Integer entryHasRelative = entry.getEntryHasRelative();
		// 有推荐人需多合并一行；
		if (entryHasRelative == null || entryHasRelative.intValue() == IEntryRelative.RELATIVE_NO) {
			// 列合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$B$" + (startRow + 4)));
		} else {
			// 列合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$B$" + (startRow + 5)));
		}

		// 是否有驾照；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("是否有驾照");
		cell.setCellStyle(rowStyle);

		// 是否有驾照；
		cell = contentRow.createCell((short) (3));
		// 有驾照还需显示驾龄；
		Integer entryHasLicence = entry.getEntryHasLicence();
		if (entryHasLicence == null || entryHasLicence.intValue() == 1) {
			cell.setCellValue("否");
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$K$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 4, 7);
		} else {
			cell.setCellValue("是");
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 4, 1);

			// 驾龄；
			cell = contentRow.createCell((short) (5));
			cell.setCellValue("驾龄");
			cell.setCellStyle(rowStyle);

			// 驾龄；
			cell = contentRow.createCell((short) (6));
			cell.setCellValue(entry.getEntryDriveAge());
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$K$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 7, 4);
		}

		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);
		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		// 兴趣爱好特长；
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("兴趣爱好特长");
		cell.setCellStyle(rowStyle);

		// 兴趣爱好内容；
		cell = contentRow.createCell((short) (3));
		cell.setCellValue(entry.getEntryInterest());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$K$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 4, 7);

		// 曾经病史；
		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);
		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);

		cell = contentRow.createCell((short) (2));
		cell.setCellValue("曾经病史");
		cell.setCellStyle(rowStyle);

		// 是否有曾经病史；
		cell = contentRow.createCell((short) (3));
		// 有病史需显示病史信息；
		Integer entryHasSick = entry.getEntryHasSick();
		if (entryHasSick == null || entryHasSick.intValue() == 0) {
			cell.setCellValue("否");
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$K$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 4, 7);
		} else {
			cell.setCellValue("是");
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 4, 1);

			// 病史描述；
			cell = contentRow.createCell((short) (5));
			cell.setCellValue("病史描述");
			cell.setCellStyle(rowStyle);

			// 驾龄；
			cell = contentRow.createCell((short) (6));
			cell.setCellValue(entry.getEntrySick());
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$K$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 7, 4);
		}

		// 求职途径；
		startRow++; //行+1
		String[] channelArray = new String[] {"BOSS直聘", "社交媒体", "离职再入职", "人才市场", "校园招聘", "猎头推荐", "内部推荐 ", "其他", "前程无忧", "智联招聘", "分子公司调岗"};
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(40);
		// 空白列；
		createBlankRow(contentRow, contentStyle, 1, 1);
		cell = contentRow.createCell((short) (2));
		cell.setCellValue("求职途径");
		cell.setCellStyle(rowStyle);
		Integer entryChannel = entry.getEntryChannel();
		cell = contentRow.createCell((short) (3));
		if (entryChannel == null || entryChannel < 0 || entryChannel > 10) {
			cell.setCellValue("");
		} else {
			cell.setCellValue(channelArray[entryChannel]);
		}
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 4, 1);

		// 求职途径名称；
		cell = contentRow.createCell((short) (5));
		cell.setCellValue("求职途径名称");
		cell.setCellStyle(rowStyle);

		cell = contentRow.createCell((short) (6));
		cell.setCellValue(entry.getEntryChannelName());
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$K$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 7, 4);

		// 有推荐人需显示推荐人信息；
		if (entryHasRelative != null && entryHasRelative.intValue() == IEntryRelative.RELATIVE_YES) {
			// 合并的序号需加1；
			startRow++;
			contentRow = sheet.createRow(startRow);
			contentRow.setHeightInPoints(40);
			// 空白列；
			createBlankRow(contentRow, contentStyle, 1, 1);

			// 推荐人姓名；
			cell = contentRow.createCell((short) (2));
			cell.setCellValue("推荐人姓名");
			cell.setCellStyle(rowStyle);

			cell = contentRow.createCell((short) (3));
			cell.setCellValue(basic.getEmpRelativeName());
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 4, 1);

			// 推荐人联系电话；
			cell = contentRow.createCell((short) (5));
			cell.setCellValue("推荐人联系电话");
			cell.setCellStyle(rowStyle);

			cell = contentRow.createCell((short) (6));
			cell.setCellValue(basic.getEmpRelativePhone());
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$H$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 7, 1);

			// 与推荐人关系；
			cell = contentRow.createCell((short) (8));
			cell.setCellValue("与推荐人关系");
			cell.setCellStyle(rowStyle);

			cell = contentRow.createCell((short) (9));
			cell.setCellValue(basic.getEmpRelativeRelation());
			cell.setCellStyle(contentStyle);
			// 行合并；
			sheet.addMergedRegion(CellRangeAddress.valueOf("$J$" + (startRow+1) + ":$K$" + (startRow+1)));
			// 空白列；
			createBlankRow(contentRow, contentStyle, 10, 1);
		}

		// 声明；
		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(60);
		cell = contentRow.createCell((short) (1));
		cell.setCellValue("本人承诺上述所填报的各项资料均属实，如有虚报或故意隐瞒，本人愿意接受贵公司的规章制度惩罚，接受贵公司与本人即时解除劳动合同，及承担由此带来的相应法律责任，并允许贵公司对以上内容进行调查、核实。");
		cell.setCellStyle(rowStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$K$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 2, 9);

		// 申请人签名；
		startRow++; //行+1
		contentRow = sheet.createRow(startRow);
		contentRow.setHeightInPoints(20);
		cell = contentRow.createCell((short) (1));
		cell.setCellValue("申请人签名");
		cell.setCellStyle(rowStyle);

		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$B$" + (startRow+1) + ":$C$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 2, 1);

		cell = contentRow.createCell((short) (3));
		cell.setCellValue("");
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + (startRow+1) + ":$E$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 4, 1);

		// 日期；
		cell = contentRow.createCell((short) (5));
		cell.setCellValue("日期");
		cell.setCellStyle(rowStyle);

		cell = contentRow.createCell((short) (6));
		cell.setCellValue(entry.getCreateTime() != null ? DateUtils.format(entry.getCreateTime(), "yyyy-MM-dd") : "");
		cell.setCellStyle(contentStyle);
		// 行合并；
		sheet.addMergedRegion(CellRangeAddress.valueOf("$G$" + (startRow+1) + ":$K$" + (startRow+1)));
		// 空白列；
		createBlankRow(contentRow, contentStyle, 7, 4);
		//设置列宽
		/*sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
		sheet.autoSizeColumn(6);
		sheet.autoSizeColumn(7);
		sheet.autoSizeColumn(8);
		sheet.autoSizeColumn(9);
		sheet.autoSizeColumn(10);*/
		try {
			workBook.write(outputStream);
		} catch (IOException e) {
			log.error("文件处理异常", e);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * 构建空白行；
	 * 
	 * @param contentRow：列对象；
	 * @param contentStyle：样式；
	 * @param start：起始位置；
	 * @param size：数量；
	 */
	private static Row createBlankRow(Row contentRow, CellStyle contentStyle, int start, int size) {
		Cell cell;
		for (int i = start; i < start + size; i++) {
			cell = contentRow.createCell((short) (i));
			cell.setCellValue("");
			cell.setCellStyle(contentStyle);
		}
		return contentRow;
	}

	/**
	 * 读取行数据；
	 *
	 * @param cell：行对象；
	 * @param formulaEvaluator：公式处理对象；
	 * @return ：处理完毕的值；
	 */
	public static String getValue(Cell cell, FormulaEvaluator formulaEvaluator) {
		if (cell != null && cell.getCellType() == CellType.FORMULA) {
			return String.valueOf(formulaEvaluator.evaluate(cell).getNumberValue());
		} else {
			return getValue(cell);
		}
	}

	/**
	 * 读取行数据；
	 *
	 * @param cell：行对象；
	 * @return ：处理完毕的值；
	 */
	public static String getValue(Cell cell) {
		if (cell == null) {
			return "";
		} else if (cell.getCellType() == CellType.BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == CellType.NUMERIC) {
			String value = "";
			// 检验是否为日期格式的数值类型
			if (DateUtil.isCellDateFormatted(cell)) {
				value = DateUtils.format(cell.getDateCellValue());
			} else {
				value = new DecimalFormat("0.##").format(cell.getNumericCellValue());
			}
			return value;
		} else if (cell.getCellType() == CellType.FORMULA) {
			return new DecimalFormat("0.##").format(cell.getNumericCellValue());
		} else {
			return cell.getStringCellValue().trim();
		}
	}

	//构建表单一项数据
	private static void buildFormOneField(Sheet sheet, Row contentRow, short cellIndex, String label,  Object value, CellStyle rowStyle, CellStyle contentStyle, String merged, int blankNum){
		Cell cell = contentRow.createCell(cellIndex);
		cell.setCellValue(label);
		cell.setCellStyle(rowStyle);
		cell = contentRow.createCell(cellIndex + 1);
		cell.setCellValue(value == null ? "" : String.valueOf(value));
		cell.setCellStyle(contentStyle);
		for(int i = 0; i < blankNum; i++){
			createBlankRow(contentRow, contentStyle, cellIndex + 2 + i, 1);//创建空白行
		}
		if(!StringUtils.isEmpty(merged)){
			sheet.addMergedRegion(CellRangeAddress.valueOf(merged));// 列合并；
		}
	}
	//构建表格单元格
	private static void buildTableCell(Sheet sheet, Row contentRow, short cellIndex, Object value,CellStyle style, String merged, int blankNum){
		Cell cell = contentRow.createCell(cellIndex);
		cell.setCellValue(value == null ? "" : String.valueOf(value));
		cell.setCellStyle(style);
		for(int i = 0; i < blankNum; i++){
			createBlankRow(contentRow, style, cellIndex + 1 + i, 1);//创建空白行
		}
		if(!StringUtils.isEmpty(merged)){
			sheet.addMergedRegion(CellRangeAddress.valueOf(merged));// 列合并；
		}
	}
	//打印借款审核详情
	public static String createBorrowFile(String templateName, String uploadDir, String webDir,List<Map> borrowList){
		String fileName = templateName + PrimaryKeyUtil.getStringUniqueKey();// 设置文件名称，使用UUID确保唯一；
		StringBuilder filePath = new StringBuilder();
		// 先创建目录；
		String dateDir = getCurrentDateDir();
		File dir = new File(uploadDir + dateDir + "download/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		filePath.append(uploadDir).append(dateDir).append("download/").append(fileName).append(".xls");
		String fullPath = filePath.toString();
		// 获取输出流；
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(fullPath);// 获取输出流；
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建一个workBook，对应一个Excel文件
			CellStyle rowStyle = getContentStyle(workBook, true);// 获取表格标题样式；
			CellStyle contentStyle = getContentStyle(workBook, false);// 获取表格内容样式；
			for(Map<String, Object> borrow : borrowList){
				Sheet sheet = workBook.createSheet(String.valueOf(borrow.get("modalTitle")));
				setTitleStyle(workBook, sheet, 9, String.valueOf(borrow.get("modalTitle")), 10);
				for (int i = 0; i < 9; i++) {
					sheet.setColumnWidth(i + 1, 15 * 256);
				}
				int startRow = 2;
				Row contentRow = sheet.createRow(startRow);
				Cell cell = contentRow.createCell((short)1);
				cell.setCellValue("借款信息");
				cell.setCellStyle(rowStyle);
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+9)));//列合并，借款信息为长列；
				buildFormOneField(sheet, contentRow, (short)2, "借款标题", borrow.get("title"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$J$"+(startRow+1), 6);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "借款编号", borrow.get("code"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1), 2);
				buildFormOneField(sheet, contentRow, (short)6, "借款类型", borrow.get("typeName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1), 2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "借款人", borrow.get("applyName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "借款人部门", borrow.get("deptName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人", borrow.get("accountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "收款人账号", borrow.get("accountBankNo"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人开户行", borrow.get("accountBankName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "期望借款日期", borrow.get("expertPayTime"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "期望借款金额", borrow.get("applyAmount"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "实际出款账户", borrow.get("outAccountName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际出款日期", borrow.get("payTime"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "实际出款金额", borrow.get("payAmount"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "借款状态", borrow.get("state"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "附件", borrow.get("affixName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "借款原因", borrow.get("remark"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$J$"+(startRow+1),6);

				if(borrow.get("auditTable") != null && ((List<Map<String, Object>>)borrow.get("auditTable")).size() > 0){
					List<Map<String, Object>> auditTable = (List<Map<String, Object>>)borrow.get("auditTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					cell = contentRow.createCell((short)1);
					cell.setCellValue("审核流程");
					cell.setCellStyle(rowStyle);
					sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+1+auditTable.size())));//列合并，借款信息为长列；
					buildTableCell(sheet, contentRow, (short)2, "审核节点", rowStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)4, "审核人", rowStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)6, "操作详情", rowStyle,"$G$"+(startRow+1)+":$H$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)8, "操作时间", rowStyle,"$I$"+(startRow+1)+":$J$"+(startRow+1),1);
					for(int i = 0; i < auditTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						buildTableCell(sheet, contentRow, (short)2, auditTable.get(i).get("name"), contentStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)4, auditTable.get(i).get("user"), contentStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)6, auditTable.get(i).get("descName"), contentStyle,"$G$"+(startRow+1)+":$H$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)8, auditTable.get(i).get("time"), contentStyle,"$I$"+(startRow+1)+":$J$"+(startRow+1),1);
					}
				}
			}
			fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			workBook.write(outputStream);
			return webDir + dateDir + "download/" + fullPath;
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
			return null;
		} catch (IOException e) {
			log.error("文件处理异常", e);
			return null;
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	//打印报销审核详情
	public static String createReimburseFile(String templateName, String uploadDir, String webDir,List<Map> reimburseList){
		String fileName = templateName + PrimaryKeyUtil.getStringUniqueKey();// 设置文件名称，使用UUID确保唯一；
		StringBuilder filePath = new StringBuilder();
		// 先创建目录；
		String dateDir = getCurrentDateDir();
		File dir = new File(uploadDir + dateDir + "download/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		filePath.append(uploadDir).append(dateDir).append("download/").append(fileName).append(".xls");
		String fullPath = filePath.toString();
		// 获取输出流；
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(fullPath);// 获取输出流；
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建一个workBook，对应一个Excel文件
			CellStyle rowStyle = getContentStyle(workBook, true);// 获取表格标题样式；
			CellStyle contentStyle = getContentStyle(workBook, false);// 获取表格内容样式；
			for(Map<String, Object> reimburse : reimburseList){
				Sheet sheet = workBook.createSheet(String.valueOf(reimburse.get("modalTitle")));
				setTitleStyle(workBook, sheet, 11, String.valueOf(reimburse.get("modalTitle")), 10);
				for (int i = 0; i < 11; i++) {
					sheet.setColumnWidth(i + 1, 15 * 256);
				}
				int detailNum = 0; //报销详情数
				if(reimburse.get("detailTable") != null && ((List<Map<String, Object>>)reimburse.get("detailTable")).size() > 0){
					detailNum = ((List<Map<String, Object>>)reimburse.get("detailTable")).size() + 1;
				}

				int startRow = 2;
				Row contentRow = sheet.createRow(startRow);
				Cell cell = contentRow.createCell((short)1);
				cell.setCellValue("报销信息");
				cell.setCellStyle(rowStyle);
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+11+detailNum)));//列合并，报销信息为长列；
				buildFormOneField(sheet, contentRow, (short)2, "报销标题", reimburse.get("title"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1), 8);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "报销编号", reimburse.get("code"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "报销时间", reimburse.get("applyTime"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "报销人", reimburse.get("applyName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1),3);
				buildFormOneField(sheet, contentRow, (short)7, "报销人部门", reimburse.get("deptName"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1),3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人", reimburse.get("accountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1),3);
				buildFormOneField(sheet, contentRow, (short)7, "收款人账号", reimburse.get("accountBankNo"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1),3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人开户行", reimburse.get("accountBankName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1),3);
				buildFormOneField(sheet, contentRow, (short)7, "报销状态", reimburse.get("state"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1),3);

				if(detailNum > 0){
					List<Map<String, Object>> detailTable = (List<Map<String, Object>>)reimburse.get("detailTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
					buildTableCell(sheet, contentRow, (short)2, "序号", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)3, "费用类型", rowStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)5, "用途", rowStyle,"$F$"+(startRow+1)+":$J$"+(startRow+1),4);
					buildTableCell(sheet, contentRow, (short)10, "金额(元)", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)11, "单据张数", rowStyle,"", 0);
					for(int i = 0; i < detailTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						if(detailTable.get(i) instanceof Reimbursement_d){
							buildTableCell(sheet, contentRow, (short)2, i+1, contentStyle,"", 0);
							buildTableCell(sheet, contentRow, (short)3, ((Reimbursement_d)detailTable.get(i)).getCostType(), contentStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
							buildTableCell(sheet, contentRow, (short)5, ((Reimbursement_d)detailTable.get(i)).getPurpose(), contentStyle,"$F$"+(startRow+1)+":$J$"+(startRow+1),4);
							buildTableCell(sheet, contentRow, (short)10, ((Reimbursement_d)detailTable.get(i)).getMoney(), contentStyle,"", 0);
							buildTableCell(sheet, contentRow, (short)11, ((Reimbursement_d)detailTable.get(i)).getNumberOfDocument(), contentStyle,"", 0);
						}else {
							buildTableCell(sheet, contentRow, (short)2, i+1, contentStyle,"", 0);
							buildTableCell(sheet, contentRow, (short)3, detailTable.get(i).get("costType"), contentStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
							buildTableCell(sheet, contentRow, (short)5, detailTable.get(i).get("purpose"), contentStyle,"$F$"+(startRow+1)+":$J$"+(startRow+1),4);
							buildTableCell(sheet, contentRow, (short)10, detailTable.get(i).get("money"), contentStyle,"", 0);
							buildTableCell(sheet, contentRow, (short)11, detailTable.get(i).get("numberOfDocument"), contentStyle,"", 0);
						}
					}
				}

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "应报销金额", reimburse.get("reimbursedMoney"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1),3);
				buildFormOneField(sheet, contentRow, (short)7, "实报销金额", reimburse.get("totalMoney"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1),3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "金额大写", reimburse.get("sumUpper"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1),3);
				buildFormOneField(sheet, contentRow, (short)7, "充抵借款", reimburse.get("unpaidLoan"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1),3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际出款账户", reimburse.get("outAccountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1),3);
				buildFormOneField(sheet, contentRow, (short)7, "实际出款日期", reimburse.get("payTime"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1),3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际出款金额", reimburse.get("payAmount"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1),8);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "附件", reimburse.get("affixName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1),8);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "报销原因", reimburse.get("remark"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1),8);

				if(reimburse.get("borrowTable") != null && ((List<Map<String, Object>>)reimburse.get("borrowTable")).size() > 0){
					List<Map<String, Object>> borrowTable = (List<Map<String, Object>>)reimburse.get("borrowTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					cell = contentRow.createCell((short)1);
					cell.setCellValue("借款抵充详情");
					cell.setCellStyle(rowStyle);
					sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+1+borrowTable.size())));//列合并，借款信息为长列；
					buildTableCell(sheet, contentRow, (short)2, "借款编号", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)3, "借款标题", rowStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)5, "借款类型", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)6, "借款人", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)7, "所属部门", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)8, "借款金额", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)9, "已还金额", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)10, "未还金额", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)11, "抵充金额", rowStyle,"", 0);
					for(int i = 0; i < borrowTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						buildTableCell(sheet, contentRow, (short)2, borrowTable.get(i).get("code"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)3,  borrowTable.get(i).get("title"), contentStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)5,  borrowTable.get(i).get("type"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)6,  borrowTable.get(i).get("applyName"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)7,  borrowTable.get(i).get("deptName"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)8,  borrowTable.get(i).get("applyAmount"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)9,  borrowTable.get(i).get("repayAmount"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)10,  borrowTable.get(i).get("remainAmount"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)11,  borrowTable.get(i).get("amount"), contentStyle,"", 0);
					}
				}
				if(reimburse.get("auditTable") != null && ((List<Map<String, Object>>)reimburse.get("auditTable")).size() > 0){
					List<Map<String, Object>> auditTable = (List<Map<String, Object>>)reimburse.get("auditTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					cell = contentRow.createCell((short)1);
					cell.setCellValue("审核流程");
					cell.setCellStyle(rowStyle);
					sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+1+auditTable.size())));//列合并，借款信息为长列；
					buildTableCell(sheet, contentRow, (short)2, "审核节点", rowStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)4, "审核人", rowStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)6, "操作详情", rowStyle,"$G$"+(startRow+1)+":$J$"+(startRow+1),3);
					buildTableCell(sheet, contentRow, (short)10, "操作时间", rowStyle,"$K$"+(startRow+1)+":$L$"+(startRow+1),1);
					for(int i = 0; i < auditTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						buildTableCell(sheet, contentRow, (short)2, auditTable.get(i).get("name"), contentStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)4, auditTable.get(i).get("user"), contentStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)6, auditTable.get(i).get("descName"), contentStyle,"$G$"+(startRow+1)+":$J$"+(startRow+1),3);
						buildTableCell(sheet, contentRow, (short)10, auditTable.get(i).get("time"), contentStyle,"$K$"+(startRow+1)+":$L$"+(startRow+1),1);
					}
				}
			}
			fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			workBook.write(outputStream);
			return webDir + dateDir + "download/" + fullPath;
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
			return null;
		} catch (IOException e) {
			log.error("文件处理异常", e);
			return null;
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	//打印退款审核详情
	public static String createRefundFile(String templateName, String uploadDir, String webDir,List<Map> refundList){
		String fileName = templateName + PrimaryKeyUtil.getStringUniqueKey();// 设置文件名称，使用UUID确保唯一；
		StringBuilder filePath = new StringBuilder();
		String dateDir = getCurrentDateDir();
		// 先创建目录；
		File dir = new File(uploadDir + dateDir + "download/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		filePath.append(uploadDir).append(dateDir).append("download/").append(fileName).append(".xls");
		String fullPath = filePath.toString();
		// 获取输出流；
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(fullPath);// 获取输出流；
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建一个workBook，对应一个Excel文件
			CellStyle rowStyle = getContentStyle(workBook, true);// 获取表格标题样式；
			CellStyle contentStyle = getContentStyle(workBook, false);// 获取表格内容样式；
			for(Map<String, Object> refund : refundList){
				Sheet sheet = workBook.createSheet(String.valueOf(refund.get("modalTitle")));
				setTitleStyle(workBook, sheet, 9, String.valueOf(refund.get("modalTitle")), 10);
				for (int i = 0; i < 9; i++) {
					sheet.setColumnWidth(i + 1, 15 * 256);
				}
				int startRow = 2;
				Row contentRow = sheet.createRow(startRow);
				Cell cell = contentRow.createCell((short)1);
				cell.setCellValue("退款信息");
				cell.setCellStyle(rowStyle);
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+10)));//列合并，借款信息为长列；
				buildFormOneField(sheet, contentRow, (short)2, "退款标题", refund.get("title"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$J$"+(startRow+1), 6);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "退款编号", refund.get("code"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1), 2);
				buildFormOneField(sheet, contentRow, (short)6, "退款类型", refund.get("type"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1), 2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "退款人", refund.get("applyName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "退款人部门", refund.get("deptName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "客户公司", refund.get("custCompanyName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "联系人", refund.get("custName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人", refund.get("accountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "收款人账号", refund.get("accountBankNo"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人开户行", refund.get("accountBankName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "期望付款日期", refund.get("expertPayTime"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "申请金额", refund.get("applyAmount"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "退款状态", refund.get("state"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际出款账户", refund.get("outAccountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "实际出款日期", refund.get("payTime"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际出款金额", refund.get("payAmount"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$F$"+(startRow+1),2);
				buildFormOneField(sheet, contentRow, (short)6, "附件", refund.get("affixName"), rowStyle, contentStyle, "$H$"+(startRow+1)+":$J$"+(startRow+1),2);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "退款备注", refund.get("remark"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$J$"+(startRow+1),6);

				if(refund.get("auditTable") != null && ((List<Map<String, Object>>)refund.get("auditTable")).size() > 0){
					List<Map<String, Object>> auditTable = (List<Map<String, Object>>)refund.get("auditTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					cell = contentRow.createCell((short)1);
					cell.setCellValue("审核流程");
					cell.setCellStyle(rowStyle);
					sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+1+auditTable.size())));//列合并，借款信息为长列；
					buildTableCell(sheet, contentRow, (short)2, "审核节点", rowStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)4, "审核人", rowStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)6, "操作详情", rowStyle,"$G$"+(startRow+1)+":$H$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)8, "操作时间", rowStyle,"$I$"+(startRow+1)+":$J$"+(startRow+1),1);
					for(int i = 0; i < auditTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						buildTableCell(sheet, contentRow, (short)2, auditTable.get(i).get("name"), contentStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)4, auditTable.get(i).get("user"), contentStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)6, auditTable.get(i).get("descName"), contentStyle,"$G$"+(startRow+1)+":$H$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)8, auditTable.get(i).get("time"), contentStyle,"$I$"+(startRow+1)+":$J$"+(startRow+1),1);
					}
				}
			}
			fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			workBook.write(outputStream);
			return webDir + dateDir + "download/" + fullPath;
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
			return null;
		} catch (IOException e) {
			log.error("文件处理异常", e);
			return null;
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	//打印请款审核详情
	public static String createOutgoFile(String templateName, String uploadDir, String webDir,List<Map> outgoList){
		String fileName = templateName + PrimaryKeyUtil.getStringUniqueKey();// 设置文件名称，使用UUID确保唯一；
		StringBuilder filePath = new StringBuilder();
		String dateDir = getCurrentDateDir();
		// 先创建目录；
		File dir = new File(uploadDir + dateDir + "download/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		filePath.append(uploadDir).append(dateDir).append("download/").append(fileName).append(".xls");
		String fullPath = filePath.toString();
		// 获取输出流；
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(fullPath);// 获取输出流；
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建一个workBook，对应一个Excel文件
			CellStyle rowStyle = getContentStyle(workBook, true);// 获取表格标题样式；
			CellStyle contentStyle = getContentStyle(workBook, false);// 获取表格内容样式；
			for(Map<String, Object> outgo : outgoList){
				Sheet sheet = workBook.createSheet(String.valueOf(outgo.get("modalTitle")));
				setTitleStyle(workBook, sheet, 11, String.valueOf(outgo.get("modalTitle")), 10);
				for (int i = 0; i < 11; i++) {
					sheet.setColumnWidth(i + 1, 15 * 256);
				}

				int rowNum = 13;
				//是否开票：1-是
				if("1".equals(String.valueOf(outgo.get("invoiceFlag")))){
					rowNum = rowNum + 2;
				}
				int startRow = 2;
				Row contentRow = sheet.createRow(startRow);
				Cell cell = contentRow.createCell((short)1);
				cell.setCellValue("请款信息");
				cell.setCellStyle(rowStyle);
				sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow + rowNum)));//列合并，报销信息为长列；
				buildFormOneField(sheet, contentRow, (short)2, "请款标题", outgo.get("title"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1), 8);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "请款编号", outgo.get("code"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "发布日期筛选", outgo.get("timeScale"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "请款人", outgo.get("applyName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "请款人部门", outgo.get("deptName"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "供应商名称", outgo.get("supplierName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "联系人", outgo.get("supplierContactor"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人", outgo.get("accountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "收款人账号", outgo.get("accountBankNo"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "收款人开户行", outgo.get("accountBankName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "期望付款日期", outgo.get("expertPayTime"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "应付合计", outgo.get("outgoSum"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "备用金额", outgo.get("fundAmount"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际请款成本", outgo.get("actualCost"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "请款成本抹零", outgo.get("costEraseAmount"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际请款金额", outgo.get("applyAmount"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "实际请款抹零", outgo.get("outgoEraseAmount"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际抹零金额", outgo.get("actualCombined"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "实际出款日期", outgo.get("payTime"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "实际出款账户", outgo.get("outAccountName"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "实际出款金额", outgo.get("payAmount"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "请款状态", outgo.get("state"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
				buildFormOneField(sheet, contentRow, (short)7, "是否开票", outgo.get("invoiceFlagName"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

				//是否开票：1-是
				if("1".equals(String.valueOf(outgo.get("invoiceFlag")))){
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
					buildFormOneField(sheet, contentRow, (short)2, "进票抬头", outgo.get("invoiceRise"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$G$"+(startRow+1), 3);
					buildFormOneField(sheet, contentRow, (short)7, "税点", outgo.get("outgoTax"), rowStyle, contentStyle, "$I$"+(startRow+1)+":$L$"+(startRow+1), 3);

					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
					buildFormOneField(sheet, contentRow, (short)2, "税金", outgo.get("taxAmount"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1), 8);
				}

				startRow++;//行+1
				contentRow = sheet.createRow(startRow);
				createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
				buildFormOneField(sheet, contentRow, (short)2, "请款备注", outgo.get("remark"), rowStyle, contentStyle, "$D$"+(startRow+1)+":$L$"+(startRow+1), 8);

				if(outgo.get("borrowTable") != null && ((List<Map<String, Object>>)outgo.get("borrowTable")).size() > 0){
					List<Map<String, Object>> borrowTable = (List<Map<String, Object>>)outgo.get("borrowTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					cell = contentRow.createCell((short)1);
					cell.setCellValue("备用金扣除详情");
					cell.setCellStyle(rowStyle);
					sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+1+borrowTable.size())));//列合并，借款信息为长列；
					buildTableCell(sheet, contentRow, (short)2, "借款编号", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)3, "借款标题", rowStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)5, "借款类型", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)6, "借款人", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)7, "所属部门", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)8, "借款金额", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)9, "已还金额", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)10, "未还金额", rowStyle,"", 0);
					buildTableCell(sheet, contentRow, (short)11, "备用金金额", rowStyle,"", 0);
					for(int i = 0; i < borrowTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						buildTableCell(sheet, contentRow, (short)2, borrowTable.get(i).get("code"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)3,  borrowTable.get(i).get("title"), contentStyle,"$D$"+(startRow+1)+":$E$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)5,  borrowTable.get(i).get("type"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)6,  borrowTable.get(i).get("apply_name"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)7,  borrowTable.get(i).get("dept_name"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)8,  borrowTable.get(i).get("apply_amount"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)9,  borrowTable.get(i).get("repay_amount"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)10,  borrowTable.get(i).get("remain_amount"), contentStyle,"", 0);
						buildTableCell(sheet, contentRow, (short)11,  borrowTable.get(i).get("amount"), contentStyle,"", 0);
					}
				}
				if(outgo.get("auditTable") != null && ((List<Map<String, Object>>)outgo.get("auditTable")).size() > 0){
					List<Map<String, Object>> auditTable = (List<Map<String, Object>>)outgo.get("auditTable");
					startRow++;//行+1
					contentRow = sheet.createRow(startRow);
					cell = contentRow.createCell((short)1);
					cell.setCellValue("审核流程");
					cell.setCellStyle(rowStyle);
					sheet.addMergedRegion(CellRangeAddress.valueOf("$B$"+(startRow+1)+":$B$"+(startRow+1+auditTable.size())));//列合并，借款信息为长列；
					buildTableCell(sheet, contentRow, (short)2, "审核节点", rowStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)4, "审核人", rowStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
					buildTableCell(sheet, contentRow, (short)6, "操作详情", rowStyle,"$G$"+(startRow+1)+":$J$"+(startRow+1),3);
					buildTableCell(sheet, contentRow, (short)10, "操作时间", rowStyle,"$K$"+(startRow+1)+":$L$"+(startRow+1),1);
					for(int i = 0; i < auditTable.size(); i++){
						startRow++;//行+1
						contentRow = sheet.createRow(startRow);
						createBlankRow(contentRow, contentStyle, 1, 1);//创建空白行
						buildTableCell(sheet, contentRow, (short)2, auditTable.get(i).get("name"), contentStyle,"$C$"+(startRow+1)+":$D$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)4, auditTable.get(i).get("user"), contentStyle,"$E$"+(startRow+1)+":$F$"+(startRow+1),1);
						buildTableCell(sheet, contentRow, (short)6, auditTable.get(i).get("descName"), contentStyle,"$G$"+(startRow+1)+":$J$"+(startRow+1),3);
						buildTableCell(sheet, contentRow, (short)10, auditTable.get(i).get("time"), contentStyle,"$K$"+(startRow+1)+":$L$"+(startRow+1),1);
					}
				}
			}
			fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
			workBook.write(outputStream);
			return webDir + dateDir + "download/" + fullPath;
		} catch (FileNotFoundException e) {
			log.error("文件未找到", e);
			return null;
		} catch (IOException e) {
			log.error("文件处理异常", e);
			return null;
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	//获取年/月/日目录结构
	private static String getCurrentDateDir(){
		return DateUtils.format(new Date(), "yyyy-MM/");
	}
}