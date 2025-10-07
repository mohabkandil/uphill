package com.uphill.infrastructure.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseBodyCaptureWrapper extends HttpServletResponseWrapper {
    
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ServletOutputStream servletOutputStream = new ServletOutputStream() {
        @Override
        public void write(final int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(final WriteListener writeListener) {
        }
    };
    
    private PrintWriter printWriter;

    public ResponseBodyCaptureWrapper(final HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(servletOutputStream);
        }
        return printWriter;
    }

    public String getResponseBody() {
        try {
            if (printWriter != null) {
                printWriter.flush();
            }
            return outputStream.toString("UTF-8");
        } catch (IOException e) {
            return null;
        }
    }
}
