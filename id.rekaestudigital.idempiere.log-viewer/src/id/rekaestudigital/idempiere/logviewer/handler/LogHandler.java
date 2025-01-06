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

import org.apache.ecs.Element;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.body;
import org.apache.ecs.xhtml.button;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.head;
import org.apache.ecs.xhtml.html;
import org.compiere.model.MSysConfig;
import org.compiere.util.Ini;
import org.idempiere.server.cluster.callable.DeleteLogsCallable;
import org.idempiere.server.cluster.callable.RotateLogCallable;

public class LogHandler extends HttpServlet {

	/**
	 * @author ZuhriUtama
	 */
	private static final long serialVersionUID = -1695538254258463076L;

	private static final int LIMIT_SIZE = 50000000; // 50 MB
	private static final String path = Ini.getAdempiereHome() + File.separator + "log";
	
	private String baseUrl = "/log-viewer";
	private boolean showLog = false;
	private File log;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		
	    response.setHeader("Cache-Control", "no-cache");
	    response.setContentType("text/html; charset=UTF-8");

	    String fileName = request.getParameter("file");
	    if (fileName != null) {
	        log = new File(path + File.separator + fileName);
	    }
	
	    html m_html = new html();
	    body m_body = new body();
	    
	    m_html.addElement(getHead());
	    
	    m_body.addElement(getNavbar());
	
	    div container = new div();


        div toast = new div();
        toast.setClass("toast toast-top toast-center");
        
	    String delete = request.getParameter("delete");
	    if (delete != null) {
	        div alert = deleteLogs();
	        toast.addElement(alert);
	    }

	    String rotate = request.getParameter("rotate");
	    if (rotate != null) {
	        div alert = rotateLogs();
	        toast.addElement(alert);
	    }
	    m_body.addElement(toast);
	    
	    container.setClass("flex");
	
	    container.addElement(getLeftPanel(request));
	    
	    container.addElement(getRightPanel());
	
	    m_body.addElement(container);
	    
	    if(showLog) {
		    m_body.addElement("<script>let table = new DataTable('#logTable', {"
		    		+ "pageLength:50,"
		    		+ "order: [[0, 'desc']],"
		    		+ "});</script>");
	    }
	    
	    m_html.addElement(m_body);
	
