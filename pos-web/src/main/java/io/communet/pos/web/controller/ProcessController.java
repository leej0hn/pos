package io.communet.pos.web.controller;

import com.alibaba.fastjson.JSON;
import io.communet.pos.common.dto.OrderInfo;
import io.communet.pos.common.dto.PosDateResponse;
import io.communet.pos.utils.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2017/01/11
 * <p>Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/pos/api/process")
public class ProcessController {

    //上传数据成功
    public static final String SUCCESS = "SUCCESS";
    //数据不完整请重新上传
    public static final String INCOMPLETE = "INCOMPLETE";
    //找不到楼层编号，请重新上传
    public static final String INVALID_FLOOR = "INVALID_FLOOR";
    //找不到商铺号，请重新上传
    public static final String INVALID_SHOP_NO = "INVALID_SHOP_NO";
    //找系统合同编号，请重新上传
    public static final String INVALID_CONTRACTION_NO = "INVALID_CONTRACTION_NO";
    //统计交易日期出错，请重新上传
    public static final String INVALID_TXN_DATE = "INVALID_TXN_DATE";
    //数据不完整（格式出错），请重新上传
    public static final String INCOMPLETE_INVALID_FILE_FORMAT = "INCOMPLETE(INVALID_FILE_FORMAT)";
    //找不到商铺信息，请重新上传
    public static final String SHOP_INFO_NOT_FOUND = "SHOP_INFO_NOT_FOUND";
    //系统资料更新中，请重新上传
    public static final String DATA_SYNC_IN_PROGRESS = "DATA_SYNC_IN_PROGRESS";

    @Value("${web.floor:}")
    private String floor;

    @Value("${web.shopNo:}")
    private String shopNo;

    @Value("${web.contracNo:}")
    private String contracNo;

    @Value("${web.getDataUrl:}")
    private String getDataUrl;

    @Value("${web.updateLoadUrl:}")
    private String updateLoadUrl;

    @Value("${web.dataPath}")
    private String dataPath;

    private boolean checkDateTime(String date) {
        if (TextUtils.isEmpty(date)) {
            // TODO: 2017/2/28  "请选择日期";
            return false;
        } else {
            String[] split = date.split("-");
            if (split.length != 3) {
                // TODO: 2017/2/28  "日期：" + date + "，格式出错";
                return false;
            }
        }
        return true;
    }

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public String get(String dateTime) {
        if (!checkDateTime(dateTime)) {
            return "日期为空或格式不对";
        }
        String url = getDataUrl + dateTime;
        List<OrderInfo> orderInfoSortedList = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            orderInfoSortedList.add(new OrderInfo(dateTime + " " + getHourString(i) + ":00:00"));
        }
        try {
            PosDateResponse posDateResponse = JSON.parseObject(OkHttpUtil.get(url), PosDateResponse.class);
            if (posDateResponse != null) {
                List<OrderInfo> msg = posDateResponse.getMsg();
                if (msg != null && msg.size() > 0) {
                    for (OrderInfo temp : msg) {
                        for (int i = 0; i < 24; i++) {
                            if (getHourString(i).equals(temp.getOrderTime().substring(11, 13))) {
                                orderInfoSortedList.get(i).accumulativeAmount(temp.getOrderAmount());
                            }
                        }
                    }
                }
            } else {
                return "获取数据失败，请检查您当前的网络情况后再重新操作";
            }
            log.info("posDateResponse : " + posDateResponse);
            log.info("orderInfoSortedList : " + orderInfoSortedList);
        } catch (Exception e) {
            return "获取数据失败，请检查您当前的网络情况后再重新操作";
        }

