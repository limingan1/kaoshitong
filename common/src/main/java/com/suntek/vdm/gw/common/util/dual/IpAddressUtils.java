package com.suntek.vdm.gw.common.util.dual;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 判断IP地址的类型及是否合法
 * 
 * @author chen
 *
 */
public class IpAddressUtils {

	// 标准IPv4地址的正则表达式：
	private static final Pattern IPV4_REGEX = Pattern
			.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	// 无全0块，标准IPv6地址的正则表达式
	private static final Pattern IPV6_STD_REGEX = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

	// 非边界压缩正则表达式
	private static final Pattern IPV6_COMPRESS_REGEX = Pattern
			.compile("^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$");

	// 边界压缩情况正则表达式
	private static final Pattern IPV6_COMPRESS_REGEX_BORDER = Pattern.compile(
			"^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$");

	// 判断是否为合法IPv4地址
	public static boolean isIPv4Address(final String input) {
		return IPV4_REGEX.matcher(input).matches();
	}

	// 判断是否为合法IPv6地址
	public static boolean isIPv6Address(final String input) {
		int NUM = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == ':')
				NUM++;
		}
		// 合法IPv6地址中不可能有多余7个的冒号(:)
		if (NUM > 7)
			return false;
		if (IPV6_STD_REGEX.matcher(input).matches()) {
			return true;
		}
		// 冒号(:)数量等于7有两种情况：无压缩、边界压缩，所以需要特别进行判断
		if (NUM == 7) {
			return IPV6_COMPRESS_REGEX_BORDER.matcher(input).matches();
		}
		// 冒号(:)数量小于七，使用于飞边界压缩的情况
		else {
			return IPV6_COMPRESS_REGEX.matcher(input).matches();
		}
	}
	
	/**
	 * 获取本机IP
	 * @return
	 */
	public static String getHostAddress() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getLocalHost();

			// String localname = inetAddress.getHostName(); // 本机名称
			String localip = inetAddress.getHostAddress(); // 本机的ip
			return localip;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 获取所有IPv4的IP地址
	 * @return
	 */
	public static List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }
	
}
