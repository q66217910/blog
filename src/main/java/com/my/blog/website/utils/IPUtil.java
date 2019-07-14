package com.my.blog.website.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


public class IPUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPUtil.class);

    //	private static final Logger log = LoggerFactory.getLogger(IPUtil.class);
    public static String getRequestIp(HttpServletRequest request) {
        String ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        } else {
            LOGGER.debug("HTTP_X_FORWARDED_FOR ==> {}", ip);
        }
        if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            LOGGER.debug("Proxy-Client-IP ==> {}", ip);
        } else {
            LOGGER.debug("X-Forwarded-For ==> {}", ip);
        }
        if (ip != null && !"".equals(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP。
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !"".equals(ip) && !"unknown".equalsIgnoreCase(ip)) {
            LOGGER.debug("X-Real-IP ==> {}", ip);
            return ip;
        }
        return request.getRemoteAddr();
    }

//	public static String getCityForIp(String ip) {
//		String city = null;
//		HttpClient client = new HttpClient("https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php",false, false);
//		Map<String, Object> map = new HashMap<String, Object>();
////		map.put("format", "js");
////		map.put("ip", ip);
//		map.put("resource_id", "6006");
//		map.put("query", ip);
//		String sb;
//		try {
//			sb = client.getString("", map);
////			Map<String, String> json = JsonUtil.toObject(sb.split("=")[1], Map.class);
////			JSONObject json =  new JSONObject(sb);
////			String data = JsonUtil.toJson(json.get("data")).replace("[", "").replace("]", "");
////			Map<String, String> dataJson = JsonUtil.toObject(data, Map.class);
////			city = dataJson.get("location").toString();
//			return ip + "(" + city + ")";
//		} catch (Exception e) {
//			System.out.println("IP地址：" + ip + "--------------------------无法解析");
//			return ip;
//		}
//	}

    public static void main(String[] args) {
//		System.out.println(IPUtil.getCityForIp("171.214.230.42"));
    }

}