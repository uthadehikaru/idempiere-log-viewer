package id.rekaestudigital.idempiere.logviewer.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.ecs.xhtml.body;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.head;
import org.apache.ecs.xhtml.html;
import org.apache.ecs.xhtml.span;
import org.compiere.util.Ini;

public class LogHandler extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1695538254258463076L;
	
	private String baseUrl = "/log-viewer";


	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    response.setHeader("Cache-Control", "no-cache");
	    response.setContentType("text/html; charset=UTF-8");
	
	    html m_html = new html();
	    head m_head = new head();
	    body m_body = new body();
	    m_head.addElement("<title>IDempiere Log Viewer</title>");
	    m_head.addElement("<link href=\"https://cdn.jsdelivr.net/npm/daisyui@4.12.23/dist/full.min.css\" rel=\"stylesheet\" type=\"text/css\" />");
	    m_head.addElement("<script src=\"https://cdn.tailwindcss.com\"></script>");
	    m_head.addElement("<script src=\"https://cdn.jsdelivr.net/npm/theme-change@2.0.2/index.js\"></script>");
	    m_html.addElement(m_head);
	
	    div navbar = new div();
	    navbar.setClass("navbar bg-base-100");
	    navbar.addElement("<div class=\"flex-1\">"
	    		+ "    <a href=\""+ baseUrl +"\" class=\"text-xl hover:underline\">IDempiere Log Viewer</a>"
	    		+ "  </div>");
	    navbar.addElement("<div class=\"flex-none\">"
	    		+ "<button type=\"button\" class=\"btn btn-sm bg-white text-gray dark:text-black mr-2\" data-set-theme=\"light\" id=\"light\">Light</button>"
	    		+ "<button type=\"button\" class=\"btn btn-sm bg-gray-500 text-white\" data-set-theme=\"dark\">Dark</button>"
	    		+ "  </div>");
	    m_body.addElement(navbar);
	
	    div container = new div();
	    container.setClass("flex");
	
	    div leftPanel = new div();
	    leftPanel.setClass("w-1/6 p-2 grid grid-cols-1 gap-2");
	    leftPanel.addElement("<a href=\"/\" class=\"text-sm hover:underline\">back to IDempiere Home</a>");
	
	    String path = Ini.getAdempiereHome() + File.separator + "log";
	    File dir = new File(path);
	    File[] files = dir.listFiles();
	    Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));
	    File log = null;
	    String fileName = request.getParameter("file");
	    if (fileName != null) {
	        log = new File(path + File.separator + fileName);
	    }
	    for (File file : files) {
	        if (log == null)
	            log = file;
	        long fileSizeInBytes = file.length();
	        String size = humanReadableByteCountSI(fileSizeInBytes);
	
	        span fileSize = new span();
	        fileSize.setClass("text-xs");
	        fileSize.addElement(size);
	        div card = new div();
	        if (log.getName().equals(file.getName()))
	            card.setClass("border rounded-md bg-success text-white p-2 flex justify-between");
	        else
	            card.setClass("border rounded-md border-success hover:bg-success hover:text-white p-2 flex justify-between");
	        card.addElement("<a href=\""+baseUrl+"?file=" + file.getName() + "\" class=\"text-sm font-bold\">" + file.getName() + " [" + fileSize + "]</a>");
	        card.addElement("<a href=\""+ baseUrl +"/download/" + file.getName() + "\" class=\"text-sm hover:font-bold\"><svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"1.5\" stroke=\"currentColor\" class=\"size-4\">"
	                + "  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3\" />"
	                + "</svg>"
	                + "</a>");
	        leftPanel.addElement(card);
	    }
	    container.addElement(leftPanel);
	
	    div rightPanel = new div();
	    rightPanel.setClass("w-5/6 p-2");
	    rightPanel.addElement("  <table class=\"table table-zebra\">"
	            + "    <!-- head -->"
	            + "    <thead>"
	            + "      <tr>");
	
	    String[] columnNames = new String[]{"Time", "Message"};
	    for (String columnName : columnNames) {
	        rightPanel.addElement("<th>"
	                + columnName
	                + "</th>");
	    }
	
	    rightPanel.addElement("</tr>"
	            + "</thead>"
	            + "    <tbody>");
	
	    try (BufferedReader br = new BufferedReader(new FileReader(log))) {
	        String line;
	        int index = 0;
	        String lastTime = "";
	        StringBuilder message = new StringBuilder();
	        List<String> logEntries = new ArrayList<String>();
	        while ((line = br.readLine()) != null) {
	            if (index == 0) {
	                index++;
	                continue;
	            }
	            String checkTime = "";
	            if (line.length() > 12) {
	                checkTime = line.substring(0, 12);
	            }
	            if (isTimeFormat(checkTime)) {
	                if (message.length() > 0) {
	                    logEntries.add(lastTime + " " + message.toString());
	                }
	                lastTime = checkTime;
	                message.setLength(0);
	                message.append(line.substring(13));
	            } else {
	                message.append(" ").append(line);
	            }
	            index++;
	        }
	        if (message.length() > 0) {
	            logEntries.add(lastTime + " " + message.toString());
	        }

	        logEntries.sort((e1, e2) -> e2.substring(0, 12).compareTo(e1.substring(0, 12)));

	        for (String entry : logEntries) {
	            String time = entry.substring(0, 12);
	            String msg = entry.substring(13);
	            rightPanel.addElement("<tr>"
	                    + "<td class=\"flex alignt-top\">" + time + "</td>"
	                    + "<td>" + msg + "</td>"
	                    + "</tr>");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	
	    rightPanel.addElement("    </tbody>"
	            + "  </table>"
	            + "</div>");
	    container.addElement(rightPanel);
	
	    m_body.addElement(container);
	
	    m_html.addElement(m_body);
	
	    //  print document
	    PrintWriter out = response.getWriter();     //  with character encoding support
	    m_html.output(out);
	    out.flush();
	    out.close();
	}

	
	public static boolean isTimeFormat(String time) {
        String regex = "\\d{2}:\\d{2}:\\d{2}\\.\\d{3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(time);
        return matcher.matches();
    }
	
	public static String humanReadableByteCountSI(long bytes) {
	    if (bytes < 1000) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(1000));
	    String pre = ("kMGTPE").charAt(exp-1) + "";
	    return String.format("%.1f %sB", bytes / Math.pow(1000, exp), pre);
	}
}
