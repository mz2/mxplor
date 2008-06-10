package net.piipari.nmica.mxplor.app;

import com.trolltech.qt.core.QMessageHandler;

/*
 * A QMessageHandler used for debugging purposes 
 * (currently used just for providing useful line numbers and
 * breakpoints for native error messages)
 */
public class DebugMessageHandler extends QMessageHandler {

	@Override
	public void critical(String arg0) {
		System.err.println("QT critical : " + arg0);
		throw new RuntimeException(arg0);
	}

	@Override
	public void debug(String arg0) {
		System.err.println("QT debug : " + arg0);
		throw new RuntimeException(arg0);
	}

	@Override
	public void fatal(String arg0) {
		System.err.println("QT fatal : " + arg0);
		throw new RuntimeException(arg0);
	}

	@Override
	public void warning(String arg0) {
		System.err.println("QT Warning : " + arg0);
		throw new RuntimeException(arg0);
	}

}
