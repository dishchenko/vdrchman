package di.vdrchman.util;

import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.servlet.http.HttpServletRequest;

public class UuidExceptionHandler extends ExceptionHandlerWrapper {

	private static final Logger logger = Logger
			.getLogger(UuidExceptionHandler.class.getName());

	private ExceptionHandler wrapped;

	public UuidExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void handle() throws FacesException {
		FacesContext context;

		context = FacesContext.getCurrentInstance();
		if (context != null) {
			handleException(context);
		}
		wrapped.handle();
	}

	private void handleException(FacesContext context) {
		Iterator<ExceptionQueuedEvent> unhandledExceptionQueuedEvents;
		Throwable exception;
		ExternalContext externalContext;
		HttpServletRequest request;
		ByteArrayOutputStream baos;
		PrintStream ps;
		String msg;

		unhandledExceptionQueuedEvents = getUnhandledExceptionQueuedEvents()
				.iterator();
		if (!unhandledExceptionQueuedEvents.hasNext()) {
			return;
		}

		exception = unhandledExceptionQueuedEvents.next().getContext()
				.getException();

		if (exception instanceof AbortProcessingException) {
			return;
		}

		unhandledExceptionQueuedEvents.remove();

		msg = "Exception UUID: " + UUID.randomUUID().toString()
				+ "\n\nHere is the stack trace:\n\n";

		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		exception.printStackTrace(ps);

		try {
			msg += baos.toString("ISO-8859-1");
		} catch (UnsupportedEncodingException ex) { // do nothing
		}

		logger.log(Level.INFO, msg);

		externalContext = context.getExternalContext();
		request = (HttpServletRequest) externalContext.getRequest();

		request.setAttribute(ERROR_MESSAGE, msg);

		if (context.getPartialViewContext().isAjaxRequest()) {
			context.setViewRoot(context.getApplication().getViewHandler()
					.createView(context, "/WEB-INF/errorpages/uuid.xhtml"));
			context.getPartialViewContext().setRenderAll(true);
		} else {
			try
			{
			externalContext.dispatch("WEB-INF/errorpages/uuid.xhtml");
			}
			catch (IOException ex)
			{
				logger.log(Level.SEVERE, ex.getMessage());
			}
		}
		context.renderResponse();

		while (unhandledExceptionQueuedEvents.hasNext()) {
			unhandledExceptionQueuedEvents.next();
			unhandledExceptionQueuedEvents.remove();
		}
	}

	@Override
	public ExceptionHandler getWrapped() {

		return wrapped;
	}

}