	    //  print document
	    PrintWriter out = response.getWriter();     //  with character encoding support
	    m_html.output(out);
	    out.flush();
	    out.close();
	}
	
	private div deleteLogs()
	{
		DeleteLogsCallable callable = new DeleteLogsCallable();
		try 
		{
			callable.call();	
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
        div alert = new div();
        alert.setClass("alert alert-success flex justify-between");
        alert.addElement("<span>Trace Logs is deleted</span>");
        alert.addElement("<a href=\""+baseUrl+"\"><svg"
        		+ "    xmlns=\"http://www.w3.org/2000/svg\""
        		+ "    class=\"h-6 w-6 shrink-0 stroke-current\""
        		+ "    fill=\"none\""
        		+ "    viewBox=\"0 0 24 24\">"
        		+ "    <path"
        		+ "      stroke-linecap=\"round\""
        		+ "      stroke-linejoin=\"round\""
        		+ "      stroke-width=\"2\""
        		+ "      d=\"M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z\" />"
        		+ "  </svg></span>");
		return alert;
	}
	
	private div rotateLogs()
	{
		RotateLogCallable callable = new RotateLogCallable();
		try 
		{
			callable.call();	
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
        div alert = new div();
        alert.setClass("alert alert-success flex justify-between");
        alert.addElement("<span>Trace Log is Rotated</span>");
        alert.addElement("<a href=\""+baseUrl+"\"><svg"
        		+ "    xmlns=\"http://www.w3.org/2000/svg\""
        		+ "    class=\"h-6 w-6 shrink-0 stroke-current\""
        		+ "    fill=\"none\""
        		+ "    viewBox=\"0 0 24 24\">"
        		+ "    <path"
        		+ "      stroke-linecap=\"round\""
        		+ "      stroke-linejoin=\"round\""
        		+ "      stroke-width=\"2\""
        		+ "      d=\"M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z\" />"
        		+ "  </svg></span>");
		return alert;
	}

	
	private Element getNavbar() {
	    div navbar = new div();
	    navbar.setClass("navbar bg-base-100");
	    navbar.addElement("<div class=\"flex-1\">"
	    		+ "    <a href=\""+ baseUrl +"\" class=\"text-xl font-bold hover:underline\">IDempiere Log Viewer</a>"
	    		+ "  </div>");
	    
	    div buttons = new div();
	    buttons.setClass("flex-none gap-2");
	    
	    a rotateLogs = new a();
	    rotateLogs.setHref(baseUrl + "?rotate=logs");
	    rotateLogs.setClass("btn btn-sm btn-warning");
	    rotateLogs.addElement("Rotate Trace Log");
	    buttons.addElement(rotateLogs);
	    
	    a deleteLogs = new a();
	    deleteLogs.setHref(baseUrl + "?delete=logs");
	    deleteLogs.setClass("btn btn-sm btn-error");
	    deleteLogs.addElement("Delete All");
	    deleteLogs.setOnClick("return confirm('Are you sure to clear all trace logs?')");
	    buttons.addElement(deleteLogs);
	    
	    button lightTheme = new button();
	    lightTheme.setClass("btn btn-sm bg-white text-gray dark:text-black");
	    lightTheme.addAttribute("data-set-theme", "light");
	    lightTheme.addElement("Light");
	    buttons.addElement(lightTheme);
	    
	    button darkTheme = new button();
	    darkTheme.setClass("btn btn-sm bg-gray-500 text-white");
	    darkTheme.addAttribute("data-set-theme", "dark");
	    darkTheme.addElement("Dark");
	    buttons.addElement(darkTheme);
	    navbar.addElement(buttons);
	    
	    return navbar;
	}


	private Element getRightPanel() {
		int limitSize = MSysConfig.getIntValue("LOG_VIEWER_LIMIT_SIZE", LIMIT_SIZE);
		
		div rightPanel = new div();
	    rightPanel.setClass("w-4/5 p-2");
	    
	    if(log.length()>limitSize) {
	    	rightPanel.addElement("<h1>File Size more than "+ humanReadableByteCountSI(limitSize) +", please download the file to read the content");
	    }else {
	    	showLog = true;
		    rightPanel.addElement("  <table id=\"logTable\" class=\"table table-zebra\">"
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
	    }
	    return rightPanel;
	}


	private Element getLeftPanel(HttpServletRequest request) {
		div leftPanel = new div();
	    leftPanel.setClass("w-1/5 p-2 flex flex-col");
	    leftPanel.addElement("<a href=\"/\" class=\"text-sm hover:underline mb-2\">back to IDempiere Home</a>");
	
	    File dir = new File(path);
	    File[] files = dir.listFiles();
	    Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));
	    for (File file : files) {
	        if (log == null)
	            log = file;
	        long fileSizeInBytes = file.length();
	        String size = humanReadableByteCountSI(fileSizeInBytes);
	        div card = new div();
	        if (log.getName().equals(file.getName()))
	            card.setClass("border rounded-md bg-success text-white mb-2 p-2 flex justify-between");
	        else
	            card.setClass("border rounded-md border-success hover:bg-success mb-2 hover:text-white p-2 flex justify-between");
	        card.addElement("<a href=\""+baseUrl+"?file=" + file.getName() + "\" class=\"text-sm font-bold\">" + file.getName() + " [" + size + "]</a>");
	        card.addElement("<a href=\""+ baseUrl +"/download/" + file.getName() + "\" class=\"text-sm hover:font-bold\"><svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"1.5\" stroke=\"currentColor\" class=\"size-4\">"
	                + "  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3\" />"
	                + "</svg>"
	                + "</a>");
	        leftPanel.addElement(card);
	    }
        return leftPanel;
	}


	private Element getHead() {
		head m_head = new head();
		m_head.addElement("<title>IDempiere Log Viewer</title>");
	    m_head.addElement("<link href=\"https://cdn.jsdelivr.net/npm/daisyui@4.12.23/dist/full.min.css\" rel=\"stylesheet\" type=\"text/css\" />");
	    m_head.addElement("<script src=\"https://cdn.tailwindcss.com\"></script>");
	    m_head.addElement("<script src=\"https://cdn.jsdelivr.net/npm/theme-change@2.0.2/index.js\"></script>");
	    m_head.addElement("<script src=\"https://code.jquery.com/jquery-3.7.1.slim.min.js\"></script>");
	    m_head.addElement("<link rel=\"stylesheet\" type=\"text/css\" href=\"//cdn.datatables.net/2.1.8/css/dataTables.dataTables.min.css\"></script>");
	    m_head.addElement("<script src=\"//cdn.datatables.net/2.1.8/js/dataTables.min.js\"></script>");
	    return m_head;
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