        try {
            checkDataPath();
            File file = new File(getPathName(dateTime));
            if (file.exists()) {
                file.delete();
            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    if (!parentFile.mkdirs()) {
                        return "创建文件夹" + parentFile.getPath() + "失败,请重新配置数据保存的文件夹路径或手动创建文件夹";
                    }
                }
            }
            boolean newFile = file.createNewFile();
            if (!newFile) {
                return "创建文件" + file.getPath() + "失败,已经存在同名文件，请返回重";
            }

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = createXmlDocument(dateTime, orderInfoSortedList, documentBuilder);
            saveXmlDocument(file, document);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            return "获取数据失败，创建xml失败";
        } catch (IOException e) {
            log.error(e.getMessage());
            return "获取数据失败，无法创建文件";
        }
        return "获取数据成功";
    }

    private void checkDataPath() {
        if (TextUtils.isEmpty(dataPath)) {
            dataPath = System.getProperty("user.dir") + File.separator + "posData";
        }
        log.info("数据保存路径：" + dataPath);
    }

    private String getPathName(String dateTime) {
        String[] split = dateTime.split("-");
        String yearStr = split[0];
        String monthStr = split[1];
        return dataPath + File.separator + yearStr + File.separator + monthStr + File.separator + dateTime + ".xml";
    }

    /**
     * 保存xml文档
     *
     * @param file
     * @param document
     * @return
     */
    private void saveXmlDocument(File file, Document document) {
        TransformerFactory tf = TransformerFactory.newInstance();
        PrintWriter pw = null;
        FileOutputStream out = null;
        try {
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            out = new FileOutputStream(file.getAbsoluteFile());
            pw = new PrintWriter(out);
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
            log.info("文件保存成功," + file.getPath());
        } catch (FileNotFoundException | TransformerException | IllegalArgumentException e) {
            log.error(e.getMessage());
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 按照文档格式创建xml文档
     *
     * @param dateTime
     * @param orderInfoSortedList
     * @param documentBuilder
     * @return
     */
    private Document createXmlDocument(String dateTime, List<OrderInfo> orderInfoSortedList, DocumentBuilder documentBuilder) {
        Document document = documentBuilder.newDocument();
        Element sales = document.createElement("Sales");
        document.appendChild(sales);

        for (int i = 0; i < orderInfoSortedList.size(); i++) {
            // 增加一个元素节点
            CDATASection cdataSection;
            Element hourlySales = document.createElement("HourlySales");
            sales.appendChild(hourlySales);

            Element element = document.createElement("Floor");
            cdataSection = document.createCDATASection(floor);
            element.appendChild(cdataSection);
            hourlySales.appendChild(element);

            element = document.createElement("ShopNo");
            cdataSection = document.createCDATASection(shopNo);
            element.appendChild(cdataSection);
            hourlySales.appendChild(element);

            element = document.createElement("ContractNo");
            cdataSection = document.createCDATASection(contracNo);
            element.appendChild(cdataSection);
            hourlySales.appendChild(element);

            element = document.createElement("TransactionDateFr");
            cdataSection = document.createCDATASection(dateTime + " " + getHourString(i) + ":00:00");
            element.appendChild(cdataSection);
            hourlySales.appendChild(element);

            element = document.createElement("TransactionDateTo");
            cdataSection = document.createCDATASection(dateTime + " " + getHourString(i) + ":59:59");
            element.appendChild(cdataSection);
            hourlySales.appendChild(element);

            element = document.createElement("Currency");
            element.setTextContent("RMB");
            hourlySales.appendChild(element);

            element = document.createElement("TotalAmt");
            element.setTextContent(orderInfoSortedList.get(i).getOrderAmount() + "");
            hourlySales.appendChild(element);

            element = document.createElement("ItemQty");
            element.setTextContent(orderInfoSortedList.get(i).getOrderCount() + "");
            hourlySales.appendChild(element);
        }
        return document;
    }

    private String getHourString(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String queryData(String dateTime) {
        if (!checkDateTime(dateTime)) {
            return "日期为空或格式不对";
        }
        File file = new File(getPathName(dateTime));
        if (file.exists()) {
            float totalAmt = 0;
            int itemQty = 0;
            DocumentBuilder domBuilder = null;
            InputStream input = null;
            try {
                domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                input = new FileInputStream(file.getPath());
                Document doc = domBuilder.parse(input);

                Element root = doc.getDocumentElement();
                NodeList hourlySalesList = root.getChildNodes();
                if (hourlySalesList != null) {
                    for (int i = 0; i < hourlySalesList.getLength(); i++) {
                        Node hourlySales = hourlySalesList.item(i);
                        for (Node node = hourlySales.getFirstChild(); node != null; node = node.getNextSibling()) {
                            if (node.getNodeType()  == Node.ELEMENT_NODE){
                                if (node.getNodeName().equals("TotalAmt")) {
                                    String value = node.getFirstChild().getNodeValue();
                                    try {
                                        float temp = Float.valueOf(value);
                                        totalAmt += temp;
                                    }catch (Exception e){
                                        log.error(e.getMessage());
                                    }
                                }else if (node.getNodeName().equals("ItemQty")) {
                                    String value = node.getFirstChild().getNodeValue();
                                    try {
                                        int temp = Integer.valueOf(value);
                                        itemQty += temp;
                                    }catch (Exception e){
                                        log.error(e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
                log.info("总金额TotalAmt=" + totalAmt, "总数量ItemQty=" + itemQty);
//                TransformerFactory tf = TransformerFactory.newInstance();
//                Transformer t = tf.newTransformer();
//                t.setOutputProperty("encoding", "GB23121");//解决中文问题，试过用GBK不行
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                t.transform(new DOMSource(doc), new StreamResult(bos));
                return"总金额：" + totalAmt +  "       总数量：" + itemQty;
            } catch (ParserConfigurationException | SAXException e) {
                log.error(e.getMessage());
            } catch (FileNotFoundException e) {
                return "不存在该日期的数据，请先获取数据";
            } catch (IOException e) {
                log.error(e.getMessage());
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
            return "数据错误，请重试";
        }else {
            return "不存在该日期的数据，请先获取数据";
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadData(String dateTime) {
        if (!checkDateTime(dateTime)) {
            return "日期为空或格式不对";
        }
        checkDataPath();
        File file = new File(getPathName(dateTime));
        if (file.exists()) {
            try {
                String uploadResult = OkHttpUtil.upload(file, updateLoadUrl);
                log.info(uploadResult);
                if ((createResultTag(SUCCESS)).equals(uploadResult.trim())) {
                    return "上传成功";
                } else {
                    return uploadResult;
                }

//                else if ((createResultTag(SHOP_INFO_NOT_FOUND)).equals(uploadResult)) {
//
//                } else if ((createResultTag(INCOMPLETE)).equals(uploadResult)) {
//
//                } else if ((createResultTag(INVALID_FLOOR)).equals(uploadResult)) {
//
//                } else if ((createResultTag(INVALID_SHOP_NO)).equals(uploadResult)) {
//
//                } else if ((createResultTag(INVALID_CONTRACTION_NO)).equals(uploadResult)) {
//
//                } else if ((createResultTag(INVALID_TXN_DATE)).equals(uploadResult)) {
//
//                } else if ((createResultTag(INCOMPLETE_INVALID_FILE_FORMAT)).equals(uploadResult)) {
//
//                } else if ((createResultTag(DATA_SYNC_IN_PROGRESS)).equals(uploadResult)) {
//
//                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return "上传失败，请返回重试";
            }
            // return "上传成功";
        } else {
            return "上传失败，请选择正确路径下的数据文件，如：" + dataPath + File.separator + "2011" + File.separator + "02" + File.separator + "2011-02-08.xml";
        }
    }

    private String createResultTag(String result) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?><PostDailySalesResult>" + result + "</PostDailySalesResult>".trim();
    }
}
