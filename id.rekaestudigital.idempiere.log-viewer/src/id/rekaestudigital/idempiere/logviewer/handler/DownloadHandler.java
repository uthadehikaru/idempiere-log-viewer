package id.rekaestudigital.idempiere.logviewer.handler;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compiere.util.Ini;

public class DownloadHandler extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4830030672124779394L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
	    String[] segments = uri.split("/");
	    String lastSegment = segments[segments.length - 1];

	    try {
	        String path = Ini.getAdempiereHome() + File.separator + "log";
	        File file = new File(path + File.separator + lastSegment);

	        if (!file.exists()) {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND);
	            return;
	        }

	        response.setContentType("application/octet-stream");
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
	        response.setContentLength((int) file.length());

	        try (ServletOutputStream out = response.getOutputStream()) {
	            byte[] buffer = new byte[2048];
	            int bytesRead;
	            try (var inputStream = new java.io.FileInputStream(file)) {
	                while ((bytesRead = inputStream.read(buffer)) != -1) {
	                    out.write(buffer, 0, bytesRead);
	                }
	            }
	            out.flush();
	        }
	    } catch (Exception e) {
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing file download");
	    }
	}
}
