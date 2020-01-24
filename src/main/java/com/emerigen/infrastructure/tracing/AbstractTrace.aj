
package com.emerigen.infrastructure.tracing;

import java.io.PrintStream;

/**
 * This class provides support for printing trace messages into a stream. The
 * trace messages consist of the class name, method name (if method) and the
 * list of parameter types.
 * <P>
 * The class is thread-safe. Different threads may use different output streams
 * by simply calling the method initStream(myStream).
 * <P>
 * This class should be extended. It defines 3 abstract crosscuts for injecting
 * the tracing functionality into any constructors and methods of any
 * application classes.
 * <P>
 *
 * One example of using this class might be
 * 
 * <PRE>
 * import tracing.lib.AbstractTrace;
 * aspect Trace extends AbstractTrace of eachJVM() {
 *   pointcut classes(): within(TwoDShape) | within(Circle) | within(Square);
 *   pointcut constructors(): executions(new(..));
 *   pointcut methods(): executions(!abstract * *(..))
 * }
 * </PRE>
 * 
 * (Make sure .../aspectj/examples is in your classpath)
 */
import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

public abstract aspect AbstractTrace {

	private final int width = 2; // Indentation Width
	private final String indentationWidthSpaces = "  "; // Indentation Width
	protected int logLineSize = 140;
	private final String entryLogPrefixSpaces = "    ";
	private final String entryLogPrefix = "--> ";
	private final String exitLogPrefix = "<-- ";

	/**
	 * Notes:
	 * The logging level is controlled by the log4j properties file (log4j.xml)
	 * located in the resources directory.
	 */
	
	private static Logger logger = Logger.getLogger(AbstractTrace.class);

	/**
	 * Application classes - left unspecified. Subclasses should concretize this
	 * crosscut with class names. Turn tracing off by commenting the line below and
	 * uncommenting the line below it.
	 */
	abstract pointcut classes();

	/**
	 * Application classes - left unspecified. Subclasses should concretize this
	 * crosscut with class names. Turn tracing off by commenting the line below and
	 * uncommenting the line below it.
	 */
	abstract pointcut ignoredClassesAndMethods();

	/**
	 * Constructors - left unspecified. Subclasses should concretize this crosscut
	 * with constructors.
	 */
	// abstract pointcut constructors();
	abstract pointcut constructors();

	/**
	 * Methods - left unspecified. Subclasses should concretize this crosscut with
	 * method names.
	 */
	abstract pointcut methods();

	/**
	 * Methods to performance test and log measurements. Subclasses should
	 * concretize this crosscut with method names.
	 */
	abstract pointcut performanceTraceMethods();

	after(): constructors()
		&& classes()  
		&& !ignoredClassesAndMethods() {
		doTraceExit(thisJoinPoint, thisJoinPointStaticPart, thisJoinPoint.getTarget(), false);
	}

	after() returning(Object result) :classes() && methods() && !ignoredClassesAndMethods() {
			doTraceExit(thisJoinPoint, thisJoinPointStaticPart, result, false);
	}

	after() returning(Object result) :classes() && constructors() && !ignoredClassesAndMethods() {
			doTraceExit(thisJoinPoint, thisJoinPointStaticPart, result, false);
	}

	after() throwing (Exception ex) :classes() && constructors() && !ignoredClassesAndMethods() {
			doTraceExit(thisJoinPoint, thisJoinPointStaticPart, ex, false);
	}

	after() throwing (Exception ex) :classes() && methods() && !ignoredClassesAndMethods() {
			doTraceExit(thisJoinPoint, thisJoinPointStaticPart, ex, false);
	}

	// Capture the performance measurements for the specified classes
	Object around() : (classes() || constructors() || methods()) 
		&& !ignoredClassesAndMethods() 
		&& call(* Logger.info(..)) {
		
		NDC.push("        ...");
		
		//Extract the message to be logged, format it
		Object[] args = thisJoinPoint.getArgs();

		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append((String)args[0]);
		String finalStrBuffer = getFormattedLogEntry(strBuffer, getPaddingSpaces(getCallDepth()*width + 6 * entryLogPrefix.length()));
		logger.debug(finalStrBuffer.toString());
		
		NDC.pop();
		return (Object)null;
	}


	before(): classes() && constructors() && !ignoredClassesAndMethods() {
			doTraceEntry(thisJoinPoint, thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart.getSignature(), true);
	}

	before(): classes() && methods() && !ignoredClassesAndMethods() {
			doTraceEntry(thisJoinPoint, thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart.getSignature(),
					false);
	}
	/*
	 * From here on, it's an ordinary class implementation. The static state is
	 * thread-safe by using ThreadLocal variables.
	 */

	/**
	 * This method initializes this thread's trace output stream. By default, the
	 * output stream is System.err, and it is the same for all threads. In
	 * multithreaded applications, you may want to define different output streams
	 * for the different threads. For doing it, simply call this method in the
	 * beginning of each thread's main loop, giving it different output streams.
	 */
	public void initStream(PrintStream _stream) {
		setStream(_stream);
	}

	private ThreadLocal stream = new ThreadLocal() {
		protected Object initialValue() {
			return System.err;
		}
	};
	private ThreadLocal callDepth = new ThreadLocal() {
		protected Object initialValue() {
			return new Integer(0);
		}
	};

	private PrintStream getStream() {
		return (PrintStream) stream.get();
	}

	private void setStream(PrintStream s) {
		stream.set(s);
	}

	private int getCallDepth() {
		return ((Integer) (callDepth.get())).intValue();
	}

	private void setCallDepth(int n) {
		callDepth.set(Integer.valueOf(n));
	}

	private void doTraceEntry(JoinPoint jp, JoinPoint.StaticPart jps, Signature sig, boolean isConstructor) {
		NDC.push("    ");
		setCallDepth(getCallDepth() + 1);
		printEntering(jp, jps, sig, isConstructor);
	}

	private void doTraceExit(JoinPoint jp, JoinPoint.StaticPart jps, Object result, boolean isConstructor) {
		printExiting(jp, jps, result, isConstructor);
		NDC.pop();
		setCallDepth(getCallDepth() - 1);
	}

	private void doTraceExit(JoinPoint jp, StaticPart jps, Exception ex, boolean isConstructor) {
		printExiting(jp, jps, ex, isConstructor);
		NDC.pop();
		setCallDepth(getCallDepth() - 1);
	}

	/**
	 * Construct and print the "method entry" information.
	 * 
	 * @param jp
	 * @param ejp
	 * @param isConstructor
	 */
	private void printEntering(JoinPoint jp, StaticPart jps, Signature sig, boolean isConstructor) {
		StringBuffer strBuffer = new StringBuffer();
		//strBuffer.append(getIndentationSpaces());
		strBuffer.append(entryLogPrefix);
		strBuffer.append("[" + Thread.currentThread().getName() + "] ");

		// Capture the caller signature
		// TODO strBuffer.append(sig.toShortString() + " invoked ");

		// If non-static method
		if (jp.getThis() != null) {
			// System.out.println("printEntering() : " + jp + ", ejp: " + ejp);
			strBuffer.append(jp.getThis().getClass().getSimpleName() + ".");
			strBuffer.append(((Signature) (jp.getStaticPart().getSignature())).getName());
			strBuffer.append(getParameterString(jp, jps));

		} else {
			// A static method was invoked, use different reflection data
			strBuffer.append(jps.getSignature().toShortString());
		}
		String finalStrBuffer = getFormattedLogEntry(strBuffer, getPaddingSpaces(getCallDepth()*width + (2* entryLogPrefix.length())));
		
		logger.debug(finalStrBuffer.toString());
	}

	/**
	 * Construct and print the "method exit" information. with exception
	 * 
	 * @param jp
	 * @param ex
	 * @param isConstructor
	 */
	private void printExiting(JoinPoint jp, JoinPoint.StaticPart jps, Exception ex, boolean isConstructor) {
		StringBuffer strBuffer = new StringBuffer();
		//strBuffer.append(getIndentationSpaces());
		strBuffer.append(exitLogPrefix);
		strBuffer.append("[" + Thread.currentThread().getName() + "] ");

		// If returning from non-static method
		if (jp.getThis() != null) {
			strBuffer.append(jp.getThis().getClass().getSimpleName() + ".");
			strBuffer.append(((Signature) (jp.getStaticPart().getSignature())).getName());
			strBuffer.append(getParameterString(jp, jps));
		} else {
			// A static method was invoked, use different reflection data
			strBuffer.append(jps.getSignature().toShortString());

		}
		strBuffer.append(" returning " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		String finalStrBuffer = getFormattedLogEntry(strBuffer, getPaddingSpaces(getCallDepth()*width + (4* entryLogPrefix.length())));
				
		logger.debug(finalStrBuffer.toString());
	}

	/**
	 * Construct and print the "method exit" information with object returned.
	 * 
	 * @param jp
	 * @param ex
	 * @param isConstructor
	 */
	private void printExiting(JoinPoint jp, StaticPart jps, Object returnedObject, boolean isConstructor) {
		StringBuffer strBuffer = new StringBuffer();
		//strBuffer.append(getIndentationSpaces());
		strBuffer.append(exitLogPrefix);
		strBuffer.append("[" + Thread.currentThread().getName() + "] ");

		// If this is a non-static method exit
		if (jp.getThis() != null) {
			strBuffer.append(jp.getThis().getClass().getSimpleName() + ".");
			strBuffer.append(((Signature) (jp.getStaticPart().getSignature())).getName());
			strBuffer.append("() ");
		} else {
			// This is a static method exit, so use different reflection data
			strBuffer.append(jps.getSignature().toShortString());
		}

		try {
			strBuffer.append(" returning: " + returnedObject);
		} catch (Exception e) {
			strBuffer.append(" returning: an object with null fields");
			System.out.println(
					"AbstractTrace.printExiting() could not log object with null fields. Exception: " + e.getMessage());
		}

		String finalStrBuffer = getFormattedLogEntry(strBuffer, getPaddingSpaces(getCallDepth()*width + (2* entryLogPrefix.length())));
		
		logger.debug(finalStrBuffer.toString());
	}

	private String getFormattedLogEntry(StringBuffer logString, String padding) {
		StringBuffer finalLogString = new StringBuffer();

		if (logString.length() > logLineSize) {

			while (logString.length() > logLineSize) {
				finalLogString.append(logString.substring(0, logLineSize)).append("\n");
				logString.delete(0, logLineSize);
				logString.insert(0, padding);
			}
		}
		finalLogString.append(logString);
		return finalLogString.toString();

	}

	private String getParameterString(JoinPoint jp, StaticPart jps) {
		MethodSignature methodSignature;
		ConstructorSignature ctorSignature;


		Object[] parms = jp.getArgs();
		String[] pnames = new String[parms.length];

		if (jp.getSignature() instanceof MethodSignature) {
			methodSignature = (MethodSignature) jp.getSignature();
			pnames = methodSignature.getParameterNames();

		} else if (jp.getSignature() instanceof ConstructorSignature) {
			ctorSignature = (ConstructorSignature) jp.getSignature();
			pnames = ctorSignature.getParameterNames();
		}

		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("(");

		for (int i = 0; i < pnames.length; i++) {

			// Print parameter values that are not null
			if (parms[i] != null && pnames[i] != null) {
				strBuffer.append(pnames[i] + "=" + parms[i].toString());
			} else {
				strBuffer.append(pnames[i] + "= null");
			}

			// Add comma if there are more parameters
			if (i < pnames.length - 1)
				strBuffer.append(", ");
		}
		strBuffer.append(")");
		return strBuffer.toString();
	}

	private String getPaddingSpaces(int numSpaces) {
		StringBuffer paddingSpaces = new StringBuffer();
		for (int i = 0; i < numSpaces; i++)
			paddingSpaces.append(" ");
		return paddingSpaces.toString();
	}

	/**
	 * @return the logLineSize
	 */
	public int getLogLineSize() {
		return logLineSize;
	}

	/**
	 * @param logLineSize the logLineSize to set
	 */
	public void setLogLineSize(int logLineSize) {
		this.logLineSize = logLineSize;
	}

}
