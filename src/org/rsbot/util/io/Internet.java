package org.rsbot.util.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Internet {

	public static URLConnection createConnection(URL url, String accept,
			String accept_charset, String accept_encoding,
			String accept_language, String connection, String host,
			String keep_alive, String referer, String user_agent) {
		try {
			URLConnection uc = url.openConnection();

			if (accept != null)
				uc.addRequestProperty("Accept", accept);
			if (accept_charset != null)
				uc.addRequestProperty("Accept-Charset", accept_charset);
			if (accept_encoding != null)
				uc.addRequestProperty("Accept-Encoding", accept_encoding);
			if (accept_language != null)
				uc.addRequestProperty("Accept-Language", accept_language);
			if (connection != null)
				uc.addRequestProperty("Connection", connection);
			if (host != null)
				uc.addRequestProperty("Host", host);
			if (keep_alive != null)
				uc.addRequestProperty("Keep-Alive", keep_alive);
			if (referer != null)
				uc.addRequestProperty("Referer", referer);
			if (user_agent != null)
				uc.addRequestProperty("User-Agent", user_agent);

			return uc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static URLConnection createConnection(URL url, String host,
			String referer) {
		return createConnection(
				url,
				"text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5",
				"ISO-8859-1,utf-8;q=0.7,*;q=0.7",
				"gzip,deflate",
				"en-gb,en;q=0.5",
				"keep-alive",
				host,
				"300",
				referer,
				"Mozilla/4.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.0.6) Gecko/20060728 Firefox/1.5.0.6");
	}

	public static byte[] readPageBytes(URL url, String host, String referer) {

		try {

			URLConnection uc = Internet.createConnection(url, host, referer);
			uc.setConnectTimeout(20000);

			if (uc != null) {
				InputStream is = uc.getInputStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				ArrayList<Byte> bytes = new ArrayList<Byte>();

				byte data;

				while ((data = (byte) br.read()) != -1)
					bytes.add(data);

				byte[] result = new byte[bytes.size()];

				for (int i = 0; i < result.length; i++)
					result[i] = bytes.get(i);

				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new byte[0];
	}

	public static String readPage(URL url, String host, String referer) {
		return new String(readPageBytes(url, host, referer));
	}

}
